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

import java.io.Serializable;

/**
 * Transporter object to request the {@link Downloader} to schedule a
 * download operation for a remote resources that is identified by the
 * information encapsulated in this request object.
 *
 * This class is not intended to be subclasses by client code, since detailed
 * configuration of how the remote resource can be accessed should be performed
 * using a client implementation of the {@link DownloadStreamFactory} that can
 * be set as property into a request object.
 *
 * @author Christian Robert
 */

public class DownloadRequest implements Serializable {

  static final long serialVersionUID = 1L;

  private DownloadStreamFactory myStreamFactory = null;
  private String myTitle = null;
  private String myTargetFileName = null;

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(this.getClass().getSimpleName());
    result.append("[title=").append(this.getTitle());
    result.append(",targetFileName=").append(this.getTargetFileName());
    result.append(",streamFactory=").append(this.getStreamFactory());
    return result.append("]").toString();
  }

  // ---------------------------------------------------------------------------
  // --- Property access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  public DownloadStreamFactory getStreamFactory() {
    return this.myStreamFactory;
  }
  public void setStreamFactory(DownloadStreamFactory streamFactory) {
    this.myStreamFactory = streamFactory;
  }

  public String getTitle() {
    return this.myTitle;
  }
  public void setTitle(String title) {
    this.myTitle = title;
  }

  public String getTargetFileName() {
    return this.myTargetFileName;
  }
  public void setTargetFileName(String targetFileName) {
    this.myTargetFileName = targetFileName;
  }

}