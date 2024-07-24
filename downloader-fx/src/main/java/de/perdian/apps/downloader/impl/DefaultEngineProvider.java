package de.perdian.apps.downloader.impl;

import de.perdian.apps.downloader.DownloaderEngineProvider;
import de.perdian.apps.downloader.core.engine.DownloadEngine;

import java.io.File;

public class DefaultEngineProvider implements DownloaderEngineProvider {

    @Override
    public DownloadEngine createEngine() {
        return new DownloadEngine(new File(System.getProperty("user.home"), "Downloads").toPath());
    }

}
