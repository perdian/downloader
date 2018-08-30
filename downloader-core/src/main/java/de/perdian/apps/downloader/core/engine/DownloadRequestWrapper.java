package de.perdian.apps.downloader.core.engine;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DownloadRequestWrapper {

    private DownloadEngine owner = null;
    private DownloadRequest request = null;
    private DownloadOperation operation = null;
    private Instant scheduledTime = null;

    DownloadRequestWrapper() {
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }

    /**
     * Forces this request to be started. Normally the {@link DownloadEngine} will
     * decide when a submitted job will be started, that is when a processor
     * thread will be assigned to perform the data transfer from the source to a
     * local file. If this method is called however the transfer will start
     * immediately. An additional processor thread will be created if all
     * processor slots are occupied and this surplus thread will then execute
     * the transfer itself. If the ob is already started then this method will
     * do nothing at all.
     */
    public void forceStart() {
        this.getOwner().executeRequest(this, true);
    }

    /**
     * Cancels the current request which means telling the operation to stop all actions and
     * data transfers that are currently active. Please note that this doesn't mean that the
     * transfer will actually stop immediately! If the processor thread itself doesn't provide an
     * option to immediately stop the process the actual download might continue until the
     * processor thread is able to stop.
     *
     * @param reason
     *     the reason why this job was cancelled
     */
    public void cancel(String reason) {
        this.getOwner().cancelRequest(this, reason);
    }

    public static class PriorityComparator implements Comparator<DownloadRequestWrapper> {

        @Override
        public int compare(DownloadRequestWrapper o1, DownloadRequestWrapper o2) {
            if (o1 == null) {
                return o2 == null ? 0 : -1;
            } else if (o2 == null) {
                return 1;
            } else {
                int p1 = o1.getRequest().getPriority();
                int p2 = o2.getRequest().getPriority();
                if (p1 == p2) {
                    if (Objects.equals(o1.getScheduledTime(), o2.getScheduledTime())) {
                        return 0;
                    } else {
                        return o1.getScheduledTime().compareTo(o2.getScheduledTime());
                    }
                } else {
                    return p1 < p2 ? 1 : -1;
                }
            }
        }

    }

    public DownloadEngine getOwner() {
        return this.owner;
    }
    void setOwner(DownloadEngine owner) {
        this.owner = owner;
    }

    public DownloadRequest getRequest() {
        return this.request;
    }
    void setRequest(DownloadRequest request) {
        this.request = request;
    }

    DownloadOperation getOperation() {
        return this.operation;
    }
    void setOperation(DownloadOperation operation) {
        this.operation = operation;
    }

    /**
     * Gets the time when the download has been accepted by a
     * {@link DownloadEngine}
     */
    public Instant getScheduledTime() {
        return this.scheduledTime;
    }
    void setScheduledTime(Instant scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

}
