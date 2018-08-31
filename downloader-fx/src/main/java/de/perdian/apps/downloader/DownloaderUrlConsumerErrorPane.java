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
package de.perdian.apps.downloader;

import org.apache.commons.lang3.exception.ExceptionUtils;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

class DownloaderUrlConsumerErrorPane extends BorderPane {

    DownloaderUrlConsumerErrorPane(Exception exception) {

        Button closeButton = new Button("Close", new ImageView(new Image(this.getClass().getResourceAsStream("/icons/18/close.png"), 18, 18, true, true)));
        closeButton.setOnAction(event -> ((Stage)this.getScene().getWindow()).close());
        BorderPane buttonPane = new BorderPane(closeButton);
        BorderPane.setMargin(buttonPane, new Insets(16, 0, 0, 0));

        GridPane centerPane = new GridPane();
        centerPane.setHgap(8);
        centerPane.setVgap(8);
        centerPane.add(this.createTextArea(ExceptionUtils.getStackTrace(exception), 100), 0, 0, 1, 1);

        this.setCenter(centerPane);
        this.setBottom(buttonPane);
        this.setPadding(new Insets(16, 16, 16, 16));

    }

    static void showAsPopup(Exception exception, Stage primaryStage) {
        DownloaderUrlConsumerErrorPane errorPane = new DownloaderUrlConsumerErrorPane(exception);
        Stage detailStage = new Stage();
        detailStage.setTitle("Cannot process URL");
        detailStage.initModality(Modality.WINDOW_MODAL);
        detailStage.initOwner(primaryStage);
        detailStage.setScene(new Scene(errorPane, 900, 400));
        detailStage.show();
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
