package de.perdian.apps.downloader.core.engine;

import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of all known {@link DownloadRequestFactory} instances
 *
 * @author Christian Robert
 */

public class DownloadRequestFactoryLookup {

    private static final Logger log = LoggerFactory.getLogger(DownloadRequestFactoryLookup.class);

    private List<DownloadRequestFactory> requestFactories = null;

    private DownloadRequestFactoryLookup() {
    }

    /**
     * Create a new lookup instance auto discovering all available factories
     */
    public static DownloadRequestFactoryLookup createLookup() {

        ServiceLoader<DownloadRequestFactory> requestFactoryServiceLoader = ServiceLoader.load(DownloadRequestFactory.class);
        List<DownloadRequestFactory> requestFactories = requestFactoryServiceLoader.stream()
            .map(provider -> provider.get())
            .sorted(Comparator.comparing(DownloadRequestFactory::getPriority))
            .collect(Collectors.toList());

        log.info("Discovered {} implementation(s) of {}", requestFactories.size(), DownloadRequestFactory.class.getSimpleName());
        DownloadRequestFactoryLookup lookup = new DownloadRequestFactoryLookup();
        lookup.setRequestFactories(requestFactories);
        return lookup;

    }

    public List<DownloadRequestFactory> getRequestFactories() {
        return this.requestFactories;
    }
    private void setRequestFactories(List<DownloadRequestFactory> requestFactories) {
        this.requestFactories = requestFactories;
    }

}
