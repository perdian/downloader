package de.perdian.apps.downloader.impl.support;

import de.perdian.apps.downloader.core.engine.DownloadPostProcessor;
import de.perdian.apps.downloader.core.support.ProgressListener;

import java.io.IOException;
import java.nio.file.Path;

public class MP3DownloadPostProcessor implements DownloadPostProcessor {

    @Override
    public void afterDownloadCompleted(Path downloadedFile, ProgressListener progressListener) throws IOException {
    }

}
