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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.downloader.core.DownloadStreamFactory;

/**
 * Special panel in which an icon is being loaded. The icon itself (its
 * underlying source) is accessed and loaded only if it is actually being
 * displayed on the screen
 *
 * @author Christian Robert
 */

public class LazyLoadingIconPanel extends JPanel {

  static final Logger log = LoggerFactory.getLogger(LazyLoadingIconPanel.class);
  static final long serialVersionUID = 1L;
  static final Executor DEFAULT_EXECUTOR = Executors.newFixedThreadPool(10);

  private DownloadStreamFactory myStreamFactory = null;
  private Executor myExecutor = null;
  private boolean stateLoadRequested = false;

  public LazyLoadingIconPanel(DownloadStreamFactory streamFactory, Dimension size) {
    this(streamFactory, size, DEFAULT_EXECUTOR);
  }

  public LazyLoadingIconPanel(DownloadStreamFactory streamFactory, Dimension size, Executor executor) {
    this.setLayout(new BorderLayout());
    this.setBackground(Color.WHITE);
    this.setExecutor(Objects.requireNonNull(executor, "Parameter 'executor' must not be null!"));
    this.setStreamFactory(streamFactory);
    this.setSize(Objects.requireNonNull(size, "Parameter 'size' must not be null!"));
    this.setPreferredSize(size);
    this.setMinimumSize(size);
    this.setMaximumSize(size);
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    synchronized(this) {
      if(!this.isLoadRequested()) {
        this.setLoadRequested(true);
        this.updateMainComponent(new JLabel("Loading...", SwingConstants.CENTER));
        this.getExecutor().execute(new LoadImageRunnable());
      }
    }
  }

  void updateImage(Image image) {
    this.updateMainComponent(new JLabel(new ImageIcon(image), SwingConstants.CENTER));
  }

  void updateMainComponent(final JComponent component) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run() {
        LazyLoadingIconPanel.this.removeAll();
        LazyLoadingIconPanel.this.add(component, BorderLayout.CENTER);
        LazyLoadingIconPanel.this.revalidate();
        LazyLoadingIconPanel.this.repaint();
      }
    });
  }

  // ---------------------------------------------------------------------------
  // --- Inner classes ---------------------------------------------------------
  // ---------------------------------------------------------------------------

  class LoadImageRunnable implements Runnable {

    @Override
    public void run() {
      DownloadStreamFactory streamFactory = LazyLoadingIconPanel.this.getStreamFactory();
      if(streamFactory == null) {
        LazyLoadingIconPanel.this.updateMainComponent(new JLabel("", SwingConstants.CENTER));
      } else {
        try(InputStream inStream = new BufferedInputStream(streamFactory.openStream())) {
          final Image image = ImageIO.read(inStream);
          final int imageWidth = image.getWidth(null);
          final int imageHeight = image.getHeight(null);
          final int targetWidth = LazyLoadingIconPanel.this.getSize().width;
          final int targetHeight = LazyLoadingIconPanel.this.getSize().height;
          if(imageWidth <= targetWidth && imageHeight <= targetHeight) {
            LazyLoadingIconPanel.this.updateImage(image);
          } else {

            // We need to scale the image to the target dimension, so that it
            // fits inside our area
            double widthRatio = (double)targetWidth / (double)imageWidth;
            double heightRatio = (double)targetHeight / (double)imageHeight;
            double useRatio = Math.min(widthRatio, heightRatio);
            int resultWidth = (int)(imageWidth * useRatio);
            int resultHeight = (int)(imageHeight * useRatio);
            LazyLoadingIconPanel.this.updateImage(image.getScaledInstance(resultWidth, resultHeight, Image.SCALE_SMOOTH));

          }
        } catch(Exception e) {
          log.debug("Cannot load image from: " + streamFactory, e);
          LazyLoadingIconPanel.this.updateMainComponent(new JLabel("Cannot load image", SwingConstants.CENTER));
        }
      }
    }

  }

  // ---------------------------------------------------------------------------
  // --- Property access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  DownloadStreamFactory getStreamFactory() {
    return this.myStreamFactory;
  }
  void setStreamFactory(DownloadStreamFactory streamFactory) {
    this.myStreamFactory = streamFactory;
  }

  private Executor getExecutor() {
    return this.myExecutor;
  }
  private void setExecutor(Executor executor) {
    this.myExecutor = executor;
  }

  private boolean isLoadRequested() {
    return this.stateLoadRequested;
  }
  private void setLoadRequested(boolean loadRequested) {
    this.stateLoadRequested = loadRequested;
  }

}