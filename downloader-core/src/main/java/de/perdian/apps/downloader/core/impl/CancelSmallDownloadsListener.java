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
package de.perdian.apps.downloader.core.impl;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.perdian.apps.downloader.core.DownloadJob;
import de.perdian.apps.downloader.core.DownloadListenerSkeleton;
import de.perdian.apps.downloader.core.DownloadStreamFactory;

/**
 * Rejects the execution of downloads smaller than a given threshold
 *
 * @author Christian Robert
 */

public class CancelSmallDownloadsListener extends DownloadListenerSkeleton {

  private static final Logger log = LogManager.getLogger(CancelSmallDownloadsListener.class);

  private long myThreshold = 0;

  public CancelSmallDownloadsListener(long threshold) {
    this.setThreshold(threshold);
  }

  @Override
  public void onJobStarted(DownloadJob job) {
    DownloadStreamFactory streamFactory = job.getRequest().getContentFactory();
    try {
      long streamSize = streamFactory.size();
      if(streamSize >= 0 && streamSize < this.getThreshold()) {
        StringBuilder cancelMessage = new StringBuilder();
        cancelMessage.append("Download too small! ");
        cancelMessage.append("Minimum: ").append(this.getThreshold()).append(" bytes. ");
        cancelMessage.append("Size: ").append(streamSize).append(" bytes.");
        job.cancel(cancelMessage.toString());
      }
    } catch(IOException e) {
      log.debug("Cannot validate size for stream: " + streamFactory, e);
    }
  }

  // ---------------------------------------------------------------------------
  // --- Property access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  public long getThreshold() {
    return this.myThreshold;
  }
  public void setThreshold(long threshold) {
    this.myThreshold = threshold;
  }

}