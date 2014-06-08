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
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

class AbstractItemContainerPane<T extends Node> extends BorderPane {

    private VBox itemBox = null;

    AbstractItemContainerPane() {

        VBox itemBox = new VBox(5d);

        ScrollPane scrollPane = new ScrollPane(itemBox);
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        this.setCenter(scrollPane);
        this.setItemBox(itemBox);
        this.setMinSize(100, 200);
        this.setMaxWidth(Double.MAX_VALUE);

    }

    void addItemPanel(T itemPanel) {
        Platform.runLater(() -> this.getItemBox().getChildren().add(itemPanel));
    }

    void removeItemPanel(T itemPanel) {
        Platform.runLater(() -> this.getItemBox().getChildren().remove(itemPanel));
    }

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    private VBox getItemBox() {
        return this.itemBox;
    }
    private void setItemBox(VBox itemBox) {
        this.itemBox = itemBox;
    }

}