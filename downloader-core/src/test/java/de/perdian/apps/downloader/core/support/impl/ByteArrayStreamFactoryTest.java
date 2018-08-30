package de.perdian.apps.downloader.core.support.impl;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ByteArrayStreamFactoryTest {

    @Test
    public void openStream() throws IOException {
        ByteArrayStreamFactory factory = new ByteArrayStreamFactory("TEST".getBytes());
        Assertions.assertArrayEquals("TEST".getBytes(), IOUtils.toByteArray(factory.openStream()));
    }

    @Test
    public void size() throws IOException {
        ByteArrayStreamFactory factory = new ByteArrayStreamFactory("TEST".getBytes());
        Assertions.assertEquals("TEST".getBytes().length, factory.size());
    }

}
