/*
 * Copyright 2013 Christian Robert
 *
 * Licimport java.io.IOException;
 * import java.io.InputStream;
 * import java.net.URL;
 * import java.net.URLConnection;
 *
 * import de.perdian.apps.downloader.core.DownloadStreamFactory;
 *
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
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import de.perdian.apps.downloader.core.DownloadStreamFactory;

public class UrlStreamFactory implements DownloadStreamFactory {

    static final long serialVersionUID = 1L;

    private Supplier<URL> urlSupplier = null;
    private HttpEntity cachedEntity = null;

    public UrlStreamFactory(URL url) {
        this(() -> url);
    }

    public UrlStreamFactory(Supplier<URL> supplier) {
        this.setUrlSupplier(supplier);
    }

    @Override
    public InputStream openStream() throws IOException {
        return this.ensureHttpEntity().getContent();
    }

    @Override
    public long size() throws IOException {
        return this.ensureHttpEntity().getContentLength();
    }

    private synchronized HttpEntity ensureHttpEntity() throws IOException {
        if (this.cachedEntity == null) {

            HttpGet httpGet = new HttpGet(this.getUrlSupplier().get().toString());
            httpGet.setHeader("User-Agent", UUID.randomUUID().toString());

            HttpClient httpClient = HttpClients.custom().build();

            HttpResponse httpResponse = httpClient.execute(httpGet);
            this.cachedEntity = httpResponse.getEntity();

        }
        return this.cachedEntity;
    }

    // ---------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // ---------------------------------------------------------------------------

    private Supplier<URL> getUrlSupplier() {
        return this.urlSupplier;
    }
    private void setUrlSupplier(Supplier<URL> urlSupplier) {
        this.urlSupplier = urlSupplier;
    }

}