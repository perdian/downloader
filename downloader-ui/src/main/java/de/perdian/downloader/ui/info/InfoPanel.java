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
package de.perdian.downloader.ui.info;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.perdian.apps.downloader.core.DownloadEngine;
import de.perdian.apps.downloader.core.DownloadListenerSkeleton;
import de.perdian.downloader.ui.resources.Icons;

/**
 * Displays information about the configured state of the engine itself and
 * options to alter the configuration of the engine at runtime
 *
 * @author Christian Robert
 */

public class InfoPanel extends JPanel {

  static final long serialVersionUID = 1L;

  private DownloadEngine myEngine = null;
  private JLabel myProcessorInfoLabel = null;
  private JButton myProcessorMinusButton = null;
  private JButton myProcessorPlusButton = null;

  public InfoPanel(final DownloadEngine engine) {
    this.setEngine(engine);
    engine.addListener(new InfoPanelDownloadListener());

    JLabel processorInfoLabel = new JLabel(String.valueOf(engine.getProcessorCount()), SwingConstants.CENTER);
    processorInfoLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), Borders.createEmptyBorder("1dlu, 1dlu, 1dlu, 1dlu")));
    this.setProcessorInfoLabel(processorInfoLabel);
    JButton processorMinusButton = new JButton(new ProcessorUpdateAction(-1));
    processorMinusButton.setEnabled(engine.getProcessorCount() > 1);
    processorMinusButton.setIcon(Icons.createIcon("16/minus.png"));
    this.setProcessorMinusButton(processorMinusButton);
    JButton processorPlusButton = new JButton(new ProcessorUpdateAction(1));
    processorPlusButton.setIcon(Icons.createIcon("16/plus.png"));
    this.setProcessorPlusButton(processorPlusButton);

    CellConstraints cc = new CellConstraints();
    FormLayout processorInfoLayout = new FormLayout("pref, 1dlu, fill:30px:grow, 1dlu, pref", "fill:pref");
    processorInfoLayout.setColumnGroups(new int[][] { { 1, 5 } });
    PanelBuilder processorInfoBuilder = new PanelBuilder(processorInfoLayout);
    processorInfoBuilder.add(processorMinusButton, cc.xy(1, 1));
    processorInfoBuilder.add(processorInfoLabel, cc.xy(3, 1));
    processorInfoBuilder.add(processorPlusButton, cc.xy(5, 1));

    FormLayout layout = new FormLayout(
      /* COLS */ "fill:default:grow, 6dlu, fill:min(100px;pref)",
      /* ROWS */ "pref"
    );
    PanelBuilder builder = new PanelBuilder(layout, this);
    builder.addLabel("Processors", cc.xywh(1, 1, 1, 1));
    builder.add(processorInfoBuilder.getPanel(), cc.xywh(3, 1, 1, 1));

  }

  // ---------------------------------------------------------------------------
  // --- Inner classes ---------------------------------------------------------
  // ---------------------------------------------------------------------------

  class InfoPanelDownloadListener extends DownloadListenerSkeleton {

    @Override
    public void processorCountUpdated(final int newProcessorCount) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override public void run() {
          InfoPanel.this.getProcessorInfoLabel().setText(String.valueOf(newProcessorCount));
          InfoPanel.this.getProcessorMinusButton().setEnabled(newProcessorCount > 1);
          InfoPanel.this.getProcessorPlusButton().setEnabled(newProcessorCount < Integer.MAX_VALUE - 1);
        }
      });
    }

  }

  class ProcessorUpdateAction extends AbstractAction {

    static final long serialVersionUID = 1L;

    private int myDirection = 0;

    public ProcessorUpdateAction(int direction) {
      this.setDirection(direction);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      new Thread(new Runnable() {
        @Override public void run() {
          int newProcessorCount = InfoPanel.this.getEngine().getProcessorCount() + ProcessorUpdateAction.this.getDirection();
          InfoPanel.this.getEngine().setProcessorCount(Math.max(1, newProcessorCount));
        }
      }).start();
    }

    // ---------------------------------------------------------------------------
    // --- Property access methods -----------------------------------------------
    // ---------------------------------------------------------------------------

    int getDirection() {
      return this.myDirection;
    }
    private void setDirection(int direction) {
      this.myDirection = direction;
    }

  }

  // ---------------------------------------------------------------------------
  // --- Property access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  DownloadEngine getEngine() {
    return this.myEngine;
  }
  private void setEngine(DownloadEngine engine) {
    this.myEngine = engine;
  }

  JLabel getProcessorInfoLabel() {
    return this.myProcessorInfoLabel;
  }
  private void setProcessorInfoLabel(JLabel processorInfoLabel) {
    this.myProcessorInfoLabel = processorInfoLabel;
  }

  JButton getProcessorMinusButton() {
    return this.myProcessorMinusButton;
  }
  private void setProcessorMinusButton(JButton processorMinusButton) {
    this.myProcessorMinusButton = processorMinusButton;
  }

  JButton getProcessorPlusButton() {
    return this.myProcessorPlusButton;
  }
  private void setProcessorPlusButton(JButton processorPlusButton) {
    this.myProcessorPlusButton = processorPlusButton;
  }

}
