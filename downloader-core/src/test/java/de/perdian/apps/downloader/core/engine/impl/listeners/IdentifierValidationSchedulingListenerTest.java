package de.perdian.apps.downloader.core.engine.impl.listeners;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;

import de.perdian.apps.downloader.core.engine.DownloadOperation;
import de.perdian.apps.downloader.core.engine.DownloadRejectedException;
import de.perdian.apps.downloader.core.engine.DownloadRequest;
import de.perdian.apps.downloader.core.engine.DownloadRequestWrapper;

public class IdentifierValidationSchedulingListenerTest {

    private FileSystem fileSystem = null;

    @BeforeEach
    public void prepareFileSystem() throws IOException {
        this.setFileSystem(MemoryFileSystemBuilder.newEmpty().build(UUID.randomUUID().toString()));
    }

    @AfterEach
    public void cleanupFileSystem() throws IOException {
        this.getFileSystem().close();
    }

    @Test
    public void onRequestSubmittedIdentifierNotExistingYet() throws Exception {

        DownloadRequest request = new DownloadRequest();
        request.setId("42");

        Path markerDirectory = this.getFileSystem().getPath("markerDirectory");
        IdentifierValidationSchedulingListener listener = new IdentifierValidationSchedulingListener(markerDirectory);
        listener.onRequestSubmitted(request);

    }

    @Test
    public void onRequestSubmittedIdentifierExisting() throws Exception {
        Assertions.assertThrows(DownloadRejectedException.class, () -> {

            DownloadRequest request = new DownloadRequest();
            request.setId("42");

            Path markerDirectory = this.getFileSystem().getPath("markerDirectory");
            Files.createDirectories(markerDirectory);
            Path markerFile = markerDirectory.resolve("42.marker");
            Files.createFile(markerFile);

            IdentifierValidationSchedulingListener listener = new IdentifierValidationSchedulingListener(markerDirectory);
            listener.onRequestSubmitted(request);

        });
    }

    @Test
    public void onOperationCompleted() throws Exception {

        DownloadRequest request = new DownloadRequest();
        request.setId("42");
        DownloadRequestWrapper requestWrapper = Mockito.mock(DownloadRequestWrapper.class);
        Mockito.when(requestWrapper.getRequest()).thenReturn(request);
        DownloadOperation operation = Mockito.mock(DownloadOperation.class);
        Mockito.when(operation.getRequestWrapper()).thenReturn(requestWrapper);
        Mockito.when(operation.getStartTime()).thenReturn(Instant.ofEpochSecond(100));
        Mockito.when(operation.getEndTime()).thenReturn(Instant.ofEpochSecond(200));

        Path markerDirectory = this.getFileSystem().getPath("markerDirectory");
        Path markerFile = markerDirectory.resolve("42.marker");
        Assertions.assertFalse(Files.exists(markerFile));

        IdentifierValidationSchedulingListener listener = new IdentifierValidationSchedulingListener(markerDirectory);
        listener.onOperationCompleted(operation);
        Assertions.assertTrue(Files.exists(markerFile));

    }

    @Test
    public void onOperationCompletedWithIdentifierNull() throws Exception {

        DownloadRequest request = new DownloadRequest();
        request.setId(null);
        DownloadRequestWrapper requestWrapper = Mockito.mock(DownloadRequestWrapper.class);
        Mockito.when(requestWrapper.getRequest()).thenReturn(request);
        DownloadOperation operation = Mockito.mock(DownloadOperation.class);
        Mockito.when(operation.getRequestWrapper()).thenReturn(requestWrapper);
        Mockito.when(operation.getStartTime()).thenReturn(Instant.ofEpochSecond(100));
        Mockito.when(operation.getEndTime()).thenReturn(Instant.ofEpochSecond(200));

        Path markerDirectory = Mockito.mock(Path.class);

        IdentifierValidationSchedulingListener listener = new IdentifierValidationSchedulingListener(markerDirectory);
        listener.onOperationCompleted(operation);
        Mockito.verifyNoMoreInteractions(markerDirectory);

    }

    @Test
    public void onOperationCancelled() throws Exception {

        DownloadRequest request = new DownloadRequest();
        request.setId("42");
        DownloadRequestWrapper requestWrapper = Mockito.mock(DownloadRequestWrapper.class);
        Mockito.when(requestWrapper.getRequest()).thenReturn(request);
        DownloadOperation operation = Mockito.mock(DownloadOperation.class);
        Mockito.when(operation.getRequestWrapper()).thenReturn(requestWrapper);
        Mockito.when(operation.getStartTime()).thenReturn(Instant.ofEpochSecond(100));
        Mockito.when(operation.getEndTime()).thenReturn(Instant.ofEpochSecond(200));

        Path markerDirectory = this.getFileSystem().getPath("markerDirectory");
        Path markerFile = markerDirectory.resolve("42.marker");
        Assertions.assertFalse(Files.exists(markerFile));

        IdentifierValidationSchedulingListener listener = new IdentifierValidationSchedulingListener(markerDirectory);
        listener.onOperationCancelled(operation);
        Assertions.assertTrue(Files.exists(markerFile));

    }

    @Test
    public void onOperationCancelledWithIdentifierNull() throws Exception {

        DownloadRequest request = new DownloadRequest();
        request.setId(null);
        DownloadRequestWrapper requestWrapper = Mockito.mock(DownloadRequestWrapper.class);
        Mockito.when(requestWrapper.getRequest()).thenReturn(request);
        DownloadOperation operation = Mockito.mock(DownloadOperation.class);
        Mockito.when(operation.getRequestWrapper()).thenReturn(requestWrapper);
        Mockito.when(operation.getStartTime()).thenReturn(Instant.ofEpochSecond(100));
        Mockito.when(operation.getEndTime()).thenReturn(Instant.ofEpochSecond(200));

        Path markerDirectory = Mockito.mock(Path.class);

        IdentifierValidationSchedulingListener listener = new IdentifierValidationSchedulingListener(markerDirectory);
        listener.onOperationCancelled(operation);
        Mockito.verifyNoMoreInteractions(markerDirectory);

    }

    private FileSystem getFileSystem() {
        return this.fileSystem;
    }
    private void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

}
