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
package de.perdian.apps.downloader.fx.engine;

import java.util.IdentityHashMap;
import java.util.Map;

import de.perdian.apps.downloader.core.engine.DownloadEngine;
import de.perdian.apps.downloader.core.engine.DownloadOperation;
import de.perdian.apps.downloader.core.engine.DownloadRequestWrapper;
import de.perdian.apps.downloader.core.engine.DownloadSchedulingListener;
import de.perdian.apps.downloader.core.support.StreamFactory;
import de.perdian.apps.downloader.fx.support.components.PreviewImagePane;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DownloadEngineQueuedRequestsPane extends BorderPane implements DownloadSchedulingListener {

    private Map<DownloadRequestWrapper, DownloadEngineRequestWrapperPane> requestWrapperPanesByRequestWrapper = null;
    private VBox requestWrapperPanesBox = null;

    public DownloadEngineQueuedRequestsPane(DownloadEngine engine) {

        VBox requestWrapperPanesBox = new VBox(4);
        requestWrapperPanesBox.setPadding(new Insets(4, 0, 4, 0));

        ScrollPane scrollPane = new ScrollPane(requestWrapperPanesBox);
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        this.setRequestWrapperPanesByRequestWrapper(new IdentityHashMap<>());
        this.setRequestWrapperPanesBox(requestWrapperPanesBox);
        this.setCenter(scrollPane);

        engine.addSchedulingListener(this);

    }

    @Override
    public void onRequestScheduled(DownloadRequestWrapper requestWrapper) {
        synchronized (requestWrapper) {
            DownloadEngineQueuedRequestsPane.DownloadEngineRequestWrapperPane requestWrapperPane = new DownloadEngineQueuedRequestsPane.DownloadEngineRequestWrapperPane(requestWrapper);
            this.getRequestWrapperPanesByRequestWrapper().put(requestWrapper, requestWrapperPane);
            Platform.runLater(() -> this.getRequestWrapperPanesBox().getChildren().add(requestWrapperPane));
        }
    }

    @Override
    public void onRequestCancelled(DownloadRequestWrapper requestWrapper) {
        synchronized (requestWrapper) {
            DownloadEngineQueuedRequestsPane.DownloadEngineRequestWrapperPane requestWrapperPane = this.getRequestWrapperPanesByRequestWrapper().get(requestWrapper);
            if (requestWrapperPane != null) {
                Platform.runLater(() -> this.getRequestWrapperPanesBox().getChildren().remove(requestWrapperPane));
            }
        }
    }

    @Override
    public void onOperationStarting(DownloadOperation operation) {
        synchronized (operation.getRequestWrapper()) {
            DownloadEngineQueuedRequestsPane.DownloadEngineRequestWrapperPane requestWrapperPane = this.getRequestWrapperPanesByRequestWrapper().get(operation.getRequestWrapper());
            if (requestWrapperPane != null) {
                Platform.runLater(() -> this.getRequestWrapperPanesBox().getChildren().remove(requestWrapperPane));
            }
        }
    }

    static class DownloadEngineRequestWrapperPane extends GridPane {

        private PreviewImagePane imagePage = null;

        DownloadEngineRequestWrapperPane(DownloadRequestWrapper requestWrapper) {

            StreamFactory previewStreamFactory = requestWrapper.getRequest().getPreviewImageFactory();
            PreviewImagePane imagePane = new PreviewImagePane(previewStreamFactory, 70, 60);
            this.setImagePage(imagePane);
            GridPane.setMargin(imagePane, new Insets(0, 5, 0, 0));

            Label titleLabel = new Label(requestWrapper.getRequest().getTitle());
            GridPane.setMargin(titleLabel, new Insets(0, 0, 5, 0));

            Button forceStartButton = new Button(null, new ImageView(new Image(this.getClass().getResourceAsStream("/icons/18/play.png"), 18, 18, true, true)));
            forceStartButton.setMaxWidth(Double.MAX_VALUE);
            forceStartButton.setMaxHeight(Double.MAX_VALUE);
            forceStartButton.setOnAction(action -> {
                forceStartButton.setDisable(true);
                Pane parentNode = (Pane)this.getParent();
                parentNode.getChildren().remove(this);
                requestWrapper.forceStart();
            });
            GridPane.setHgrow(forceStartButton, Priority.ALWAYS);
            GridPane.setVgrow(forceStartButton, Priority.ALWAYS);
            GridPane.setMargin(forceStartButton, new Insets(0, 2, 0, 0));

            Button cancelButton = new Button(null, new ImageView(new Image(this.getClass().getResourceAsStream("/icons/18/cancel.png"), 18, 18, true, true)));
            cancelButton.setMaxWidth(Double.MAX_VALUE);
            cancelButton.setMaxHeight(Double.MAX_VALUE);
            cancelButton.setOnAction(action -> {
                cancelButton.setDisable(true);
                Pane parentNode = (Pane)this.getParent();
                parentNode.getChildren().remove(this);
                requestWrapper.cancel("Cancelled by user");
            });
            GridPane.setHgrow(cancelButton, Priority.ALWAYS);
            GridPane.setVgrow(cancelButton, Priority.ALWAYS);
            GridPane.setMargin(cancelButton, new Insets(0, 0, 0, 2));

            this.setPadding(new Insets(2, 4, 2, 4));

            this.add(imagePane, 1, 1, 1, 2);
            this.add(titleLabel, 2, 1, 2, 1);
            this.add(forceStartButton, 2, 2, 1, 1);
            this.add(cancelButton, 3, 2, 1, 1);

        }

        PreviewImagePane getImagePage() {
            return this.imagePage;
        }
        private void setImagePage(PreviewImagePane imagePage) {
            this.imagePage = imagePage;
        }

    }

    private Map<DownloadRequestWrapper, DownloadEngineRequestWrapperPane> getRequestWrapperPanesByRequestWrapper() {
        return this.requestWrapperPanesByRequestWrapper;
    }
    private void setRequestWrapperPanesByRequestWrapper(Map<DownloadRequestWrapper, DownloadEngineRequestWrapperPane> requestWrapperPanesByRequestWrapper) {
        this.requestWrapperPanesByRequestWrapper = requestWrapperPanesByRequestWrapper;
    }

    private VBox getRequestWrapperPanesBox() {
        return this.requestWrapperPanesBox;
    }
    private void setRequestWrapperPanesBox(VBox requestWrapperPanesBox) {
        this.requestWrapperPanesBox = requestWrapperPanesBox;
    }

}
