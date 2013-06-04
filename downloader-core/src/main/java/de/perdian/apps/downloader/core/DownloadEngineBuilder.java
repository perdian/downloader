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

import java.nio.file.Path;

/**
 * Builder from which a {@link DownloadEngine} can be obtained. The builder
 * itself remains ignorant upon engines created by it, therefore multiple
 * invocations upon the {@code build} methode will always create a new
 * engine that is completely independent of the builder from which it was
 * created as well as others engines that have been created by the builder
 * instance.
 *
 * @author Christian Robert
 */

public class DownloadEngineBuilder {

  private int myProcessorCount = 1;
  private Path myMetadataDirectory = null;
  private Path myWorkingDirectory = null;
  private Path myTargetDirectory = null;

  /**
   * Creates a new engine implementation that is configured with the information
   * configured in the builder at the time this method is called.
   * Subsequent changes of the builders state will not be reflected in the
   * created engine, as well as subsequent changes in the created engine will
   * not be reflected in the state of the builder.
   */
  public DownloadEngine build() {
    if(this.getWorkingDirectory() == null) {
      throw new IllegalArgumentException("Property workingDirectory must not be null!");
    } else if(this.getMetadataDirectory() == null) {
      throw new IllegalArgumentException("Property metadataDirectory must not be null!");
    } else if(this.getTargetDirectory() == null) {
      throw new IllegalArgumentException("Property targetDirectory must not be null!");
    } else if(this.getProcessorCount() <= 0) {
      throw new IllegalArgumentException("Property processorCount must be larger than 0! (Value found: " + this.getProcessorCount() + ")");
    } else {
      DownloadEngine engine = new DownloadEngine();
      engine.setProcessorCount(this.getProcessorCount());
      engine.setTargetDirectory(this.getTargetDirectory());
      engine.setMetadataDirectory(this.getMetadataDirectory());
      engine.setWorkingDirectory(this.getWorkingDirectory());
      return engine;
    }
  }

  // ---------------------------------------------------------------------------
  // --- Property access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  public int getProcessorCount() {
    return this.myProcessorCount;
  }
  public void setProcessorCount(int processorCount) {
    this.myProcessorCount = processorCount;
  }

  public Path getWorkingDirectory() {
    return this.myWorkingDirectory;
  }
  public void setWorkingDirectory(Path workingDirectory) {
    this.myWorkingDirectory = workingDirectory;
  }

  public Path getMetadataDirectory() {
    return this.myMetadataDirectory;
  }
  public void setMetadataDirectory(Path metadataDirectory) {
    this.myMetadataDirectory = metadataDirectory;
  }

  public Path getTargetDirectory() {
    return this.myTargetDirectory;
  }
  public void setTargetDirectory(Path targetDirectory) {
    this.myTargetDirectory = targetDirectory;
  }

}