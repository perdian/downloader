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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

class DownloadJobTransferHandler {

  private Path myTargetDirectory = null;
  private Path myWorkingDirectory = null;

  Path executeTransfer(DownloadJob job) throws IOException {

    Path targetFilePath = this.getTargetDirectory().resolve(job.getRequest().getTargetFileName());
    Path workingFilePath = this.getWorkingDirectory().resolve(targetFilePath.getFileName());
    if(!Files.exists(workingFilePath.getParent())) {
      Files.createDirectory(workingFilePath.getParent());
    }

    long inStreamSize = job.getRequest().getStreamFactory().size();
    try(InputStream inStream = job.getRequest().getStreamFactory().openStream()) {
      try(OutputStream outStream = Files.newOutputStream(workingFilePath, Files.exists(workingFilePath) ? StandardOpenOption.WRITE : StandardOpenOption.CREATE)) {
        long totalBytesWritten = 0;
        byte[] buffer = new byte[8092];
        for(int bufferSize = inStream.read(buffer); bufferSize > -1; bufferSize = inStream.read(buffer)) {
          outStream.write(buffer, 0, bufferSize);
          totalBytesWritten += bufferSize;
          job.fireProgress(totalBytesWritten, inStreamSize);
        }
        outStream.flush();
      }
    }

    if(!Files.exists(targetFilePath.getParent())) {
      Files.createDirectory(targetFilePath.getParent());
    }
    Files.move(workingFilePath, targetFilePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    return targetFilePath;

  }

  // ---------------------------------------------------------------------------
  // --- Property access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  Path getTargetDirectory() {
    return this.myTargetDirectory;
  }
  void setTargetDirectory(Path targetDirectory) {
    this.myTargetDirectory = targetDirectory;
  }

  Path getWorkingDirectory() {
    return this.myWorkingDirectory;
  }
  void setWorkingDirectory(Path workingDirectory) {
    this.myWorkingDirectory = workingDirectory;
  }

}