package de.perdian.apps.downloader.core.engine;

import de.perdian.apps.downloader.core.support.ProgressListener;

import java.io.IOException;
import java.nio.file.Path;

public interface DownloadPostProcessor {

    void afterDownloadCompleted(Path downloadedFile, ProgressListener progressListener) throws IOException;

}
