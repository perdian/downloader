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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import de.perdian.apps.downloader.core.DownloadJob;
import de.perdian.downloader.ui.fx.DownloadFxUtils;

class QueuedDownloadItemPane extends GridPane {

    private static final int ICON_WIDTH = 70;
    private static final int ICON_HEIGHT = 60;

    private Label iconLabel = null;

    QueuedDownloadItemPane(DownloadJob job) {

        Label iconLabel = new Label();
        iconLabel.setOnMouseClicked(event -> ImagePreviewPopup.handleMouseClickedEvent(event, job, this.getScene()));
        iconLabel.setText("Loading...");
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setMinSize(ICON_WIDTH, ICON_HEIGHT);
        iconLabel.setMaxSize(ICON_WIDTH, ICON_HEIGHT);
        GridPane.setMargin(iconLabel, new Insets(0, 5, 0, 0));
        DownloadFxUtils.loadImageInBackground(iconLabel, job, ICON_WIDTH, ICON_HEIGHT, true);

        Label titleLabel = new Label(job.getRequest().getTitle());
        GridPane.setMargin(titleLabel, new Insets(0, 0, 5, 0));

        Button forceStartButton = new Button(null, new ImageView(new Image(this.getClass().getResourceAsStream("/de/perdian/downloader/ui/fx/resources/16/play.png"))));
        forceStartButton.setMaxWidth(Double.MAX_VALUE);
        forceStartButton.setMaxHeight(Double.MAX_VALUE);
        forceStartButton.setOnAction(action -> {
            forceStartButton.setDisable(true);
            Pane parentNode = (Pane)this.getParent();
            parentNode.getChildren().remove(this);
            job.forceStart();
        });
        GridPane.setHgrow(forceStartButton, Priority.ALWAYS);
        GridPane.setVgrow(forceStartButton, Priority.ALWAYS);
        GridPane.setMargin(forceStartButton, new Insets(0, 2, 0, 0));

        Button cancelButton = new Button(null, new ImageView(new Image(this.getClass().getResourceAsStream("/de/perdian/downloader/ui/fx/resources/16/cancel.png"))));
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setMaxHeight(Double.MAX_VALUE);
        cancelButton.setOnAction(action -> {
            cancelButton.setDisable(true);
            Pane parentNode = (Pane)this.getParent();
            parentNode.getChildren().remove(this);
            job.cancel("Cancelled by user");
        });
        GridPane.setHgrow(cancelButton, Priority.ALWAYS);
        GridPane.setVgrow(cancelButton, Priority.ALWAYS);
        GridPane.setMargin(cancelButton, new Insets(0, 0, 0, 2));

        this.setPadding(new Insets(2, 5, 2, 5));
        this.setIconLabel(iconLabel);
        this.add(iconLabel, 1, 1, 1, 2);
        this.add(titleLabel, 2, 1, 2, 1);
        this.add(forceStartButton, 2, 2, 1, 1);
        this.add(cancelButton, 3, 2, 1, 1);

    }

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    Label getIconLabel() {
        return this.iconLabel;
    }
    void setIconLabel(Label iconLabel) {
        this.iconLabel = iconLabel;
    }

}