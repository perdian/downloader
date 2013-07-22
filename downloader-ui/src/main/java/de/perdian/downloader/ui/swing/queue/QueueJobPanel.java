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
package de.perdian.downloader.ui.swing.queue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.perdian.apps.downloader.core.DownloadJob;
import de.perdian.downloader.ui.swing.DownloadGuiHelper;
import de.perdian.downloader.ui.swing.resources.Icons;
import de.perdian.downloader.ui.swing.support.LazyLoadingIconPanel;
import de.perdian.downloader.ui.swing.support.PopupIconMouseListener;

/**
 * Represents a single job within the queue panel
 *
 * @author Christian Robert
 */

class QueueJobPanel extends JPanel {

  static final long serialVersionUID = 1L;

  QueueJobPanel(final DownloadJob job) {

    LazyLoadingIconPanel iconPanel = new LazyLoadingIconPanel(job.getRequest().getPreviewImageFactory(), new Dimension(70, 60));
    iconPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    iconPanel.addMouseListener(new PopupIconMouseListener(job));

    JButton forceStartButton = new JButton(new AbstractAction() {
      static final long serialVersionUID = 1L;
      @Override public void actionPerformed(ActionEvent e) {
        ((JButton)e.getSource()).setEnabled(false);
        job.forceStart();
      }
    });
    forceStartButton.setFocusable(false);
    forceStartButton.setIcon(Icons.createIcon("16/play.png"));
    forceStartButton.setToolTipText("Force start");
    JButton cancelButton = new JButton(new AbstractAction() {
      static final long serialVersionUID = 1L;
      @Override public void actionPerformed(ActionEvent e) {
        ((JButton)e.getSource()).setEnabled(false);
        job.cancel("Removed by user from queue");
      }
    });
    cancelButton.setFocusable(false);
    cancelButton.setIcon(Icons.createIcon("16/cancel.png"));
    cancelButton.setToolTipText("Cancel");

    JTextField titleLabel = DownloadGuiHelper.createLabelField(job.getRequest().getTitle() == null ? "No title" : job.getRequest().getTitle());

    CellConstraints cc = new CellConstraints();
    FormLayout buttonLayout = new FormLayout("fill:pref:grow, 4dlu, fill:pref:grow", "fill:pref:grow");
    buttonLayout.setColumnGroups(new int[][] { { 1, 3 } });
    PanelBuilder buttonBuilder = new PanelBuilder(buttonLayout);
    buttonBuilder.add(forceStartButton, cc.xy(1, 1));
    buttonBuilder.add(cancelButton, cc.xy(3, 1));

    FormLayout layout = new FormLayout(
      /* COLS */ "fill:default, 4dlu, fill:0px:grow",
      /* ROWS */ "pref, 4dlu, fill:pref:grow"
    );
    PanelBuilder builder = new PanelBuilder(layout, this);
    builder.setBorder(Borders.createEmptyBorder("2dlu, 0, 2dlu, 0"));
    builder.add(iconPanel, cc.xywh(1, 1, 1, 3));
    builder.add(titleLabel, cc.xywh(3, 1, 1, 1));
    builder.add(buttonBuilder.getPanel(), cc.xywh(3, 3, 1, 1));

  }

}