/*
 * Copyright 2013 Christian Robert
 *
 * Licimport java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import de.perdian.apps.downloader.core.DownloadStreamFactory;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import de.perdian.apps.downloader.core.DownloadStreamFactory;

public abstract class AbstractUrlStreamFactory implements DownloadStreamFactory {

  static final long serialVersionUID = 1L;

  private URL myCachedUrl = null;

  @Override
  public InputStream openStream() throws IOException {
    return this.ensureCachedUrl().openStream();
  }

  @Override
  public long size() throws IOException {
    URLConnection urlConnection = this.ensureCachedUrl().openConnection();
    return urlConnection.getContentLengthLong();
  }

  protected abstract URL createUrl() throws IOException;

  // ---------------------------------------------------------------------------
  // ---  Property access methods  ---------------------------------------------
  // ---------------------------------------------------------------------------

  private URL ensureCachedUrl() throws IOException {
    if(this.myCachedUrl == null) {
      this.myCachedUrl = this.createUrl();
    }
    return this.myCachedUrl;
  }

}