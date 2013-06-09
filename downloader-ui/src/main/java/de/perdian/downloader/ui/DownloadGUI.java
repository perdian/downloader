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

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.perdian.apps.downloader.core.DownloadEngine;

/**
 * Launches the Downloader UI application
 *
 * @author Christian Robert
 */

public class DownloadGUI {

  static final Logger log = LogManager.getLogger(DownloadGUI.class);

  /**
   * Launches the UI application
   */
  public static DownloadEngine open(DownloadEngine engine) {
    return DownloadGUI.open(engine, new Dimension(800, 600));
  }

  /**
   * Launches the UI application
   */
  public static DownloadEngine open(DownloadEngine engine, Dimension windowSize) {

    log.trace("Installing JGoodies Look and Feel");
    try {
      UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
    } catch(Exception e) {
      log.warn("Cannot install JGoodies Look and Feel", e);
    }

    log.debug("Launching Downloader application");
    DownloadPanel applicationPanel = new DownloadPanel(engine);
    JFrame applicationFrame = new JFrame();
    applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    applicationFrame.setContentPane(applicationPanel);
    applicationFrame.setMinimumSize(new Dimension(640, 480));
    applicationFrame.setSize(windowSize);
    applicationFrame.setTitle("Downloader");
    applicationFrame.setLocationRelativeTo(null);
    applicationFrame.setVisible(true);

    log.debug("Downloader application launch completed");
    return engine;

  }

}