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
package de.perdian.apps.downloader.core.engine.impl.listeners;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;

import de.perdian.apps.downloader.core.engine.DownloadOperation;
import de.perdian.apps.downloader.core.engine.DownloadTask;
import de.perdian.apps.downloader.core.support.StreamFactory;

public class CancelSmallDownloadsSchedulingListenerTest {

    @Test
    public void onOperationTransferStartingWithThresholdNotReached() throws Exception {

        StreamFactory contentFactory = Mockito.mock(StreamFactory.class);
        Mockito.when(contentFactory.size()).thenReturn(4200L);
        DownloadTask task = new DownloadTask();
        task.setContentFactory(contentFactory);
        DownloadOperation operation = Mockito.mock(DownloadOperation.class);

        FileSystem fileSystem = MemoryFileSystemBuilder.newEmpty().build(UUID.randomUUID().toString());
        Path file = fileSystem.getPath("test");

        CancelSmallDownloadsSchedulingListener listener = new CancelSmallDownloadsSchedulingListener(1000);
        listener.onOperationTransferStarting(task, file, operation);
        Mockito.verify(operation, Mockito.never()).cancel(Mockito.any());

    }

    @Test
    public void onOperationTransferStartingWithThresholdReached() throws Exception {

        StreamFactory contentFactory = Mockito.mock(StreamFactory.class);
        Mockito.when(contentFactory.size()).thenReturn(42L);
        DownloadTask task = new DownloadTask();
        task.setContentFactory(contentFactory);
        DownloadOperation operation = Mockito.mock(DownloadOperation.class);

        FileSystem fileSystem = MemoryFileSystemBuilder.newEmpty().build(UUID.randomUUID().toString());
        Path file = fileSystem.getPath("test");

        CancelSmallDownloadsSchedulingListener listener = new CancelSmallDownloadsSchedulingListener(1000);
        listener.onOperationTransferStarting(task, file, operation);
        Mockito.verify(operation).cancel(Mockito.any());

    }

//
//    @Test
//    public void jobStartedThresholdReached() throws Exception {
//
//        DownloadStreamFactory contentFactory = Mockito.mock(DownloadStreamFactory.class);
//        Mockito.when(contentFactory.size()).thenReturn(Long.valueOf(1001));
//        DownloadRequest request = new DownloadRequest();
//        request.setContentFactory(contentFactory);
//        DownloadJob job = Mockito.mock(DownloadJob.class);
//        Mockito.when(job.getRequest()).thenReturn(request);
//
//        CancelSmallDownloadsSchedulingListener listener = new CancelSmallDownloadsSchedulingListener(1000);
//        listener.onJobStarted(job);
//        Mockito.verify(job).getRequest();
//        Mockito.verifyNoMoreInteractions(job);
//
//    }

}
