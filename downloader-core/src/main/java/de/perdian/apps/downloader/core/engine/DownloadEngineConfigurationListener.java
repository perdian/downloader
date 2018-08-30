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

import java.nio.file.Path;

/**
 * Callback interface to get notified upon configuration changes in a {@link DownloadEngine}
 *
 * @author Christian Robert
 */

public interface DownloadEngineConfigurationListener {

    /**
     * Called when the {@code processorCount} property of the
     * {@link DownloadEngine} has been changed
     */
    default void onProcessorCountUpdated(int newProcessorCount) {
    }

    /**
     * Called when the {@code targetDirectory} property of the
     * {@link DownloadEngine} has been changed
     */
    default void onTargetDirectoryUpdated(Path newTargetDirectory) {
    }


}
