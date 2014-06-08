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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import de.perdian.apps.downloader.core.DownloadJob;
import de.perdian.apps.downloader.core.DownloadProgressListener;
import de.perdian.downloader.ui.fx.DownloadFxUtils;

class ActiveDownloadItemPane extends GridPane {

    private static final int ICON_WIDTH = 120;
    private static final int ICON_HEIGHT = 100;

    private Label progressBytesLabel = null;
    private Label progressTimeLabel = null;
    private ProgressBar progressBar = null;

    ActiveDownloadItemPane(DownloadJob job) {

        Label iconLabel = new Label("Loading...");
        iconLabel.setOnMouseClicked(event -> ImagePreviewPopup.handleMouseClickedEvent(event, job, this.getScene()));
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setMinSize(ICON_WIDTH, ICON_HEIGHT);
        iconLabel.setMaxSize(ICON_WIDTH, ICON_HEIGHT);
        DownloadFxUtils.loadImageInBackground(iconLabel, job, ICON_WIDTH, ICON_HEIGHT, false);
        GridPane.setMargin(iconLabel, new Insets(0, 4, 0, 0));

        Label titleLabel = new Label(job.getRequest().getTitle());
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(titleLabel, Priority.ALWAYS);

        Label fileNameLabel = new Label("File: " + job.getTargetFile().toString());
        fileNameLabel.setMaxWidth(Double.MAX_VALUE);
        GridPane.setMargin(fileNameLabel, new Insets(0, 0, 4, 0));

        ProgressBar progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setMaxHeight(Double.MAX_VALUE);
        GridPane.setHgrow(progressBar, Priority.ALWAYS);
        GridPane.setVgrow(progressBar, Priority.ALWAYS);
        GridPane.setMargin(progressBar, new Insets(0, 0, 4, 0));
        this.setProgressBar(progressBar);

        Button cancelButton = new Button("Cancel", new ImageView(new Image(this.getClass().getResourceAsStream("/de/perdian/downloader/ui/fx/resources/16/cancel.png"))));
        cancelButton.setMaxHeight(Double.MAX_VALUE);
        cancelButton.setOnAction(action -> {
            cancelButton.setDisable(true);
            job.cancel("Cancelled by user");
        });
        GridPane.setVgrow(cancelButton, Priority.ALWAYS);
        GridPane.setMargin(cancelButton, new Insets(0, 0, 0, 4));

        Label progressBytesLabel = new Label("");
        progressBytesLabel.setMaxWidth(Double.MAX_VALUE);
        progressBytesLabel.setAlignment(Pos.CENTER_LEFT);
        GridPane.setHgrow(progressBytesLabel, Priority.ALWAYS);
        this.setProgressBytesLabel(progressBytesLabel);

        Label progressTimeLabel = new Label("");
        progressTimeLabel.setMaxWidth(Double.MAX_VALUE);
        progressTimeLabel.setAlignment(Pos.CENTER_RIGHT);
        GridPane.setHgrow(progressTimeLabel, Priority.ALWAYS);
        this.setProgressTimeLabel(progressTimeLabel);

        HBox progressInfoBox = new HBox(progressBytesLabel, progressTimeLabel);
        HBox.setHgrow(progressTimeLabel, Priority.ALWAYS);
        HBox.setHgrow(progressBytesLabel, Priority.ALWAYS);

        this.add(iconLabel, 1, 1, 1, 4);
        this.add(titleLabel, 2, 1, 2, 1);
        this.add(fileNameLabel, 2, 2, 2, 1);
        this.add(progressBar, 2, 3, 1, 1);
        this.add(cancelButton, 3, 3, 1, 2);
        this.add(progressInfoBox, 2, 4, 1, 1);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setPadding(new Insets(2, 10, 2, 10));

        job.addProgressListener(new DownloadProgressListenerImpl());

    }

    // -------------------------------------------------------------------------
    // --- Inner classes -------------------------------------------------------
    // -------------------------------------------------------------------------

    class DownloadProgressListenerImpl implements DownloadProgressListener {

        @Override
        public void onProgress(DownloadJob job, long bytesWritten, long totalBytes) {

            if (totalBytes > 0) {

                final StringBuilder remainingInfo = new StringBuilder();
                long startTime = job.getStartTime();
                long runningTime = System.currentTimeMillis() - startTime;
                if (runningTime > 1000 * 5) {
                    double bytesPerCompleteTime = bytesWritten;
                    double timePerByte = runningTime / bytesPerCompleteTime;
                    double timeRemaining = (totalBytes - bytesWritten) * timePerByte;
                    double secondsRemaining = timeRemaining / 1000d;
                    remainingInfo.append("Remaining: ");
                    if (secondsRemaining > 60) {
                        NumberFormat numberFormat = new DecimalFormat("#,##0.0");
                        remainingInfo.append(numberFormat.format(secondsRemaining / 60)).append(" min");
                    } else {
                        NumberFormat numberFormat = new DecimalFormat("#,##0");
                        remainingInfo.append(numberFormat.format(secondsRemaining)).append(" sec");
                    }
                }

                final NumberFormat fileSizeFormat = new DecimalFormat("#,##0");
                final StringBuilder progressInfo = new StringBuilder();
                progressInfo.append(fileSizeFormat.format(bytesWritten / 1024)).append(" KiB");
                progressInfo.append(" / ").append(fileSizeFormat.format(totalBytes / 1024)).append(" KiB");

                double unitValue = 100d / totalBytes;
                double progressValue = unitValue * bytesWritten;

                Platform.runLater(() -> {
                    ActiveDownloadItemPane.this.getProgressBar().setProgress(progressValue * 0.01);
                    ActiveDownloadItemPane.this.getProgressBytesLabel().setText(progressInfo.toString());
                    if (remainingInfo.length() > 0) {
                        ActiveDownloadItemPane.this.getProgressTimeLabel().setText(remainingInfo.toString());
                    }
                });

            }

        }

    }

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    ProgressBar getProgressBar() {
        return this.progressBar;
    }
    private void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    Label getProgressBytesLabel() {
        return this.progressBytesLabel;
    }
    private void setProgressBytesLabel(Label progressBytesLabel) {
        this.progressBytesLabel = progressBytesLabel;
    }

    Label getProgressTimeLabel() {
        return this.progressTimeLabel;
    }
    private void setProgressTimeLabel(Label progressTimeLabel) {
        this.progressTimeLabel = progressTimeLabel;
    }

}