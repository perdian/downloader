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
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import de.perdian.apps.downloader.core.DownloadStreamFactory;

public class TestCachingStreamFactory {

    @Test
    public void testSize() throws IOException {

        DownloadStreamFactory realStreamFactory = Mockito.mock(DownloadStreamFactory.class);
        Mockito.when(realStreamFactory.size()).thenReturn(Long.valueOf(42));

        CachingStreamFactory cachingStreamFactory = new CachingStreamFactory(realStreamFactory);
        Assert.assertEquals(42, cachingStreamFactory.size());
        Assert.assertEquals(Long.valueOf(42), cachingStreamFactory.getCachedSize());
        Mockito.verify(realStreamFactory).size();

        // When calling the size method of the CachingStreamFactory again, the
        // cached value should be used and therefore the original factory must
        // not be called again
        Assert.assertEquals(42, cachingStreamFactory.size());
        Mockito.verifyNoMoreInteractions(realStreamFactory);

    }

    @Test
    public void testSizeNoValueFromRealFactory() throws IOException {

        DownloadStreamFactory realStreamFactory = Mockito.mock(DownloadStreamFactory.class);
        Mockito.when(realStreamFactory.size()).thenReturn(Long.valueOf(-1));

        CachingStreamFactory cachingStreamFactory = new CachingStreamFactory(realStreamFactory);
        Assert.assertEquals(-1, cachingStreamFactory.size());
        Assert.assertNull(cachingStreamFactory.getCachedSize());
        Mockito.verify(realStreamFactory, Mockito.only()).size();

        // When calling the size method of the CachingStreamFactory again, a new
        // request must be made to the underlying factory, since the value of -1
        // must not be cached
        Assert.assertEquals(-1, cachingStreamFactory.size());
        Mockito.verify(realStreamFactory, Mockito.times(2)).size();

    }

    @Test
    public void testOpenStream() throws IOException {

        byte[] testBytes = "test".getBytes();
        DownloadStreamFactory realStreamFactory = Mockito.mock(DownloadStreamFactory.class);
        Mockito.when(realStreamFactory.openStream()).thenReturn(new ByteArrayInputStream(testBytes));

        CachingStreamFactory cachingStreamFactory = new CachingStreamFactory(realStreamFactory);
        ByteArrayOutputStream targetStream = new ByteArrayOutputStream();
        try (InputStream cachingStream = cachingStreamFactory.openStream()) {
            for (int data = cachingStream.read(); data > -1; data = cachingStream.read()) {
                targetStream.write(data);
            }
        }

        Assert.assertArrayEquals(testBytes, targetStream.toByteArray());
        Assert.assertArrayEquals(testBytes, cachingStreamFactory.getCachedBytes());
        Mockito.verify(realStreamFactory, Mockito.times(1)).openStream();

        // When calling the openStream method again, we can directly deliver the
        // data from the cache and must not open a new stream from the
        // underlying factory again
        ByteArrayOutputStream newTargetStream = new ByteArrayOutputStream();
        try (InputStream newInputStream = cachingStreamFactory.openStream()) {
            for (int data = newInputStream.read(); data > -1; data = newInputStream.read()) {
                newTargetStream.write(data);
            }
        }
        Assert.assertArrayEquals(testBytes, newTargetStream.toByteArray());
        Mockito.verify(realStreamFactory, Mockito.times(1)).openStream();

    }

    @Test
    public void testOpenStreamWithErrorInStreamRead() throws IOException {

        DownloadStreamFactory realStreamFactory = Mockito.mock(DownloadStreamFactory.class);
        IOException realException = new IOException("Invalid");
        InputStream realStream = Mockito.mock(InputStream.class);
        Mockito.when(realStream.read()).thenThrow(realException);
        Mockito.when(realStreamFactory.openStream()).thenReturn(realStream);

        CachingStreamFactory cachingStreamFactory = new CachingStreamFactory(realStreamFactory);
        try (InputStream cachingStream = cachingStreamFactory.openStream()) {
            try {
                cachingStream.read();
                Assert.fail("Exception expected");
            } catch (IOException e) {
                Assert.assertSame(realException, e);
            }
        }
        Assert.assertNull(cachingStreamFactory.getCachedBytes());
        Mockito.verify(realStreamFactory, Mockito.times(1)).openStream();

        // When calling the openStream method again, we want the underlying
        // stream to be opened again, since the first time an error occured and
        // therefore another stream has to be returned
        cachingStreamFactory.openStream();
        Mockito.verify(realStreamFactory, Mockito.times(2)).openStream();

    }

    @Test
    public void testSerializable() throws Exception {

        CachingStreamFactory originalStreamFactory = new CachingStreamFactory(() -> null);
        originalStreamFactory.setCachedBytes("test".getBytes());
        originalStreamFactory.setCachedSize(Long.valueOf(42));

        // After serialization and deserialization, the cached properties must
        // not be set any more
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutStream = new ObjectOutputStream(byteOutStream)) {
            objectOutStream.writeObject(originalStreamFactory);
        }
        ObjectInputStream objectInStream = new ObjectInputStream(new ByteArrayInputStream(byteOutStream.toByteArray()));
        CachingStreamFactory reloadedStreamFactory = (CachingStreamFactory)objectInStream.readObject();
        Assert.assertNull(reloadedStreamFactory.getCachedBytes());
        Assert.assertNull(reloadedStreamFactory.getCachedSize());

    }

}