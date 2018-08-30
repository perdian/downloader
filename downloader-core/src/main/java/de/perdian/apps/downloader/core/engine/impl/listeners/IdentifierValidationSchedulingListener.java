package de.perdian.apps.downloader.core.engine.impl.listeners;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;

import de.perdian.apps.downloader.core.engine.DownloadEngine;
import de.perdian.apps.downloader.core.engine.DownloadOperation;
import de.perdian.apps.downloader.core.engine.DownloadRejectedException;
import de.perdian.apps.downloader.core.engine.DownloadRequest;
import de.perdian.apps.downloader.core.engine.DownloadSchedulingListener;

/**
 * Makes sure that a {@link DownloadRequest} will only be accepted by a
 * {@link DownloadEngine} if it's identifier has not already been downloaded
 * at a previous time. The listener itself keeps track of downloads already
 * finished by storing marker files inside a marker directory.
 *
 * @author Christian Robert
 */

public class IdentifierValidationSchedulingListener implements DownloadSchedulingListener {

    private Path markerDirectory = null;

    public IdentifierValidationSchedulingListener(Path markerDirectory) {
        this.setMarkerDirectory(Objects.requireNonNull(markerDirectory, "Parameter 'markerDirectory' must not be null"));
    }

    @Override
    public void onRequestSubmit(DownloadRequest request) throws DownloadRejectedException {
        if (request.getId() != null && this.hasIdentifier(request.getId())) {
            throw new DownloadRejectedException("Marker file existing for id: " + request.getId());
        }
    }

    @Override
    public void onOperationCompleted(DownloadOperation operation) {
        if (operation.getRequestWrapper().getRequest().getId() != null) {
            if (operation.getError() != null) {
                this.pushIdentifier(operation.getRequestWrapper().getRequest().getId(), "ERROR [" + operation.getError() + "]");
            } else if (operation.getCancelTime() != null) {
                this.pushIdentifier(operation.getRequestWrapper().getRequest().getId(), "CANCELLED[" + (operation.getCancelReason() == null ? "<No reason>" : operation.getCancelReason()) + "]");
            } else {
                this.pushIdentifier(operation.getRequestWrapper().getRequest().getId(), "COMPLETED [" + Duration.between(operation.getStartTime(), operation.getEndTime()) + "]");
            }
        }
    }

    @Override
    public void onOperationCancelled(DownloadOperation operation) {
        if (operation.getRequestWrapper().getRequest().getId() != null) {
            this.pushIdentifier(operation.getRequestWrapper().getRequest().getId(), "CANCELLED[" + (operation.getCancelReason() == null ? "<No reason>" : operation.getCancelReason()) + "]");
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.getClass().getSimpleName());
        result.append("[markerDirectory=").append(this.getMarkerDirectory());
        return result.append("]").toString();
    }

    // -------------------------------------------------------------------------
    // --- Identifier handling -------------------------------------------------
    // -------------------------------------------------------------------------

    private void pushIdentifier(String identifier, String comment) {
        try {

            if (!Files.exists(this.getMarkerDirectory())) {
                Files.createDirectory(this.getMarkerDirectory());
            }
            Path markerFile = this.getMarkerDirectory().resolve(identifier + ".marker");
            if (!Files.exists(markerFile)) {
                Files.createFile(markerFile);
            }

            StringBuilder fileContent = new StringBuilder();
            fileContent.append(new Date()).append("\n");
            fileContent.append(comment);
            Files.write(markerFile, fileContent.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);

        } catch (IOException e) {
            throw new RuntimeException("Cannot access marker directory at: " + this.getMarkerDirectory(), e);
        }
    }

    private boolean hasIdentifier(String identifier) {
        return Files.exists(this.getMarkerDirectory().resolve(identifier + ".marker"));
    }

    // ---------------------------------------------------------------------------
    // --- Property access methods
    // -----------------------------------------------
    // ---------------------------------------------------------------------------

    public Path getMarkerDirectory() {
        return this.markerDirectory;
    }
    public void setMarkerDirectory(Path markerDirectory) {
        this.markerDirectory = markerDirectory;
    }

}
