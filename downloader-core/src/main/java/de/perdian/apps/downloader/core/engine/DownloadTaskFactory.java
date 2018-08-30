package de.perdian.apps.downloader.core.engine;

import java.io.IOException;

import de.perdian.apps.downloader.core.support.ProgressListener;

@FunctionalInterface
public interface DownloadTaskFactory {

    DownloadTask createTask(ProgressListener progressListener) throws IOException;

}
