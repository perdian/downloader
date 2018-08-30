package de.perdian.apps.downloader.fx.engine;

import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.Map;

import de.perdian.apps.downloader.core.engine.DownloadEngine;
import de.perdian.apps.downloader.core.engine.DownloadOperation;
import de.perdian.apps.downloader.core.engine.DownloadSchedulingListener;
import de.perdian.apps.downloader.core.engine.DownloadTask;
import de.perdian.apps.downloader.core.support.StreamFactory;
import de.perdian.apps.downloader.fx.support.components.PreviewImagePane;
import de.perdian.apps.downloader.fx.support.components.ProgressPane;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DownloadEngineActiveOperationsPane extends BorderPane implements DownloadSchedulingListener {

    private Map<DownloadOperation, DownloadOperationPane> operationPanesByOperation = null;
    private VBox operationPanesBox = null;

    public DownloadEngineActiveOperationsPane(DownloadEngine engine) {

        VBox operationPanesBox = new VBox(4);
        operationPanesBox.setPadding(new Insets(4, 0, 4, 0));

        ScrollPane scrollPane = new ScrollPane(operationPanesBox);
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        this.setOperationPanesByOperation(new IdentityHashMap<>());
        this.setOperationPanesBox(operationPanesBox);
        this.setCenter(scrollPane);

        engine.addSchedulingListener(this);

    }

    @Override
    public void onOperationStarting(DownloadOperation operation) {
        synchronized (operation) {
            DownloadEngineActiveOperationsPane.DownloadOperationPane operationPane = new DownloadEngineActiveOperationsPane.DownloadOperationPane(operation);
            this.getOperationPanesByOperation().put(operation, operationPane);
            Platform.runLater(() -> this.getOperationPanesBox().getChildren().add(operationPane));
        }
    }

    @Override
    public void onOperationCompleted(DownloadOperation operation) {
        synchronized (operation) {
            DownloadEngineActiveOperationsPane.DownloadOperationPane operationPane = this.getOperationPanesByOperation().remove(operation);
            if (operationPane != null) {
                Platform.runLater(() -> this.getOperationPanesBox().getChildren().remove(operationPane));
            }
        }
    }

    @Override
    public void onOperationTransferStarting(DownloadTask task, Path targetFile, DownloadOperation operation) {
        DownloadEngineActiveOperationsPane.DownloadOperationPane operationPane = this.getOperationPanesByOperation().get(operation);
        if (operationPane != null) {
            Platform.runLater(() -> {
                operationPane.getProgressPane().setSubtitle("File: " + targetFile.getFileName().toString());
                operationPane.getImagePane().setStreamFactory(task.getPreviewImageFactory());
            });
        }
    }

    static class DownloadOperationPane extends GridPane {

        private DownloadOperation operation = null;
        private PreviewImagePane imagePane = null;
        private ProgressPane progressPane = null;

        DownloadOperationPane(DownloadOperation operation) {

            StreamFactory previewImageFactory = operation.getRequestWrapper().getRequest().getPreviewImageFactory();
            PreviewImagePane imagePane = new PreviewImagePane(previewImageFactory, 120, 100);
            this.setImagePane(imagePane);

            ProgressPane progressPane = new ProgressPane();
            progressPane.setTitle(operation.getRequestWrapper().getRequest().getTitle());
            operation.addProgressListener(progressPane);
            this.setProgressPane(progressPane);
            GridPane.setHgrow(progressPane, Priority.ALWAYS);

            Button cancelButton = new Button("Cancel", new ImageView(new Image(this.getClass().getResourceAsStream("/icons/18/cancel.png"), 18, 18, true, true)));
            cancelButton.setMaxWidth(Double.MAX_VALUE);
            cancelButton.setMaxHeight(Double.MAX_VALUE);
            cancelButton.setOnAction(action -> {
                cancelButton.setDisable(true);
                operation.cancel("Cancelled by user");
            });
            GridPane.setHgrow(cancelButton, Priority.ALWAYS);
            GridPane.setVgrow(cancelButton, Priority.ALWAYS);

            Button infoButton = new Button("Info", new ImageView(new Image(this.getClass().getResourceAsStream("/icons/18/help.png"), 18, 18, true, true)));
            infoButton.setDisable(true);
            infoButton.setMaxWidth(Double.MAX_VALUE);
            infoButton.setMaxHeight(Double.MAX_VALUE);
            GridPane.setHgrow(infoButton, Priority.ALWAYS);
            GridPane.setVgrow(infoButton, Priority.ALWAYS);

            GridPane buttonPane = new GridPane();
            buttonPane.setVgap(2);
            buttonPane.setPrefWidth(90);
            buttonPane.setMinWidth(90);
            buttonPane.setMaxWidth(90);
            buttonPane.add(cancelButton, 0, 0, 1, 1);
            buttonPane.add(infoButton, 0, 1, 1, 1);

            this.add(imagePane, 0, 0, 1, 1);
            this.add(progressPane, 1, 0, 1, 1);
            this.add(buttonPane, 2, 0, 1, 1);
            this.setHgap(4);
            this.setMaxWidth(Double.MAX_VALUE);
            this.setPadding(new Insets(2, 8, 2, 8));
            this.setOperation(operation);

        }

        ProgressPane getProgressPane() {
            return this.progressPane;
        }
        private void setProgressPane(ProgressPane progressPane) {
            this.progressPane = progressPane;
        }

        DownloadOperation getOperation() {
            return this.operation;
        }
        private void setOperation(DownloadOperation operation) {
            this.operation = operation;
        }

        PreviewImagePane getImagePane() {
            return this.imagePane;
        }
        private void setImagePane(PreviewImagePane imagePane) {
            this.imagePane = imagePane;
        }

    }

    private Map<DownloadOperation, DownloadOperationPane> getOperationPanesByOperation() {
        return this.operationPanesByOperation;
    }
    private void setOperationPanesByOperation(Map<DownloadOperation, DownloadOperationPane> operationPanesByOperation) {
        this.operationPanesByOperation = operationPanesByOperation;
    }

    private VBox getOperationPanesBox() {
        return this.operationPanesBox;
    }
    private void setOperationPanesBox(VBox operationPanesBox) {
        this.operationPanesBox = operationPanesBox;
    }

}
