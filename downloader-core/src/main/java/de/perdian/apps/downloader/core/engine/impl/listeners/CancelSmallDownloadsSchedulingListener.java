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
