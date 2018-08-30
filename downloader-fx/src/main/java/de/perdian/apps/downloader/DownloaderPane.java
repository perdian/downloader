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
import de.perdian.apps.downloader.fx.engine.DownloadEngineActiveOperationsPane;
import de.perdian.apps.downloader.fx.engine.DownloadEngineQueuedRequestsPane;
import de.perdian.apps.downloader.fx.engine.DownloadEngineSettingsPane;
import de.perdian.apps.downloader.fx.input.InputDragAndDropPane;
import de.perdian.apps.downloader.fx.input.InputManualPane;
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

        DownloadEngineSettingsPane downloadEngineSettingsPane = new DownloadEngineSettingsPane(engine);
        downloadEngineSettingsPane.setPadding(new Insets(8, 8, 8, 8));
        TitledPane settingsTitledPane = new TitledPane("Settings", downloadEngineSettingsPane);
        settingsTitledPane.setCollapsible(false);

        DownloadEngineQueuedRequestsPane downloadEngineQueuedRequestsPane = new DownloadEngineQueuedRequestsPane(engine);
        downloadEngineQueuedRequestsPane.setMinWidth(300);
        downloadEngineQueuedRequestsPane.setPrefWidth(300);
        TitledPane downloadEngineQueuedRequestsTitledPane = new TitledPane("Queued downloads", downloadEngineQueuedRequestsPane);
        downloadEngineQueuedRequestsTitledPane.setCollapsible(false);
        downloadEngineQueuedRequestsTitledPane.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(downloadEngineQueuedRequestsTitledPane, Priority.ALWAYS);

        DownloadEngineActiveOperationsPane downloadEngineActiveOperationsPane = new DownloadEngineActiveOperationsPane(engine);
        TitledPane downloadEngineActiveOperationsTitledPane = new TitledPane("Active downloads", downloadEngineActiveOperationsPane);
        downloadEngineActiveOperationsTitledPane.setCollapsible(false);
        downloadEngineActiveOperationsTitledPane.setMaxHeight(Double.MAX_VALUE);
        downloadEngineActiveOperationsTitledPane.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(downloadEngineActiveOperationsTitledPane, Priority.ALWAYS);
        GridPane.setVgrow(downloadEngineActiveOperationsTitledPane, Priority.ALWAYS);

        this.add(inputManualTitledPane, 0, 0, 1, 1);
        this.add(inputDragAndDropTitledPane, 0, 1, 1, 1);
        this.add(settingsTitledPane, 0, 2, 1, 1);
        this.add(downloadEngineQueuedRequestsTitledPane, 1, 0, 1, 3);
        this.add(downloadEngineActiveOperationsTitledPane, 2, 0, 1, 3);
        this.setPadding(new Insets(8, 8, 8, 8));
        this.setHgap(8);
        this.setVgap(8);

    }

}
