package com.artinus.plugin.csrng;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "csrng")
public record CsrngProperties(
        String baseUrl,
        int min,
        int max
) {
    public CsrngProperties {
        if (baseUrl == null || baseUrl.isBlank())
            baseUrl = "https://csrng.net/csrng/csrng.php";
    }
}
