package de.perdian.apps.downloader.core.support.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.perdian.apps.downloader.core.support.StreamFactory;

public class CachingStreamFactoryTest {

    @Test
    public void size() throws IOException {

        StreamFactory realStreamFactory = Mockito.mock(StreamFactory.class);
        Mockito.when(realStreamFactory.size()).thenReturn(Long.valueOf(42));

        CachingStreamFactory cachingStreamFactory = new CachingStreamFactory(realStreamFactory);
        Assertions.assertEquals(42, cachingStreamFactory.size());
        Assertions.assertEquals(Long.valueOf(42), cachingStreamFactory.getCachedSize());
        Mockito.verify(realStreamFactory).size();

        // When calling the size method of the CachingStreamFactory again, the
        // cached value should be used and therefore the original factory must
        // not be called again
        Assertions.assertEquals(42, cachingStreamFactory.size());
        Mockito.verifyNoMoreInteractions(realStreamFactory);

    }

    @Test
    public void sizeWithNoValueDelegee() throws IOException {

        StreamFactory realStreamFactory = Mockito.mock(StreamFactory.class);
        Mockito.when(realStreamFactory.size()).thenReturn(Long.valueOf(-1));

        CachingStreamFactory cachingStreamFactory = new CachingStreamFactory(realStreamFactory);
        Assertions.assertEquals(-1, cachingStreamFactory.size());
        Assertions.assertNull(cachingStreamFactory.getCachedSize());
        Mockito.verify(realStreamFactory, Mockito.only()).size();

        // When calling the size method of the CachingStreamFactory again, a new
        // request must be made to the underlying factory, since the value of -1
        // must not be cached
        Assertions.assertEquals(-1, cachingStreamFactory.size());
        Mockito.verify(realStreamFactory, Mockito.times(2)).size();

    }

    @Test
    public void openStream() throws IOException {

        byte[] testBytes = "test".getBytes();
        StreamFactory realStreamFactory = Mockito.mock(StreamFactory.class);
        Mockito.when(realStreamFactory.openStream()).thenReturn(new ByteArrayInputStream(testBytes));

        CachingStreamFactory cachingStreamFactory = new CachingStreamFactory(realStreamFactory);
        ByteArrayOutputStream targetStream = new ByteArrayOutputStream();
        try (InputStream cachingStream = cachingStreamFactory.openStream()) {
            for (int data = cachingStream.read(); data > -1; data = cachingStream.read()) {
                targetStream.write(data);
            }
        }

        Assertions.assertArrayEquals(testBytes, targetStream.toByteArray());
        Assertions.assertArrayEquals(testBytes, cachingStreamFactory.getCachedBytes());
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
        Assertions.assertArrayEquals(testBytes, newTargetStream.toByteArray());
        Mockito.verify(realStreamFactory, Mockito.times(1)).openStream();

    }

    @Test
    public void openStreamWithErrorInStreamRead() throws IOException {

        StreamFactory realStreamFactory = Mockito.mock(StreamFactory.class);
        IOException realException = new IOException("Invalid");
        InputStream realStream = Mockito.mock(InputStream.class);
        Mockito.when(realStream.read()).thenThrow(realException);
        Mockito.when(realStreamFactory.openStream()).thenReturn(realStream);

        CachingStreamFactory cachingStreamFactory = new CachingStreamFactory(realStreamFactory);
        try (InputStream cachingStream = cachingStreamFactory.openStream()) {
            try {
                cachingStream.read();
                Assertions.fail("Exception expected");
            } catch (IOException e) {
                Assertions.assertSame(realException, e);
            }
        }
        Assertions.assertNull(cachingStreamFactory.getCachedBytes());
        Mockito.verify(realStreamFactory, Mockito.times(1)).openStream();

        // When calling the openStream method again, we want the underlying
        // stream to be opened again, since the first time an error occured and
        // therefore another stream has to be returned
        cachingStreamFactory.openStream();
        Mockito.verify(realStreamFactory, Mockito.times(2)).openStream();

    }

}
