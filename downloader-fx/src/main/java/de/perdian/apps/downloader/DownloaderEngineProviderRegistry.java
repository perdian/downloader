package de.perdian.apps.downloader;

import de.perdian.apps.downloader.impl.DefaultEngineProvider;

public class DownloaderEngineProviderRegistry {

    private static DownloaderEngineProvider provider = new DefaultEngineProvider();

    public static DownloaderEngineProvider getProvider() {
        return DownloaderEngineProviderRegistry.provider;
    }
    public static void setProvider(DownloaderEngineProvider provider) {
        DownloaderEngineProviderRegistry.provider = provider;
    }

}
