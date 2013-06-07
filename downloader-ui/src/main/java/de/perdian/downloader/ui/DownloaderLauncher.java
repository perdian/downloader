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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

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

public class DownloaderLauncher {

  static final Logger log = LogManager.getLogger(DownloaderLauncher.class);

  private List<DownloaderLauncherAction> myActions = new CopyOnWriteArrayList<>();
  private DownloadEngine myEngine = null;

  public DownloaderLauncher(DownloadEngine engine) {
    this.setEngine(Objects.requireNonNull(engine, "Parameter 'engine' must not be null"));
  }

  /**
   * Launches the UI application
   */
  public void launch() {

    log.trace("Installing JGoodies Look and Feel");
    try {
      UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
    } catch(Exception e) {
      log.warn("Cannot install JGoodies Look and Feel", e);
    }

    log.debug("Launching Downloader application");
    DownloaderPanel applicationPanel = new DownloaderPanel(this.getEngine());
    JFrame applicationFrame = new JFrame();
    applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    applicationFrame.setContentPane(applicationPanel);
    applicationFrame.setMinimumSize(new Dimension(640, 480));
    applicationFrame.setSize(new Dimension(800, 600));
    applicationFrame.setTitle("Downloader");
    applicationFrame.setLocationRelativeTo(null);
    applicationFrame.setVisible(true);

    List<DownloaderLauncherAction> actions = this.getActions();
    if(actions != null && actions.size() > 0) {
      log.debug("Executing {} actions", actions.size());
      final CountDownLatch latch = new CountDownLatch(actions.size());
      for(final DownloaderLauncherAction action : actions) {
        Thread actionThread = new Thread(new Runnable() {
          @Override public void run() {
            try {
              log.debug("Executing action: {}", action);
              action.execute(DownloaderLauncher.this.getEngine());
            } catch(Exception e) {
              log.error("Cannot execute action: " + action, e);
            } finally {
              latch.countDown();
            }
          }
        });
        actionThread.setName(DownloaderLauncherAction.class.getSimpleName() + "[" + action + "]");
        actionThread.start();
      }
      try {
        latch.await();
      } catch(InterruptedException e) {
        // Ignore here
      }
    }

    log.debug("Downloader application launch completed");

  }

  // ---------------------------------------------------------------------------
  // --- Property access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  public DownloadEngine getEngine() {
    return this.myEngine;
  }
  public void setEngine(DownloadEngine engine) {
    this.myEngine = engine;
  }

  public void addAction(DownloaderLauncherAction action) {
    this.getActions().add(action);
  }
  List<DownloaderLauncherAction> getActions() {
    return this.myActions;
  }
  void setActions(List<DownloaderLauncherAction> actions) {
    this.myActions = actions;
  }

}