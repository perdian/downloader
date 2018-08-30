package de.perdian.apps.downloader.core.engine;

import de.perdian.apps.downloader.core.support.StreamFactory;

public class DownloadTask {

    private String title = null;
    private String targetFileName = null;
    private StreamFactory contentFactory = null;
    private StreamFactory previewImageFactory = null;

    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getTargetFileName() {
        return this.targetFileName;
    }
    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }

    public StreamFactory getContentFactory() {
        return this.contentFactory;
    }
    public void setContentFactory(StreamFactory contentFactory) {
        this.contentFactory = contentFactory;
    }

    public StreamFactory getPreviewImageFactory() {
        return this.previewImageFactory;
    }
    public void setPreviewImageFactory(StreamFactory previewImageFactory) {
        this.previewImageFactory = previewImageFactory;
    }

}
