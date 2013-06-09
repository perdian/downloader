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
package de.perdian.downloader.ui.progress;

import com.jgoodies.forms.factories.Borders;

import de.perdian.apps.downloader.core.DownloadEngine;
import de.perdian.apps.downloader.core.DownloadJob;
import de.perdian.apps.downloader.core.DownloadListenerSkeleton;
import de.perdian.downloader.ui.support.AbstractListPanel;

/**
 * Contains all the active downloads
 *
 * @author Christian Robert
 */

public class ProgressPanel extends AbstractListPanel<DownloadJob, ProgressJobPanel> {

  static final long serialVersionUID = 1L;

  public ProgressPanel(DownloadEngine engine) {
    engine.addListener(new ProgressPanelDownloadListener());
  }

  @Override
  protected String createEmptyMessage() {
    return "No downloads active";
  }

  @Override
  protected ProgressJobPanel createItemPanel(DownloadJob item) {
    ProgressJobPanel jobPanel = new ProgressJobPanel(item);
    jobPanel.setBorder(Borders.createEmptyBorder("2dlu, 0, 4dlu, 4dlu"));
    return jobPanel;
  }

  // ---------------------------------------------------------------------------
  // --- Inner classes ---------------------------------------------------------
  // ---------------------------------------------------------------------------

  class ProgressPanelDownloadListener extends DownloadListenerSkeleton {

    @Override
    public void onJobStarted(DownloadJob job) {
      ProgressPanel.this.insertItem(job);
    }

    @Override
    public void onJobCompleted(DownloadJob job) {
      ProgressPanel.this.removeItem(job);
    }

  }

}