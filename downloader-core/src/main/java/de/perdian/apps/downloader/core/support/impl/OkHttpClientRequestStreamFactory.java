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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import de.perdian.apps.downloader.core.support.StreamFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpClientRequestStreamFactory implements StreamFactory {

    private OkHttpClient httpClient = null;
    private String url = null;
    private Response response = null;

    public OkHttpClientRequestStreamFactory(String url) {
        this(url, new OkHttpClient.Builder().build());
    }

    public OkHttpClientRequestStreamFactory(String url, OkHttpClient httpClient) {
        this.setUrl(url);
        this.setHttpClient(httpClient);
    }

    @Override
    public String toString() {
        ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE);
        toStringBuilder.append("url", this.getUrl());
        return toStringBuilder.toString();
    }

    private Response ensureResponse() throws IOException {
        if (this.response == null) {
            Request request = new Request.Builder().url(this.getUrl()).build();
            this.response = this.getHttpClient().newCall(request).execute();
        }
        return this.response;
    }

    @Override
    public InputStream openStream() throws IOException {
        return this.ensureResponse().body().byteStream();
    }

    @Override
    public long size() throws IOException {
        return this.ensureResponse().body().contentLength();
    }

    public OkHttpClient getHttpClient() {
        return this.httpClient;
    }
    public void setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public Response getResponse() {
        return this.response;
    }
    public void setResponse(Response response) {
        this.response = response;
    }

}
