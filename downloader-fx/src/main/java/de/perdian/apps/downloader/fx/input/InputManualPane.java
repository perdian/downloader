package de.perdian.apps.downloader.fx.input;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class InputManualPane extends GridPane {

    private static final Logger log = LoggerFactory.getLogger(InputManualPane.class);

    private Consumer<URL> urlConsumer = null;

    public InputManualPane(Consumer<URL> urlConsumer) {

        Button urlParseButton = new Button("Use URL");
        urlParseButton.setDisable(true);
        TextField urlField = new TextField();
        urlField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                urlParseButton.fire();
            }
        });
        urlParseButton.fire();
        urlField.textProperty().addListener((o, oldValue, newValue) -> {
            try {
                URL parsedUrl = new URL(newValue);
                log.trace("Parsed URL: {}", parsedUrl);
                urlParseButton.setDisable(false);
            } catch (MalformedURLException e) {
                urlParseButton.setDisable(true);
            }
        });
        urlParseButton.setOnAction(event -> {
            try {
                this.getUrlConsumer().accept(new URL(urlField.getText()));
            } catch (MalformedURLException e) {
                log.trace("Ignoreing invalid URL: " + urlField.getText());
            }
        });
        GridPane.setHgrow(urlField, Priority.ALWAYS);

        this.add(new Label("Manually enter URL"), 0, 0, 2, 1);
        this.add(urlField, 0, 1, 1, 1);
        this.add(urlParseButton, 1, 1, 1, 1);
        this.setPadding(new Insets(0, 4, 0, 4));
        this.setHgap(2);

        this.setUrlConsumer(urlConsumer);

    }

    private Consumer<URL> getUrlConsumer() {
        return this.urlConsumer;
    }
    private void setUrlConsumer(Consumer<URL> urlConsumer) {
        this.urlConsumer = urlConsumer;
    }

}
