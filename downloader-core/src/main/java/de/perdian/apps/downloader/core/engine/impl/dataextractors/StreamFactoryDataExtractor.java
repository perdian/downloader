/*
 * Copyright 2013-2019 Christian Robert
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
package de.perdian.apps.downloader.core.engine.impl.dataextractors;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

import de.perdian.apps.downloader.core.engine.DownloadDataExtractor;
import de.perdian.apps.downloader.core.engine.DownloadOperationStatus;
import de.perdian.apps.downloader.core.support.ProgressListener;
import de.perdian.apps.downloader.core.support.StreamFactory;

public class StreamFactoryDataExtractor implements DownloadDataExtractor {

    private StreamFactory streamFactory = null;
    private int bufferSize = 1024 * 8; // 8 KiB
    private int notificationSize = 1024 * 64; // 64 KiB

    public StreamFactoryDataExtractor(StreamFactory streamFactory) {
        this.setStreamFactory(streamFactory);
    }

    @Override
    public void extractData(OutputStream targetStream, ProgressListener progressListener, Supplier<DownloadOperationStatus> statusSupplier) throws Exception {
        long inStreamSize = this.getStreamFactory().size();
        try (InputStream inStream = this.getStreamFactory().openStream()) {

            long notificationBlockSize = Math.max(this.getBufferSize(), this.getNotificationSize());
            long nextNotification = notificationBlockSize;
            long totalBytesWritten = 0;
            progressListener.onProgress(null, 0L, inStreamSize);

            byte[] buffer = new byte[this.getBufferSize()];
            for (int bufferSize = inStream.read(buffer); bufferSize > -1 && DownloadOperationStatus.ACTIVE.equals(statusSupplier.get()); bufferSize = inStream.read(buffer)) {
                targetStream.write(buffer, 0, bufferSize);
                totalBytesWritten += bufferSize;
                if (totalBytesWritten > nextNotification) {
                    nextNotification += notificationBlockSize;
                    progressListener.onProgress(null, totalBytesWritten, inStreamSize);
                }
            }
            progressListener.onProgress(null, totalBytesWritten, inStreamSize);

        }
    }

    public StreamFactory getStreamFactory() {
        return this.streamFactory;
    }
    public void setStreamFactory(StreamFactory streamFactory) {
        this.streamFactory = streamFactory;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }
    public void setBufferSize(int bufferSize) {
        if (bufferSize < 1) {
            throw new IllegalArgumentException("Parameter 'bufferSize' must be larger than 1");
        } else {
            this.bufferSize = bufferSize;
        }
    }

    public int getNotificationSize() {
        return this.notificationSize;
    }
    public void setNotificationSize(int notificationSize) {
        if (notificationSize < 1) {
            throw new IllegalArgumentException("Parameter 'notificationSize' must be larger than 1");
        } else {
            this.notificationSize = notificationSize;
        }
    }

}
