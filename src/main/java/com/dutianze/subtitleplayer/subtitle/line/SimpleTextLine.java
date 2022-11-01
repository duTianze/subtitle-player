package com.dutianze.subtitleplayer.subtitle.line;

import java.util.List;

/**
 * @author dutianze
 * @date 2022/11/1
 */
public class SimpleTextLine implements TextLine {

  private String line;

  public SimpleTextLine(String line) {
    this.line = line;
  }

  @Override
  public List<TextBlock> getTextBlock() {
    return List.of(new TextBlock(line));
  }

  @Override
  public String getText() {
    return line;
  }
}
