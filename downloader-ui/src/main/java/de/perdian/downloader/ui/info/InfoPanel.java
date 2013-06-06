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

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.perdian.apps.downloader.core.DownloadEngine;
import de.perdian.apps.downloader.core.DownloadListenerSkeleton;

/**
 * Displays information about the configured state of the engine itself and
 * options to alter the configuration of the engine at runtime
 *
 * @author Christian Robert
 */

public class InfoPanel extends JPanel {

  static final long serialVersionUID = 1L;

  public InfoPanel(final DownloadEngine engine) {

    final JSpinner processorCountSpinner = new JSpinner(new SpinnerNumberModel(engine.getProcessorCount(), 1, Integer.MAX_VALUE, 1));
    processorCountSpinner.getModel().addChangeListener(new ChangeListener() {
      @Override public void stateChanged(ChangeEvent e) {
        new Thread(new Runnable() {
          @Override public void run() {
            engine.setProcessorCount(((Number)processorCountSpinner.getValue()).intValue());
          }
        }).start();
      }
    });
    engine.addListener(new DownloadListenerSkeleton() {
      @Override public void processorCountUpdated(final int newProcessorCount) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override public void run() {
            processorCountSpinner.setValue(Integer.valueOf(newProcessorCount));
          }
        });
      }
    });

    CellConstraints cc = new CellConstraints();
    FormLayout layout = new FormLayout(
      /* COLS */ "fill:default:grow, 6dlu, fill:min(75px;default)",
      /* ROWS */ "pref"
    );
    PanelBuilder builder = new PanelBuilder(layout, this);
    builder.addLabel("Processors", cc.xywh(1, 1, 1, 1));
    builder.add(processorCountSpinner, cc.xywh(3, 1, 1, 1));

  }

}