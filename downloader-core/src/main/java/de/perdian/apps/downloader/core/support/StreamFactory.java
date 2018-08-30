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
