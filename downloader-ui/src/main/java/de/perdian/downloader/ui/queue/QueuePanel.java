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
package de.perdian.downloader.ui.queue;

import com.jgoodies.forms.factories.Borders;

import de.perdian.apps.downloader.core.DownloadEngine;
import de.perdian.apps.downloader.core.DownloadJob;
import de.perdian.apps.downloader.core.DownloadListenerSkeleton;
import de.perdian.downloader.ui.support.AbstractListPanel;

/**
 * Displays all the download jobs currently waiting for execution
 *
 * @author Christian Robert
 */

public class QueuePanel extends AbstractListPanel<DownloadJob, QueueJobPanel> {

  static final long serialVersionUID = 201306061233L;

  public QueuePanel(DownloadEngine engine) {
    engine.addListener(new QueuePanelDownloadListener());
  }

  @Override
  protected String createEmptyMessage() {
    return "No jobs queued";
  }

  @Override
  protected QueueJobPanel createItemPanel(DownloadJob job) {
    QueueJobPanel jobPanel = new QueueJobPanel(job);
    jobPanel.setBorder(Borders.createEmptyBorder("1dlu, 0, 2dlu, 4dlu"));
    return jobPanel;
  }

  // ---------------------------------------------------------------------------
  // --- Inner classes ---------------------------------------------------------
  // ---------------------------------------------------------------------------

  class QueuePanelDownloadListener extends DownloadListenerSkeleton {

    @Override
    public void jobScheduled(DownloadJob job) {
      QueuePanel.this.insertItem(job);
    }

    @Override
    public void jobStarted(DownloadJob job) {
      QueuePanel.this.removeItem(job);
    }

    @Override
    public void jobCancelled(DownloadJob job) {
      QueuePanel.this.removeItem(job);
    }

  }

}