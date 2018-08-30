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
