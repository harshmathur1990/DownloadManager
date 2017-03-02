package com.download;

import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Created by harshmathur on 17/02/17.
 */
public class NewURLDownloadManager {

    private final String uri;
    private final String saveDir;
    private static final int BUFFER_SIZE = 256000;
    private final ExecutorService service;
    private final Integer maxNumberOfConnections;
    private final Boolean resume;

    public NewURLDownloadManager(String uri, String saveDir,
                                 Integer maxNumberOfConnections, Boolean resume, ExecutorService service) {
        this.uri = uri;
        this.saveDir = saveDir;
        this.service = service;
        this.maxNumberOfConnections = maxNumberOfConnections;
        this.resume = resume;
    }

    public CompletableFuture<URLDownloadStatus> startDownload() {
        final URL url;
        try {
            url = new URL(this.uri);
            URLConnection connection = url.openConnection();
            Long contentLength = connection.getContentLengthLong();
            if (contentLength < 1) {
                CompletableFuture.completedFuture(new URLDownloadStatus(this.uri, ConnectionStatus.ZERO_LENGTH_CONTENT, 0L));
            }
            final Integer numberOfConnections;
            final Long numberofBytesPerConnection;
            String acceptRanges = connection.getHeaderField("Accept-Ranges");
            if (acceptRanges != null && acceptRanges.equals("bytes")) {
                numberOfConnections = this.maxNumberOfConnections;
            } else {
                numberOfConnections = 1;
            }
            numberofBytesPerConnection = contentLength / numberOfConnections;

            List<Integer> connectionRange = Utilities.getRange(0, numberOfConnections);

            List<CompletableFuture<ConnectionData>> connectionDataListOfFutures = connectionRange
                    .stream()
                    .map(connectionNumber -> CompletableFuture.supplyAsync(
                            () -> getPartialData(
                                    connectionNumber, url, numberofBytesPerConnection, resume, service
                            ),
                            service
                        )
                    )
                    .collect(Collectors.toList());

            CompletableFuture<List<ConnectionData>> futureListConnectionData = Utilities.sequence(connectionDataListOfFutures, service);

            return futureListConnectionData.thenApplyAsync(connectionDatas -> merge(connectionDatas, url), service);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(new URLDownloadStatus(uri, ConnectionStatus.DOWNLOAD_FAILED, 0L));
        } catch (IOException e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(new URLDownloadStatus(uri, ConnectionStatus.DOWNLOAD_FAILED, 0L));
        }
    }

    public ConnectionData getPartialData(Integer connectionNumber,
                                         URL url, Long numberofBytesPerConnection, Boolean resume, ExecutorService service) {
        NewDownloadWorker worker = new NewDownloadWorker(connectionNumber, url, numberofBytesPerConnection, resume, service);
        return worker.download();
    }

    public URLDownloadStatus merge (List<ConnectionData> connectionDataList, URL url){
        try{
            Long totalSpeed = 0L;
            InputStream inputStream = null;
            FileOutputStream out = new FileOutputStream(Utilities.getFileName(url, "", this.saveDir));
            for (ConnectionData connectionData: connectionDataList) {
                if (connectionData.getConnectionStatus() != ConnectionStatus.PART_DOWNLOADED) {
                    return new URLDownloadStatus(uri, ConnectionStatus.DOWNLOAD_FAILED, 0L);
                }
                totalSpeed += connectionData.getSpeed();
                inputStream = new FileInputStream(connectionData.getFileName());
                byte[] buffer = new byte[BUFFER_SIZE];
                Integer bytesRead = -1;
                while((bytesRead = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                new File(connectionData.getFileName()).delete();
            }
            out.close();
            return new URLDownloadStatus(uri, ConnectionStatus.MERGED, totalSpeed);
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
            return new URLDownloadStatus(uri, ConnectionStatus.MERGE_FAILED, 0L);
        } catch (IOException e) {
            e.printStackTrace();
            return new URLDownloadStatus(uri, ConnectionStatus.MERGE_FAILED, 0L);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new URLDownloadStatus(uri, ConnectionStatus.MERGE_FAILED, 0L);
        }
    }
}
