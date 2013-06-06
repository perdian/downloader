/*
 * Copyright 2013 Christian Robert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.perdian.apps.downloader.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;

public class TestDownloadEngine {

  private FileSystem myFileSystem = null;
  private DownloadEngineBuilder myEngineBuilder = null;

  @Before
  public void prepareProperties() throws IOException {

    FileSystem fileSystem = MemoryFileSystemBuilder.newEmpty().build(UUID.randomUUID().toString());
    this.setFileSystem(fileSystem);

    DownloadEngineBuilder engineBuilder = new DownloadEngineBuilder();
    engineBuilder.setTargetDirectory(fileSystem.getPath("target/"));
    this.setEngineBuilder(engineBuilder);

  }

  @After
  public void cleanupProperties() throws IOException {
    this.getFileSystem().close();
  }

  @Test
  public void completeCycleOkay() throws Exception {

    DownloadProgressListener progressListener = Mockito.mock(DownloadProgressListener.class);
    DownloadListener listener = Mockito.mock(DownloadListener.class);

    byte[] streamBytes = "TEST".getBytes();
    DownloadStreamFactory streamFactory = Mockito.mock(DownloadStreamFactory.class);
    Mockito.when(streamFactory.size()).thenReturn(Long.valueOf(streamBytes.length));
    Mockito.when(streamFactory.openStream()).thenReturn(new ByteArrayInputStream(streamBytes));

    DownloadRequest request = new DownloadRequest();
    request.setTargetFileName("abc.def");
    request.setStreamFactory(streamFactory);

    DownloadEngine engine = this.getEngineBuilder().build();
    engine.addListener(listener);
    DownloadJob job = engine.submit(request);
    job.addProgressListener(progressListener);
    engine.shutdownAndWait();

    Assert.assertNull(job.getCancelTime());
    Assert.assertNotNull(job.getEndTime());
    Assert.assertNull(job.getError());
    Assert.assertSame(engine, job.getOwner());
    Assert.assertNotNull(job.getScheduleTime());
    Assert.assertNotNull(job.getStartTime());
    Assert.assertEquals(DownloadStatus.COMPLETED, job.getStatus());
    Assert.assertArrayEquals(streamBytes, Files.readAllBytes(job.getResult()));

    Mockito.verify(listener).requestSubmitted(Matchers.eq(request));
    Mockito.verify(listener).jobStarted(Matchers.eq(job));
    Mockito.verify(listener).jobCompleted(Matchers.eq(job));
    Mockito.verifyNoMoreInteractions(listener);
    Mockito.verify(progressListener, Mockito.atLeast(1)).progress(Matchers.eq(job), Matchers.anyLong(), Matchers.anyLong());

  }

  @Test
  public void completeCycleErrorInStreamCreation() throws Exception {

    DownloadListener listener = Mockito.mock(DownloadListener.class);
    DownloadProgressListener progressListener = Mockito.mock(DownloadProgressListener.class);

    IOException streamException = new IOException("ERROR");
    DownloadStreamFactory streamFactory = Mockito.mock(DownloadStreamFactory.class);
    Mockito.when(streamFactory.size()).thenReturn(Long.valueOf(-1));
    Mockito.when(streamFactory.openStream()).thenThrow(streamException);

    DownloadRequest request = new DownloadRequest();
    request.setTargetFileName("abc.def");
    request.setStreamFactory(streamFactory);

    DownloadEngine engine = this.getEngineBuilder().build();
    engine.addListener(listener);
    DownloadJob job = engine.submit(request);
    job.addProgressListener(progressListener);
    engine.shutdownAndWait();

    Assert.assertNull(job.getCancelTime());
    Assert.assertNotNull(job.getEndTime());
    Assert.assertSame(streamException, job.getError());
    Assert.assertSame(engine, job.getOwner());
    Assert.assertNotNull(job.getScheduleTime());
    Assert.assertNotNull(job.getStartTime());
    Assert.assertEquals(DownloadStatus.COMPLETED, job.getStatus());

    Mockito.verify(listener).requestSubmitted(Matchers.eq(request));
    Mockito.verify(listener).jobStarted(Matchers.eq(job));
    Mockito.verify(listener).jobCompleted(Matchers.eq(job));
    Mockito.verifyNoMoreInteractions(listener);
    Mockito.verifyNoMoreInteractions(progressListener);

  }

  @Test(expected=IllegalArgumentException.class)
  public void submitWithNullTargetFileName() {
    DownloadRequest request = new DownloadRequest();
    request.setStreamFactory(Mockito.mock(DownloadStreamFactory.class));
    request.setTargetFileName(null);
    DownloadEngine engine = this.getEngineBuilder().build();
    engine.submit(request);
  }

  @Test(expected=IllegalArgumentException.class)
  public void submitWithNullStreamFactory() {
    DownloadRequest request = new DownloadRequest();
    request.setStreamFactory(null);
    request.setTargetFileName("targetFileName");
    DownloadEngine engine = this.getEngineBuilder().build();
    engine.submit(request);
  }

  @Test(expected=IllegalStateException.class)
  public void submitWhenShutdown() {
    DownloadRequest request = new DownloadRequest();
    request.setStreamFactory(Mockito.mock(DownloadStreamFactory.class));
    request.setTargetFileName("targetFileName");
    DownloadEngine engine = this.getEngineBuilder().build();
    engine.shutdown();
    engine.submit(request);
  }

  @Test
  public void submitWithValidatorReject() throws Exception {

    DownloadListener listener = Mockito.mock(DownloadListener.class);
    Mockito.doThrow(new DownloadRejectedException("X")).when(listener).requestSubmitted(Matchers.any(DownloadRequest.class));

    DownloadRequest request = new DownloadRequest();
    request.setStreamFactory(Mockito.mock(DownloadStreamFactory.class));
    request.setTargetFileName("targetFileName");

    DownloadEngine engine = this.getEngineBuilder().build();
    engine.addListener(listener);
    Assert.assertNull(engine.submit(request));
    Mockito.verify(listener).requestSubmitted(Matchers.eq(request));
    Mockito.verifyNoMoreInteractions(listener);

  }

  @Test(expected=IllegalArgumentException.class)
  public void updateProcessorCountNegativeValue() {
    DownloadEngine engine = this.getEngineBuilder().build();
    engine.setProcessorCount(-1);
  }

  @Test
  public void updateProcessorCountSameValue() {
    DownloadListener listener = Mockito.mock(DownloadListener.class);
    DownloadEngine engine = this.getEngineBuilder().build();
    engine.addListener(listener);
    engine.setProcessorCount(this.getEngineBuilder().getProcessorCount());
    Mockito.verifyNoMoreInteractions(listener);
  }

  @Test
  public void updateProcessorCountNewValue() {
    DownloadListener listener = Mockito.mock(DownloadListener.class);
    DownloadEngine engine = this.getEngineBuilder().build();
    engine.addListener(listener);
    engine.setProcessorCount(this.getEngineBuilder().getProcessorCount() + 1);
    Mockito.verify(listener).processorCountUpdated(Matchers.eq(this.getEngineBuilder().getProcessorCount() + 1));
  }

  // ---------------------------------------------------------------------------
  // --- Property access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  private FileSystem getFileSystem() {
    return this.myFileSystem;
  }
  private void setFileSystem(FileSystem fileSystem) {
    this.myFileSystem = fileSystem;
  }

  private DownloadEngineBuilder getEngineBuilder() {
    return this.myEngineBuilder;
  }
  private void setEngineBuilder(DownloadEngineBuilder engineBuilder) {
    this.myEngineBuilder = engineBuilder;
  }

}