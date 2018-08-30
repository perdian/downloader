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

import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.downloader.core.engine.DownloadEngine;
import de.perdian.apps.downloader.core.engine.DownloadRequest;
import de.perdian.apps.downloader.core.engine.DownloadRequestFactory;

class DownloaderUrlConsumer implements Consumer<URL> {

    private static final Logger log = LoggerFactory.getLogger(DownloaderUrlConsumer.class);

    private DownloadEngine engine = null;
    private List<DownloadRequestFactory> requestFactories = null;
    private Consumer<Exception> exceptionConsumer = e -> {};

    @Override
    public void accept(URL url) {
        try {
            for (DownloadRequestFactory requestFactory : this.getRequestFactories()) {
                List<DownloadRequest> requests = requestFactory.createRequests(url);
                if (requests != null) {
                    this.getEngine().submitAll(requests);
                    return;
                }
            }
            throw new UnsupportedOperationException("Cannot find DownloadRequestFactory for URL: " + url);
        } catch (Exception e) {
            log.error("Cannot process URL: " + url, e);
            this.getExceptionConsumer().accept(e);
        }
    }

    DownloadEngine getEngine() {
        return this.engine;
    }
    void setEngine(DownloadEngine engine) {
        this.engine = engine;
    }

    List<DownloadRequestFactory> getRequestFactories() {
        return this.requestFactories;
    }
    void setRequestFactories(List<DownloadRequestFactory> requestFactories) {
        this.requestFactories = requestFactories;
    }

    Consumer<Exception> getExceptionConsumer() {
        return this.exceptionConsumer;
    }
    void setExceptionConsumer(Consumer<Exception> exceptionConsumer) {
        this.exceptionConsumer = exceptionConsumer;
    }

}
