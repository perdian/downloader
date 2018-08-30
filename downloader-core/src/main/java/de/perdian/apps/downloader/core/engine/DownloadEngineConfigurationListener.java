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
