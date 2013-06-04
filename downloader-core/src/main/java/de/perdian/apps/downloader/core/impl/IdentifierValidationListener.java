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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.Objects;

import de.perdian.apps.downloader.core.DownloadEngine;
import de.perdian.apps.downloader.core.DownloadJob;
import de.perdian.apps.downloader.core.DownloadListenerSkeleton;
import de.perdian.apps.downloader.core.DownloadRejectedException;
import de.perdian.apps.downloader.core.DownloadRequest;

/**
 * Makes sure that a {@link DownloadRequest} will only be accepted by a
 * {@link DownloadEngine} if it's identifier has not already been downloaded
 * at a previous time. The listener itself keeps track of downloads already
 * finished by storing marker files inside a marker directory.
 *
 * @author Christian Robert
 */

public class IdentifierValidationListener extends DownloadListenerSkeleton {

  private Path myMarkerDirectory = null;

  public IdentifierValidationListener(Path markerDirectory) {
    this.setMarkerDirectory(Objects.requireNonNull(markerDirectory, "Parameter 'markerDirectory' must not be null"));
  }

  @Override
  public void requestSubmitted(DownloadRequest request) throws DownloadRejectedException {
    if(request.getId() != null && this.hasIdentifier(request.getId())) {
      throw new DownloadRejectedException("Marker file existing for id: " + request.getId());
    }
  }

  @Override
  public void jobCompleted(DownloadJob job) {
    if(job.getRequest().getId() != null) {
      this.pushIdentifier(job.getRequest().getId(), job.getError() == null ? "COMPLETED" : "ERROR [" + job.getError() + "]");
    }
  }

  @Override
  public void jobCancelled(DownloadJob job) {
    if(job.getRequest().getId() != null) {
      this.pushIdentifier(job.getRequest().getId(), "CANCELLED");
    }
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(this.getClass().getSimpleName());
    result.append("[markerDirectory=").append(this.getMarkerDirectory());
    return result.append("]").toString();
  }

  // ---------------------------------------------------------------------------
  // --- Identifier handling ---------------------------------------------------
  // ---------------------------------------------------------------------------

  private void pushIdentifier(String identifier, String comment) {
    try {

      if(!Files.exists(this.getMarkerDirectory())) {
        Files.createDirectory(this.getMarkerDirectory());
      }
      Path markerFile = this.getMarkerDirectory().resolve(identifier + ".marker");
      if(!Files.exists(markerFile)) {
        Files.createFile(markerFile);
      }

      StringBuilder fileContent = new StringBuilder();
      fileContent.append(new Date()).append("\n");
      fileContent.append(comment);
      Files.write(markerFile, fileContent.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);

    } catch(IOException e) {
      throw new RuntimeException("Cannot access marker directory at: " + this.getMarkerDirectory(), e);
    }
  }

  private boolean hasIdentifier(String identifier) {
    return Files.exists(this.getMarkerDirectory().resolve(identifier + ".marker"));
  }

  // ---------------------------------------------------------------------------
  // --- Property access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  public Path getMarkerDirectory() {
    return this.myMarkerDirectory;
  }
  public void setMarkerDirectory(Path markerDirectory) {
    this.myMarkerDirectory = markerDirectory;
  }

}