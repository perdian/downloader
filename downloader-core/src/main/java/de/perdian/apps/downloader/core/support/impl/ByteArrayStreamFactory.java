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
