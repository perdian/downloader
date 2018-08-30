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
package de.perdian.apps.downloader.core.engine;

import de.perdian.apps.downloader.core.support.StreamFactory;

public class DownloadTask {

    private String title = null;
    private String targetFileName = null;
    private StreamFactory contentFactory = null;
    private StreamFactory previewImageFactory = null;

    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getTargetFileName() {
        return this.targetFileName;
    }
    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }

    public StreamFactory getContentFactory() {
        return this.contentFactory;
    }
    public void setContentFactory(StreamFactory contentFactory) {
        this.contentFactory = contentFactory;
    }

    public StreamFactory getPreviewImageFactory() {
        return this.previewImageFactory;
    }
    public void setPreviewImageFactory(StreamFactory previewImageFactory) {
        this.previewImageFactory = previewImageFactory;
    }

}
