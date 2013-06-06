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
package de.perdian.apps.downloader.core.impl;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;

import de.perdian.apps.downloader.core.DownloadJob;
import de.perdian.apps.downloader.core.DownloadRequest;

public class TestMoveCompletedDownloadsListener {


  private FileSystem myFileSystem = null;

  @Before
  public void prepareFileSystem() throws IOException {
    this.setFileSystem(MemoryFileSystemBuilder.newEmpty().build(UUID.randomUUID().toString()));
  }

  @After
  public void cleanupFileSystem() throws IOException {
    this.getFileSystem().close();
  }

  @Test
  public void jobCompleted() throws Exception {

    Path inFile = this.getFileSystem().getPath("working.txt");
    Files.write(inFile, "test".getBytes(), StandardOpenOption.CREATE);

    Path outDirectory = Files.createDirectory(this.getFileSystem().getPath("out/"));

    DownloadRequest request = new DownloadRequest();
    request.setTargetFileName("42.txt");
    DownloadJob job = Mockito.mock(DownloadJob.class);
    Mockito.when(job.getCancelTime()).thenReturn(null);
    Mockito.when(job.getRequest()).thenReturn(request);
    Mockito.when(job.getResult()).thenReturn(inFile);

    MoveCompletedDownloadsListener listener = new MoveCompletedDownloadsListener(outDirectory);
    listener.jobCompleted(job);

    Path outFile = outDirectory.resolve("42.txt");
    Assert.assertFalse(Files.exists(inFile));
    Assert.assertTrue(Files.exists(outFile));
    Assert.assertArrayEquals("test".getBytes(), Files.readAllBytes(outFile));

  }

  @Test
  public void jobCompletedStatusCancelled() throws Exception {

    Path inFile = this.getFileSystem().getPath("working.txt");
    Files.write(inFile, "test".getBytes(), StandardOpenOption.CREATE);

    Path outDirectory = Files.createDirectory(this.getFileSystem().getPath("out/"));

    DownloadRequest request = new DownloadRequest();
    request.setTargetFileName("42.txt");
    DownloadJob job = Mockito.mock(DownloadJob.class);
    Mockito.when(job.getCancelTime()).thenReturn(Long.valueOf(1L));
    Mockito.when(job.getRequest()).thenReturn(request);
    Mockito.when(job.getResult()).thenReturn(inFile);

    MoveCompletedDownloadsListener listener = new MoveCompletedDownloadsListener(outDirectory);
    listener.jobCompleted(job);

    Assert.assertFalse(Files.exists(inFile));
    Assert.assertFalse(Files.exists(outDirectory.resolve("42.txt")));

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

}