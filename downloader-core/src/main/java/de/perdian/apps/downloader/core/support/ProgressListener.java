package de.perdian.apps.downloader.core.support;

import java.util.Collection;

/**
 * Callback interface to get notified when a change in the state of a
 * {@code DownloadOperation} has been detected
 *
 * @author Christian Robert
 */

public interface ProgressListener {

    void onProgress(String message, Long bytesWritten, Long bytesTotal);

    public static ProgressListener compose(Collection<ProgressListener> operationListeners) {
        return (message, bytesWritten, bytesTotal) -> {
            for (ProgressListener progressListener : operationListeners) {
                progressListener.onProgress(message, bytesWritten, bytesTotal);
            }
        };
    }

}
