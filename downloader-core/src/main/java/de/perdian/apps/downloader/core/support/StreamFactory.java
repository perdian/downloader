package de.perdian.apps.downloader.core.support;

import java.io.IOException;
import java.io.InputStream;

/**
 * The source from which an {@code InputStream} can be constructed. The decouple the stream
 * from any lookup or further configuration efforts, this {@code StreamFactory} is used.
 *
 * @author Christian Robert
 */

@FunctionalInterface
public interface StreamFactory {

    /**
     * Open the stream from which to read the information
     *
     * @return
     *     the stream
     * @throws IOException
     *     thrown if the remote resource cannot be accessed
     */
    InputStream openStream() throws IOException;

    /**
     * Returns the size of the remote resource in bytes.
     *
     * @return
     *     the size of the remote resource. If the remote resource doesn't
     *     provide a way of determining it's size in advance, this method should
     *     return the special value of <code>-1</code>.
     * @throws IOException
     *     thrown if the remote resource cannot be accessed
     */
    default long size() throws IOException {
        return -1;
    }

}
