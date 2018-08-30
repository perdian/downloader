package de.perdian.apps.downloader.core.engine;

import java.nio.file.Path;

/**
 * Callback interface to get notified when a new request is being executed.
 *
 * @author Christian Robert
 */

public interface DownloadSchedulingListener {

    /**
     * Called when a new request is about to be added into a
     * {@link DownloadEngine}
     *
     * @throws DownloadRejectedException
     *     thrown to indicate that the request should be discarded and no
     *     further interaction within the {@link DownloadEngine} should be
     *     performed.
     */
    default void onRequestSubmitted(DownloadRequest request) throws DownloadRejectedException {
    }

    /**
     * Called when a new job has been submitted into the {@link DownloadEngine} instance, has been
     * accepted and scheduled for future execution
     */
    default void onRequestScheduled(DownloadRequestWrapper requestWrapper) {
    }

    /**
     * Called when a request operation has been cancelled
     */
    default void onRequestCancelled(DownloadRequestWrapper requestWrapper) {
    }

    /**
     * Called when an operation has been started, that is: it's execution has begun
     */
    default void onOperationStarting(DownloadOperation operation) {
    }

    /**
     * Called when an operation has been completed by a {@link DownloadEngine} instance
     */
    default void onOperationCompleted(DownloadOperation operation) {
    }

    /**
     * Called when an operation has been cancelled
     */
    default void onOperationCancelled(DownloadOperation operation) {
    }

    /**
     * Called when the actual data transfer is started
     */
    default void onOperationTransferStarting(DownloadTask task, Path targetFile, DownloadOperation operation) {
    }

    /**
     * Called when the actual data transfer is completed
     */
    default void onOperationTransferCompleted(DownloadTask task, Path targetFile, DownloadOperation operation) {
    }

}
