package com.download;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by harshmathur on 14/02/17.
 */
public class NewDownloadWorker {

    private static final int BUFFER_SIZE = 256000;
    private FileOutputStream out;
    private final URL url;
    private final Long numberofBytesPerConnection;
    private final Integer connectionNumber;
    private final ExecutorService service;
    private final Boolean resume;

    public NewDownloadWorker(Integer connectionNumber, URL url,
                             Long numberofBytesPerConnection, Boolean resume, ExecutorService service) {
        this.url = url;
        this.numberofBytesPerConnection = numberofBytesPerConnection;
        this.connectionNumber = connectionNumber;
        this.service = service;
        this.resume = resume;
    }

    public ConnectionData download() {
        Long startTime = new Date().getTime();
        Long endTime = null;
        String fileName = Utilities.getFileName(url, Integer.toString(connectionNumber), "/tmp");
        try {
            Long start = new Long(connectionNumber * numberofBytesPerConnection);
            Long end = start+numberofBytesPerConnection-1;
            if (this.resume) {
                File fileExists = new File(fileName);
                Long length = fileExists.length();
                if (length < numberofBytesPerConnection) {
                    start = start + length;
                } else if (length == numberofBytesPerConnection) {
                    return new ConnectionData(connectionNumber, ConnectionStatus.PART_DOWNLOADED, this.url, Long.MAX_VALUE, fileName);
                }
            }
            URLConnection connection = this.url.openConnection();
            connection.setRequestProperty("Range", "bytes="+start+"-"+end);
            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
            byte[] buffer = new byte[BUFFER_SIZE];
            Integer bytesRead = -1;
            this.out = new FileOutputStream(fileName);
            while (true) {
                bytesRead = in.read(buffer);
                if(bytesRead == -1) {
                    break;
                }
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            in.close();
            endTime = new Date().getTime();
            return new ConnectionData(connectionNumber, ConnectionStatus.PART_DOWNLOADED, url, (end-start)/(endTime-startTime), fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return new ConnectionData(connectionNumber, ConnectionStatus.PART_DOWNLOAD_FAILED, url, 0L, null);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ConnectionData(connectionNumber, ConnectionStatus.PART_DOWNLOAD_FAILED, url, 0L, null);
        }
    }
}
