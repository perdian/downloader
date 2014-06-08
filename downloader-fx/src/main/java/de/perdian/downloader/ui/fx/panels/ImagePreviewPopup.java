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

import java.io.InputStream;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import de.perdian.apps.downloader.core.DownloadJob;

public class ImagePreviewPopup {

    public static void handleMouseClickedEvent(MouseEvent event, DownloadJob job, Scene parentScene) {
        if (job.getRequest().getPreviewImageFactory() != null) {
            ImagePreviewPopup.showPopupWindow(job, event.getScreenX(), event.getScreenY(), parentScene);
        }
    }

    private static void showPopupWindow(DownloadJob job, double x, double y, Scene parentScene) {

        Stage dialog = new Stage();
        dialog.setTitle("Details for job: " + job);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentScene.getWindow());
        dialog.setX(x - 50);
        dialog.setY(y - 50);

        Label imageLabel = new Label("Loading preview image...");
        imageLabel.setAlignment(Pos.CENTER);
        imageLabel.setMinSize(500, 400);
        imageLabel.setMaxSize(500, 400);

        new Thread(() -> {
            try (InputStream previewStream = job.getRequest().getPreviewImageFactory().openStream()) {
                Image image = new Image(previewStream);
                Platform.runLater(() -> {
                    imageLabel.setText("");
                    imageLabel.setGraphic(new ImageView(image));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    imageLabel.setText("Cannot load preview image");
                });
            }
        }).start();

        Label infoLabel = new Label(job.getRequest().getTitle());
        infoLabel.setAlignment(Pos.CENTER);
        infoLabel.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(infoLabel, new Insets(5, 5, 5, 5));

        VBox vBox = new VBox(imageLabel, infoLabel);
        Scene dialogScene = new Scene(vBox);
        dialog.setScene(dialogScene);
        imageLabel.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                job.cancel("Cancelled by user");
            }
            dialog.close();
        });
        dialog.show();

    }

}