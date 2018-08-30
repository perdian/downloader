package de.perdian.apps.downloader.core.support.impl;

import java.io.IOException;
import java.io.InputStream;

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
