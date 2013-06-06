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

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.perdian.apps.downloader.core.DownloadEngine;
import de.perdian.downloader.ui.info.InfoPanel;
import de.perdian.downloader.ui.progress.ProgressPanel;
import de.perdian.downloader.ui.queue.QueuePanel;

/**
 * The main panel in which a downloader is presented and from which the
 * interaction with the user is done
 *
 * @author Christian Robert
 */

public class DownloaderPanel extends JPanel {

  static final long serialVersionUID = 1L;

  public DownloaderPanel(DownloadEngine engine) {

    InfoPanel infoPanel = new InfoPanel(engine);
    infoPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(" Engine information "), Borders.createEmptyBorder("4dlu, 4dlu, 2dlu, 4dlu")));

    QueuePanel queuePanel = new QueuePanel(engine);
    queuePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(" Queued downloads "), Borders.createEmptyBorder("4dlu, 4dlu, 2dlu, 4dlu")));

    ProgressPanel progressPanel = new ProgressPanel(engine);
    progressPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(" Active downloads "), Borders.createEmptyBorder("4dlu, 4dlu, 2dlu, 4dlu")));

    CellConstraints cc = new CellConstraints();
    FormLayout layout = new FormLayout(
      /* COLS */ "fill:0px:grow, 4dlu, fill:max(default;200px)",
      /* ROWS */ "fill:pref, 4dlu, fill:0px:grow"
    );
    PanelBuilder builder = new PanelBuilder(layout, this);
    builder.setDefaultDialogBorder();
    builder.add(progressPanel, cc.xywh(1, 1, 1, 3));
    builder.add(infoPanel, cc.xywh(3, 1, 1, 1));
    builder.add(queuePanel, cc.xywh(3, 3, 1, 1));

  }

}