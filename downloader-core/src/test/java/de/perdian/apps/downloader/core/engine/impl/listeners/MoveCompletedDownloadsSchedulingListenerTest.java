package de.perdian.apps.downloader.core.engine.impl.listeners;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;

import de.perdian.apps.downloader.core.engine.DownloadOperation;
import de.perdian.apps.downloader.core.engine.DownloadTask;
import de.perdian.apps.downloader.core.engine.impl.tasks.StreamFactoryTask;

public class MoveCompletedDownloadsSchedulingListenerTest {

    private FileSystem myFileSystem = null;

    @BeforeEach
    public void prepareFileSystem() throws IOException {
        this.setFileSystem(MemoryFileSystemBuilder.newEmpty().build(UUID.randomUUID().toString()));
    }

    @AfterEach
    public void cleanupFileSystem() throws IOException {
        this.getFileSystem().close();
    }

    @Test
    public void onOperationTransferCompleted() throws Exception {

        Path inFile = this.getFileSystem().getPath("working.txt");
        Files.write(inFile, "test".getBytes(), StandardOpenOption.CREATE);

        Path outDirectory = Files.createDirectory(this.getFileSystem().getPath("out/"));

        DownloadOperation operation = Mockito.mock(DownloadOperation.class);
        DownloadTask task = new StreamFactoryTask(() -> null);

        MoveCompletedDownloadsSchedulingListener listener = new MoveCompletedDownloadsSchedulingListener(outDirectory);
        listener.onOperationTransferCompleted(task, inFile, operation);

        Path outFile = outDirectory.resolve("working.txt");
        Assertions.assertFalse(Files.exists(inFile));
        Assertions.assertTrue(Files.exists(outFile));
        Assertions.assertArrayEquals("test".getBytes(), Files.readAllBytes(outFile));

    }

    private FileSystem getFileSystem() {
        return this.myFileSystem;
    }
    private void setFileSystem(FileSystem fileSystem) {
        this.myFileSystem = fileSystem;
    }

}
