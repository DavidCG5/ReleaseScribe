package com.releasescribe.service;

public interface AiClient {

    AiResult classifyAndSummarize(String rawCommits);
}
