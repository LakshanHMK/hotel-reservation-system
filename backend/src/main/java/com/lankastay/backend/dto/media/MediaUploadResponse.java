package com.lankastay.backend.dto.media;

public class MediaUploadResponse {

    private String url;
    private String mediaUrl;
    private String filename;
    private long size;
    private String mimeType;

    public MediaUploadResponse() {}

    public MediaUploadResponse(String url, String filename, long size, String mimeType) {
        this.url = url;
        this.mediaUrl = url;
        this.filename = filename;
        this.size = size;
        this.mimeType = mimeType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        this.mediaUrl = url;
    }

    public String getMediaUrl() {
        return mediaUrl != null ? mediaUrl : url;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
        if (this.url == null) {
            this.url = mediaUrl;
        }
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
