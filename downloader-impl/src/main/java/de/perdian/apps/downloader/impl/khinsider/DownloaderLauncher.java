package de.perdian.apps.downloader.impl.khinsider;

import de.perdian.apps.downloader.DownloaderApplicationLauncher;
import de.perdian.apps.downloader.DownloaderEngineProviderRegistry;
import de.perdian.apps.downloader.core.engine.*;
import de.perdian.apps.downloader.core.engine.impl.dataextractors.StreamFactoryDataExtractor;
import de.perdian.apps.downloader.core.support.StreamFactory;
import de.perdian.apps.downloader.core.support.impl.ByteArrayStreamFactory;
import de.perdian.apps.downloader.core.support.impl.OkHttpClientRequestStreamFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class DownloaderLauncher {

    private static final Logger log = LoggerFactory.getLogger(DownloaderLauncher.class);
    private static final List<String> pages = List.of(
        "https://downloads.khinsider.com/game-soundtracks/album/who-wants-to-be-a-millionaire-the-album"
    );

    public static void main(String[] args) throws Exception {

        log.info("Opening UI");
        DownloadEngine downloadEngine = new DownloadEngine(Path.of(System.getProperty("user.home"), "Downloads/khinsider"));
        downloadEngine.setProcessorCount(5);
        DownloaderEngineProviderRegistry.setProvider(() -> downloadEngine);
        Thread.ofPlatform().name("JavaFX Launcher Thread").start(() -> DownloaderApplicationLauncher.main(args));

        log.info("Adding download jobs");
        OkHttpClient httpClient = new OkHttpClient();
        for (String page : pages) {
            DownloaderLauncher.addJobs(page, downloadEngine, httpClient);
        }

    }

    private static void addJobs(String albumUrl, DownloadEngine downloadEngine, OkHttpClient httpClient) throws IOException {
        log.debug("Loading album content from URL: {}", albumUrl);
        Request albumPageRequest = new Request.Builder().get().url(albumUrl).build();
        try (Response albumPageResponse = httpClient.newCall(albumPageRequest).execute()) {
            Document albumPageDocument = Jsoup.parse(albumPageResponse.body().string());
            DownloaderLauncher.addJobsFromAlbumPage(albumPageDocument, albumUrl, downloadEngine, httpClient);
        }
    }

    private static void addJobsFromAlbumPage(Document albumPageDocument, String albumPageUrl, DownloadEngine downloadEngine, OkHttpClient httpClient) throws IOException {

        Element albumTitleElement = albumPageDocument.selectFirst("h2");
        String albumTitle = albumTitleElement.text().strip();

        Element albumImageElement = albumPageDocument.selectFirst(".albumImage img");
        String albumImageUrl = albumImageElement == null ? null : albumImageElement.attr("src");
        byte[] albumImageBytes = StringUtils.isEmpty(albumImageUrl) ? null : IOUtils.toByteArray(URI.create(albumImageUrl));
        if (albumImageBytes != null) {
            DownloaderLauncher.addJobFromDownloadUrl(albumImageUrl, albumTitle, albumImageBytes,  "Cover", "cover." + FilenameUtils.getExtension(albumImageUrl), downloadEngine, httpClient);
        }

        Elements songlistRows = albumPageDocument.select("table#songlist tr:not(#songlist_header):not(#songlist_footer)");
        log.debug("Processing {} songs on album page", songlistRows.size(), albumPageUrl);
        NumberFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMinimumIntegerDigits(String.valueOf(songlistRows.size()).length());
        int songIndex = 1;
        for (Element songlistRow : songlistRows) {
            Element songTitleElement = songlistRow.select("td").get(2).selectFirst("a");
            StringBuilder songTitle = new StringBuilder();
            songTitle.append(decimalFormat.format(songIndex++));
            songTitle.append(" ").append(songTitleElement.text().strip());
            Element downloadPageLink = songlistRow.selectFirst("td.playlistDownloadSong a");
            if (downloadPageLink != null) {
                Thread.ofVirtual().start(() -> {
                    String downloadPageUrl = downloadPageLink.attr("href");
                    try {
                        log.debug("Computing download URL from download page {} for title: {}", downloadPageUrl, songTitle);
                        DownloaderLauncher.addJobFromDownloadPage(downloadPageUrl, albumTitle, albumImageBytes, songTitle.toString(), downloadEngine, httpClient);
                    } catch (Exception e) {
                        log.error("Cannot add downloader job from page {}", downloadPageUrl);
                    }
                });
            }
        }

    }

    private static void addJobFromDownloadPage(String downloadPageUrl, String albumTitle, byte[] albumImageBytes, String songTitle, DownloadEngine downloadEngine, OkHttpClient httpClient) throws IOException {
        URI downloadPageUri = URI.create("https://downloads.khinsider.com/").resolve(downloadPageUrl);
        Request downloadPageRequest = new Request.Builder().get().url(downloadPageUri.toString()).build();
        try (Response downloadPageResponse = httpClient.newCall(downloadPageRequest).execute()) {

            Document downloadPageDocument = Jsoup.parse(downloadPageResponse.body().string());
            Element downloadLinkParagraph = downloadPageDocument.selectFirst("p:has(.songDownloadLink)");
            Element downloadLink = downloadLinkParagraph.selectFirst("a");
            String downloadUrl = downloadLink.attr("href");
            DownloaderLauncher.addJobFromDownloadUrl(downloadUrl, albumTitle, albumImageBytes, songTitle, songTitle, downloadEngine, httpClient);

        }
    }

    private static void addJobFromDownloadUrl(String downloadUrl, String albumTitle, byte[] albumImageBytes, String fileTitle, String fileName, DownloadEngine downloadEngine, OkHttpClient httpClient) throws IOException {

        StringBuilder downloadFileName = new StringBuilder();
        downloadFileName.append(albumTitle).append("/").append(fileTitle);
        downloadFileName.append(".").append(FilenameUtils.getExtension(downloadUrl));

        StreamFactory downloadStreamFactory = new OkHttpClientRequestStreamFactory(downloadUrl, httpClient);
        DownloadDataExtractor downloadDataExtractor = new StreamFactoryDataExtractor(downloadStreamFactory);
        DownloadTaskFactory downloadTaskFactory = progressListener -> new DownloadTask(downloadFileName.toString(), downloadDataExtractor);
        DownloadRequest downloadRequest = new DownloadRequest();
        downloadRequest.setPreviewImageFactory(albumImageBytes == null ? null : new ByteArrayStreamFactory(albumImageBytes));
        downloadRequest.setTaskFactory(downloadTaskFactory);
        downloadRequest.setTitle(fileTitle);
        downloadEngine.submit(downloadRequest);

    }


}
