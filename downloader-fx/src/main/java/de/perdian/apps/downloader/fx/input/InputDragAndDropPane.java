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
package de.perdian.apps.downloader.fx.input;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;

public class InputDragAndDropPane extends BorderPane {

    private static final Logger log = LoggerFactory.getLogger(InputDragAndDropPane.class);

    private Consumer<URL> urlConsumer = null;
    private Label statusLabel = null;

    public InputDragAndDropPane(Consumer<URL> urlConsumer) {

        Label infoLabel = new Label("Drag a URL into this area to initiate the download process");
        infoLabel.setWrapText(true);
        this.setTop(infoLabel);

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        this.setBottom(statusLabel);
        this.setStatusLabel(statusLabel);

        this.setOnDragOver(this::handleDragOver);
        this.setOnDragExited(this::handleDragExited);
        this.setUrlConsumer(urlConsumer);

    }

    private void handleDragOver(DragEvent dragEvent) {
        dragEvent.acceptTransferModes(TransferMode.COPY);
    }

    private void handleDragExited(DragEvent dragEvent) {
        if (dragEvent.getAcceptingObject() != null) {
            Dragboard dragboard = dragEvent.getDragboard();
            Object dragboardContent = dragboard.getContent(DataFormat.URL);
            if (dragboardContent != null) {
                this.handleUrlString(dragboardContent.toString());
            }
        }
    }

    private void handleUrlString(String urlString) {
        try {
            URL url = new URL(urlString);
            this.getStatusLabel().setText("Detected URL: " + url);
            log.info("Handling Drag&Drop for URL: " + url);
            this.getUrlConsumer().accept(url);
        } catch (MalformedURLException e) {
            this.getStatusLabel().setText("Cannot parse value into URL: " + urlString);
        }
    }

    private Consumer<URL> getUrlConsumer() {
        return this.urlConsumer;
    }
    private void setUrlConsumer(Consumer<URL> urlConsumer) {
        this.urlConsumer = urlConsumer;
    }

    private Label getStatusLabel() {
        return this.statusLabel;
    }
    private void setStatusLabel(Label statusLabel) {
        this.statusLabel = statusLabel;
    }

}
