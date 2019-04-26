package de.perdian.apps.downloader.core.engine;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;

import de.perdian.apps.downloader.core.engine.impl.tasks.StreamFactoryTask;
import de.perdian.apps.downloader.core.support.StreamFactory;
import de.perdian.apps.downloader.core.support.impl.ByteArrayStreamFactory;

public class DownloadEngineTest {

    private FileSystem myFileSystem = null;
    private DownloadEngine myEngine = null;

    @BeforeEach
    public void prepareProperties() throws IOException {
        FileSystem fileSystem = MemoryFileSystemBuilder.newEmpty().build(UUID.randomUUID().toString());
        this.setFileSystem(fileSystem);
        this.setEngine(new DownloadEngine(fileSystem.getPath("target/")));
    }

    @AfterEach
    public void cleanupProperties() throws IOException {
        this.getFileSystem().close();
    }

    @Test
    public void completeCycleOkay() throws Exception {

        DownloadTask task = new StreamFactoryTask(new ByteArrayStreamFactory("TEST".getBytes()));
        DownloadRequest request = new DownloadRequest();
        request.setId("42");
        request.setTitle("Foo");
        request.setTargetFileNameSupplier(() -> "targetFileName");
        request.setTaskSupplier(() -> task);

        DownloadSchedulingListener schedulingListener = Mockito.mock(DownloadSchedulingListener.class);
        this.getEngine().addSchedulingListener(schedulingListener);

        DownloadRequestWrapper requestWrapper = this.getEngine().submit(request);
        Assertions.assertEquals(this.getEngine(), requestWrapper.getOwner());
        Assertions.assertEquals(request, requestWrapper.getRequest());
        Assertions.assertNotNull(requestWrapper.getScheduledTime());

        this.getEngine().waitUntilAllDownloadsComplete();

        Mockito.verify(schedulingListener).onRequestSubmit(Mockito.eq(request));
        Mockito.verify(schedulingListener, Mockito.never()).onRequestScheduled(Mockito.any());

        ArgumentCaptor<DownloadOperation> operationStartedCaptor = ArgumentCaptor.forClass(DownloadOperation.class);
        Mockito.verify(schedulingListener).onOperationStarting(operationStartedCaptor.capture());
        Assertions.assertNull(operationStartedCaptor.getValue().getCancelTime());
        Assertions.assertNotNull(operationStartedCaptor.getValue().getEndTime());
        Assertions.assertNull(operationStartedCaptor.getValue().getError());
        Assertions.assertSame(this.getEngine(), operationStartedCaptor.getValue().getOwner());
        Assertions.assertNotNull(operationStartedCaptor.getValue().getStartTime());
        Assertions.assertEquals(DownloadOperationStatus.COMPLETED, operationStartedCaptor.getValue().getStatus());

        ArgumentCaptor<DownloadTask> taskTransferStartingCaptor = ArgumentCaptor.forClass(DownloadTask.class);
        ArgumentCaptor<Path> pathTransferStartingCaptor = ArgumentCaptor.forClass(Path.class);
        ArgumentCaptor<DownloadOperation> operationTransferStartingCaptor = ArgumentCaptor.forClass(DownloadOperation.class);
        Mockito.verify(schedulingListener).onOperationTransferStarting(taskTransferStartingCaptor.capture(), pathTransferStartingCaptor.capture(), operationTransferStartingCaptor.capture());
        Assertions.assertNull(operationTransferStartingCaptor.getValue().getCancelTime());
        Assertions.assertNotNull(operationTransferStartingCaptor.getValue().getEndTime());
        Assertions.assertNull(operationTransferStartingCaptor.getValue().getError());
        Assertions.assertSame(this.getEngine(), operationTransferStartingCaptor.getValue().getOwner());
        Assertions.assertNotNull(operationTransferStartingCaptor.getValue().getStartTime());
        Assertions.assertEquals(DownloadOperationStatus.COMPLETED, operationTransferStartingCaptor.getValue().getStatus());
        Assertions.assertEquals("targetFileName", pathTransferStartingCaptor.getValue().getFileName().toString());
        Assertions.assertEquals(task, taskTransferStartingCaptor.getValue());

        ArgumentCaptor<DownloadTask> taskTransferCompletedCaptor = ArgumentCaptor.forClass(DownloadTask.class);
        ArgumentCaptor<Path> pathTransferCompletedCaptor = ArgumentCaptor.forClass(Path.class);
        ArgumentCaptor<DownloadOperation> operationTransferCompletedCaptor = ArgumentCaptor.forClass(DownloadOperation.class);
        Mockito.verify(schedulingListener).onOperationTransferCompleted(taskTransferCompletedCaptor.capture(), pathTransferCompletedCaptor.capture(), operationTransferCompletedCaptor.capture());
        Assertions.assertNull(operationTransferCompletedCaptor.getValue().getCancelTime());
        Assertions.assertNotNull(operationTransferCompletedCaptor.getValue().getEndTime());
        Assertions.assertNull(operationTransferCompletedCaptor.getValue().getError());
        Assertions.assertSame(this.getEngine(), operationTransferCompletedCaptor.getValue().getOwner());
        Assertions.assertNotNull(operationTransferCompletedCaptor.getValue().getStartTime());
        Assertions.assertEquals(DownloadOperationStatus.COMPLETED, operationTransferCompletedCaptor.getValue().getStatus());
        Assertions.assertEquals("targetFileName", pathTransferCompletedCaptor.getValue().getFileName().toString());
        Assertions.assertEquals(task, taskTransferCompletedCaptor.getValue());
        Assertions.assertArrayEquals("TEST".getBytes(), Files.readAllBytes(pathTransferCompletedCaptor.getValue()));

        ArgumentCaptor<DownloadOperation> operationCompletedCaptor = ArgumentCaptor.forClass(DownloadOperation.class);
        Mockito.verify(schedulingListener).onOperationCompleted(operationCompletedCaptor.capture());
        Assertions.assertNull(operationCompletedCaptor.getValue().getCancelTime());
        Assertions.assertNotNull(operationCompletedCaptor.getValue().getEndTime());
        Assertions.assertNull(operationCompletedCaptor.getValue().getError());
        Assertions.assertSame(this.getEngine(), operationCompletedCaptor.getValue().getOwner());
        Assertions.assertNotNull(operationCompletedCaptor.getValue().getStartTime());
        Assertions.assertEquals(DownloadOperationStatus.COMPLETED, operationCompletedCaptor.getValue().getStatus());

        Mockito.verifyNoMoreInteractions(schedulingListener);

    }

    @Test
    public void completeCycleError() throws Exception {

        IOException error = new IOException("ERROR");
        StreamFactory streamFactory = Mockito.mock(StreamFactory.class);
        Mockito.when(streamFactory.openStream()).thenThrow(error);
        DownloadTask task = new StreamFactoryTask(streamFactory);
        DownloadRequest request = new DownloadRequest();
        request.setId("42");
        request.setTitle("Foo");
        request.setTargetFileNameSupplier(() -> "targetFileName");
        request.setTaskSupplier(() -> task);

        DownloadSchedulingListener schedulingListener = Mockito.mock(DownloadSchedulingListener.class);
        this.getEngine().addSchedulingListener(schedulingListener);

        DownloadRequestWrapper requestWrapper = this.getEngine().submit(request);
        Assertions.assertEquals(this.getEngine(), requestWrapper.getOwner());
        Assertions.assertEquals(request, requestWrapper.getRequest());
        Assertions.assertNotNull(requestWrapper.getScheduledTime());

        this.getEngine().waitUntilAllDownloadsComplete();

        Mockito.verify(schedulingListener).onRequestSubmit(Mockito.eq(request));
        Mockito.verify(schedulingListener, Mockito.never()).onRequestScheduled(Mockito.any());

        ArgumentCaptor<DownloadOperation> operationStartedCaptor = ArgumentCaptor.forClass(DownloadOperation.class);
        Mockito.verify(schedulingListener).onOperationStarting(operationStartedCaptor.capture());
        Assertions.assertNull(operationStartedCaptor.getValue().getCancelTime());
        Assertions.assertNotNull(operationStartedCaptor.getValue().getEndTime());
        Assertions.assertEquals(error, operationStartedCaptor.getValue().getError());
        Assertions.assertSame(this.getEngine(), operationStartedCaptor.getValue().getOwner());
        Assertions.assertNotNull(operationStartedCaptor.getValue().getStartTime());
        Assertions.assertEquals(DownloadOperationStatus.COMPLETED, operationStartedCaptor.getValue().getStatus());

        ArgumentCaptor<DownloadTask> taskTransferStartingCaptor = ArgumentCaptor.forClass(DownloadTask.class);
        ArgumentCaptor<Path> pathTransferStartingCaptor = ArgumentCaptor.forClass(Path.class);
        ArgumentCaptor<DownloadOperation> operationTransferStartingCaptor = ArgumentCaptor.forClass(DownloadOperation.class);
        Mockito.verify(schedulingListener).onOperationTransferStarting(taskTransferStartingCaptor.capture(), pathTransferStartingCaptor.capture(), operationTransferStartingCaptor.capture());
        Assertions.assertNull(operationTransferStartingCaptor.getValue().getCancelTime());
        Assertions.assertNotNull(operationTransferStartingCaptor.getValue().getEndTime());
        Assertions.assertEquals(error, operationTransferStartingCaptor.getValue().getError());
        Assertions.assertSame(this.getEngine(), operationTransferStartingCaptor.getValue().getOwner());
        Assertions.assertNotNull(operationTransferStartingCaptor.getValue().getStartTime());
        Assertions.assertEquals(DownloadOperationStatus.COMPLETED, operationTransferStartingCaptor.getValue().getStatus());
        Assertions.assertEquals("targetFileName", pathTransferStartingCaptor.getValue().getFileName().toString());
        Assertions.assertEquals(task, taskTransferStartingCaptor.getValue());

        ArgumentCaptor<DownloadTask> taskTransferCompletedCaptor = ArgumentCaptor.forClass(DownloadTask.class);
        ArgumentCaptor<Path> pathTransferCompletedCaptor = ArgumentCaptor.forClass(Path.class);
        ArgumentCaptor<DownloadOperation> operationTransferCompletedCaptor = ArgumentCaptor.forClass(DownloadOperation.class);
        Mockito.verify(schedulingListener).onOperationTransferCompleted(taskTransferCompletedCaptor.capture(), pathTransferCompletedCaptor.capture(), operationTransferCompletedCaptor.capture());
        Assertions.assertNull(operationTransferCompletedCaptor.getValue().getCancelTime());
        Assertions.assertNotNull(operationTransferCompletedCaptor.getValue().getEndTime());
        Assertions.assertEquals(error, operationTransferCompletedCaptor.getValue().getError());
        Assertions.assertSame(this.getEngine(), operationTransferCompletedCaptor.getValue().getOwner());
        Assertions.assertNotNull(operationTransferCompletedCaptor.getValue().getStartTime());
        Assertions.assertEquals(DownloadOperationStatus.COMPLETED, operationTransferCompletedCaptor.getValue().getStatus());
        Assertions.assertEquals("targetFileName", pathTransferCompletedCaptor.getValue().getFileName().toString());
        Assertions.assertEquals(task, taskTransferCompletedCaptor.getValue());
        Assertions.assertFalse(Files.exists(pathTransferCompletedCaptor.getValue()));

        ArgumentCaptor<DownloadOperation> operationCompletedCaptor = ArgumentCaptor.forClass(DownloadOperation.class);
        Mockito.verify(schedulingListener).onOperationCompleted(operationCompletedCaptor.capture());
        Assertions.assertNull(operationCompletedCaptor.getValue().getCancelTime());
        Assertions.assertNotNull(operationCompletedCaptor.getValue().getEndTime());
        Assertions.assertEquals(error, operationCompletedCaptor.getValue().getError());
        Assertions.assertSame(this.getEngine(), operationCompletedCaptor.getValue().getOwner());
        Assertions.assertNotNull(operationCompletedCaptor.getValue().getStartTime());
        Assertions.assertEquals(DownloadOperationStatus.COMPLETED, operationCompletedCaptor.getValue().getStatus());

        Mockito.verifyNoMoreInteractions(schedulingListener);

    }

    @Test
    public void submitWithListenerReject() throws Exception {

        DownloadSchedulingListener listener = Mockito.mock(DownloadSchedulingListener.class);
        Mockito.doThrow(new DownloadRejectedException("X")).when(listener).onRequestSubmit(Mockito.any());
        this.getEngine().addSchedulingListener(listener);

        DownloadRequest request = new DownloadRequest();
        request.setTitle("TITLE");
        request.setTaskSupplier(() -> null);

        Assertions.assertNull(this.getEngine().submit(request));
        Mockito.verify(listener).onRequestSubmit(Mockito.eq(request));
        Mockito.verifyNoMoreInteractions(listener);

    }

    @Test
    public void cancelOperation() throws Exception {

        DownloadSchedulingListener listener = Mockito.mock(DownloadSchedulingListener.class);
        this.getEngine().addSchedulingListener(listener);

        DownloadOperation operation = Mockito.mock(DownloadOperation.class);
        this.getEngine().getActiveOperations().add(operation);

        // Canceling the operation does _not_ remove it from the list of active operations!
        // It simply sets the state to CANCELLED and informs everyone by generating an event.
        // The actual removal is done by the processor thread. Since in this test we do not have
        // a processor thread, the operation will remain in the activeOperations list.
        Assertions.assertTrue(this.getEngine().cancelOperation(operation, null));
        Assertions.assertTrue(this.getEngine().getActiveOperations().contains(operation));
        Mockito.verify(listener).onOperationCancelled(Mockito.eq(operation));
        Mockito.verifyNoMoreInteractions(listener);

    }

    @Test
    public void cancelOperationNotFound() throws Exception {

        DownloadSchedulingListener listener = Mockito.mock(DownloadSchedulingListener.class);
        this.getEngine().addSchedulingListener(listener);

        Assertions.assertFalse(this.getEngine().cancelOperation(Mockito.mock(DownloadOperation.class), null));
        Mockito.verifyNoMoreInteractions(listener);

    }

    @Test
    public void cancelRequest() throws Exception {

        DownloadSchedulingListener listener = Mockito.mock(DownloadSchedulingListener.class);
        this.getEngine().addSchedulingListener(listener);

        DownloadRequestWrapper requestWrapper = Mockito.mock(DownloadRequestWrapper.class);
        this.getEngine().getWaitingRequests().add(requestWrapper);

        Assertions.assertTrue(this.getEngine().cancelRequest(requestWrapper, null));
        Assertions.assertFalse(this.getEngine().getWaitingRequests().contains(requestWrapper));
        Mockito.verify(listener).onRequestCancelled(Mockito.eq(requestWrapper));
        Mockito.verifyNoMoreInteractions(listener);

    }

    @Test
    public void cancelRequestNotFound() throws Exception {

        DownloadSchedulingListener listener = Mockito.mock(DownloadSchedulingListener.class);
        this.getEngine().addSchedulingListener(listener);

        Assertions.assertFalse(this.getEngine().cancelRequest(Mockito.mock(DownloadRequestWrapper.class), null));
        Mockito.verifyNoMoreInteractions(listener);

    }

    @Test
    public void cancelRequestWithOperation() throws Exception {

        DownloadOperation operation = Mockito.mock(DownloadOperation.class);
        this.getEngine().getActiveOperations().add(operation);

        DownloadRequestWrapper requestWrapper = Mockito.mock(DownloadRequestWrapper.class);
        Mockito.when(requestWrapper.getOperation()).thenReturn(operation);

        DownloadSchedulingListener listener = Mockito.mock(DownloadSchedulingListener.class);
        this.getEngine().addSchedulingListener(listener);

        // Canceling the operation does _not_ remove it from the list of active operations!
        // It simply sets the state to CANCELLED and informs everyone by generating an event.
        // The actual removal is done by the processor thread. Since in this test we do not have
        // a processor thread, the operation will remain in the activeOperations list.
        Assertions.assertTrue(this.getEngine().cancelOperation(operation, null));
        Assertions.assertTrue(this.getEngine().getActiveOperations().contains(operation));
        Mockito.verify(listener).onOperationCancelled(Mockito.eq(operation));
        Mockito.verifyNoMoreInteractions(listener);

    }

    private FileSystem getFileSystem() {
        return this.myFileSystem;
    }
    private void setFileSystem(FileSystem fileSystem) {
        this.myFileSystem = fileSystem;
    }

    private DownloadEngine getEngine() {
        return this.myEngine;
    }
    private void setEngine(DownloadEngine engine) {
        this.myEngine = engine;
    }

}
