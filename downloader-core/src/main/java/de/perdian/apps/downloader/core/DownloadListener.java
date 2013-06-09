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

/**
 * Callback interface to get notified upon changes in an engine
 *
 * @author Christian Robert
 */

public interface DownloadListener {

  /**
   * Called when the {@code processorCount} property of the
   * {@link DownloadEngine} has been changed
   */
  public void onProcessorCountUpdated(int newProcessorCount);

  /**
   * Called when a new request is about to be added into a
   * {@link DownloadEngine}
   *
   * @throws DownloadRejectedException
   *   thrown to indicate that the request should be discarded and no further
   *   interaction within the {@link DownloadEngine} should be performed.
   */
  public void onRequestSubmitted(DownloadRequest request) throws DownloadRejectedException;

  /**
   * Called when a new job has been submitted into the {@link DownloadEngine}
   * instance
   */
  public void onJobScheduled(DownloadJob job);

  /**
   * Called when a job has been started, that is: it's execution has begone
   */
  public void onJobStarted(DownloadJob job);

  /**
   * Called when a job has been completed by a {@link DownloadEngine} instance
   */
  public void onJobCompleted(DownloadJob job);

  /**
   * Called when a job has been cancelled, which means it has been removed from
   * the queue of waiting jobs
   */
  public void onJobCancelled(DownloadJob job);

}