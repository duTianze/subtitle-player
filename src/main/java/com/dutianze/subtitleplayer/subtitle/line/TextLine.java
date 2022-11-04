package com.dutianze.subtitleplayer.subtitle.line;

import java.util.List;

/**
 * @author dutianze
 * @date 2022/11/1
 */
public interface TextLine {

  List<TextBlock> getTextBlock();

  String getText();

  default void tokenize() {
  }
}
