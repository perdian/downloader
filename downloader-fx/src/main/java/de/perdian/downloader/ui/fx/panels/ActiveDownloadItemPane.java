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

import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

class ActiveDownloadItemPane extends BorderPane {

    ActiveDownloadItemPane() {
        this.setMinWidth(50);
        this.setMinHeight(50);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setCenter(new Button("X"));
    }

}