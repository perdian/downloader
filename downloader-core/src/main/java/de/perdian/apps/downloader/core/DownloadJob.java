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
import java.util.Collections;
import java.util.List;

/**
 * Wrapper for all the available information about a download that has been
 * accepted into a {@link Downloader} object.
 *
 * @author Christian Robert
 */

public class DownloadJob {

  private Downloader myOwner = null;
  private DownloadRequest myRequest = null;
  private DownloadStatus myStatus = null;
  private Long myScheduleTime = null;
  private Long myStartTime = null;
  private Long myEndTime = null;
  private Long myCancelTime = null;
  private Path myResult = null;
  private Exception myError = null;
  private List<DownloadJobListener> myListeners = Collections.emptyList();

  DownloadJob(Downloader owner) {
    this.setOwner(owner);
  }

  // ---------------------------------------------------------------------------
  // --- Actions ---------------------------------------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Cancels the current job which means stopping all operations that are
   * currently using the jon. If the job has not been started yet it will be
   * removed from the queue of waiting jobs and will not start at all. If the
   * job has already been started it's status will be updated. Please note that
   * this doesn't mean that the transfer will immediately stop! If the processor
   * thread itself doesn't provide an option to immedately stop the process the
   * actual download might continue until the processor thread is able to stop.
   */
  public void cancel() {
    this.getOwner().cancelJob(this);
  }

  /**
   * Forces this job to be started. Normally the {@link Downloader} will decide
   * when a submitted job will be started, that is when a processor thread will
   * be assigned to perform the data transfer from the source to a local file.
   * If this method is called however the transfer will start immediately. An
   * additional processor thread will be created if all processor slots are
   * occupied and this surplus thread will then execute the transfer itself.
   * If the ob is already started then this method will do nothing at all.
   */
  public void forceStart() {
    this.getOwner().startJob(this, false);
  }

  // ---------------------------------------------------------------------------
  // --- Listener access -------------------------------------------------------
  // ---------------------------------------------------------------------------

  private void fireStatusUpdated(DownloadStatus status) {
    for(DownloadJobListener listener : this.getListeners()) {
      switch(status) {
        case ACTIVE:
          listener.jobStarted(this);
          break;
        case CANCELLED:
          listener.jobCancelled(this);
          break;
        case COMPLETED:
          listener.jobCompleted(this);
          break;
        case SCHEDULED:
          listener.jobScheduled(this);
          break;
      }
    }
  }

  // ---------------------------------------------------------------------------
  // --- Listener access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  void fireProgress(long bytesWritten, long totalBytes) {
    for(DownloadJobListener listener : this.getListeners()) {
      listener.jobProgress(this, bytesWritten, totalBytes);
    }
  }
  List<DownloadJobListener> getListeners() {
    return this.myListeners;
  }
  void setListeners(List<DownloadJobListener> listeners) {
    this.myListeners = listeners;
  }

  // ---------------------------------------------------------------------------
  // --- Public property access methods ----------------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Gets the request from which this {@link DownloadJob} has been created
   */
  public DownloadRequest getRequest() {
    return this.myRequest;
  }
  void setRequest(DownloadRequest request) {
    this.myRequest = request;
  }

  /**
   * Gets the status of the current download
   */
  public DownloadStatus getStatus() {
    return this.myStatus;
  }
  void setStatus(DownloadStatus status) {
    this.myStatus = status;
    this.fireStatusUpdated(status);
  }

  /**
   * Gets the time when the download has been accepted by a {@link Downloader}
   */
  public Long getScheduleTime() {
    return this.myScheduleTime;
  }
  void setScheduleTime(Long scheduleTime) {
    this.myScheduleTime = scheduleTime;
  }

  /**
   * Gets the time when the job was started, which means the time when the
   * {@link Downloader} instance has assigned a processor thread to load the
   * data from a remote resource that reads the content and writes them into a
   * temporary file
   */
  public Long getStartTime() {
    return this.myStartTime;
  }
  void setStartTime(Long startTime) {
    this.myStartTime = startTime;
  }

  /**
   * Gets the time when the job was finished, which means the processor thread
   * transfering the data from a remote resource to a local file because either
   * the content has been transfered completely, or because of a error that
   * stopped the transfer.
   */
  public Long getEndTime() {
    return this.myEndTime;
  }
  void setEndTime(Long endTime) {
    this.myEndTime = endTime;
  }

  /**
   * Gets the time when the job has been cancelled
   */
  public Long getCancelTime() {
    return this.myCancelTime;
  }
  void setCancelTime(Long cancelTime) {
    this.myCancelTime = cancelTime;
  }

  /**
   * Gets an {@code Exception} that occured during the transfer process.
   */
  public Exception getError() {
    return this.myError;
  }
  void setError(Exception error) {
    this.myError = error;
  }

  /**
   * Gets the owner of this job, which means the {@link Downloader} in which the
   * current job resides and who is responsible for activating it.
   */
  public Downloader getOwner() {
    return this.myOwner;
  }
  void setOwner(Downloader owner) {
    this.myOwner = owner;
  }

  /**
   * Gets the result path into which the content has been written
   */
  public Path getResult() {
    return this.myResult;
  }
  void setResult(Path result) {
    this.myResult = result;
  }

}