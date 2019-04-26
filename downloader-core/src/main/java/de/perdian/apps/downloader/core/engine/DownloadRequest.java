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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import de.perdian.apps.downloader.core.support.StreamFactory;

/**
 * Transporter object to request the {@link DownloadEngine} to schedule a download operation for a
 * remote resources that is identified by the information encapsulated in this request object.
 *
 * This class is not intended to be subclassed by client code, since detailed configuration of how
 * the remote resource can be accessed should be performed using the client implementation of the
 * {@code DownloadTask} interface that is stored in the property {@code taskFactory}.
 *
 * @author Christian Robert
 */

public class DownloadRequest {

    private String id = null;
    private String title = null;
    private DownloadTaskFactory taskFactory = null;
    private StreamFactory previewImageFactory = null;
    private int priority = 0;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }

    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public DownloadTaskFactory getTaskFactory() {
        return this.taskFactory;
    }
    public void setTaskFactory(DownloadTaskFactory taskFactory) {
        this.taskFactory = taskFactory;
    }

    public StreamFactory getPreviewImageFactory() {
        return this.previewImageFactory;
    }
    public void setPreviewImageFactory(StreamFactory previewImageFactory) {
        this.previewImageFactory = previewImageFactory;
    }

    public int getPriority() {
        return this.priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }

}
