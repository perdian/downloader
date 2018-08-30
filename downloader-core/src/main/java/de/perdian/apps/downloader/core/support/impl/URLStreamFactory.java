package de.perdian.apps.downloader.core.support.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import de.perdian.apps.downloader.core.support.StreamFactory;

public class URLStreamFactory implements StreamFactory {

    private URL url = null;
    private URLConnection urlConnection = null;

    public URLStreamFactory(URL url) {
        this.setUrl(url);
    }

    private synchronized URLConnection ensureUrlConnection() throws IOException {
        if (this.urlConnection == null) {
            this.urlConnection = this.getUrl().openConnection();
        }
        return this.urlConnection;
    }

    @Override
    public InputStream openStream() throws IOException {
        return this.ensureUrlConnection().getInputStream();
    }

    @Override
    public long size() throws IOException {
        return this.ensureUrlConnection().getContentLength();
    }

    public URL getUrl() {
        return this.url;
    }
    private void setUrl(URL url) {
        this.url = url;
    }

}
