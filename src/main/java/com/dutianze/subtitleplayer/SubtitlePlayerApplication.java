package com.dutianze.subtitleplayer;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SubtitlePlayerApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(SubtitlePlayerApplication.class)
        .headless(false)
        .web(WebApplicationType.NONE)
        .run(args);
  }
}
