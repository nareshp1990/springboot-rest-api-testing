package com.api.test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.FileReader;
import java.io.Reader;
import java.util.Collections;

@SpringBootApplication
@Slf4j
public class SpringbootRestApiTestingApplication {

    @Autowired
    private RestTemplate restTemplate;

    public static void main(String[] args) {
        SpringApplication.run(SpringbootRestApiTestingApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {

            String[] CSV_HEADERS = {"id", "title"};

            Reader sourceFileReader = new FileReader(ResourceUtils.getFile("classpath:test-data.csv").toPath().toString());
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withHeader(CSV_HEADERS)
                    .withFirstRecordAsHeader()
                    .parse(sourceFileReader);

//            log.info("### Total API requests to make :: {}" , Iterables.size(records));

            for (CSVRecord record : records) {

                String id = record.get("id");
                String title = record.get("title");

                HttpHeaders sourceHeaders = new HttpHeaders();
                //sourceHeaders.setBearerAuth("token to be replaced");
                //sourceHeaders.setBasicAuth("","");
                sourceHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                ResponseEntity<String> sourceResponse = restTemplate.exchange("https://jsonplaceholder.typicode.com/todos/{id}", HttpMethod.GET, new HttpEntity(sourceHeaders), String.class, ImmutableMap.of("id", id));

                if (sourceResponse != null && sourceResponse.getStatusCode().is2xxSuccessful() && sourceResponse.hasBody()) {

                    String sourceResponseBody = sourceResponse.getBody();

                    JSONObject sourceJsonObject = new JSONObject(sourceResponseBody);
                    sourceJsonObject.put("title", title);

                    HttpHeaders targetHttpHeaders = new HttpHeaders();
                    //targetHttpHeaders.setBearerAuth("token to be replaced");
                    //targetHttpHeaders.setBasicAuth("","");
                    targetHttpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    targetHttpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    ResponseEntity<String> targetResponse = restTemplate.exchange("https://jsonplaceholder.typicode.com/todos/{id}", HttpMethod.PUT, new HttpEntity(sourceJsonObject.toString(), targetHttpHeaders), String.class, ImmutableMap.of("id", id));

                    if (targetResponse != null && targetResponse.getStatusCode().is2xxSuccessful()) {
                        log.info("### Target API Call Success, Response for id {} :: {}", id, targetResponse.getBody());
                    }else {
                        log.warn("### Target API request failed for id :: {}, response :: {}", id, targetResponse);
                    }

                }else {
                    log.warn("### Source API request failed for id :: {}, response :: {}", id, sourceResponse);
                }

            }
        };
    }

}
