package com.releasescribe.dto;

public class GenerateRequest {

    private String rawCommits;
    private String version;

    public String getRawCommits() { return rawCommits; }
    public void setRawCommits(String rawCommits) { this.rawCommits = rawCommits; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
