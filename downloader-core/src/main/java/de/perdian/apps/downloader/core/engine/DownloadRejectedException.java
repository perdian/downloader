package de.perdian.apps.downloader.core.engine;

/**
 * Thrown to indicate that a {@link DownloadRequest} should not be accepted by a
 * {@link DownloadEngine}
 *
 * @author Christian Robert
 */

public class DownloadRejectedException extends Exception {

    static final long serialVersionUID = 1L;

    public DownloadRejectedException(String message) {
        super(message);
    }

}
