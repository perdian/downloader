package de.perdian.apps.downloader.impl.khinsider;

import de.perdian.apps.downloader.DownloaderApplicationLauncher;
import de.perdian.apps.downloader.DownloaderEngineProviderRegistry;
import de.perdian.apps.downloader.core.engine.*;
import de.perdian.apps.downloader.core.engine.impl.dataextractors.StreamFactoryDataExtractor;
import de.perdian.apps.downloader.core.support.StreamFactory;
import de.perdian.apps.downloader.core.support.impl.ByteArrayStreamFactory;
import de.perdian.apps.downloader.core.support.impl.OkHttpClientRequestStreamFactory;
import de.perdian.apps.downloader.impl.support.MP3DownloadPostProcessor;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloaderLauncher {

    private static final Logger log = LoggerFactory.getLogger(DownloaderLauncher.class);
    private static final List<String> pages = List.of(
        "https://downloads.khinsider.com/game-soundtracks/album/mario-kart-8-deluxe-the-definitive-soundtrack-switch-gamerip-2014"
//        "https://downloads.khinsider.com/game-soundtracks/album/who-wants-to-be-a-millionaire-the-album"
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

        Element songlistHeaderRowElement = albumPageDocument.selectFirst("table#songlist tr#songlist_header");
        Integer cdColumnIndex = DownloaderLauncher.findColumnIndex(songlistHeaderRowElement, "CD");
        Integer songTitleColumnIndex = DownloaderLauncher.findColumnIndex(songlistHeaderRowElement, "Song Name");

        Elements songlistRows = albumPageDocument.select("table#songlist tr:not(#songlist_header):not(#songlist_footer)");
        log.debug("Processing {} songs on album page", songlistRows.size(), albumPageUrl);
        Map<Integer, AtomicInteger> songIndexByDisc = new HashMap<>();
        for (Element songlistRow : songlistRows) {

            Elements songColumns = songlistRow.select("td");
            StringBuilder songFileName = new StringBuilder();

            int cdIndex = cdColumnIndex == null ? 1 : Integer.parseInt(songColumns.get(cdColumnIndex).text().strip());
            if (cdColumnIndex != null) {
                NumberFormat cdIndexDecimalFormat = new DecimalFormat("0");
                songFileName.append(cdIndexDecimalFormat.format(cdIndex));
                songFileName.append(".");
            }

            AtomicInteger songIndexCounter = songIndexByDisc.compute(cdIndex, (k, v) -> v == null ? new AtomicInteger(1) : v);
            int songIndex = songIndexCounter.getAndIncrement();
            NumberFormat songIndexDecimalFormat = new DecimalFormat("00");
            songFileName.append(songIndexDecimalFormat.format(songIndex));

            Element songTitleElement = songColumns.get(songTitleColumnIndex).selectFirst("a");
            String songTitle = songTitleElement.text().strip();
            songFileName.append(" ").append(songTitle);

            Element downloadPageLink = songlistRow.selectFirst("td.playlistDownloadSong a");
            if (downloadPageLink != null) {
                Thread.ofVirtual().start(() -> {
                    String downloadPageUrl = downloadPageLink.attr("href");
                    try {

                        log.debug("Computing download URL from download page {} for title: {}", downloadPageUrl, songTitle);
                        URI downloadPageUri = URI.create("https://downloads.khinsider.com/").resolve(downloadPageUrl);
                        Request downloadPageRequest = new Request.Builder().get().url(downloadPageUri.toString()).build();
                        try (Response downloadPageResponse = httpClient.newCall(downloadPageRequest).execute()) {

                            Document downloadPageDocument = Jsoup.parse(downloadPageResponse.body().string());
                            Element downloadLinkParagraph = downloadPageDocument.selectFirst("p:has(.songDownloadLink)");
                            Element downloadLink = downloadLinkParagraph.selectFirst("a");
                            String downloadUrl = downloadLink.attr("href");

                            StringBuilder downloadFilePath = new StringBuilder();
                            downloadFilePath.append(DownloaderLauncher.createSafeFilename(albumTitle)).append("/");
                            downloadFilePath.append(DownloaderLauncher.createSafeFilename(songFileName));
                            downloadFilePath.append(".").append(FilenameUtils.getExtension(downloadUrl));

                            StreamFactory downloadStreamFactory = new OkHttpClientRequestStreamFactory(downloadUrl, httpClient);
                            DownloadDataExtractor downloadDataExtractor = new StreamFactoryDataExtractor(downloadStreamFactory);
                            DownloadTaskFactory downloadTaskFactory = progressListener -> new DownloadTask(downloadFilePath.toString(), downloadDataExtractor);
                            DownloadRequest downloadRequest = new DownloadRequest();
                            downloadRequest.setPreviewImageFactory(albumImageBytes == null ? null : new ByteArrayStreamFactory(albumImageBytes));
                            downloadRequest.setTaskFactory(downloadTaskFactory);
                            downloadRequest.setTitle(songFileName.toString());
                            if (downloadUrl.toLowerCase().endsWith(".mp3")) {
                                MP3DownloadPostProcessor mp3PostProcessor = new MP3DownloadPostProcessor();
                                downloadRequest.addProcessor(mp3PostProcessor);
                            }
                            downloadEngine.submit(downloadRequest);

                        }

                    } catch (Exception e) {
                        log.error("Cannot add downloader job from page {}", downloadPageUrl);
                    }
                });
            }
        }

    }

    private static Integer findColumnIndex(Element songlistHeaderRowElement, String title) {
        Elements songlistColumnElements = songlistHeaderRowElement.select("th");
        for (int i=0; i < songlistColumnElements.size(); i++) {
            Element songlistHeaderColumnElement = songlistColumnElements.get(i);
            Element titleElement = songlistHeaderColumnElement.selectFirst("b");
            String titleElementText = titleElement == null ? null : titleElement.text().strip();
            if (title.equalsIgnoreCase(titleElementText)) {
                return i;
            }
        }
        return null;
    }

    private static String createSafeFilename(CharSequence inputFilename) {
        StringBuilder resultFilename = new StringBuilder();
        for (char c: inputFilename.toString().toCharArray()) {
            if (c == '/') {
                resultFilename.append("-");
            } else {
                resultFilename.append(c);
            }
        }
        return resultFilename.toString();
    }

}
