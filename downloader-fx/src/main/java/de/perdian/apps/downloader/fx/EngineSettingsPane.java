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

import java.io.File;
import java.nio.file.Path;

import de.perdian.apps.downloader.core.engine.DownloadEngine;
import de.perdian.apps.downloader.core.engine.DownloadEngineConfigurationListener;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

public class EngineSettingsPane extends VBox {

    public EngineSettingsPane(DownloadEngine engine) {

        int maxProcessorValue = 64;
        Button minusButton = new Button("–");
        minusButton.setPrefWidth(20);
        minusButton.setDisable(engine.getProcessorCount() <= 1);
        minusButton.setOnAction(action -> engine.setProcessorCount(Math.max(1, engine.getProcessorCount() - 1)));
        Button plusButton = new Button("+");
        plusButton.setPrefWidth(20);
        plusButton.setOnAction(action -> engine.setProcessorCount(Math.min(maxProcessorValue, engine.getProcessorCount() + 1)));
        plusButton.setDisable(engine.getProcessorCount() >= maxProcessorValue);
        TextField valueField = new TextField(String.valueOf(engine.getProcessorCount()));
        valueField.setAlignment(Pos.CENTER);
        valueField.setMaxWidth(50);
        valueField.setEditable(false);
        Label infoLabel = new Label("Download processors");
        infoLabel.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(infoLabel, Priority.ALWAYS);

        GridPane processorCountPane = new GridPane();
        processorCountPane.setHgap(2);
        processorCountPane.add(infoLabel, 1, 1);
        processorCountPane.add(minusButton, 2, 1);
        processorCountPane.add(valueField, 3, 1);
        processorCountPane.add(plusButton, 4, 1);
        this.getChildren().add(processorCountPane);

        Label targetDirectoryLabel = new Label("Target directory");
        TextField targetDirectoryField = new TextField(engine.getTargetDirectory().toFile().getAbsolutePath());
        targetDirectoryField.setEditable(false);
        Button targetDirectorySelectButton = new Button("Select");
        targetDirectorySelectButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(engine.getTargetDirectory().toFile());
            directoryChooser.setTitle("Select target directory");
            File targetDirectory = directoryChooser.showDialog(EngineSettingsPane.this.getScene().getWindow());
            if (targetDirectory != null && targetDirectory.exists()) {
                engine.setTargetDirectory(targetDirectory.toPath());
            }
        });
        GridPane.setHgrow(targetDirectoryField, Priority.ALWAYS);
        GridPane targetDirectoryPane = new GridPane();
        targetDirectoryPane.setHgap(2);
        targetDirectoryPane.setVgap(2);
        targetDirectoryPane.add(targetDirectoryLabel, 0, 0, 2, 1);
        targetDirectoryPane.add(targetDirectoryField, 0, 1, 1, 1);
        targetDirectoryPane.add(targetDirectorySelectButton, 1, 1, 1, 1);
        this.getChildren().add(targetDirectoryPane);

        this.setSpacing(4);

        engine.addEngineConfigurationListener(new DownloadEngineConfigurationListener() {
            @Override public void onProcessorCountUpdated(int newProcessorCount) {
                Platform.runLater(() ->  {
                    valueField.setText(String.valueOf(newProcessorCount));
                    minusButton.setDisable(newProcessorCount <= 1);
                    plusButton.setDisable(newProcessorCount >= maxProcessorValue);
                });
            }
            @Override public void onTargetDirectoryUpdated(Path newTargetDirectory) {
                Platform.runLater(() -> {
                    targetDirectoryField.setText(newTargetDirectory.toFile().getAbsolutePath());
                });
            }
        });

    }

}
