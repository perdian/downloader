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

import java.util.Map;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

class DownloaderSettingsPane extends TabPane {

    DownloaderSettingsPane(DownloaderConfiguration configuration) {

        for (Map.Entry<String, Pane> settingsPaneEntry : configuration.getSettingsPanes().entrySet()) {

            BorderPane settingsPaneWrapper = new BorderPane(settingsPaneEntry.getValue());
            settingsPaneWrapper.setPadding(new Insets(8, 8, 8, 8));

            ScrollPane settingsPaneScroller = new ScrollPane(settingsPaneWrapper);
            settingsPaneScroller.setBorder(null);
            settingsPaneScroller.setFitToWidth(true);
            settingsPaneScroller.setHbarPolicy(ScrollBarPolicy.NEVER);
            settingsPaneScroller.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

            Tab settingsTab = new Tab(settingsPaneEntry.getKey(), settingsPaneScroller);
            settingsTab.setClosable(false);
            this.getTabs().add(settingsTab);

        }

    }

}
