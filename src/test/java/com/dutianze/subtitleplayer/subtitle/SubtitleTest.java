package com.dutianze.subtitleplayer.subtitle;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * @author dutianze
 * @date 2022/10/4
 */
class SubtitleTest {

  @Test
  void parse() throws IOException, SubtitleParsingException {
    InputStream inputStream = Subtitle.class.getResourceAsStream(
        "/Kanojo_Mo_Kanojo_001.srt");
    Subtitle srtObject = new Subtitle(inputStream);

    SubtitleLine subtitleLine = srtObject.getSubtitleLine(105836L);

    assertNotNull(subtitleLine);
  }
}