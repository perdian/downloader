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

import java.net.URL;
import java.util.function.Consumer;

import de.perdian.apps.downloader.core.engine.DownloadEngine;
import de.perdian.apps.downloader.fx.ActiveOperationsPane;
import de.perdian.apps.downloader.fx.FailedOperationsPane;
import de.perdian.apps.downloader.fx.InputDragAndDropPane;
import de.perdian.apps.downloader.fx.InputManualPane;
import de.perdian.apps.downloader.fx.QueuedRequestsPane;
import javafx.geometry.Insets;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class DownloaderPane extends GridPane {

    public DownloaderPane(DownloadEngine engine, DownloaderConfiguration configuration, Consumer<URL> urlConsumer) {

        InputManualPane inputManualPane = new InputManualPane(urlConsumer);
        inputManualPane.setPadding(new Insets(8, 8, 8, 8));
        TitledPane inputManualTitledPane = new TitledPane("Manual input", inputManualPane);
        inputManualTitledPane.setCollapsible(false);

        InputDragAndDropPane inputDragAndDropPane = new InputDragAndDropPane(urlConsumer);
        inputDragAndDropPane.setPadding(new Insets(8, 8, 8, 8));
        TitledPane inputDragAndDropTitledPane = new TitledPane("Drag and drop target", inputDragAndDropPane);
        inputDragAndDropTitledPane.setPrefSize(300, 300);
        inputDragAndDropTitledPane.setMinSize(300, 200);
        inputDragAndDropTitledPane.setCollapsible(false);
        inputDragAndDropTitledPane.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(inputDragAndDropTitledPane, Priority.ALWAYS);

        DownloaderSettingsPane settingsPane = new DownloaderSettingsPane(configuration);
        settingsPane.setMinSize(300, 200);
        settingsPane.setPrefSize(300, 200);
        TitledPane settingsTitledPane = new TitledPane("Settings", settingsPane);
        settingsTitledPane.setCollapsible(false);

        QueuedRequestsPane queuedRequestsPane = new QueuedRequestsPane(engine);
        queuedRequestsPane.setMinWidth(300);
        queuedRequestsPane.setPrefWidth(300);
        TitledPane queuedRequestsTitledPane = new TitledPane("Queued downloads", queuedRequestsPane);
        queuedRequestsTitledPane.setCollapsible(false);
        queuedRequestsTitledPane.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(queuedRequestsTitledPane, Priority.ALWAYS);

        FailedOperationsPane failedOperationsPane = new FailedOperationsPane(engine);
        failedOperationsPane.setMinSize(300, 200);
        failedOperationsPane.setPrefSize(300, 200);
        TitledPane failedOperationsTitledPane = new TitledPane("Failed downloads", failedOperationsPane);
        failedOperationsTitledPane.setCollapsible(false);
        failedOperationsTitledPane.setMaxHeight(Double.MAX_VALUE);

        ActiveOperationsPane activeOperationsPane = new ActiveOperationsPane(engine);
        TitledPane activeOperationsTitledPane = new TitledPane("Active downloads", activeOperationsPane);
        activeOperationsTitledPane.setCollapsible(false);
        activeOperationsTitledPane.setMaxHeight(Double.MAX_VALUE);
        activeOperationsTitledPane.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(activeOperationsTitledPane, Priority.ALWAYS);
        GridPane.setVgrow(activeOperationsTitledPane, Priority.ALWAYS);

        GridPane inputAndSettingsPane = new GridPane();
        inputAndSettingsPane.setVgap(8);
        inputAndSettingsPane.add(inputManualTitledPane, 0, 0, 1, 1);
        inputAndSettingsPane.add(inputDragAndDropTitledPane, 0, 1, 1, 1);
        inputAndSettingsPane.add(settingsTitledPane, 0, 2, 1, 1);

        GridPane queuedRequestsAndFailedOperationsPane = new GridPane();
        queuedRequestsAndFailedOperationsPane.setVgap(8);
        queuedRequestsAndFailedOperationsPane.setHgap(8);
        queuedRequestsAndFailedOperationsPane.add(queuedRequestsTitledPane, 0, 0, 1, 1);
        queuedRequestsAndFailedOperationsPane.add(failedOperationsTitledPane, 0, 1, 1, 1);

        this.add(inputAndSettingsPane, 0, 0, 1, 1);
        this.add(queuedRequestsAndFailedOperationsPane, 1, 0, 1, 1);
        this.add(activeOperationsTitledPane, 2, 0, 1, 1);
        this.setPadding(new Insets(8, 8, 8, 8));
        this.setHgap(8);

    }

}
