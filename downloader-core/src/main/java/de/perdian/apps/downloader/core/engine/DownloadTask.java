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
package de.perdian.apps.downloader.core.engine;

import java.util.Objects;

import de.perdian.apps.downloader.core.support.StreamFactory;

public class DownloadTask {

    private String targetFileName = null;
    private DownloadDataExtractor dataExtractor = null;
    private StreamFactory previewImageFactory = null;

    public DownloadTask(String targetFileName, DownloadDataExtractor dataExtractor) {
        this(targetFileName, dataExtractor, null);
    }

    public DownloadTask(String targetFileName, DownloadDataExtractor dataExtractor, StreamFactory previewImageFactory) {
        this.setTargetFileName(targetFileName);
        this.setDataExtractor(dataExtractor);
        this.setPreviewImageFactory(previewImageFactory);
    }

    public String getTargetFileName() {
        return this.targetFileName;
    }
    public void setTargetFileName(String targetFileName) {
        this.targetFileName = Objects.requireNonNull(targetFileName, "Parameter 'targetFileName' must not be null");
    }

    public DownloadDataExtractor getDataExtractor() {
        return this.dataExtractor;
    }
    public void setDataExtractor(DownloadDataExtractor dataExtractor) {
        this.dataExtractor = Objects.requireNonNull(dataExtractor, "Parameter 'dataExtractor' must not be null");
    }

    public StreamFactory getPreviewImageFactory() {
        return this.previewImageFactory;
    }
    public void setPreviewImageFactory(StreamFactory previewImageFactory) {
        this.previewImageFactory = previewImageFactory;
    }

}
