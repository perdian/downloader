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
  private DownloadEngine myEngine = null;

  @Before
  public void prepareProperties() throws IOException {
    FileSystem fileSystem = MemoryFileSystemBuilder.newEmpty().build(UUID.randomUUID().toString());
    this.setFileSystem(fileSystem);
    this.setEngine(new DownloadEngine(fileSystem.getPath("target/")));
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
    DownloadStreamFactory contentFactory = Mockito.mock(DownloadStreamFactory.class);
    Mockito.when(contentFactory.size()).thenReturn(Long.valueOf(streamBytes.length));
    Mockito.when(contentFactory.openStream()).thenReturn(new ByteArrayInputStream(streamBytes));

    DownloadRequest request = new DownloadRequest();
    request.setTargetFileName("abc.def");
    request.setContentFactory(contentFactory);

    this.getEngine().addListener(listener);
    DownloadJob job = this.getEngine().submit(request);
    job.addProgressListener(progressListener);
    this.getEngine().waitUntilAllDownloadsComplete();

    Assert.assertNull(job.getCancelTime());
    Assert.assertNotNull(job.getEndTime());
    Assert.assertNull(job.getError());
    Assert.assertSame(this.getEngine(), job.getOwner());
    Assert.assertNotNull(job.getScheduleTime());
    Assert.assertNotNull(job.getStartTime());
    Assert.assertEquals(DownloadStatus.COMPLETED, job.getStatus());
    Assert.assertArrayEquals(streamBytes, Files.readAllBytes(job.getTargetFile()));

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
    DownloadStreamFactory contentFactory = Mockito.mock(DownloadStreamFactory.class);
    Mockito.when(contentFactory.size()).thenReturn(Long.valueOf(-1));
    Mockito.when(contentFactory.openStream()).thenThrow(streamException);

    DownloadRequest request = new DownloadRequest();
    request.setTargetFileName("abc.def");
    request.setContentFactory(contentFactory);

    this.getEngine().addListener(listener);
    DownloadJob job = this.getEngine().submit(request);
    job.addProgressListener(progressListener);
    this.getEngine().waitUntilAllDownloadsComplete();

    Assert.assertNull(job.getCancelTime());
    Assert.assertNotNull(job.getEndTime());
    Assert.assertSame(streamException, job.getError());
    Assert.assertSame(this.getEngine(), job.getOwner());
    Assert.assertNotNull(job.getScheduleTime());
    Assert.assertNotNull(job.getStartTime());
    Assert.assertEquals(DownloadStatus.COMPLETED, job.getStatus());

    Mockito.verify(listener).requestSubmitted(Matchers.eq(request));
    Mockito.verify(listener).jobStarted(Matchers.eq(job));
    Mockito.verify(listener).jobCompleted(Matchers.eq(job));
    Mockito.verifyNoMoreInteractions(listener);
    Mockito.verifyNoMoreInteractions(progressListener);

  }

  @Test(expected=NullPointerException.class)
  public void submitWithNullTargetFileName() {
    DownloadRequest request = new DownloadRequest();
    request.setContentFactory(Mockito.mock(DownloadStreamFactory.class));
    request.setTargetFileName(null);
    this.getEngine().submit(request);
  }

  @Test(expected=NullPointerException.class)
  public void submitWithNullContentFactory() {
    DownloadRequest request = new DownloadRequest();
    request.setContentFactory(null);
    request.setTargetFileName("targetFileName");
    this.getEngine().submit(request);
  }

  @Test
  public void submitWithValidatorReject() throws Exception {

    DownloadListener listener = Mockito.mock(DownloadListener.class);
    Mockito.doThrow(new DownloadRejectedException("X")).when(listener).requestSubmitted(Matchers.any(DownloadRequest.class));
    this.getEngine().addListener(listener);

    DownloadRequest request = new DownloadRequest();
    request.setContentFactory(Mockito.mock(DownloadStreamFactory.class));
    request.setTargetFileName("targetFileName");

    Assert.assertNull(this.getEngine().submit(request));
    Mockito.verify(listener).requestSubmitted(Matchers.eq(request));
    Mockito.verifyNoMoreInteractions(listener);

  }

  @Test(expected=IllegalArgumentException.class)
  public void updateProcessorCountNegativeValue() {
    this.getEngine().setProcessorCount(-1);
  }

  @Test
  public void updateProcessorCountSameValue() {

    DownloadListener listener = Mockito.mock(DownloadListener.class);
    this.getEngine().addListener(listener);

    this.getEngine().setProcessorCount(this.getEngine().getProcessorCount());
    Mockito.verifyNoMoreInteractions(listener);

  }

  @Test
  public void updateProcessorCountNewValue() {

    DownloadListener listener = Mockito.mock(DownloadListener.class);
    this.getEngine().addListener(listener);

    int previousProcessorCount = this.getEngine().getProcessorCount();
    this.getEngine().setProcessorCount(previousProcessorCount + 1);
    Mockito.verify(listener).processorCountUpdated(Matchers.eq(previousProcessorCount + 1));

  }

  @Test
  public void cancelFromActiveJobs() throws Exception {

    DownloadListener listener = Mockito.mock(DownloadListener.class);
    this.getEngine().addListener(listener);

    DownloadJob job = Mockito.mock(DownloadJob.class);
    this.getEngine().getActiveJobs().add(job);

    Assert.assertTrue(this.getEngine().cancelJob(job));
    Assert.assertTrue(this.getEngine().getActiveJobs().contains(job));
    Mockito.verify(listener).jobCancelled(Matchers.eq(job));

  }

  @Test
  public void cancelFromWaitingJobs() throws Exception {

    DownloadListener listener = Mockito.mock(DownloadListener.class);
    this.getEngine().addListener(listener);

    DownloadJob job = Mockito.mock(DownloadJob.class);
    this.getEngine().getWaitingJobs().add(job);

    Assert.assertTrue(this.getEngine().cancelJob(job));
    Assert.assertFalse(this.getEngine().getWaitingJobs().contains(job));
    Mockito.verify(listener).jobCancelled(Matchers.eq(job));

  }

  @Test
  public void cancelNotInQueue() throws Exception {

    DownloadListener listener = Mockito.mock(DownloadListener.class);
    this.getEngine().addListener(listener);

    Assert.assertFalse(this.getEngine().cancelJob(Mockito.mock(DownloadJob.class)));
    Mockito.verify(listener, Mockito.never()).jobCancelled(Matchers.any(DownloadJob.class));

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

  private DownloadEngine getEngine() {
    return this.myEngine;
  }
  private void setEngine(DownloadEngine engine) {
    this.myEngine = engine;
  }

}