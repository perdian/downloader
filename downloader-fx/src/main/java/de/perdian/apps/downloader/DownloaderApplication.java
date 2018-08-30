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
package de.perdian.apps.downloader;

import java.io.File;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.downloader.core.engine.DownloadEngine;
import de.perdian.apps.downloader.core.engine.DownloadRequestFactory;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class DownloaderApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(DownloaderApplication.class);

    public static void main(String[] args) {
        Application.launch(DownloaderApplication.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        log.info("Creating DownloaderConfiguration");
        DownloaderConfiguration configuration = this.createConfiguration();

        log.info("Creating DownloadEngine");
        DownloadEngine engine = this.createEngine();

        DownloaderUrlConsumer urlConsumer = new DownloaderUrlConsumer();
        urlConsumer.setEngine(engine);
        urlConsumer.setRequestFactories(configuration.getRequestFactories());

        log.info("Creating JavaFX UI");
        DownloaderPane downloaderPane = new DownloaderPane(engine, configuration, urlConsumer);

        log.info("Opening JavaFX stage");
        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/icons/96/download.png")));
        primaryStage.setScene(new Scene(downloaderPane));
        primaryStage.setOnCloseRequest(event -> System.exit(0));
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setTitle("Downloader");
        primaryStage.setWidth(1200);
        primaryStage.setHeight(700);
        primaryStage.show();

        log.info("Application start completed");

    }

    protected DownloadEngine createEngine() {
        Path targetDirectory = new File(System.getProperty("user.home"), "Downloads").toPath();
        return new DownloadEngine(targetDirectory);
    }

    protected DownloaderConfiguration createConfiguration() {
        DownloaderConfiguration configuration = new DownloaderConfiguration();
        configuration.setRequestFactories(DownloadRequestFactory.createDefaultFactories());
        return configuration;
    }

}
