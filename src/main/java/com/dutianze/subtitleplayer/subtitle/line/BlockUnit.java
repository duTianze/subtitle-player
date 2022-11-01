package com.dutianze.subtitleplayer.subtitle.line;

import com.atilika.kuromoji.jumandic.Token;
import java.lang.Character.UnicodeBlock;
import java.util.List;
import java.util.stream.IntStream;
import lombok.Data;

/**
 * @author dutianze
 * @date 2022/11/1
 */
@Data
public class BlockUnit {

  private String surface;
  private String reading;

  public BlockUnit(String surface) {
    this.surface = surface;
  }

  public BlockUnit(String surface, String reading) {
    this.surface = surface;
    this.reading = reduceReading(surface, reading);
  }

  private String reduceReading(String surface, String reading) {
    int startIndex = IntStream.range(0, surface.toCharArray().length)
        .filter(i -> isKana(surface.charAt(i)))
        .findFirst().orElse(surface.length());
    String hiragana = surface.substring(startIndex);
    return reading.replace(hiragana, "");
  }

  private boolean isKana(int c) {
    return TokenizeTextLine.KANA_CODES.contains(UnicodeBlock.of(c));
  }

  public void tokenize() {
    List<Token> tokens = TokenizeTextLine.TOKENIZER.tokenize(surface);
    this.reading = reduceReading(surface, tokens.get(0).getReading());
  }
}
