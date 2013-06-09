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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.perdian.apps.downloader.core.DownloadJob;
import de.perdian.apps.downloader.core.DownloadListenerSkeleton;

/**
 * Moves completed downloads into a specified target directory
 *
 * @author Christian Robert
 */

public class MoveCompletedDownloadsListener extends DownloadListenerSkeleton {

  private static final Logger log = LogManager.getLogger(MoveCompletedDownloadsListener.class);

  private Path myTargetDirectory = null;

  public MoveCompletedDownloadsListener(Path targetDirectory) {
    this.setTargetDirectory(targetDirectory);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(this.getClass().getSimpleName());
    result.append("[targetDirectory=").append(this.getTargetDirectory());
    return result.append("]").toString();
  }

  @Override
  public void onJobCompleted(DownloadJob job) {
    if(job.getTargetFile() != null && job.getCancelTime() != null) {
      try {
        Files.deleteIfExists(job.getTargetFile());
      } catch(Exception e) {
        log.warn("Cannot delete file for cancelled download at: " + job.getTargetFile(), e);
      }
    } else if(job.getTargetFile() != null && Files.exists(job.getTargetFile())) {
      Path targetFilePath = this.getTargetDirectory().resolve(job.getRequest().getTargetFileName());
      try {

        if(!Files.exists(targetFilePath.getParent())) {
          log.trace("Creating target directory at: {}", targetFilePath.getParent());
          Files.createDirectory(targetFilePath.getParent());
        }

        log.trace("Moving downloaded file from '{}' to '{}'", job.getTargetFile(), targetFilePath);
        Files.move(job.getTargetFile(), targetFilePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

      } catch(Exception e) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Cannot move downloaded file from '").append(job.getTargetFile());
        errorMessage.append("' to '").append(targetFilePath).append("'");
        log.error(errorMessage.toString(), e);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // --- Property access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  public Path getTargetDirectory() {
    return this.myTargetDirectory;
  }
  public void setTargetDirectory(Path targetDirectory) {
    this.myTargetDirectory = Objects.requireNonNull(targetDirectory, "Parameter 'targetDirectory' must not be null");
  }

}