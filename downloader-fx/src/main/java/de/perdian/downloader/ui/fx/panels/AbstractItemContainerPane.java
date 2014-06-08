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

import java.util.IdentityHashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import de.perdian.apps.downloader.core.DownloadJob;

abstract class AbstractItemContainerPane<T extends Node> extends BorderPane {

    private Map<DownloadJob, T> itemPaneByJob = null;
    private VBox itemBox = null;

    AbstractItemContainerPane() {

        VBox itemBox = new VBox(5);
        itemBox.setPadding(new Insets(5, 0, 5, 0));

        ScrollPane scrollPane = new ScrollPane(itemBox);
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        this.setItemPaneByJob(new IdentityHashMap<>());
        this.setCenter(scrollPane);
        this.setItemBox(itemBox);
        this.setMinSize(100, 200);
        this.setMaxWidth(Double.MAX_VALUE);

    }


    protected void removeDownloadJob(DownloadJob job) {
        synchronized (job) {
            T itemPane = this.getItemPaneByJob().remove(job);
            if (itemPane != null) {
                Platform.runLater(() -> this.getItemBox().getChildren().remove(itemPane));
            }
        }
    }

    protected void addDownloadJob(DownloadJob job) {
        synchronized (job) {
            T itemPane = this.createItemPane(job);
            this.getItemPaneByJob().put(job, itemPane);
            Platform.runLater(() -> this.getItemBox().getChildren().add(itemPane));
        }
    }

    protected abstract T createItemPane(DownloadJob job);

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    private VBox getItemBox() {
        return this.itemBox;
    }
    private void setItemBox(VBox itemBox) {
        this.itemBox = itemBox;
    }

    private Map<DownloadJob, T> getItemPaneByJob() {
        return this.itemPaneByJob;
    }
    private void setItemPaneByJob(Map<DownloadJob, T> itemPaneByJob) {
        this.itemPaneByJob = itemPaneByJob;
    }

}