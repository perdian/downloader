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
package de.perdian.apps.downloader.fx;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import de.perdian.apps.downloader.core.engine.DownloadEngine;
import de.perdian.apps.downloader.core.engine.DownloadOperation;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.Stage;

class FailedOperationsDetailPane extends BorderPane {

    FailedOperationsDetailPane(DownloadOperation operation, List<DownloadOperation> allOperations, DownloadEngine engine) {

        Button closeButton = new Button("Close", new ImageView(new Image(this.getClass().getResourceAsStream("/icons/18/close.png"), 18, 18, true, true)));
        closeButton.setOnAction(event -> ((Stage)this.getScene().getWindow()).close());
        ButtonBar.setButtonData(closeButton, ButtonData.RIGHT);

        Button deleteButton = new Button("Delete", new ImageView(new Image(this.getClass().getResourceAsStream("/icons/18/delete.png"), 18, 18, true, true)));
        deleteButton.setOnAction(action -> {
            allOperations.remove(operation);
            closeButton.fire();
        });
        ButtonBar.setButtonData(deleteButton, ButtonData.LEFT);

        Button resubmitButton = new Button("Resubmit", new ImageView(new Image(this.getClass().getResourceAsStream("/icons/18/refresh.png"), 18, 18, true, true)));
        resubmitButton.setOnAction(action -> {
            allOperations.remove(operation);
            engine.submit(operation.getRequestWrapper().getRequest());
            closeButton.fire();
        });
        ButtonBar.setButtonData(resubmitButton, ButtonData.LEFT);

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(deleteButton, resubmitButton, closeButton);
        BorderPane.setMargin(buttonBar, new Insets(16, 0, 0, 0));

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z");
        GridPane centerPane = new GridPane();
        centerPane.setHgap(8);
        centerPane.setVgap(8);
        centerPane.add(new Label("Title"), 0, 0, 1, 1);
        centerPane.add(this.createTextField(operation.getRequestWrapper().getRequest().getTitle()), 1, 0, 1, 1);
        centerPane.add(new Label("Scheduled at"), 0, 1, 1, 1);
        centerPane.add(this.createTextField(dateTimeFormatter.format(operation.getRequestWrapper().getScheduledTime().atZone(ZoneId.systemDefault()))), 1, 1, 1, 1);
        centerPane.add(new Label("Started at"), 0, 2, 1, 1);
        centerPane.add(this.createTextField(dateTimeFormatter.format(operation.getStartTime().atZone(ZoneId.systemDefault()))), 1, 2, 1, 1);
        centerPane.add(new Label("Completed at"), 0, 3, 1, 1);
        centerPane.add(this.createTextField(dateTimeFormatter.format(operation.getEndTime().atZone(ZoneId.systemDefault()))), 1, 3, 1, 1);
        centerPane.add(new Label("Error"), 0, 4, 1, 1);
        centerPane.add(this.createTextArea(ExceptionUtils.getStackTrace(operation.getError()), 100), 1, 4, 1, 1);

        this.setCenter(centerPane);
        this.setBottom(buttonBar);
        this.setPadding(new Insets(16, 16, 16, 16));

    }

    private TextField createTextField(String content) {
        TextField textField = new TextField(content);
        textField.setEditable(false);
        textField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        return textField;
    }

    private TextArea createTextArea(String content, int prefHeight) {
        TextArea textArea = new TextArea(content);
        textArea.setFont(Font.font("Courier New", 12));
        textArea.setEditable(false);
        textArea.setPrefHeight(prefHeight);
        textArea.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        return textArea;
    }

}
