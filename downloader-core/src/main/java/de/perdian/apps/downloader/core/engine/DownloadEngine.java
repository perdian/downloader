package de.perdian.apps.downloader.core.engine;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.downloader.core.support.ProgressListener;

/**
 * An engine represents the central manager object, into which new requests that are supposed to
 * download information from an external resource will be added and from which information about the
 * current state of the engine and it's operations can be requested.
 *
 * @author Christian Robert
 */

public class DownloadEngine {

    private static final Logger log = LoggerFactory.getLogger(DownloadEngine.class);

    private Clock clock = Clock.systemDefaultZone();
    private ExecutorService executorService = null;
    private List<DownloadEngineConfigurationListener> engineConfigurationListeners = null;
    private List<DownloadSchedulingListener> schedulingListeners = null;
    private Queue<DownloadRequestWrapper> waitingRequests = null;
    private List<DownloadOperation> activeOperations = null;
    private Path targetDirectory = null;
    private int processorCount = 0;
    private int bufferSize = 1024 * 8; // 8 KiB
    private int notificationSize = 1024 * 64; // 64 KiB

    public DownloadEngine(Path targetDirectory) {
        this.setExecutorService(Executors.newCachedThreadPool());
        this.setEngineConfigurationListeners(new CopyOnWriteArrayList<>());
        this.setSchedulingListeners(new CopyOnWriteArrayList<>());
        this.setWaitingRequests(new PriorityQueue<>(10, new DownloadRequestWrapper.PriorityComparator()));
        this.setActiveOperations(new ArrayList<>());
        this.setTargetDirectory(targetDirectory);
        this.setProcessorCount(1);
    }

    /**
     * Submits all new download into this engine.
     *
     * @param request
     *     the requests containing all the information from which a download can
     *     be constructed.
     * @return
     *     a list of {@link DownloadRequestWrapper} which the engine has accepted
     */
    public List<DownloadRequestWrapper> submitAll(Collection<DownloadRequest> requests) {
        List<DownloadRequestWrapper> acceptedRequestWrappers = new ArrayList<>();
        requests.forEach(request -> {
            DownloadRequestWrapper requestWrapper = this.submit(request);
            if (requestWrapper != null) {
                acceptedRequestWrappers.add(requestWrapper);
            }
        });
        return Collections.unmodifiableList(acceptedRequestWrappers);
    }

    /**
     * Submits a new download into this engine. It is up to the engine to decide
     * whether or not the job can be started immediately or if it needs to be
     * put in any kind of queue and await a free download slot.
     *
     * @param request
     *     the request containing all the information from which a download can
     *     be constructed.
     * @return
     *     a {@link DownloadRequestWrapper} if the engine has accepted the request and
     *     scheduled it for execution or {@code null} if the engine instance
     *     rejected the job and will not execute the transfer process.
     */
    public DownloadRequestWrapper submit(DownloadRequest request) {
        if (request == null) {
            throw new NullPointerException("Parameter 'request' must not be null!");
        } else if (request.getTitle() == null) {
            throw new NullPointerException("Property 'title' of request must not be null!");
        } else if (request.getTaskFactory() == null) {
            throw new NullPointerException("Property 'taskFactory' of request must not be null!");
        } else {

            // First we contact all the validators and make sure the new request
            // might actually be processed
            if (!this.fireRequestSubmitted(request)) {
                return null;
            } else {

                log.info("Accepted request: {}", request);
                DownloadRequestWrapper requestWrapper = new DownloadRequestWrapper();
                requestWrapper.setOwner(this);
                requestWrapper.setRequest(request);
                requestWrapper.setScheduledTime(this.getClock().instant());

                synchronized (this) {
                    if (!this.executeRequest(requestWrapper, false)) {
                        this.getWaitingRequests().add(requestWrapper);
                        this.getSchedulingListeners().forEach(l -> l.onRequestScheduled(requestWrapper));
                    }
                }
                return requestWrapper;

            }
        }
    }

    private boolean fireRequestSubmitted(DownloadRequest request) {
        for (DownloadSchedulingListener listener : this.getSchedulingListeners()) {
            try {
                listener.onRequestSubmitted(request);
            } catch (DownloadRejectedException e) {
                log.info("Request rejected by listener {}: (Request: {}, Message: {})", listener.getClass().getSimpleName(), request, e.getMessage());
                return false;
            }
        }
        return true;
    }

    synchronized boolean executeRequest(DownloadRequestWrapper requestWrapper, boolean ignoreSlots) {
        if (ignoreSlots || this.getProcessorCount() > this.getActiveOperations().size()) {

            // Make sure we remove the job from the waiting list, so no matter
            // from where we come from we always leave in a consistent state
            this.getWaitingRequests().remove(requestWrapper);

            // Now make sure we have the right status upon the job itself
            boolean requestAlreadyActive = this.getActiveOperations().stream()
                .map(operation -> operation.getRequestWrapper())
                .filter(requestWrapperFromOperation -> Objects.equals(requestWrapperFromOperation, requestWrapper))
                .findAny()
                .isPresent();

            if (!requestAlreadyActive) {

                DownloadOperation operation = new DownloadOperation();
                operation.setStartTime(this.getClock().instant());
                operation.setStatus(DownloadStatus.ACTIVE);
                operation.setOwner(this);
                operation.setRequestWrapper(requestWrapper);
                requestWrapper.setOperation(operation);
                log.info("Starting operation: {}", operation);

                this.getActiveOperations().add(operation);
                this.getExecutorService().submit(() -> this.executeOperation(operation));

            }
            return true;

        } else {
            return false;
        }
    }

    private void executeOperation(DownloadOperation operation) {
        try {
            log.debug("Running operation: {}", operation);
            this.getSchedulingListeners().forEach(l -> l.onOperationStarting(operation));
            this.executeOperationTransfer(operation);
            operation.setEndTime(this.getClock().instant());
            operation.setStatus(DownloadStatus.COMPLETED);
            log.info("Operation completed: {} in {}", operation, Duration.between(operation.getStartTime(), operation.getEndTime()));
        } catch (Exception e) {
            operation.setEndTime(this.getClock().instant());
            operation.setError(e);
            operation.setStatus(DownloadStatus.COMPLETED);
            log.info("Exception occured during operation execution: " + operation, e);
        } finally {
            try {
                synchronized (this) {

                    // Make sure the job is removed from the list of currently
                    // active jobs
                    this.getActiveOperations().remove(operation);

                    // After the current processor is finished we want to make
                    // sure that the next item in the queue get's picked up
                    this.checkWaitingRequests();

                }
            } finally {

                // Update the job itself
                this.getSchedulingListeners().forEach(l -> l.onOperationCompleted(operation));

            }
        }

    }

    private void executeOperationTransfer(DownloadOperation operation) throws Exception {

        ProgressListener progressListener = ProgressListener.compose(operation.getProgressListeners());
        DownloadRequest request = operation.getRequestWrapper().getRequest();
        DownloadTask task = request.getTaskFactory().createTask(progressListener);
        Path targetFilePath = this.getTargetDirectory().resolve(Objects.requireNonNull(task.getTargetFileName(), "Task targetFileName must not be null!"));
        if (!Files.exists(targetFilePath.getParent())) {
            Files.createDirectories(targetFilePath.getParent());
        }
        this.getSchedulingListeners().forEach(l -> l.onOperationTransferStarting(task, targetFilePath, operation));

        if (DownloadStatus.ACTIVE.equals(operation.getStatus())) {

            long inStreamSize = task.getContentFactory().size();
            try (InputStream inStream = task.getContentFactory().openStream()) {
                try (OutputStream outStream = Files.newOutputStream(targetFilePath, Files.exists(targetFilePath) ? StandardOpenOption.WRITE : StandardOpenOption.CREATE)) {

                    long notificationBlockSize = Math.max(this.getBufferSize(), this.getNotificationSize());
                    long nextNotification = notificationBlockSize;
                    long totalBytesWritten = 0;
                    progressListener.onProgress(null, 0L, inStreamSize);

                    byte[] buffer = new byte[this.getBufferSize()];
                    for (int bufferSize = inStream.read(buffer); bufferSize > -1 && DownloadStatus.ACTIVE.equals(operation.getStatus()); bufferSize = inStream.read(buffer)) {
                        outStream.write(buffer, 0, bufferSize);
                        totalBytesWritten += bufferSize;
                        if (totalBytesWritten > nextNotification) {
                            nextNotification += notificationBlockSize;
                            progressListener.onProgress(null, totalBytesWritten, inStreamSize);
                        }
                    }
                    progressListener.onProgress(null, totalBytesWritten, inStreamSize);
                    outStream.flush();

                }
            } catch (final Exception e) {
                log.warn("Error occured during file transfer [" + operation + "]", e);
                try {
                    Files.deleteIfExists(targetFilePath);
                } catch (Exception e2) {
                    log.debug("Cannot delete target file (after error during transfer) at: " + targetFilePath, e2);
                }
                throw e;
            } finally {
                this.getSchedulingListeners().forEach(l -> l.onOperationTransferCompleted(task, targetFilePath, operation));
            }

            if (!DownloadStatus.ACTIVE.equals(operation.getStatus())) {
                try {
                    Files.deleteIfExists(targetFilePath);
                } catch (Exception e) {
                    log.debug("Cannot delete target file (after cancel) at: " + targetFilePath, e);
                }
            }

        }

    }

    private synchronized void checkWaitingRequests() {
        Queue<DownloadRequestWrapper> queue = this.getWaitingRequests();
        int maxRequestsToRemove = this.getProcessorCount() - this.getActiveOperations().size();
        if (maxRequestsToRemove > 0) {
            List<DownloadRequestWrapper> removedRequests = new ArrayList<>(maxRequestsToRemove);
            for (int i = 0; i < maxRequestsToRemove && !queue.isEmpty(); i++) {
                removedRequests.add(queue.remove());
            }
            for (DownloadRequestWrapper requestWrapper : removedRequests) {
                this.executeRequest(requestWrapper, false);
            }
        }
    }

    synchronized boolean cancelOperation(DownloadOperation operation, String reason) {
        if (DownloadStatus.CANCELLED.equals(operation.getStatus())) {

            // The download is already cancelled, so there must be someone else
            // calling the cancel method twice.
            return true;

        } else if (!this.getActiveOperations().contains(operation)) {

            // The job could not be found in the list of active jobs, which
            // means we have no way of handling him at all - so we just exit
            return false;

        } else {

            log.debug("Cancelling operation {} with reason: {}", operation, reason);
            operation.setStatus(DownloadStatus.CANCELLED);
            operation.setCancelTime(this.getClock().instant());
            operation.setCancelReason(reason);
            this.getSchedulingListeners().forEach(l -> l.onOperationCancelled(operation));
            this.checkWaitingRequests();
            return true;

        }
    }

    synchronized boolean cancelRequest(DownloadRequestWrapper requestWrapper, String reason) {
        if (this.getWaitingRequests().remove(requestWrapper)) {
            this.getSchedulingListeners().forEach(l -> l.onRequestCancelled(requestWrapper));
            return true;
        } else if (requestWrapper.getOperation() != null) {
            return this.cancelOperation(requestWrapper.getOperation(), reason);
        } else {
            return false;
        }
    }

    /**
     * Wait until all jobs currently executing and waiting inside this executor
     * have been completed
     */
    public void waitUntilAllDownloadsComplete() {
        synchronized (this) {
            if (!this.isBusy()) {
                return;
            }
        }
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            this.addSchedulingListener(new DownloadSchedulingListener() {
                @Override public void onOperationCompleted(DownloadOperation operation) {
                    synchronized (DownloadEngine.this) {
                        if (!DownloadEngine.this.isBusy()) {
                            DownloadEngine.this.removeSchedulingListener(this);
                            latch.countDown();
                        }
                    }
                }
            });
            latch.await();
        } catch (InterruptedException e) {
            // Ignore here
        }
    }

    Clock getClock() {
        return this.clock;
    }
    void setClock(Clock clock) {
        this.clock = clock;
    }

    private ExecutorService getExecutorService() {
        return this.executorService;
    }
    private void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public boolean addEngineConfigurationListener(DownloadEngineConfigurationListener listener) {
        return this.getEngineConfigurationListeners().add(listener);
    }
    public boolean removeEngineConfigurationListener(DownloadEngineConfigurationListener listener) {
        return this.getEngineConfigurationListeners().remove(listener);
    }
    private List<DownloadEngineConfigurationListener> getEngineConfigurationListeners() {
        return this.engineConfigurationListeners;
    }
    private void setEngineConfigurationListeners(List<DownloadEngineConfigurationListener> engineConfigurationListeners) {
        this.engineConfigurationListeners = engineConfigurationListeners;
    }

    public boolean addSchedulingListener(DownloadSchedulingListener listener) {
        return this.getSchedulingListeners().add(listener);
    }
    public boolean removeSchedulingListener(DownloadSchedulingListener listener) {
        return this.getSchedulingListeners().remove(listener);
    }
    private List<DownloadSchedulingListener> getSchedulingListeners() {
        return this.schedulingListeners;
    }
    private void setSchedulingListeners(List<DownloadSchedulingListener> schedulingListeners) {
        this.schedulingListeners = schedulingListeners;
    }

    /**
     * List all currently waiting requests, which means the requests for which no processing slot
     * has been assigned yet. The request itself is therefore sitting in a queue waiting to be
     * picked up and to be executed.
     *
     * @return
     *     the list of waiting requests at the time this method was called. Subsequent changes in
     *     the engine's state (a request was picked up for execution or was cancelled) will not be
     *     reflected into this result list. The list itself will therefore be immutable.
     */
    public List<DownloadRequestWrapper> listWaitingRequests() {
        return Collections.unmodifiableList(new ArrayList<>(this.getWaitingRequests()));
    }

    /**
     * Removes all currently queued requests that have not been picked up yet
     *
     * @return
     *     the requests that were cleared from this engine at the time this method was called.
     *     Subsequent changes in the engines state will not be reflected into this result list.
     *     The list itself will therefore be immutable.
     */
    public synchronized List<DownloadRequestWrapper> clearWaitingRequests() {
        List<DownloadRequestWrapper> resultList = new ArrayList<>(this.getWaitingRequests());
        this.getWaitingRequests().clear();
        return Collections.unmodifiableList(resultList);
    }

    Queue<DownloadRequestWrapper> getWaitingRequests() {
        return this.waitingRequests;
    }
    private void setWaitingRequests(Queue<DownloadRequestWrapper> waitingRequests) {
        this.waitingRequests = waitingRequests;
    }

    /**
     * List all currently active operations, which means the requests for which a processing slot
     * has been allocated and for which data is currently being transfered from the source to a
     * target.
     *
     * @return
     *     the list of active operation at the time this method was called. Subsequent changes in
     *     the engine's state (an operation was completed or cancelled) will not be reflected into
     *     this result list. The list itself will therefore be immutable.
     */
    public List<DownloadOperation> listActiveOperations() {
        return Collections.unmodifiableList(new ArrayList<>(this.getActiveOperations()));
    }

    List<DownloadOperation> getActiveOperations() {
        return this.activeOperations;
    }
    private void setActiveOperations(List<DownloadOperation> activeOperations) {
        this.activeOperations = activeOperations;
    }

    /**
     * Checks if there is any work to be done, meaning that either jobs are
     * waiting to be executed or are being executed right now
     */
    public synchronized boolean isBusy() {
        return !this.getWaitingRequests().isEmpty() || !this.getActiveOperations().isEmpty();
    }

    public Path getTargetDirectory() {
        return this.targetDirectory;
    }
    public void setTargetDirectory(Path targetDirectory) {
        if (targetDirectory == null) {
            throw new NullPointerException("Parameter 'targetDirectory' must not be null!");
        } else if (!Objects.equals(this.targetDirectory, targetDirectory)) {
            this.targetDirectory = targetDirectory;
            this.getEngineConfigurationListeners().forEach(l -> l.onTargetDirectoryUpdated(targetDirectory));
        }
    }

    public int getProcessorCount() {
        return this.processorCount;
    }
    public void setProcessorCount(int processorCount) {
        if (processorCount <= 0) {
            throw new IllegalArgumentException("Parameter 'processorCount' must be larger than 0");
        } else if (this.processorCount != processorCount) {
            int oldProcessorCount = this.processorCount;
            log.debug("Updating processor count from {} to {}", oldProcessorCount, processorCount);
            synchronized (this) {
                this.processorCount = processorCount;
                if (processorCount > oldProcessorCount) {
                    this.checkWaitingRequests();
                }
            }
            this.getEngineConfigurationListeners().forEach(l -> l.onProcessorCountUpdated(processorCount));
        }
    }

    public int getBufferSize() {
        return this.bufferSize;
    }
    public void setBufferSize(int bufferSize) {
        if (bufferSize < 1) {
            throw new IllegalArgumentException("Parameter 'bufferSize' must be larger than 1");
        } else {
            this.bufferSize = bufferSize;
        }
    }

    public int getNotificationSize() {
        return this.notificationSize;
    }
    public void setNotificationSize(int notificationSize) {
        if (notificationSize < 1) {
            throw new IllegalArgumentException("Parameter 'notificationSize' must be larger than 1");
        } else {
            this.notificationSize = notificationSize;
        }
    }

}
