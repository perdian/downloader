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
import de.perdian.apps.downloader.core.DownloadEngineBuilder;

/**
 * Launches the Downloader UI application
 *
 * @author Christian Robert
 */

public class DownloaderLauncher {

  static final Logger log = LogManager.getLogger(DownloaderLauncher.class);

  private List<DownloaderLauncherAction> myActions = new CopyOnWriteArrayList<>();
  private DownloadEngineBuilder myEngineBuilder = null;

  public DownloaderLauncher(DownloadEngineBuilder engineBuilder) {
    this.setEngineBuilder(Objects.requireNonNull(engineBuilder, "Parameter 'engineBuilder' must not be null"));
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
    final DownloadEngineBuilder engineBuilder = this.getEngineBuilder();
    final DownloadEngine engine = engineBuilder.build();

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

    List<DownloaderLauncherAction> actions = this.getActions();
    if(actions != null && actions.size() > 0) {
      log.debug("Executing {} actions", actions.size());
      final CountDownLatch latch = new CountDownLatch(actions.size());
      for(final DownloaderLauncherAction action : actions) {
        Thread actionThread = new Thread(new Runnable() {
          @Override public void run() {
            try {
              log.debug("Executing action: {}", action);
              action.execute(engine);
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