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
package de.perdian.apps.downloader.fx;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import de.perdian.apps.downloader.core.engine.DownloadEngine;
import de.perdian.apps.downloader.core.engine.DownloadOperation;
import de.perdian.apps.downloader.core.engine.DownloadSchedulingListener;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FailedOperationsPane extends BorderPane implements DownloadSchedulingListener {

    private ObservableList<DownloadOperation> operations = null;

    public FailedOperationsPane(DownloadEngine engine) {

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        TableColumn<DownloadOperation, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setMinWidth(80);
        timeColumn.setSortable(false);
        timeColumn.setCellValueFactory(operation -> new SimpleStringProperty(timeFormatter.format(operation.getValue().getEndTime().atZone(ZoneId.systemDefault()))));
        TableColumn<DownloadOperation, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(operation -> new SimpleStringProperty(operation.getValue().getRequestWrapper().getRequest().getTitle()));
        titleColumn.setSortable(false);
        titleColumn.setMaxWidth(Double.MAX_VALUE);

        ObservableList<DownloadOperation> operations = FXCollections.observableArrayList();
        TableView<DownloadOperation> operationsTable = new TableView<>(operations);
        operationsTable.setStyle("-fx-background-color: transparent;");
        operationsTable.getColumns().addAll(List.of(timeColumn, titleColumn));
        operationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        operationsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        MenuItem showDetailsMenuItem = new MenuItem("Show details", new ImageView(new Image(this.getClass().getResourceAsStream("/icons/18/details.png"), 18, 18, true, true)));
        showDetailsMenuItem.setDisable(true);
        showDetailsMenuItem.setOnAction(event -> this.showOperationDetails(operationsTable.getSelectionModel().getSelectedItem(), operations, engine));
        MenuItem clearAllMenuItem = new MenuItem("Clear all entries", new ImageView(new Image(this.getClass().getResourceAsStream("/icons/18/clear.png"), 18, 18, true, true)));
        clearAllMenuItem.setDisable(true);
        clearAllMenuItem.setOnAction(event -> operations.clear());
        MenuItem clearSelectedMenuItem = new MenuItem("Clear selected entries", new ImageView(new Image(this.getClass().getResourceAsStream("/icons/18/clear.png"), 18, 18, true, true)));
        clearSelectedMenuItem.setDisable(true);
        clearSelectedMenuItem.setOnAction(event -> operations.removeAll(operationsTable.getSelectionModel().getSelectedItems()));
        MenuItem resubmitMenuItem = new MenuItem("Resubmit selected entries", new ImageView(new Image(this.getClass().getResourceAsStream("/icons/18/refresh.png"), 18, 18, true, true)));
        resubmitMenuItem.setDisable(true);
        resubmitMenuItem.setOnAction(event -> {
            List<DownloadOperation> selectedOperations = new ArrayList<>(operationsTable.getSelectionModel().getSelectedItems());
            operations.removeAll(selectedOperations);
            selectedOperations.forEach(operation -> engine.submit(operation.getRequestWrapper().getRequest()));
        });
        operationsTable.setContextMenu(new ContextMenu(showDetailsMenuItem, resubmitMenuItem, clearSelectedMenuItem, clearAllMenuItem));
        operationsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() > 1 && event.getButton() == MouseButton.PRIMARY) {
                this.showOperationDetails(operationsTable.getSelectionModel().getSelectedItem(), operations, engine);
            }
        });

        operations.addListener((ListChangeListener.Change<? extends DownloadOperation> change) -> {
            clearAllMenuItem.setDisable(change.getList().isEmpty());
        });
        operationsTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends DownloadOperation> change) -> {
            showDetailsMenuItem.setDisable(change.getList().size() != 1);
            clearSelectedMenuItem.setDisable(change.getList().isEmpty());
            resubmitMenuItem.setDisable(change.getList().isEmpty());
        });

        this.setOperations(operations);
        this.setCenter(operationsTable);

        engine.addSchedulingListener(this);

    }

    @Override
    public void onOperationCompleted(DownloadOperation operation) {
        if (operation.getError() != null) {
            Platform.runLater(() -> this.getOperations().add(0, operation));
        }
    }

    private void showOperationDetails(DownloadOperation selectedOperation, List<DownloadOperation> allOperations, DownloadEngine engine) {
        if (selectedOperation != null) {
            FailedOperationsDetailPane detailPane = new FailedOperationsDetailPane(selectedOperation, allOperations, engine);
            Stage detailStage = new Stage();
            detailStage.setTitle("Operation: " + selectedOperation.getRequestWrapper().getRequest().getTitle());
            detailStage.initModality(Modality.WINDOW_MODAL);
            detailStage.initOwner(this.getScene().getWindow());
            detailStage.setScene(new Scene(detailPane, 800, 600));
            detailStage.show();
        }
    }

    private ObservableList<DownloadOperation> getOperations() {
        return this.operations;
    }
    private void setOperations(ObservableList<DownloadOperation> operations) {
        this.operations = operations;
    }

}
