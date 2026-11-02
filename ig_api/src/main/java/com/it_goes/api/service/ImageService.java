package com.it_goes.api.service;

public interface ImageService {
    String BASE_URL = "https://it-goes.s3.us-east-2.amazonaws.com";

    static String buildImageUrl(String key) {
        return String.format("%s/%s", BASE_URL, key);
    }
}
