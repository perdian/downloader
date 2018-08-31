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
package de.perdian.apps.downloader.core.support.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import de.perdian.apps.downloader.core.support.StreamFactory;

public class URLStreamFactory implements StreamFactory {

    private URL url = null;

    public URLStreamFactory(URL url) {
        this.setUrl(url);
    }

    @Override
    public String toString() {
        ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        toStringBuilder.append("url", this.getUrl());
        return toStringBuilder.toString();
    }

    @Override
    public InputStream openStream() throws IOException {
        return this.getUrl().openStream();
    }

    @Override
    public long size() throws IOException {
        return this.getUrl().openConnection().getContentLengthLong();
    }

    public URL getUrl() {
        return this.url;
    }
    private void setUrl(URL url) {
        this.url = url;
    }

}
