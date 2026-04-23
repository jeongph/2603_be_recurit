package com.artinus.plugin.csrng;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record CsrngResponse(String status, Integer random) {
}
