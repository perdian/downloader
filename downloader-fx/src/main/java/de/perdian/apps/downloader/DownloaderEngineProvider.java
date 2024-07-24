package de.perdian.apps.downloader;

import de.perdian.apps.downloader.core.engine.DownloadEngine;

@FunctionalInterface
public interface DownloaderEngineProvider {

    DownloadEngine createEngine();

}
