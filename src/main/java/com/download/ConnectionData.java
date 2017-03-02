package com.download;

import java.net.URL;

/**
 * Created by harshmathur on 17/02/17.
 */
public class ConnectionData {

    private Integer job;

    private ConnectionStatus connectionStatus;

    private URL url;

    private final Long speed;

    public String getFileName() {
        return fileName;
    }

    private final String fileName;

    public ConnectionData(Integer job, ConnectionStatus connectionStatus, URL url, Long speed, String fileName) {
        this.job = job;
        this.connectionStatus = connectionStatus;
        this.url = url;
        this.speed = speed;
        this.fileName = fileName;
    }

    public Long getSpeed() {
        return speed;
    }

    public Integer getJob() {
        return job;
    }

    public void setJob(Integer job) {
        this.job = job;
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
