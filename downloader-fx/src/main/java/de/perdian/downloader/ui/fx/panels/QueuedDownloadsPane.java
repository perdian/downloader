/*
 * Copyright 2013 Christian Robert
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
package de.perdian.downloader.ui.fx.panels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.downloader.core.DownloadEngine;
import de.perdian.apps.downloader.core.DownloadJob;
import de.perdian.apps.downloader.core.DownloadListener;

public class QueuedDownloadsPane extends AbstractItemContainerPane<QueuedDownloadItemPane> {

    static final Logger log = LoggerFactory.getLogger(QueuedDownloadsPane.class);

    public QueuedDownloadsPane(DownloadEngine engine) {
        this.setMinWidth(200);
        engine.addListener(new DownloadListenerImpl());
    }

    @Override
    protected QueuedDownloadItemPane createItemPane(DownloadJob job) {
        return new QueuedDownloadItemPane(job);
    }

    // -------------------------------------------------------------------------
    // --- Inner classes -------------------------------------------------------
    // -------------------------------------------------------------------------

    class DownloadListenerImpl implements DownloadListener {

        @Override
        public void onJobScheduled(DownloadJob job) {
            QueuedDownloadsPane.this.addDownloadJob(job);
        }

        @Override
        public void onJobStarted(DownloadJob job) {
            QueuedDownloadsPane.this.removeDownloadJob(job);
        }

        @Override
        public void onJobCompleted(DownloadJob job) {
            QueuedDownloadsPane.this.removeDownloadJob(job);
        }

        @Override
        public void onJobCancelled(DownloadJob job) {
            QueuedDownloadsPane.this.removeDownloadJob(job);
        }

    }

}