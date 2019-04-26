package de.perdian.apps.downloader.core.engine;

import java.io.OutputStream;
import java.util.function.Supplier;

import de.perdian.apps.downloader.core.support.ProgressListener;

public interface DownloadDataExtractor {

    void extractData(OutputStream targetStream, ProgressListener progressListener, Supplier<DownloadOperationStatus> statusSupplier) throws Exception;

}
