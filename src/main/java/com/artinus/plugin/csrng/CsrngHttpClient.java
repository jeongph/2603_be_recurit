package com.artinus.plugin.csrng;

import com.artinus.subscription.service.port.GateUnavailableException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Component
@Profile("real-csrng")
class CsrngHttpClient {

    private final RestClient restClient;
    private final CsrngProperties properties;

    CsrngHttpClient(CsrngProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(2).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(2).toMillis());
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .build();
    }

    CsrngResponse fetch() {
        try {
            CsrngResponse[] body = restClient.get()
                    .uri(uri -> uri
                            .queryParam("min", properties.min())
                            .queryParam("max", properties.max())
                            .build())
                    .retrieve()
                    .body(CsrngResponse[].class);

            if (body == null || body.length == 0) {
                throw new GateUnavailableException("csrng returned empty response");
            }
            return body[0];
        } catch (RestClientException e) {
            throw new GateUnavailableException("csrng call failed", e);
        }
    }
}
