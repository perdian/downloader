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
package de.perdian.apps.downloader.core.impl;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import de.perdian.apps.downloader.core.DownloadJob;
import de.perdian.apps.downloader.core.DownloadRequest;
import de.perdian.apps.downloader.core.DownloadStreamFactory;

public class TestCancelSmallDownloadsListener {

    @Test
    public void jobStartedThresholdNotReached() throws Exception {

        DownloadStreamFactory contentFactory = Mockito.mock(DownloadStreamFactory.class);
        Mockito.when(contentFactory.size()).thenReturn(Long.valueOf(999));
        DownloadRequest request = new DownloadRequest();
        request.setContentFactory(contentFactory);
        DownloadJob job = Mockito.mock(DownloadJob.class);
        Mockito.when(job.getRequest()).thenReturn(request);

        CancelSmallDownloadsListener listener = new CancelSmallDownloadsListener(1000);
        listener.onJobStarted(job);
        Mockito.verify(job).cancel(Matchers.any(String.class));

    }

    @Test
    public void jobStartedThresholdReached() throws Exception {

        DownloadStreamFactory contentFactory = Mockito.mock(DownloadStreamFactory.class);
        Mockito.when(contentFactory.size()).thenReturn(Long.valueOf(1001));
        DownloadRequest request = new DownloadRequest();
        request.setContentFactory(contentFactory);
        DownloadJob job = Mockito.mock(DownloadJob.class);
        Mockito.when(job.getRequest()).thenReturn(request);

        CancelSmallDownloadsListener listener = new CancelSmallDownloadsListener(1000);
        listener.onJobStarted(job);
        Mockito.verify(job).getRequest();
        Mockito.verifyNoMoreInteractions(job);

    }

}