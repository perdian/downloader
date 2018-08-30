package de.perdian.apps.downloader.core.support.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.perdian.apps.downloader.core.support.StreamFactory;

public class ByteArrayStreamFactory implements StreamFactory {

    private byte[] bytes = null;

    public ByteArrayStreamFactory(byte[] bytes) {
        this.setBytes(bytes);
    }

    @Override
    public InputStream openStream() throws IOException {
        return new ByteArrayInputStream(this.getBytes());
    }

    @Override
    public long size() throws IOException {
        return this.getBytes().length;
    }

    private byte[] getBytes() {
        return this.bytes;
    }
    private void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

}
