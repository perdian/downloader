/*
 * Copyright 2013 Christian Robert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.perdian.apps.downloader.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An engine represents the central manager object, into which new jobs that are
 * supposed to download information from an external resource will be added and
 * from which information about the current state of the engine and it's jobs
 * can be requested.
 *
 * Client code should only change the state of the download queue and associated
 * processes using the methods provided in this API class.
 *
 * @author Christian Robert
 */

public class DownloadEngine {

  private static final Logger log = LogManager.getLogger(DownloadEngine.class);

  private ExecutorService myExecutorService = Executors.newCachedThreadPool();
  private List<DownloadListener> myListeners = new CopyOnWriteArrayList<>();
  private Path myTargetDirectory = null;
  private int myBufferSize = 4096;
  private int myProcessorCount = 1;
  private Queue<DownloadJob> myWaitingJobs = new PriorityQueue<>(10, new DownloadJob.PriorityComparator());
  private List<DownloadJob> myActiveJobs = new ArrayList<>();

  /**
   * Create a new engine
   *
   * @param targetDirectory
   *   the directory into which the downloads will be written
   */
  public DownloadEngine(Path targetDirectory) {
    this.setTargetDirectory(Objects.requireNonNull(targetDirectory, "Parameter 'targetDirectory' must not be null!"));
  }

  /**
   * Submits a new download into this engine. It is up to the engine to decide
   * whether or not the job can be started immediately or if it needs to be put
   * in any kind of queue and await a free download slot.
   *
   * @param request
   *   the request containing all the information from which a download can be
   *   constructuted.
   * @return
   *   a {@link DownloadJob} if the engine has accepted the request and
   *   scheduled it for execution or {@code null} if the engine instance
   *   rejected the job and will not execute the transfer process.
   */
  public DownloadJob submit(DownloadRequest request) {
    if(request == null) {
      throw new NullPointerException("Parameter 'request' must not be null!");
    } else if(request.getTargetFileName() == null) {
      throw new NullPointerException("Property 'targetFileName' of request must not be null!");
    } else if(request.getContentFactory() == null) {
      throw new NullPointerException("Property 'contentFactory' of request must not be null!");
    } else {

      // First we contact all the validators and make sure the new request might
      // actually be processed
      if(!this.fireRequestSubmitted(request)) {
        return null;
      }

      log.info("Accepted request: {}", request);
      DownloadJob downloadJob = new DownloadJob(this);
      downloadJob.setRequest(request);
      downloadJob.setScheduleTime(System.currentTimeMillis());
      downloadJob.setStatus(DownloadStatus.SCHEDULED);
      synchronized(this) {
        if(!this.startJob(downloadJob, false)) {
          this.getWaitingJobs().add(downloadJob);
          this.fireJobScheduled(downloadJob);
        }
      }
      return downloadJob;

    }
  }

  /**
   * List all currently active jobs, which means the jobs for which processor
   * thread is currently transfering data from the source to a target.
   *
   * @return
   *   the list of active jobs at the time this method was called. Subsequent
   *   changes in the engines state (a download was completed or cancelled)
   *   will not be reflected into this result list. The list itself will
   *   therefore be immutable
   */
  public List<DownloadJob> listActiveJobs() {
    return Collections.unmodifiableList(new ArrayList<>(this.getActiveJobs()));
  }

  /**
   * List all currently waitinbg jobs, which means the jobs for which no
   * processor has been assigned yet. The job itself is therefore sitting in a
   * queue waiting to be picked up and to be executed.
   *
   * @return
   *   the list of waiting jobs at the time this method was called. Subsequent
   *   changes in the engines state (a download was picked up for execution
   *   or was cancelled) will not be reflected into this result list. The list
   *   itself will therefore be immutable
   */
  public List<DownloadJob> listWaitingJobs() {
    return Collections.unmodifiableList(new ArrayList<>(this.getWaitingJobs()));
  }

  /**
   * Removes all currently queued jobs
   *
   * @return
   *   the jobs that were cleared from this engine
   */
  public synchronized List<DownloadJob> clearWaitingJobs() {
    List<DownloadJob> resultList = new ArrayList<>(this.getWaitingJobs());
    this.getWaitingJobs().clear();
    return Collections.unmodifiableList(resultList);
  }

  /**
   * Wait until all jobs currently executing and waiting inside this executor
   * have been completed
   */
  public void waitUntilAllDownloadsComplete() {
    try {
      final CountDownLatch latch = new CountDownLatch(1);
      this.addListener(new DownloadListenerSkeleton() {
        @Override public void jobCompleted(DownloadJob job) {
          synchronized(DownloadEngine.this) {
            if(!DownloadEngine.this.isBusy()) {
              DownloadEngine.this.removeListener(this);
              latch.countDown();
            }
          }
        }
      });
      latch.await();
    } catch(InterruptedException e) {
      // Ignore here
    }
  }

  // ---------------------------------------------------------------------------
  // --- Status transitions ----------------------------------------------------
  // ---------------------------------------------------------------------------

  synchronized boolean startJob(final DownloadJob job, boolean ignoreSlots) {
    if(ignoreSlots || this.getProcessorCount() > this.getActiveJobs().size()) {

      // Make sure we remove the job from the waiting list, so no matter from
      // where we come from we always leave in a consistent state
      this.getWaitingJobs().remove(job);

      // Now make sure we have the right satus upon the job itself
      job.setStartTime(System.currentTimeMillis());
      job.setStatus(DownloadStatus.ACTIVE);
      DownloadEngine.this.getActiveJobs().add(job);

      this.getExecutorService().submit(new Runnable() {
        @Override public void run() {
          DownloadEngine.this.runJob(job);
        }
        @Override public String toString() {
          return DownloadJob.class.getSimpleName() + "-Processor[" + job + "]";
        }
      });
      return true;

    } else {
      return false;
    }
  }

  synchronized boolean cancelJob(DownloadJob job) {
    if(!this.getActiveJobs().contains(job) && !this.getWaitingJobs().remove(job)) {

      // The job could not be found in the list of active jobs, which means we
      // have no way of handling him at all - so we just exit
      return false;

    } else {

      job.setStatus(DownloadStatus.CANCELLED);
      job.setCancelTime(System.currentTimeMillis());
      this.fireJobCancelled(job);
      this.checkWaitingJobs();
      return true;

    }
  }

  synchronized void checkWaitingJobs() {
    Queue<DownloadJob> queue = this.getWaitingJobs();
    int maxJobsToRemove = this.getProcessorCount() - this.getActiveJobs().size();
    if(maxJobsToRemove > 0) {
      List<DownloadJob> removedJobs = new ArrayList<DownloadJob>(maxJobsToRemove);
      for(int i=0; i < maxJobsToRemove && !queue.isEmpty(); i++) {
        removedJobs.add(queue.remove());
      }
      for(DownloadJob job : removedJobs) {
        this.startJob(job, false);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // --- Job execution ---------------------------------------------------------
  // ---------------------------------------------------------------------------

  void runJob(DownloadJob job) {
    try {
      log.info("Running job: {}", job);
      this.runJobTransfer(job);
      job.setEndTime(System.currentTimeMillis());
      job.setStatus(DownloadStatus.COMPLETED);
      log.info("Job completed: {} in {} ms", job.getStatus(), (job.getEndTime() - job.getStartTime()));
    } catch(Exception e) {
      job.setEndTime(System.currentTimeMillis());
      job.setError(e);
      job.setStatus(DownloadStatus.COMPLETED);
      log.info("Exception occured during job execution: " + job, e);
    } finally {
      try {
        synchronized(this) {

          // Make sure the job is removed from the list of currently active jobs
          this.getActiveJobs().remove(job);

          // After the current processor is finshed we want to make sure that the
          // next item in the queue get's picked up
          this.checkWaitingJobs();

        }
      } finally {

        // Update the job itself
        this.fireJobCompleted(job);

      }
    }
  }

  Path runJobTransfer(DownloadJob job) throws IOException {

    Path targetFilePath = this.getTargetDirectory().resolve(job.getRequest().getTargetFileName());
    if(!Files.exists(targetFilePath.getParent())) {
      Files.createDirectory(targetFilePath.getParent());
    }
    job.setTargetFile(targetFilePath);
    this.fireJobStarted(job);

    if(DownloadStatus.ACTIVE.equals(job.getStatus())) {
      long inStreamSize = job.getRequest().getContentFactory().size();
      try(InputStream inStream = job.getRequest().getContentFactory().openStream()) {
        try(OutputStream outStream = Files.newOutputStream(targetFilePath, Files.exists(targetFilePath) ? StandardOpenOption.WRITE : StandardOpenOption.CREATE)) {
          long totalBytesWritten = 0;
          byte[] buffer = new byte[this.getBufferSize()];
          for(int bufferSize = inStream.read(buffer); bufferSize > -1 && DownloadStatus.ACTIVE.equals(job.getStatus()); bufferSize = inStream.read(buffer)) {
            outStream.write(buffer, 0, bufferSize);
            totalBytesWritten += bufferSize;
            job.fireProgress(totalBytesWritten, inStreamSize);
          }
          outStream.flush();
        } catch(final Exception e) {
          log.warn("Error occured during file transfer [" + job + "]", e);
          try {
            Files.deleteIfExists(targetFilePath);
          } catch(Exception e2) {
            log.debug("Cannot delete target file (after error during transfer) at: " + targetFilePath, e2);
          }
          throw e;
        }
      }
      if(!DownloadStatus.ACTIVE.equals(job.getStatus())) {
        try {
          Files.deleteIfExists(targetFilePath);
        } catch(Exception e) {
          log.debug("Cannot delete target file (after cancel) at: " + targetFilePath, e);
        }
      }
    }

    return targetFilePath;

  }

  // ---------------------------------------------------------------------------
  // --- Listener access -------------------------------------------------------
  // ---------------------------------------------------------------------------

  public void addListener(DownloadListener listener) {
    log.debug("Adding listener to engine: {}", listener);
    this.getListeners().add(Objects.requireNonNull(listener));
  }
  public boolean removeListener(DownloadListener listener) {
    return this.getListeners().remove(listener);
  }
  List<DownloadListener> getListeners() {
    return this.myListeners;
  }
  void setListeners(List<DownloadListener> listeners) {
    this.myListeners = listeners;
  }

  private void fireProcessorCountUpdated(int processorCount) {
    for(DownloadListener listener : this.getListeners()) {
      listener.processorCountUpdated(processorCount);
    }
  }

  private boolean fireRequestSubmitted(DownloadRequest request) {
    for(DownloadListener listener : this.getListeners()) {
      try {
        listener.requestSubmitted(request);
      } catch(DownloadRejectedException e) {
        log.info("Request rejected by listener: {} (Listener: {}, Message: {})", request, listener, e.getMessage());
        return false;
      }
    }
    return true;
  }

  private void fireJobScheduled(DownloadJob job) {
    for(DownloadListener listener : this.getListeners()) {
      listener.jobScheduled(job);
    }
  }

  private void fireJobStarted(DownloadJob job) {
    for(DownloadListener listener : this.getListeners()) {
      listener.jobStarted(job);
    }
  }

  private void fireJobCompleted(DownloadJob job) {
    for(DownloadListener listener : this.getListeners()) {
      listener.jobCompleted(job);
    }
  }

  private void fireJobCancelled(DownloadJob job) {
    for(DownloadListener listener : this.getListeners()) {
      listener.jobCancelled(job);
    }
  }

  // ---------------------------------------------------------------------------
  // --- Public property access methods ----------------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Checks if there is any work to be done, meaning that either jobs are
   * waiting to be executed or are being executed right now
   */
  public synchronized boolean isBusy() {
    return !this.getWaitingJobs().isEmpty() || !this.getActiveJobs().isEmpty();
  }

  public int getProcessorCount() {
    return this.myProcessorCount;
  }
  public void setProcessorCount(int newProcessorCount) {
    if(newProcessorCount <= 0) {
      throw new IllegalArgumentException("Parameter 'processorCount' must be larger than 0");
    } else if(this.myProcessorCount != newProcessorCount) {
      int oldProcessorCount = this.myProcessorCount;
      log.debug("Updating processor count from {} to {}", oldProcessorCount, newProcessorCount);
      synchronized(this) {
        this.myProcessorCount = newProcessorCount;
        if(newProcessorCount > oldProcessorCount) {
          this.checkWaitingJobs();
        }
      }
      this.fireProcessorCountUpdated(newProcessorCount);
    }
  }

  // ---------------------------------------------------------------------------
  // --- Private property access methods ---------------------------------------
  // ---------------------------------------------------------------------------

  ExecutorService getExecutorService() {
    return this.myExecutorService;
  }
  void setExecutorService(ExecutorService executorService) {
    this.myExecutorService = executorService;
  }

  Queue<DownloadJob> getWaitingJobs() {
    return this.myWaitingJobs;
  }
  void setWaitingJobs(Queue<DownloadJob> waitingJobs) {
    this.myWaitingJobs = waitingJobs;
  }

  List<DownloadJob> getActiveJobs() {
    return this.myActiveJobs;
  }
  void setActiveJobs(List<DownloadJob> activeJobs) {
    this.myActiveJobs = activeJobs;
  }

  Path getTargetDirectory() {
    return this.myTargetDirectory;
  }
  void setTargetDirectory(Path targetDirectory) {
    this.myTargetDirectory = targetDirectory;
  }

  int getBufferSize() {
    return this.myBufferSize;
  }
  void setBufferSize(int bufferSize) {
    if(bufferSize < 1) {
      throw new IllegalArgumentException("Parameter 'bufferSize' must be larger than 1");
    } else {
      this.myBufferSize = bufferSize;
    }
  }

}