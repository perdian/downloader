package de.perdian.apps.downloader.core.engine;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import de.perdian.apps.downloader.core.support.ProgressListener;

public class DownloadOperation {

    private DownloadEngine owner = null;
    private DownloadRequestWrapper requestWrapper = null;
    private DownloadStatus status = null;
    private Instant startTime = null;
    private Instant endTime = null;
    private Instant cancelTime = null;
    private String cancelReason = null;
    private Exception error = null;
    private List<ProgressListener> progressListeners = new CopyOnWriteArrayList<>();

    DownloadOperation() {
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }

    /**
     * Cancels the current operation which means telling the operation to stop all actions and
     * data transfers that are currently active. Please note that this doesn't mean that the
     * transfer will actually stop immediately! If the processor thread itself doesn't provide an
     * option to immediately stop the process the actual download might continue until the
     * processor thread is able to stop.
     *
     * @param reason
     *     the reason why this job was cancelled
     */
    public void cancel(String reason) {
        this.getOwner().cancelOperation(this, reason);
    }

    /**
     * Gets the owner of this operation, which means the {@link DownloadEngine} in which the current
     * operation resides and who is responsible for activating it.
     */
    public DownloadEngine getOwner() {
        return this.owner;
    }
    void setOwner(DownloadEngine owner) {
        this.owner = owner;
    }

    /**
     * Gets the request from which this {@link DownloadOperation} has been created
     */
    public DownloadRequestWrapper getRequestWrapper() {
        return this.requestWrapper;
    }
    void setRequestWrapper(DownloadRequestWrapper requestWrapper) {
        this.requestWrapper = requestWrapper;
    }

    /**
     * Gets the status of the current download
     */
    public DownloadStatus getStatus() {
        return this.status;
    }
    void setStatus(DownloadStatus status) {
        this.status = status;
    }

    /**
     * Gets the time when the operation was started, which means the time when the
     * {@link DownloadEngine} instance has assigned a processor thread to load the data from a
     * remote resource
     */
    public Instant getStartTime() {
        return this.startTime;
    }
    void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the time when the operation was finished, which means the processor thread transferring
     * the data from a remote resource has been completed, or because of a error that stopped the
     * transfer.
     */
    public Instant getEndTime() {
        return this.endTime;
    }
    void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets the time when the job has been cancelled
     */
    public Instant getCancelTime() {
        return this.cancelTime;
    }
    void setCancelTime(Instant cancelTime) {
        this.cancelTime = cancelTime;
    }

    /**
     * Gets the reason why this job was cancelled
     */
    public String getCancelReason() {
        return this.cancelReason;
    }
    void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    /**
     * Gets an {@code Exception} that occured during the transfer process.
     */
    public Exception getError() {
        return this.error;
    }
    void setError(Exception error) {
        this.error = error;
    }

    public boolean addProgressListener(ProgressListener progressListener) {
        return this.getProgressListeners().add(progressListener);
    }
    public boolean removeProgressListener(ProgressListener progressListener) {
        return this.getProgressListeners().remove(progressListener);
    }
    public List<ProgressListener> getProgressListeners() {
        return this.progressListeners;
    }
    public void setProgressListeners(List<ProgressListener> progressListeners) {
        this.progressListeners = progressListeners;
    }

}
