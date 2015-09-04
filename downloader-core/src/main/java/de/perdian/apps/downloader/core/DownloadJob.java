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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Wrapper for all the available information about a download that has been
 * accepted into a {@link DownloadEngine} object.
 *
 * @author Christian Robert
 */

public class DownloadJob {

    private DownloadEngine owner = null;
    private DownloadRequest request = null;
    private DownloadStatus status = null;
    private Long scheduleTime = null;
    private Long startTime = null;
    private Long endTime = null;
    private Long cancelTime = null;
    private String cancelReason = null;
    private Path targetFile = null;
    private Exception error = null;
    private int priority = 0;
    private List<DownloadProgressListener> progressListeners = new ArrayList<>();

    DownloadJob(DownloadEngine owner) {
        this.setOwner(owner);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.getClass().getSimpleName());
        result.append("[status=").append(this.getStatus());
        result.append(",targetFile=").append(this.getTargetFile());
        result.append(",request=").append(this.getRequest());
        return result.append("]").toString();
    }

    // -------------------------------------------------------------------------
    // --- Actions -------------------------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * Cancels the current job which means stopping all operations that are
     * currently using the jon. If the job has not been started yet it will be
     * removed from the queue of waiting jobs and will not start at all. If the
     * job has already been started it's status will be updated. Please note
     * that this doesn't mean that the transfer will immediately stop! If the
     * processor thread itself doesn't provide an option to immedately stop the
     * process the actual download might continue until the processor thread is
     * able to stop.
     *
     * @param reason
     *     the reason why this job was cancelled
     */
    public void cancel(String reason) {
        this.getOwner().cancelJob(this, reason);
    }

    /**
     * Forces this job to be started. Normally the {@link DownloadEngine} will
     * decide when a submitted job will be started, that is when a processor
     * thread will be assigned to perform the data transfer from the source to a
     * local file. If this method is called however the transfer will start
     * immediately. An additional processor thread will be created if all
     * processor slots are occupied and this surplus thread will then execute
     * the transfer itself. If the ob is already started then this method will
     * do nothing at all.
     */
    public void forceStart() {
        this.getOwner().startJob(this, true);
    }

    // -------------------------------------------------------------------------
    // --- Listener access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    void fireProgress(long bytesWritten, long totalBytes) {
        this.getProgressListeners().forEach(l -> l.onProgress(this, bytesWritten, totalBytes));
    }
    List<DownloadProgressListener> getProgressListeners() {
        return this.progressListeners;
    }
    void setProgressListeners(List<DownloadProgressListener> progressListeners) {
        this.progressListeners = progressListeners;
    }
    public void addProgressListener(DownloadProgressListener progressListener) {
        this.getProgressListeners().add(Objects.requireNonNull(progressListener));
    }
    public boolean removeProgressListener(DownloadProgressListener progressListener) {
        return this.getProgressListeners().remove(progressListener);
    }

    // -------------------------------------------------------------------------
    // --- Inner classes -------------------------------------------------------
    // -------------------------------------------------------------------------

    public static class PriorityComparator implements Comparator<DownloadJob> {

        @Override
        public int compare(DownloadJob o1, DownloadJob o2) {
            if (o1 == null) {
                return o2 == null ? 0 : -1;
            } else if (o2 == null) {
                return 1;
            } else {
                int p1 = o1.getPriority();
                int p2 = o2.getPriority();
                if (p1 == p2) {
                    if (o1.getScheduleTime() == o2.getScheduleTime()) {
                        return 0;
                    } else {
                        return o1.getScheduleTime() < o2.getScheduleTime() ? -1 : 1;
                    }
                } else {
                    return p1 < p2 ? 1 : -1;
                }
            }
        }

    }

    // ---------------------------------------------------------------------------
    // --- Public property access methods ----------------------------------------
    // ---------------------------------------------------------------------------

    /**
     * Gets the request from which this {@link DownloadJob} has been created
     */
    public DownloadRequest getRequest() {
        return this.request;
    }
    void setRequest(DownloadRequest request) {
        this.request = request;
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
     * Gets the time when the download has been accepted by a
     * {@link DownloadEngine}
     */
    public Long getScheduleTime() {
        return this.scheduleTime;
    }
    void setScheduleTime(Long scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    /**
     * Gets the time when the job was started, which means the time when the
     * {@link DownloadEngine} instance has assigned a processor thread to load
     * the data from a remote resource that reads the content and writes them
     * into a temporary file
     */
    public Long getStartTime() {
        return this.startTime;
    }
    void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the time when the job was finished, which means the processor thread
     * transfering the data from a remote resource to a local file because
     * either the content has been transfered completely, or because of a error
     * that stopped the transfer.
     */
    public Long getEndTime() {
        return this.endTime;
    }
    void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets the time when the job has been cancelled
     */
    public Long getCancelTime() {
        return this.cancelTime;
    }
    void setCancelTime(Long cancelTime) {
        this.cancelTime = cancelTime;
    }

    /**
     * Sets the reason why this job was cancelled
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

    /**
     * Gets the owner of this job, which means the {@link DownloadEngine} in
     * which the current job resides and who is responsible for activating it.
     */
    public DownloadEngine getOwner() {
        return this.owner;
    }
    void setOwner(DownloadEngine owner) {
        this.owner = owner;
    }

    /**
     * Gets the result path into which the content has been written
     */
    public Path getTargetFile() {
        return this.targetFile;
    }
    void setTargetFile(Path targetFile) {
        this.targetFile = targetFile;
    }

    /**
     * Gets the priority of the job. Jobs with higher priority will get picked
     * up first by the engine.
     */
    public int getPriority() {
        return this.priority;
    }
    void setPriority(int priority) {
        this.priority = priority;
    }

}