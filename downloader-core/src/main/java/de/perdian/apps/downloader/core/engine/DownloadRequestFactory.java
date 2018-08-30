package de.perdian.apps.downloader.core.engine;

import java.net.URL;
import java.util.List;

/**
 * Factory to create one or multiple {@link DownloadRequest} instance(s) from a specific URL
 *
 * @author Christian Robert
 */

@FunctionalInterface
public interface DownloadRequestFactory {

    /**
     * Analyze the URL and create the request(s)
     *
     * @return
     *      a list of requests or {@code null} to signalize that the current factory is not able
     *      to analyze the URL
     */
    List<DownloadRequest> createRequests(URL url);

    /**
     * Gets the priority of this factory
     */
    default int getPriority() {
        return 0;
    }

}
