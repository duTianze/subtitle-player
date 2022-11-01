package com.dutianze.subtitleplayer.subtitle.line;

import com.atilika.kuromoji.jumandic.Token;
import com.atilika.kuromoji.jumandic.Tokenizer;
import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author dutianze
 * @date 2022/11/1
 */
public class TokenizeTextLine implements TextLine {

  public static final Tokenizer TOKENIZER = new Tokenizer();

  private final String line;
  private final List<TextBlock> textBlocks;

  public static Set<UnicodeBlock> KANA_CODES = Set.of(UnicodeBlock.HIRAGANA, UnicodeBlock.KATAKANA);

  public TokenizeTextLine(String line) {
    this.line = line;
    textBlocks = new ArrayList<>();
  }

  public void tokenize() {
    if (!containsKana()) {
      textBlocks.add(new TextBlock(line));
      return;
    }

    List<Token> tokenize = TOKENIZER.tokenize(line);
    for (Token token : tokenize) {
      TextBlock textBlock = new TextBlock(token.getSurface(), token.getReading(), token.isKnown());
      textBlocks.add(textBlock);
    }
  }

  @Override
  public List<TextBlock> getTextBlock() {
    return textBlocks;
  }

  @Override
  public String getText() {
    return textBlocks.stream().map(TextBlock::getText).collect(Collectors.joining());
  }

  private boolean containsKana() {
    return line.chars().anyMatch(this::isKana);
  }

  private boolean isKana(int c) {
    return KANA_CODES.contains(UnicodeBlock.of(c));
  }
}
