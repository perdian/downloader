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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A downloader represents the central manager object, into which new jobs that
 * are supposed to download information from an external resource will be added
 * and from which information about the current state of the downloader and it's
 * jobs can be requested.
 *
 * Client code should only change the state of the download queue and associated
 * processes using the methods provided in this API class.
 *
 * @author Christian Robert
 */

public class Downloader {

  private static final Logger log = LogManager.getLogger(Downloader.class);

  private ExecutorService myExecutorService = Executors.newCachedThreadPool();
  private List<DownloaderListener> myListeners = new CopyOnWriteArrayList<>();
  private Path myTargetDirectory = null;
  private Path myWorkingDirectory = null;
  private Path myMetadataDirectory = null;
  private int myProcessorCount = 1;
  private List<DownloadJob> myWaitingJobs = new CopyOnWriteArrayList<>();
  private List<DownloadJob> myActiveJobs = new CopyOnWriteArrayList<>();
  private boolean stateShutdown = false;

  /**
   * The downloader is not supposed to be instantiated directly by client code.
   * It should only be created using the {@code buildEngine} method provided by
   * a {@link DownloaderBuilder} instance.
   */
  Downloader() {
  }

  /**
   * Submits a new download into this downloader. It is up to the downloader
   * implementation to decide whether or not the job can be started immediately
   * or if it needs to be put in any kind of queue and await a free download
   * slot.
   *
   * @param request
   *   the request containing all the information from which a download can be
   *   constructuted.
   * @return
   *   a {@link DownloadJob} if the downloader has accepted the request and
   *   scheduled it for execution or {@code null} if the downloader instance
   *   rejected the job and will not execute the transfer process.
   */
  public DownloadJob submit(DownloadRequest request) {
    return this.submit(request, null);
  }

  /**
   * Submits a new download into this downloader. It is up to the downloader
   * implementation to decide whether or not the job can be started immediately
   * or if it needs to be put in any kind of queue and await a free download
   * slot.
   *
   * @param request
   *   the request containing all the information from which a download can be
   *   constructuted.
   * @param listeners
   *   initial list of listeners to be used for the created job.
   * @return
   *   a {@link DownloadJob} if the downloader has accepted the request and
   *   scheduled it for execution or {@code null} if the downloader instance
   *   rejected the job and will not execute the transfer process.
   */
  public DownloadJob submit(DownloadRequest request, List<? extends DownloadJobListener> listeners) {
    if(this.isShutdown()) {
      throw new IllegalStateException(Downloader.class.getSimpleName() + " is shutdown!");
    } else if(request.getTargetFileName() == null) {
      throw new IllegalArgumentException("Property targetFileName must not be null!");
    } else if(request.getStreamFactory() == null) {
      throw new IllegalArgumentException("Property streamFactory must not be null!");
    } else {
      DownloadJob downloadJob = new DownloadJob(this);
      downloadJob.setListeners(listeners == null ? new ArrayList<DownloadJobListener>() : new ArrayList<>(listeners));
      downloadJob.setRequest(request);
      downloadJob.setScheduleTime(System.currentTimeMillis());
      downloadJob.setStatus(DownloadStatus.SCHEDULED);
      synchronized(this) {
        if(!this.startJob(downloadJob, false)) {
          this.getWaitingJobs().add(downloadJob);
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
   *   changes in the downloader state (a download was completed or cancelled)
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
   *   changes in the downloader state (a download was picked up for execution
   *   or was cancelled) will not be reflected into this result list. The list
   *   itself will therefore be immutable
   */
  public List<DownloadJob> listWaitingJobs() {
    return Collections.unmodifiableList(new ArrayList<>(this.getWaitingJobs()));
  }

  /**
   * Shutdown the current downloader, which means execute all remaining jobs
   * that are currently active but do not accept any further jobs to be added
   * to this downloader.
   *
   * @return
   *   the list of currently waiting jobs which are not going to be executed
   *   any more
   */
  public synchronized List<DownloadJob> shutdown() {
    if(!this.isShutdown()) {
      this.setShutdown(true);
      this.getExecutorService().shutdown();
    }
    return Collections.unmodifiableList(this.getWaitingJobs());
  }

  /**
   * Shutdown the current downloader, which means execute all remaining jobs
   * that are currently active but do not accept any further jobs to be added
   * to this downloader. Also wait until all remaining jobs have been completed
   *
   * @return
   *   the list of currently waiting jobs which are not going to be executed
   *   any more
   */
  public List<DownloadJob> shutdownAndWait() {
    if(!this.isShutdown()) {
      this.shutdown();
      try {
        this.getExecutorService().awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
      } catch(InterruptedException e) {
        log.warn("Cannot wait for shutdown of ExecutorService", e);
      }
    }
    return Collections.unmodifiableList(this.getWaitingJobs());
  }

  // ---------------------------------------------------------------------------
  // --- Status transitions ----------------------------------------------------
  // ---------------------------------------------------------------------------

  synchronized boolean startJob(final DownloadJob job, boolean ignoreSlots) {
    if(!this.isShutdown() && (ignoreSlots || this.getProcessorCount() > this.getActiveJobs().size())) {

      // Make sure we remove the job from the waiting list, so no matter from
      // where we come from we always leave in a consistent state
      this.getWaitingJobs().remove(job);

      // Now make sure we have the right satus upon the job itself
      job.setStartTime(System.currentTimeMillis());
      job.setStatus(DownloadStatus.ACTIVE);
      Downloader.this.getActiveJobs().add(job);

      this.getExecutorService().submit(new Runnable() {
        @Override public void run() {
          Downloader.this.runJob(job);
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
    if(this.getWaitingJobs().remove(job)) {

      // The job could be found in the list of waiting jobs, so all we need to
      // do is remove him from there and we're done!
      return true;

    } else if(!this.getActiveJobs().contains(job)) {

      // The job could not be found in the list of active jobs, which means we
      // have no way of handling him at all - so we just exit
      return false;

    } else {

      job.setStatus(DownloadStatus.CANCELLED);
      job.setCancelTime(System.currentTimeMillis());
      return true;

    }
  }

  synchronized void checkWaitingJobs() {
    while(!this.isShutdown() && !this.getWaitingJobs().isEmpty()) {
      if(this.getActiveJobs().size() >= this.getProcessorCount()) {
        return;
      } else {
        this.startJob(this.getWaitingJobs().remove(0), false);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // --- Job execution ---------------------------------------------------------
  // ---------------------------------------------------------------------------

  void runJob(DownloadJob job) {
    try {
      DownloadJobTransferHandler fileHandler = new DownloadJobTransferHandler();
      fileHandler.setWorkingDirectory(this.getWorkingDirectory());
      fileHandler.setTargetDirectory(this.getTargetDirectory());
      job.setResult(fileHandler.executeTransfer(job));
    } catch(Exception e) {
      job.setError(e);
    } finally {
      try {

        // Update the job itself
        job.setStatus(DownloadStatus.COMPLETED);
        job.setEndTime(System.currentTimeMillis());

      } finally {

        // Make sure the downloader itself remains in a consistent state
        synchronized(this) {

          // Make sure the job is removed from the list of currently active jobs
          this.getActiveJobs().remove(job);

          // After the current processor is finshed we want to make sure that the
          // next item in the queue get's picked up
          this.checkWaitingJobs();

        }

      }
    }
  }

  // ---------------------------------------------------------------------------
  // --- Public property access methods ----------------------------------------
  // ---------------------------------------------------------------------------

  public synchronized int getProcessorCount() {
    return this.myProcessorCount;
  }
  public synchronized void setProcessorCount(int processorCount) {
    this.myProcessorCount = processorCount;
    this.checkWaitingJobs();
    for(DownloaderListener listener : this.getListeners()) {
      listener.processorCountUpdated(processorCount);
    }
  }

  public void addListener(DownloaderListener listener) {
    this.getListeners().add(listener);
  }
  public boolean removeListener(DownloaderListener listener) {
    return this.getListeners().remove(listener);
  }
  List<DownloaderListener> getListeners() {
    return this.myListeners;
  }
  void setListeners(List<DownloaderListener> listeners) {
    this.myListeners = listeners;
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

  List<DownloadJob> getWaitingJobs() {
    return this.myWaitingJobs;
  }
  void setWaitingJobs(List<DownloadJob> waitingJobs) {
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

  Path getWorkingDirectory() {
    return this.myWorkingDirectory;
  }
  void setWorkingDirectory(Path workingDirectory) {
    this.myWorkingDirectory = workingDirectory;
  }

  Path getMetadataDirectory() {
    return this.myMetadataDirectory;
  }
  void setMetadataDirectory(Path metadataDirectory) {
    this.myMetadataDirectory = metadataDirectory;
  }

  boolean isShutdown() {
    return this.stateShutdown;
  }
  void setShutdown(boolean shutdown) {
    this.stateShutdown = shutdown;
  }

}