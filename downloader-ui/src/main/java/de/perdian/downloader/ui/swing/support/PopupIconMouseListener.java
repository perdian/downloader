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
package de.perdian.downloader.ui.swing.support;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.perdian.apps.downloader.core.DownloadJob;
import de.perdian.apps.downloader.core.DownloadStreamFactory;
import de.perdian.downloader.ui.swing.DownloadGuiHelper;

public class PopupIconMouseListener extends MouseAdapter {

  private static final Logger log = LogManager.getLogger(PopupIconMouseListener.class);

  private DownloadJob myJob = null;

  public PopupIconMouseListener(DownloadJob job) {
    this.setJob(job);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    try {
      final DownloadStreamFactory previewImageFactory = this.getJob().getRequest().getPreviewImageFactory();
      if(e.getClickCount() > 0 && previewImageFactory != null) {
        final Point mouseLocation = e.getLocationOnScreen();
        Thread previewLoaderThread = new Thread(new Runnable() {
          @Override public void run() {
            JPanel waitingPanel = PopupIconMouseListener.this.createLoadingPanel();
            JDialog previewDialog = new JDialog();
            previewDialog.setTitle(PopupIconMouseListener.this.getJob().getRequest().getTitle());
            previewDialog.setContentPane(waitingPanel);
            previewDialog.pack();
            previewDialog.setLocation(mouseLocation.x - 50, mouseLocation.y - 50);
            previewDialog.setVisible(true);
            PopupIconMouseListener.this.updatePreviewImage(previewImageFactory, previewDialog);
          }
        });
        previewLoaderThread.setName("PreviewWindow[" + this.getJob() + "]");
        previewLoaderThread.start();
      }
    } catch(Exception exception) {
      // Ignore here
    }
  }

  JPanel createLoadingPanel() {
    JLabel waitingLabel = new JLabel("Loading preview...");
    waitingLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    JPanel waitingPanel = new JPanel(new BorderLayout());
    waitingLabel.addMouseListener(new DisposeParentDialogListener());
    waitingPanel.add(waitingLabel, BorderLayout.CENTER);
    return waitingPanel;
  }

  void updatePreviewImage(DownloadStreamFactory previewFactory, final JDialog targetDialog) {
    try {
      try(InputStream previewInputStream = new BufferedInputStream(previewFactory.openStream())) {
        final BufferedImage image = ImageIO.read(previewInputStream);
        final JLabel imageLabel = new JLabel(new ImageIcon(image));
        imageLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        imageLabel.addMouseListener(new DisposeParentDialogListener());
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            targetDialog.setContentPane(imageLabel);
            targetDialog.pack();
          }
        });
      }
    } catch(Exception e) {
      log.warn("Cannot load preview image from: " + this.getJob(), e);
      targetDialog.dispose();
    }
  }

  // ---------------------------------------------------------------------------
  // --- Inner classes -------------------------------------------------------
  // ---------------------------------------------------------------------------

  class DisposeParentDialogListener extends MouseAdapter {

    @Override
    public void mouseClicked(MouseEvent e) {
      try {
        if(e.getButton() != MouseEvent.BUTTON1) {
          PopupIconMouseListener.this.getJob().cancel("Cancelled from preview dialog");
        }
      } finally {
        DownloadGuiHelper.lookupParent(JDialog.class, e.getComponent()).dispose();;
      }
    }

  }

  // ---------------------------------------------------------------------------
  // --- Property access methods ---------------------------------------------
  // ---------------------------------------------------------------------------

  DownloadJob getJob() {
    return this.myJob;
  }
  private void setJob(DownloadJob job) {
    this.myJob = job;
  }

}