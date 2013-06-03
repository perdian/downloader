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
package de.perdian.apps.downloader.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * The source data from which a download should constructed is expected to be
 * an {@code InputStream} providing the actual data. The decouple the stream
 * from any lookup or further configuration efforts, this
 * {@code DownloadStreamFactory} is used. The {@code openStream} method is
 * called not until the actual download is supposed to start, so any
 * implementation might safely assume that the stream will be immediately read
 * from after it has been created.
 *
 * @author Christian Robert
 */

public interface DownloadStreamFactory extends Serializable {

  /**
   * Open the stream from which to read the information that should be
   * downloaded to the local file system
   *
   * @return
   *   the stream
   * @throws IOException
   *   thrown if the remote ressource cannot be accessed
   */
  public InputStream openStream() throws IOException;

  /**
   * Returns the size of the remote resource in bytes.
   *
   * @return
   *   the size of the remote resource. If the remote resource doesn't provide
   *   a way of determining it's size in advance, this method should return the
   *   special value of <code>-1</code>.
   * @throws IOException
   *   thrown if the remote ressource cannot be accessed
   */
  public long size() throws IOException;

}