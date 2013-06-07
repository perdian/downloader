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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.perdian.apps.downloader.core.DownloadJob;
import de.perdian.apps.downloader.core.DownloadProgressListener;
import de.perdian.downloader.ui.resources.Icons;
import de.perdian.downloader.ui.support.LazyLoadingIconPanel;

class ProgressJobPanel extends JPanel {

  static final long serialVersionUID = 1L;

  private JProgressBar myProgressBar = null;
  private JButton myCancelButton = null;
  private JLabel myProgressInfoLeftLabel = null;
  private JLabel myProgressInfoRightLabel = null;

  ProgressJobPanel(final DownloadJob job) {

    JComponent iconPanel = new LazyLoadingIconPanel(job.getRequest().getPreviewImageFactory(), new Dimension(110, 90));
    iconPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

    JLabel titleLabel = new JLabel(job.getRequest().getTitle() == null ? "No title" : job.getRequest().getTitle());
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));

    StringBuilder fileName = new StringBuilder();
    fileName.append("File: ").append(job.getTargetFile());
    JLabel fileNameLabel = new JLabel(fileName.toString());

    JProgressBar progressBar = new JProgressBar();
    progressBar.setIndeterminate(true);
    this.setProgressBar(progressBar);

    JButton cancelButton = new JButton(new AbstractAction("Cancel") {
      static final long serialVersionUID = 1L;
      @Override public void actionPerformed(ActionEvent e) {
        ProgressJobPanel.this.setEnabled(false);
        job.cancel();
      }
    });
    cancelButton.setIcon(Icons.createIcon("16/cancel.png"));
    cancelButton.setHorizontalTextPosition(SwingConstants.CENTER);
    cancelButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    cancelButton.setPreferredSize(new Dimension(70, 70));
    this.setCancelButton(cancelButton);

    CellConstraints cc = new CellConstraints();
    JLabel progressInfoLeftLabel = new JLabel(" ", SwingConstants.LEFT);
    this.setProgressInfoLeftLabel(progressInfoLeftLabel);
    JLabel progressInfoRightLabel = new JLabel(" ", SwingConstants.RIGHT);
    this.setProgressInfoRightLabel(progressInfoRightLabel);
    FormLayout progressInfoLayout = new FormLayout("fill:0px:grow, 2dlu, fill:0px:grow", "pref");
    progressInfoLayout.setColumnGroups(new int[][] { { 1, 3 } });
    JPanel progressInfoPanel = new JPanel(progressInfoLayout);
    progressInfoPanel.add(progressInfoLeftLabel, cc.xy(1, 1));
    progressInfoPanel.add(progressInfoRightLabel, cc.xy(3, 1));

    FormLayout layout = new FormLayout(
      /* COLS */ "fill:default, 4dlu, fill:0px:grow, 4dlu, pref",
      /* ROWS */ "pref, pref, 4dlu, fill:30px:grow, 1dlu, pref"
    );
    PanelBuilder builder = new PanelBuilder(layout, this);
    builder.add(iconPanel, cc.xywh(1, 1, 1, 6));
    builder.add(titleLabel, cc.xywh(3, 1, 3, 1));
    builder.add(fileNameLabel, cc.xywh(3, 2, 3, 1));
    builder.add(progressBar, cc.xywh(3, 4, 1, 1));
    builder.add(progressInfoPanel, cc.xywh(3, 6, 1, 1));
    builder.add(cancelButton, cc.xywh(5, 4, 1, 3));

    job.addProgressListener(new ProgressJobPanelProgressListener());

  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    this.getCancelButton().setEnabled(enabled);
    this.getProgressBar().setEnabled(enabled);
  }

  // ---------------------------------------------------------------------------
  // --- Property access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  class ProgressJobPanelProgressListener implements DownloadProgressListener {

    @Override
    public void progress(DownloadJob job, final long bytesWritten, final long totalBytes) {
      if(totalBytes > 0) {

        final StringBuilder remainingInfo = new StringBuilder();
        long startTime = job.getStartTime();
        long runningTime = System.currentTimeMillis() - startTime;
        if(runningTime > 1000 * 5) {
          double bytesPerCompleteTime = bytesWritten;
          double timePerByte = runningTime / bytesPerCompleteTime;
          double timeRemaining = (totalBytes - bytesWritten) * timePerByte;
          double secondsRemaining = timeRemaining / 1000d;
          remainingInfo.append("Remaining: ");
          if(secondsRemaining > 60) {
            NumberFormat numberFormat = new DecimalFormat("#,##0.0");
            remainingInfo.append(numberFormat.format(secondsRemaining / 60)).append(" min");
          } else {
            NumberFormat numberFormat = new DecimalFormat("#,##0");
            remainingInfo.append(numberFormat.format(secondsRemaining)).append(" sec");
          }
        }

        final NumberFormat fileSizeFormat = new DecimalFormat("#,##0");
        final StringBuilder progressInfo = new StringBuilder();
        progressInfo.append(fileSizeFormat.format(bytesWritten / 1024)).append(" KiB");
        progressInfo.append(" / ").append(fileSizeFormat.format(totalBytes / 1024)).append(" KiB");

        SwingUtilities.invokeLater(new Runnable() {
          @Override public void run() {

            JProgressBar progressBar = ProgressJobPanel.this.getProgressBar();
            progressBar.setIndeterminate(false);
            progressBar.setStringPainted(true);
            progressBar.setMaximum((int)totalBytes);
            progressBar.setValue((int)bytesWritten);
            progressBar.repaint();

            ProgressJobPanel.this.getProgressInfoLeftLabel().setText(progressInfo.toString());
            if(remainingInfo.length() > 0) {
              ProgressJobPanel.this.getProgressInfoRightLabel().setText(remainingInfo.toString());
            }

          }
        });

      }
    }

  }

  // ---------------------------------------------------------------------------
  // --- Property access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  JProgressBar getProgressBar() {
    return this.myProgressBar;
  }
  private void setProgressBar(JProgressBar progressBar) {
    this.myProgressBar = progressBar;
  }

  JButton getCancelButton() {
    return this.myCancelButton;
  }
  private void setCancelButton(JButton cancelButton) {
    this.myCancelButton = cancelButton;
  }

  JLabel getProgressInfoLeftLabel() {
    return this.myProgressInfoLeftLabel;
  }
  private void setProgressInfoLeftLabel(JLabel progressInfoLeftLabel) {
    this.myProgressInfoLeftLabel = progressInfoLeftLabel;
  }

  JLabel getProgressInfoRightLabel() {
    return this.myProgressInfoRightLabel;
  }
  private void setProgressInfoRightLabel(JLabel progressInfoRightLabel) {
    this.myProgressInfoRightLabel = progressInfoRightLabel;
  }

}
