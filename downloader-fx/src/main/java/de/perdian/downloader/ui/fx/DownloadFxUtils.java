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
package de.perdian.downloader.ui.fx;

import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.downloader.core.DownloadJob;
import de.perdian.apps.downloader.core.DownloadStreamFactory;

public class DownloadFxUtils {

    static final Logger log = LoggerFactory.getLogger(DownloadFxUtils.class);
    static final ExecutorService IMAGE_LOADER_THREADPOOL = Executors.newFixedThreadPool(3);

    public static void loadImageInBackground(Label targetLabel, DownloadJob job, double width, double height, boolean usePool) {
        DownloadStreamFactory previewStreamFactory = job.getRequest().getPreviewImageFactory();
        if (previewStreamFactory != null) {
            Executor executor = usePool ? IMAGE_LOADER_THREADPOOL : new ExecutorDummy();
            executor.execute(() -> {
                try {
                    try (InputStream previewStream = previewStreamFactory.openStream()) {
                        if (previewStream != null) {
                            Image previewImage = new Image(previewStream, width, height, true, true);
                            Platform.runLater(() -> {
                                targetLabel.setText(null);
                                targetLabel.setGraphic(new ImageView(previewImage));
                            });
                        }
                    }
                } catch (Exception e) {
                    log.warn("Cannot open preview stream for job: " + job, e);
                }
            });
        }
    }

    // -------------------------------------------------------------------------
    // --- Inner classes -------------------------------------------------------
    // -------------------------------------------------------------------------

    static class ExecutorDummy implements Executor {

        @Override
        public void execute(Runnable command) {
            new Thread(command).start();
        }

    }

}