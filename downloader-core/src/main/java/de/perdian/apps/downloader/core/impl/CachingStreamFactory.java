/*
 * Copyright 2013 Christian Robert
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
package de.perdian.apps.downloader.core.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import de.perdian.apps.downloader.core.DownloadStreamFactory;

/**
 * {@ink DownloaderStreamFactory} implementation that caches any output it
 * receives from another stream factory and returns the cached ouput for
 * subsequent requests.
 *
 * @author Christian Robert
 */

public class CachingStreamFactory implements DownloadStreamFactory {

    static final long serialVersionUID = 1L;

    private DownloadStreamFactory delegee = null;
    private transient byte[] cachedBytes = null;
    private transient Long cachedSize = null;

    public CachingStreamFactory(DownloadStreamFactory delegee) {
        this.setDelegee(Objects.requireNonNull(delegee, "Parameter 'delegee' must not be null!"));
    }

    @Override
    public InputStream openStream() throws IOException {
        byte[] cachedBytes = this.getCachedBytes();
        if (cachedBytes != null) {
            return new ByteArrayInputStream(cachedBytes);
        } else {
            final InputStream realStream = this.getDelegee().openStream();
            final ByteArrayOutputStream cacheStream = new ByteArrayOutputStream();
            return new FilterInputStream(realStream) {

                private IOException myReadException = null;

                @Override
                public int read() throws IOException {
                    try {
                        int data = super.read();
                        if (data > -1) {
                            cacheStream.write(data);
                        }
                        return data;
                    } catch (IOException e) {
                        this.setReadException(e);
                        throw e;
                    }
                }

                @Override
                public int read(byte[] b) throws IOException {
                    try {
                        int bytesRead = super.read(b);
                        if (bytesRead > 0) {
                            cacheStream.write(b, 0, bytesRead);
                        }
                        return bytesRead;
                    } catch (IOException e) {
                        this.setReadException(e);
                        throw e;
                    }
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    try {
                        int bytesRead = super.read(b, off, len);
                        if (bytesRead > 0) {
                            cacheStream.write(b, off, bytesRead);
                        }
                        return bytesRead;
                    } catch (IOException e) {
                        this.setReadException(e);
                        throw e;
                    }
                }

                @Override
                public void close() throws IOException {
                    super.close();
                    if (this.getReadException() == null) {
                        cacheStream.flush();
                        CachingStreamFactory.this.setCachedBytes(cacheStream.toByteArray());
                    }
                }

                // -------------------------------------------------------------
                // --- Property access methods ---------------------------------
                // -------------------------------------------------------------

                private IOException getReadException() {
                    return this.myReadException;
                }
                private void setReadException(IOException readException) {
                    this.myReadException = readException;
                }

            };
        }
    }

    @Override
    public long size() throws IOException {
        Long cachedSize = this.getCachedSize();
        if (cachedSize != null && cachedSize.longValue() > -1) {
            return cachedSize.longValue();
        } else {
            long realSize = this.getDelegee().size();
            if (realSize > -1) {
                this.setCachedSize(Long.valueOf(realSize));
            }
            return realSize;
        }
    }

    // ---------------------------------------------------------------------------
    // --- Property access methods
    // -----------------------------------------------
    // ---------------------------------------------------------------------------

    DownloadStreamFactory getDelegee() {
        return this.delegee;
    }
    void setDelegee(DownloadStreamFactory delegee) {
        this.delegee = delegee;
    }

    Long getCachedSize() {
        return this.cachedSize;
    }
    void setCachedSize(Long cachedSize) {
        this.cachedSize = cachedSize;
    }

    byte[] getCachedBytes() {
        return this.cachedBytes;
    }
    void setCachedBytes(byte[] cachedBytes) {
        this.cachedBytes = cachedBytes;
    }

}