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
package de.perdian.apps.downloader.core.engine.impl.taskfactories;

import java.io.IOException;
import java.net.URL;

import de.perdian.apps.downloader.core.engine.DownloadTask;
import de.perdian.apps.downloader.core.engine.DownloadTaskFactory;
import de.perdian.apps.downloader.core.support.ProgressListener;
import de.perdian.apps.downloader.core.support.impl.URLStreamFactory;

/**
 * Implementation of the {@link DownloadTaskFactory} interface that downloads the content directly from a given URL
 *
 * @author Christian Robert
 */

public class SimpleTaskFactory implements DownloadTaskFactory {

    private URL url = null;

    public SimpleTaskFactory(URL url) {
        this.setUrl(url);
    }

    @Override
    public DownloadTask createTask(ProgressListener progressListener) throws IOException {

        String urlPath = this.getUrl().getPath();
        int lastIndexOfSlash = urlPath.lastIndexOf("/");
        String fileName = lastIndexOfSlash < 0 || lastIndexOfSlash + 1 == urlPath.length() ? urlPath : urlPath.substring(lastIndexOfSlash + 1);

        DownloadTask downloadTask = new DownloadTask();
        downloadTask.setContentFactory(new URLStreamFactory(this.getUrl()));
        downloadTask.setTargetFileName(fileName);
        return downloadTask;

    }

    public URL getUrl() {
        return this.url;
    }
    public void setUrl(URL url) {
        this.url = url;
    }

}

