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

import de.perdian.apps.downloader.core.engine.DownloadEngine;
import de.perdian.apps.downloader.core.engine.DownloadRequestFactory;
import de.perdian.apps.downloader.fx.EngineSettingsPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloaderApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(DownloaderApplication.class);

    @Override
    public void start(Stage primaryStage) throws Exception {

        log.info("Creating DownloadEngine");
        DownloadEngine engine = DownloaderEngineProviderRegistry.getProvider().createEngine();

        log.info("Creating DownloaderConfiguration");
        DownloaderConfiguration configuration = this.createConfiguration(engine);

        log.info("Creating JavaFX UI");
        DownloaderPane downloaderPane = new DownloaderPane(engine, configuration, new DownloaderUrlConsumer(engine, configuration.getRequestFactories()));

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

    protected DownloaderConfiguration createConfiguration(DownloadEngine engine) {
        DownloaderConfiguration configuration = new DownloaderConfiguration();
        configuration.getSettingsPanes().put("Engine", new EngineSettingsPane(engine));
        configuration.getRequestFactories().addAll(DownloadRequestFactory.createDefaultFactories());
        return configuration;
    }

}
