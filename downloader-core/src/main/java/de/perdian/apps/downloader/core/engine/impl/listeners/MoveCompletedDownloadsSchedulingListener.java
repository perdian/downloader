/*
 * Copyright 2013-2019 Christian Robert
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
package de.perdian.apps.downloader.core.engine.impl.listeners;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.downloader.core.engine.DownloadOperation;
import de.perdian.apps.downloader.core.engine.DownloadSchedulingListener;
import de.perdian.apps.downloader.core.engine.DownloadTask;

/**
 * Moves completed downloads into a specified target directory
 *
 * @author Christian Robert
 */

public class MoveCompletedDownloadsSchedulingListener implements DownloadSchedulingListener {

    private static final Logger log = LoggerFactory.getLogger(MoveCompletedDownloadsSchedulingListener.class);

    private Path targetDirectory = null;

    public MoveCompletedDownloadsSchedulingListener(Path targetDirectory) {
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
    public void onOperationTransferCompleted(DownloadTask task, Path intermediateFilePath, DownloadOperation operation) {
        if (intermediateFilePath != null && operation.getCancelTime() != null) {
            try {
                Files.deleteIfExists(intermediateFilePath);
            } catch (Exception e) {
                log.warn("Cannot delete file for cancelled download at: " + intermediateFilePath, e);
            }
        } else if (intermediateFilePath != null && Files.exists(intermediateFilePath)) {
            Path targetFilePath = this.getTargetDirectory().resolve(intermediateFilePath.getFileName());
            try {

                if (!Files.exists(targetFilePath.getParent())) {
                    log.trace("Creating target directory at: {}", targetFilePath.getParent());
                    Files.createDirectory(targetFilePath.getParent());
                }

                log.trace("Moving downloaded file from '{}' to '{}'", intermediateFilePath, targetFilePath);
                Files.move(intermediateFilePath, targetFilePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

            } catch (Exception e) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Cannot move downloaded file from '").append(intermediateFilePath);
                errorMessage.append("' to '").append(targetFilePath).append("'");
                log.error(errorMessage.toString(), e);
            }
        }
    }

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    public Path getTargetDirectory() {
        return this.targetDirectory;
    }
    public void setTargetDirectory(Path targetDirectory) {
        this.targetDirectory = Objects.requireNonNull(targetDirectory, "Parameter 'targetDirectory' must not be null");
    }

}
