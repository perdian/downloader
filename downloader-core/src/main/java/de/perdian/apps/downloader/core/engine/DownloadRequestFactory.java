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
package de.perdian.apps.downloader.core.engine;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

/**
 * Factory to create one or multiple {@link DownloadRequest} instance(s) from a specific URL
 *
 * @author Christian Robert
 */

@FunctionalInterface
public interface DownloadRequestFactory {

    /**
     * Analyze the URL and create the request(s)
     *
     * @return
     *      a list of requests or {@code null} to signalize that the current factory is not able
     *      to analyze the URL
     */
    List<DownloadRequest> createRequests(URL url) throws IOException;

    /**
     * Gets the priority of this factory
     */
    default int getPriority() {
        return 0;
    }

    public static List<DownloadRequestFactory> createDefaultFactories() {

        ServiceLoader<DownloadRequestFactory> requestFactoryServiceLoader = ServiceLoader.load(DownloadRequestFactory.class);
        List<DownloadRequestFactory> requestFactories = requestFactoryServiceLoader.stream()
            .map(provider -> provider.get())
            .sorted(Comparator.comparing(DownloadRequestFactory::getPriority))
            .collect(Collectors.toList());

        LoggerFactory.getLogger(DownloadRequestFactory.class).info("Discovered {} implementation(s) of {}", requestFactories.size(), DownloadRequestFactory.class.getSimpleName());
        return requestFactories;

    }

}
