package com.releasescribe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GenerateRequest {

    @NotBlank(message = "rawCommits es obligatorio")
    @Size(max = 100_000, message = "rawCommits no puede exceder 100.000 caracteres")
    private String rawCommits;

    @Size(max = 50)
    private String version;

    public String getRawCommits() { return rawCommits; }
    public void setRawCommits(String rawCommits) { this.rawCommits = rawCommits; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
