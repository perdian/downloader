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
package de.perdian.apps.downloader;

import java.util.List;

import de.perdian.apps.downloader.core.engine.DownloadRequestFactory;

public class DownloaderConfiguration {

    private List<DownloadRequestFactory> requestFactories = null;

    public List<DownloadRequestFactory> getRequestFactories() {
        return this.requestFactories;
    }
    public void setRequestFactories(List<DownloadRequestFactory> requestFactories) {
        this.requestFactories = requestFactories;
    }

}
