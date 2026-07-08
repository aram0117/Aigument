package com.example.aigument.common.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AppUrlProperties {

    @Value("${server.url}")
    private String serverUrl;

    @Value("${main.page.url}")
    private String mainPageUrl;
}
