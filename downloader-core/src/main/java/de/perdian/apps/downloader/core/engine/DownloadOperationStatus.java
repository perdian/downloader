/*
 * Copyright 2013-2018 Christian Robert
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
package de.perdian.apps.downloader.core.engine;

/**
 * Enumeration to describe the state of a {@link DownloadOperation} within a
 * {@link DownloadEngine}.
 *
 * @author Christian Robert
 */

public enum DownloadOperationStatus {

    /**
     * Signalizes that a {@link DownloadEngine} has started to process the data
     * for a specific {@link DownloadOperation} which means that a processor thread is
     * reading bytes from a source and writing them into a target file.
     */
    ACTIVE,

    /**
     * Signalizes that a {@link DownloadEngine} has completed the transfer
     * process for a {@link DownloadOperation}. No further interaction between the
     * {@link DownloadEngine} and any other kind of system will be performed.
     */
    COMPLETED,

    /**
     * Signalizes that a download operation has been cancelled by the user.
     */
    CANCELLED,

}
