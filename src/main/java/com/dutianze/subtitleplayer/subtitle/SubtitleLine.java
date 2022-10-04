package com.dutianze.subtitleplayer.subtitle;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * @author dutianze
 * @date 2022/10/3
 */
@Data
public class SubtitleLine {

  private int id;
  private TimeCode startTime;
  private TimeCode endTime;
  private List<String> textLine;

  public SubtitleLine() {
  }

  public boolean isEmpty() {
    return textLine == null || textLine.isEmpty();
  }

  public void addLine(String text) {
    if (textLine == null) {
      textLine = new ArrayList<>();
    }
    textLine.add(text);
  }

  public String getText() {
    return String.join("\n", textLine);
  }
}
