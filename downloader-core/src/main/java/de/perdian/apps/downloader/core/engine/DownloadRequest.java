package de.perdian.apps.downloader.core.engine;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import de.perdian.apps.downloader.core.support.StreamFactory;

/**
 * Transporter object to request the {@link DownloadEngine} to schedule a download operation for a
 * remote resources that is identified by the information encapsulated in this request object.
 *
 * This class is not intended to be subclasses by client code, since detailed configuration of how
 * the remote resource can be accessed should be performed using the client implementation of the
 * {@code DownloadTaskFactory} for a {@link DownloadTask} interface that can be set as property into
 * a request object.
 *
 * @author Christian Robert
 */

public class DownloadRequest {

    private String id = null;
    private String title = null;
    private DownloadTaskFactory taskFactory = null;
    private StreamFactory previewImageFactory = null;
    private int priority = 0;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }

    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public DownloadTaskFactory getTaskFactory() {
        return this.taskFactory;
    }
    public void setTaskFactory(DownloadTaskFactory taskFactory) {
        this.taskFactory = taskFactory;
    }

    public StreamFactory getPreviewImageFactory() {
        return this.previewImageFactory;
    }
    public void setPreviewImageFactory(StreamFactory previewImageFactory) {
        this.previewImageFactory = previewImageFactory;
    }

    public int getPriority() {
        return this.priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }

}
