package com.download;

/**
 * Created by harshmathur on 02/03/17.
 */
public class URLDownloadStatus {

    public URLDownloadStatus(String url, ConnectionStatus status, Long speed) {
        this.url = url;
        this.status = status;
        this.speed = speed;
    }

    private  String url;

    public String getUrl() {
        return url;
    }

    public ConnectionStatus getStatus() {
        return status;
    }

    private ConnectionStatus status;

    public Long getSpeed() {
        return speed;
    }

    private Long speed;
}
