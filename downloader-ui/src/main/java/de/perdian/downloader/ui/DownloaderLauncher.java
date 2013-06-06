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
package de.perdian.downloader.ui;

import java.awt.Dimension;
import java.io.File;
import java.util.Objects;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.perdian.apps.downloader.core.DownloadEngine;
import de.perdian.apps.downloader.core.DownloadEngineBuilder;

/**
 * Launches the Downloader UI application
 *
 * @author Christian Robert
 */

public class DownloaderLauncher {

  private static final Logger log = LogManager.getLogger(DownloaderLauncher.class);

  private DownloadEngineBuilder myEngineBuilder = null;

  public DownloaderLauncher() {
    DownloadEngineBuilder engineBuilder = new DownloadEngineBuilder();
    engineBuilder.setTargetDirectory(new File(System.getProperty("user.home"), ".downloader/").toPath());
    this.setEngineBuilder(engineBuilder);
  }

  /**
   * Launches the UI application
   *
   * @return
   *   the underlying {@link DownloadEngine} that is used to actually perform
   *   the logics requested by the UI
   */
  public DownloadEngine launch() {

    log.trace("Installing JGoodies Look and Feel");
    try {
      UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
    } catch(Exception e) {
      log.warn("Cannot install JGoodies Look and Feel", e);
    }

    log.trace("Creating DownloadEngine");
    DownloadEngineBuilder engineBuilder = this.getEngineBuilder();
    DownloadEngine engine = engineBuilder.build();

    log.debug("Launching Downloader application");
    DownloaderPanel applicationPanel = new DownloaderPanel(engine);
    JFrame applicationFrame = new JFrame();
    applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    applicationFrame.setContentPane(applicationPanel);
    applicationFrame.setMinimumSize(new Dimension(640, 480));
    applicationFrame.setSize(new Dimension(800, 600));
    applicationFrame.setTitle("Downloader");
    applicationFrame.setLocationRelativeTo(null);
    applicationFrame.setVisible(true);

    log.debug("Downloader application launch completed");
    return engine;

  }

  // ---------------------------------------------------------------------------
  // --- Property access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  public DownloadEngineBuilder getEngineBuilder() {
    return this.myEngineBuilder;
  }
  public void setEngineBuilder(DownloadEngineBuilder engineBuilder) {
    this.myEngineBuilder = Objects.requireNonNull(engineBuilder, "Parameter 'engineBuilder' must not be null");
  }

}