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
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;

public class TestDownloader {

  @Test
  public void completeCycleOkay() throws Exception {
    FileSystem memoryFileSystem = MemoryFileSystemBuilder.newEmpty().build("temp");
    try {

      DownloadJobListener listener = Mockito.mock(DownloadJobListener.class);

      byte[] streamBytes = "TEST".getBytes();
      DownloadStreamFactory streamFactory = Mockito.mock(DownloadStreamFactory.class);
      Mockito.when(streamFactory.size()).thenReturn(Long.valueOf(streamBytes.length));
      Mockito.when(streamFactory.openStream()).thenReturn(new ByteArrayInputStream(streamBytes));

      DownloadRequest request = new DownloadRequest();
      request.setTargetFileName("abc.def");
      request.setStreamFactory(streamFactory);

      DownloaderBuilder builder = new DownloaderBuilder();
      builder.setMetadataDirectory(memoryFileSystem.getPath("meta/"));
      builder.setWorkingDirectory(memoryFileSystem.getPath("working/"));
      builder.setTargetDirectory(memoryFileSystem.getPath("target/"));

      Downloader downloader = builder.build();
      DownloadJob job = downloader.submit(request, Arrays.asList(listener));
      downloader.shutdownAndWait();

      Assert.assertNull(job.getCancelTime());
      Assert.assertNotNull(job.getEndTime());
      Assert.assertNull(job.getError());
      Assert.assertSame(downloader, job.getOwner());
      Assert.assertNotNull(job.getScheduleTime());
      Assert.assertNotNull(job.getStartTime());
      Assert.assertEquals(DownloadStatus.COMPLETED, job.getStatus());
      Assert.assertArrayEquals(streamBytes, Files.readAllBytes(job.getResult()));

      Mockito.verify(listener).jobScheduled(Matchers.eq(job));
      Mockito.verify(listener).jobStarted(Matchers.eq(job));
      Mockito.verify(listener).jobCompleted(Matchers.eq(job));
      Mockito.verify(listener, Mockito.never()).jobCancelled(Matchers.eq(job));
      Mockito.verify(listener, Mockito.atLeast(1)).jobProgress(Matchers.eq(job), Matchers.anyLong(), Matchers.anyLong());

    } finally {
      memoryFileSystem.close();
    }
  }

  @Test
  public void completeCycleErrorInStreamCreation() throws Exception {
    FileSystem memoryFileSystem = MemoryFileSystemBuilder.newEmpty().build("temp");
    try {

      DownloadJobListener listener = Mockito.mock(DownloadJobListener.class);

      IOException streamException = new IOException("ERROR");
      DownloadStreamFactory streamFactory = Mockito.mock(DownloadStreamFactory.class);
      Mockito.when(streamFactory.size()).thenReturn(Long.valueOf(-1));
      Mockito.when(streamFactory.openStream()).thenThrow(streamException);

      DownloadRequest request = new DownloadRequest();
      request.setTargetFileName("abc.def");
      request.setStreamFactory(streamFactory);

      DownloaderBuilder builder = new DownloaderBuilder();
      builder.setMetadataDirectory(memoryFileSystem.getPath("meta/"));
      builder.setWorkingDirectory(memoryFileSystem.getPath("working/"));
      builder.setTargetDirectory(memoryFileSystem.getPath("target/"));

      Downloader downloader = builder.build();
      DownloadJob job = downloader.submit(request, Arrays.asList(listener));
      downloader.shutdownAndWait();

      Assert.assertNull(job.getCancelTime());
      Assert.assertNotNull(job.getEndTime());
      Assert.assertSame(streamException, job.getError());
      Assert.assertSame(downloader, job.getOwner());
      Assert.assertNotNull(job.getScheduleTime());
      Assert.assertNotNull(job.getStartTime());
      Assert.assertEquals(DownloadStatus.COMPLETED, job.getStatus());

      Mockito.verify(listener).jobScheduled(Matchers.eq(job));
      Mockito.verify(listener).jobStarted(Matchers.eq(job));
      Mockito.verify(listener).jobCompleted(Matchers.eq(job));
      Mockito.verify(listener, Mockito.never()).jobCancelled(Matchers.eq(job));
      Mockito.verify(listener, Mockito.never()).jobProgress(Matchers.eq(job), Matchers.anyLong(), Matchers.anyLong());

    } finally {
      memoryFileSystem.close();
    }
  }

}