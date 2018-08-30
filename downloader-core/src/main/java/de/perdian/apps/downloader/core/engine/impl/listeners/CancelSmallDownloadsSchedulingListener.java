package de.perdian.apps.downloader.core.engine.impl.listeners;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.downloader.core.engine.DownloadOperation;
import de.perdian.apps.downloader.core.engine.DownloadSchedulingListener;
import de.perdian.apps.downloader.core.engine.DownloadTask;
import de.perdian.apps.downloader.core.support.StreamFactory;

/**
 * Rejects the execution of downloads smaller than a given threshold
 *
 * @author Christian Robert
 */

public class CancelSmallDownloadsSchedulingListener implements DownloadSchedulingListener {

    private static final Logger log = LoggerFactory.getLogger(CancelSmallDownloadsSchedulingListener.class);

    private long myThreshold = 0;

    public CancelSmallDownloadsSchedulingListener(long threshold) {
        this.setThreshold(threshold);
    }

    @Override
    public void onOperationTransferStarting(DownloadTask task, Path targetPath, DownloadOperation operation) {
        StreamFactory streamFactory = task.getContentFactory();
        try {
            long streamSize = streamFactory.size();
            if (streamSize >= 0 && streamSize < this.getThreshold()) {
                StringBuilder cancelMessage = new StringBuilder();
                cancelMessage.append("Download too small! ");
                cancelMessage.append("Minimum: ").append(this.getThreshold()).append(" bytes. ");
                cancelMessage.append("Size: ").append(streamSize).append(" bytes.");
                operation.cancel(cancelMessage.toString());
            }
        } catch (IOException e) {
            log.debug("Cannot validate size for stream: " + streamFactory, e);
        }
    }

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    public long getThreshold() {
        return this.myThreshold;
    }
    public void setThreshold(long threshold) {
        this.myThreshold = threshold;
    }

}
