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
package de.perdian.downloader.ui.fx.panels;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import de.perdian.apps.downloader.core.DownloadEngine;
import de.perdian.apps.downloader.core.DownloadListener;

public class EngineInformationPane extends VBox {

    public EngineInformationPane(DownloadEngine engine) {

        final int maxProcessorValue = 64;
        final Button minusButton = new Button(null, new ImageView(new Image(this.getClass().getResourceAsStream("/de/perdian/downloader/ui/fx/resources/16/minus.png"))));
        minusButton.setDisable(engine.getProcessorCount() <= 1);
        minusButton.setOnAction(action -> engine.setProcessorCount(Math.max(1, engine.getProcessorCount() - 1)));
        final Button plusButton = new Button(null, new ImageView(new Image(this.getClass().getResourceAsStream("/de/perdian/downloader/ui/fx/resources/16/plus.png"))));
        plusButton.setOnAction(action -> engine.setProcessorCount(Math.min(maxProcessorValue, engine.getProcessorCount() + 1)));
        plusButton.setDisable(engine.getProcessorCount() >= maxProcessorValue);
        final TextField valueField = new TextField(String.valueOf(engine.getProcessorCount()));
        valueField.setAlignment(Pos.CENTER);
        valueField.setMaxWidth(50);
        valueField.setEditable(false);
        final Label infoLabel = new Label("Processors");
        infoLabel.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(infoLabel, Priority.ALWAYS);

        GridPane processorCountPane = new GridPane();
        processorCountPane.setPadding(new Insets(5, 5, 5, 5));
        processorCountPane.add(infoLabel, 1, 1);
        processorCountPane.add(minusButton, 2, 1);
        processorCountPane.add(valueField, 3, 1);
        processorCountPane.add(plusButton, 4, 1);
        this.getChildren().add(processorCountPane);

        engine.addListener(new DownloadListener() {
            @Override public void onProcessorCountUpdated(int newProcessorCount) {
                Platform.runLater(() ->  {
                    valueField.setText(String.valueOf(newProcessorCount));
                    minusButton.setDisable(newProcessorCount <= 1);
                    plusButton.setDisable(newProcessorCount >= maxProcessorValue);
                });
            }
        });

    }

}