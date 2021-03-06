package com.example.core;

import com.example.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class RestClient {
    Logger logger = LoggerFactory.getLogger(RestClient.class);

    private String server;
    private RestTemplate rest;
    private HttpHeaders headers;
    private HttpStatus status;

    public RestClient(String server) {
        this.server = server;
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
    }

    public String get(String uri) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri, HttpMethod.GET, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity.getBody();
    }

    public byte[] getBytes(String uri) {
        logger.info("GETTING RESOURCE: " + server + uri);
        //HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        HttpEntity<String> requestEntity = new HttpEntity<String>("");
        ResponseEntity<byte[]> responseEntity = rest.exchange(server + uri, HttpMethod.GET, requestEntity, byte[].class);
        logger.info("GETTING RESOURCE STATUS: " + responseEntity.getStatusCode());
        this.setStatus(responseEntity.getStatusCode());
//        logger.error("RESPONSE BODY: " + new String(responseEntity.getBody()));
        return responseEntity.getBody();
    }



    public String post(String uri, String json) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json, headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri, HttpMethod.POST, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity.getBody();
    }

    public void put(String uri, String json) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json, headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri, HttpMethod.PUT, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
    }

    public void delete(String uri) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri, HttpMethod.DELETE, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}
