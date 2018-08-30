/*
 * Copyright 2013-2018 Christian Robert
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
package de.perdian.apps.downloader.fx.support.components;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.downloader.core.support.StreamFactory;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public class PreviewImagePane extends BorderPane {

    static final Logger log = LoggerFactory.getLogger(PreviewImagePane.class);
    static final ExecutorService IMAGE_LOADER_THREADPOOL = Executors.newFixedThreadPool(3);

    private Label imageLabel = null;
    private int imageWidth = 0;
    private int imageHeight = 0;

    public PreviewImagePane(StreamFactory previewImageFactory, int width, int height) {

        Label iconLabel = new Label(previewImageFactory == null ? "No image" : "Loading...");
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setMinSize(width, height);
        iconLabel.setMaxSize(width, height);

        this.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        this.setCenter(iconLabel);
        this.setImageLabel(iconLabel);
        this.setImageWidth(width);
        this.setImageHeight(height);
        this.setStreamFactory(previewImageFactory);

    }

    public void setStreamFactory(StreamFactory streamFactory) {
        if (streamFactory != null) {
            IMAGE_LOADER_THREADPOOL.execute(() -> {
                try {
                    try (InputStream previewStream = streamFactory.openStream()) {
                        if (previewStream != null) {
                            Image previewImage = new Image(previewStream, this.getImageWidth(), this.getImageHeight(), true, true);
                            Platform.runLater(() -> {
                                this.getImageLabel().setText(null);
                                this.getImageLabel().setGraphic(new ImageView(previewImage));
                            });
                        }
                    }
                } catch (Exception e) {
                    log.warn("Cannot load preview image for steam factory: " + streamFactory, e);
                }
            });
        }
    }

    private Label getImageLabel() {
        return this.imageLabel;
    }
    private void setImageLabel(Label imageLabel) {
        this.imageLabel = imageLabel;
    }

    public int getImageWidth() {
        return this.imageWidth;
    }
    private void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return this.imageHeight;
    }
    private void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

}
