package com.dutianze.subtitleplayer.subtitle;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

/**
 * @author dutianze
 * @date 2022/10/3
 */
@Getter
@SuppressWarnings("UnstableApiUsage")
public class Subtitle {

  private final String fileName;
  private final List<SubtitleLine> subtitleLines = new ArrayList<>();
  private final Map<Integer, SubtitleLine> idMap = new HashMap<>();
  private final RangeMap<Long, SubtitleLine> timeRangeMap = TreeRangeMap.create();

  public SubtitleLine getSubtitleLine(long time) {
    return timeRangeMap.get(time);
  }

  private void addSubtitleLine(SubtitleLine subtitleLine) {
    if (subtitleLine == null || subtitleLine.isEmpty()) {
      return;
    }
    subtitleLines.add(subtitleLine);
  }

  public Subtitle(InputStream is, String fileName) throws IOException {
    this.fileName = fileName;
    BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

    SubtitleLineStatus status = SubtitleLineStatus.ID;
    SubtitleLine subtitleLine = null;

    String textLine;
    while ((textLine = br.readLine()) != null) {
      textLine = textLine.trim();
      switch (status) {
        case ID -> {
          try {
            if (textLine.isEmpty()) {
              continue;
            }
            textLine = textLine.replaceAll("\\uFEFF", "");
            subtitleLine = new SubtitleLine();
            Integer.parseInt(textLine);
            // use subtitle order number
            subtitleLine.setId(subtitleLines.size());
            status = SubtitleLineStatus.TIME_CODE;
          } catch (Exception e) {
            throw new RuntimeException(String.format("Unable to parse id: %s", textLine));
          }
        }
        case TIME_CODE -> {
          if (!textLine.startsWith("-->", 13)) {
            throw new RuntimeException(String.format("Bad format Time code: %s", textLine));
          }
          subtitleLine.setStartTime(new TimeCode(textLine.substring(0, 12)));
          subtitleLine.setEndTime(new TimeCode(textLine.substring(17)));
          status = SubtitleLineStatus.TEXT;
        }
        case TEXT -> {
          // line end
          if (textLine.isEmpty()) {
            addSubtitleLine(subtitleLine);
            status = SubtitleLineStatus.ID;
            continue;
          }
          subtitleLine.addLine(textLine);
        }
      }
    }

    // index
    for (SubtitleLine line : subtitleLines) {
      Range<Long> range = Range.closed(line.getStartTime().getTime(), line.getEndTime().getTime());
      timeRangeMap.put(range, line);
      idMap.put(line.getId(), line);
    }
  }

  private enum SubtitleLineStatus {
    ID,
    TIME_CODE,
    TEXT
  }
}
