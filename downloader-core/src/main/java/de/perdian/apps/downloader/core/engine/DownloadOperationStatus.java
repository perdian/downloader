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
