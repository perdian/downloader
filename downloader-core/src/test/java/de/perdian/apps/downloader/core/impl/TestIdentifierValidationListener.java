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
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;

import de.perdian.apps.downloader.core.DownloadJob;
import de.perdian.apps.downloader.core.DownloadRejectedException;
import de.perdian.apps.downloader.core.DownloadRequest;

public class TestIdentifierValidationListener {

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
  public void requestSubmittedIdentifierNotExistingYet() throws Exception {

    DownloadRequest request = new DownloadRequest();
    request.setId("42");

    Path markerDirectory = this.getFileSystem().getPath("markerDirectory");
    IdentifierValidationListener listener = new IdentifierValidationListener(markerDirectory);
    listener.requestSubmitted(request);

  }

  @Test(expected=DownloadRejectedException.class)
  public void requestSubmittedIdentifierExisting() throws Exception {

    DownloadRequest request = new DownloadRequest();
    request.setId("42");

    Path markerDirectory = this.getFileSystem().getPath("markerDirectory");
    Files.createDirectories(markerDirectory);
    Path markerFile = markerDirectory.resolve("42.marker");
    Files.createFile(markerFile);

    IdentifierValidationListener listener = new IdentifierValidationListener(markerDirectory);
    listener.requestSubmitted(request);

  }

  @Test
  public void jobCompleted() throws Exception {

    DownloadRequest request = new DownloadRequest();
    request.setId("42");
    DownloadJob job = Mockito.mock(DownloadJob.class);
    Mockito.when(job.getRequest()).thenReturn(request);

    Path markerDirectory = this.getFileSystem().getPath("markerDirectory");
    Path markerFile = markerDirectory.resolve("42.marker");
    Assert.assertFalse(Files.exists(markerFile));

    IdentifierValidationListener listener = new IdentifierValidationListener(markerDirectory);
    listener.jobCompleted(job);
    Assert.assertTrue(Files.exists(markerFile));

  }

  @Test
  public void jobCompletedIdentifierNull() throws Exception {

    DownloadRequest request = new DownloadRequest();
    request.setId(null);
    DownloadJob job = Mockito.mock(DownloadJob.class);
    Mockito.when(job.getRequest()).thenReturn(request);

    Path markerDirectory = Mockito.mock(Path.class);
    IdentifierValidationListener listener = new IdentifierValidationListener(markerDirectory);
    listener.jobCompleted(job);
    Mockito.verifyNoMoreInteractions(markerDirectory);

  }

  @Test
  public void jobCancelled() throws Exception {

    DownloadRequest request = new DownloadRequest();
    request.setId("42");
    DownloadJob job = Mockito.mock(DownloadJob.class);
    Mockito.when(job.getRequest()).thenReturn(request);

    Path markerDirectory = this.getFileSystem().getPath("markerDirectory");
    Path markerFile = markerDirectory.resolve("42.marker");
    Assert.assertFalse(Files.exists(markerFile));

    IdentifierValidationListener listener = new IdentifierValidationListener(markerDirectory);
    listener.jobCancelled(job);
    Assert.assertTrue(Files.exists(markerFile));

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