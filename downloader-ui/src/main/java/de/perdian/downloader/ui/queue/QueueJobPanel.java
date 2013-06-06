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

import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.perdian.apps.downloader.core.DownloadJob;

/**
 * Represents a single job within the queue panel
 *
 * @author Christian Robert
 */

class QueueJobPanel extends JPanel {

  static final long serialVersionUID = 1L;

  private DownloadJob myJob = null;

  QueueJobPanel(DownloadJob job) {
    this.setJob(job);

    CellConstraints cc = new CellConstraints();
    FormLayout layout = new FormLayout(
      /* COLS */ "fill:default, 4dlu, fill:0px:grow",
      /* ROWS */ "pref"
    );
    PanelBuilder builder = new PanelBuilder(layout, this);
    builder.setBorder(Borders.createEmptyBorder("2dlu, 0, 2dlu, 0"));
    builder.addLabel(job.getRequest().getTitle() == null ? "No title" : job.getRequest().getTitle(), cc.xywh(3, 1, 1, 1));

  }

  // ---------------------------------------------------------------------------
  // --- Property access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  DownloadJob getJob() {
    return this.myJob;
  }
  private void setJob(DownloadJob job) {
    this.myJob = job;
  }

}