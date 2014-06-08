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
package de.perdian.downloader.ui.fx;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import de.perdian.apps.downloader.core.DownloadEngine;
import de.perdian.downloader.ui.fx.panels.ActiveDownloadsPane;
import de.perdian.downloader.ui.fx.panels.EngineInformationPane;
import de.perdian.downloader.ui.fx.panels.QueuedDownloadsPane;

/**
 * Central entry point for creating a Downloader GUI that is based upon (or more
 * precisely is indented to be used in) a JavaFX application
 *
 * @author Christian Robert
 */

public class DownloadFx {

    /**
     * Creates the main pane in which all information for the given engine are
     * contained
     *
     * @param engine
     *     the engine for which to create the pane
     * @return
     *    the result
     */
    public static Parent createEnginePane(DownloadEngine engine) {

        ActiveDownloadsPane activeDownloadsPane = new ActiveDownloadsPane(engine);
        EngineInformationPane engineInformationPane  = new EngineInformationPane(engine);
        QueuedDownloadsPane queuedDownloadsPane = new QueuedDownloadsPane(engine);

        TitledPane rightTopPane = new TitledPane("Engine information", engineInformationPane);
        rightTopPane.setCollapsible(false);
        rightTopPane.setMaxHeight(Double.MAX_VALUE);
        TitledPane rightCenterPane = new TitledPane("Queued downloads", queuedDownloadsPane);
        rightCenterPane.setCollapsible(false);
        rightCenterPane.setMaxHeight(Double.MAX_VALUE);
        BorderPane rightPane = new BorderPane(rightCenterPane, rightTopPane, null, null, null);
        rightPane.setMaxHeight(Double.MAX_VALUE);

        TitledPane centerPane = new TitledPane("Active downloads", activeDownloadsPane);
        centerPane.setCollapsible(false);
        centerPane.setMaxHeight(Double.MAX_VALUE);

        BorderPane.setMargin(rightTopPane, new Insets(0, 0, 5, 0));
        BorderPane.setMargin(rightCenterPane, new Insets(5, 0, 0, 0));
        BorderPane.setMargin(centerPane, new Insets(0, 5, 0, 0));
        BorderPane.setMargin(rightPane, new Insets(0, 0, 0, 5));

        BorderPane rootPane = new BorderPane();
        rootPane.setCenter(centerPane);
        rootPane.setRight(rightPane);
        rootPane.setPadding(new Insets(10, 10, 10, 10));
        return rootPane;

    }

}