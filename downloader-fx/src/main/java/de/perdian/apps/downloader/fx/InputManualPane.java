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

import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class InputManualPane extends GridPane {

    private Consumer<String> urlConsumer = null;

    public InputManualPane(Consumer<String> urlConsumer) {

        Button urlParseButton = new Button("Use URL");
        TextField urlField = new TextField();
        urlField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                urlParseButton.fire();
            }
        });
        urlParseButton.fire();
        urlParseButton.setOnAction(event -> this.getUrlConsumer().accept(urlField.getText()));
        GridPane.setHgrow(urlField, Priority.ALWAYS);

        this.add(new Label("Manually enter URL"), 0, 0, 2, 1);
        this.add(urlField, 0, 1, 1, 1);
        this.add(urlParseButton, 1, 1, 1, 1);
        this.setPadding(new Insets(0, 4, 0, 4));
        this.setHgap(2);

        this.setUrlConsumer(urlConsumer);

    }

    private Consumer<String> getUrlConsumer() {
        return this.urlConsumer;
    }
    private void setUrlConsumer(Consumer<String> urlConsumer) {
        this.urlConsumer = urlConsumer;
    }

}
