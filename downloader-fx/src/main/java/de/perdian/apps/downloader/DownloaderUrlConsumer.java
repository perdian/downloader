/*
 * Copyright 2013-2019 Christian Robert
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.downloader.core.engine.DownloadEngine;
import de.perdian.apps.downloader.core.engine.DownloadRequest;
import de.perdian.apps.downloader.core.engine.DownloadRequestFactory;
import de.perdian.apps.downloader.core.engine.impl.tasks.StreamFactoryTask;
import de.perdian.apps.downloader.core.support.impl.URLStreamFactory;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;

class DownloaderUrlConsumer implements Consumer<String> {

    private static final Logger log = LoggerFactory.getLogger(DownloaderUrlConsumer.class);

    private DownloadEngine engine = null;
    private List<DownloadRequestFactory> requestFactories = null;

    public DownloaderUrlConsumer(DownloadEngine engine, List<DownloadRequestFactory> requestFactories) {
        this.setEngine(engine);
        this.setRequestFactories(requestFactories);
    }

    @Override
    public void accept(String input) {
        try {

            URL inputUrl = this.parseUrl(input);
            if (inputUrl != null) {

                for (DownloadRequestFactory requestFactory : this.getRequestFactories()) {
                    try {
                        List<DownloadRequest> requests = requestFactory.createRequests(inputUrl);
                        if (requests != null) {
                            this.getEngine().submitAll(requests);
                            return;
                        }
                    } catch (IOException e) {
                        log.info("Cannot create DownloadRequest objects from DownloadRequestFactory '{}' for URL: {}", requestFactory, inputUrl);
                    }
                }

                // If we have reached this point none of the DownloadRequestFactories was able to create a DownloadRequest
                // so we give the user the chance to force using the actual URL as download source
                ButtonType useOriginalUrlButtonType = new ButtonType("Use original URL", ButtonData.OK_DONE);
                Alert requestFactoryNotFoundAlert = new Alert(AlertType.INFORMATION);
                requestFactoryNotFoundAlert.setTitle("Unknown input URL");
                requestFactoryNotFoundAlert.setHeaderText("The downloader doesn't know how to initiate a download for the content behind the URL.\nYou can still initiate a manual download which will try to read the content directly from the original URL");
                requestFactoryNotFoundAlert.setContentText(inputUrl.toString());
                requestFactoryNotFoundAlert.getButtonTypes().setAll(useOriginalUrlButtonType, ButtonType.CANCEL);
                ButtonType requestFactoryNotFoundButtonType = requestFactoryNotFoundAlert.showAndWait().orElse(ButtonType.CANCEL);
                if (useOriginalUrlButtonType.equals(requestFactoryNotFoundButtonType)) {
                    DownloadRequest downloadRequest = new DownloadRequest();
                    downloadRequest.setId(UUID.randomUUID().toString());
                    downloadRequest.setTargetFileNameSupplier(() -> inputUrl.getFile());
                    downloadRequest.setTitle(inputUrl.toString());
                    downloadRequest.setTaskSupplier(() -> new StreamFactoryTask(new URLStreamFactory(inputUrl)));
                    this.getEngine().submit(downloadRequest);
                }

            }

        } catch (MalformedURLException | URISyntaxException e) {
            Alert exceptionAlert = new Alert(AlertType.ERROR);
            exceptionAlert.setTitle("Invalid input URL");
            exceptionAlert.setContentText("The URL entered is not valid");
            exceptionAlert.showAndWait();
        }
    }

    private URL parseUrl(String input) throws MalformedURLException, URISyntaxException {
        URI inputURI = new URI(input);
        if (StringUtils.isEmpty(inputURI.getScheme())) {

            ButtonType httpButtonType = new ButtonType("Use HTTP", ButtonData.LEFT);
            ButtonType httpsButtonType = new ButtonType("Use HTTPS", ButtonData.LEFT);

            Alert noSchemeAlert = new Alert(AlertType.WARNING);
            noSchemeAlert.setTitle("Invalud input URL");
            noSchemeAlert.setHeaderText("The entered URL is missing a scheme part");
            noSchemeAlert.setContentText("You may continue by using a default scheme part (HTTP or HTTPS)");
            noSchemeAlert.getButtonTypes().setAll(httpButtonType, httpsButtonType, ButtonType.CANCEL);
            ((Button)noSchemeAlert.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);

            ButtonType noSchemeButtonType = noSchemeAlert.showAndWait().orElse(ButtonType.CANCEL);
            if (httpButtonType.equals(noSchemeButtonType)) {
                return new URL("http://" + input);
            } else if (httpsButtonType.equals(noSchemeButtonType)) {
                return new URL("https://" + input);
            } else {
                return null;
            }

        } else {
            return inputURI.toURL();
        }
    }

    private DownloadEngine getEngine() {
        return this.engine;
    }
    private void setEngine(DownloadEngine engine) {
        this.engine = engine;
    }

    private List<DownloadRequestFactory> getRequestFactories() {
        return this.requestFactories;
    }
    private void setRequestFactories(List<DownloadRequestFactory> requestFactories) {
        this.requestFactories = requestFactories;
    }

}
