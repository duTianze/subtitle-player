package com.dutianze.subtitleplayer.subtitle;

import com.dutianze.subtitleplayer.subtitle.line.TextLine;
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
  private final List<Cue> cues = new ArrayList<>();
  private final Map<Integer, Cue> idMap = new HashMap<>();
  private final RangeMap<Long, Cue> timeRangeMap = TreeRangeMap.create();

  public Cue getSubtitleLine(long time) {
    return timeRangeMap.get(time);
  }

  private void addSubtitleLine(Cue cue) {
    if (cue == null || cue.isEmpty()) {
      return;
    }
    cues.add(cue);
  }

  public Subtitle(InputStream is, String fileName) throws IOException {
    this.fileName = fileName;
    BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

    SubtitleLineStatus status = SubtitleLineStatus.ID;
    Cue cue = null;

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
            cue = new Cue();
            Integer.parseInt(textLine);
            // use subtitle order number
            cue.setId(cues.size());
            status = SubtitleLineStatus.TIME_CODE;
          } catch (Exception e) {
            throw new RuntimeException(String.format("Unable to parse id: %s", textLine));
          }
        }
        case TIME_CODE -> {
          if (!textLine.startsWith("-->", 13)) {
            throw new RuntimeException(String.format("Bad format Time code: %s", textLine));
          }
          cue.setStartTime(new CueTiming(textLine.substring(0, 12)));
          cue.setEndTime(new CueTiming(textLine.substring(17)));
          status = SubtitleLineStatus.TEXT;
        }
        case TEXT -> {
          // subtitle is empty or all character is symbol -> it is line end
          if (textLine.isEmpty() || textLine.matches("[.]+")) {
            addSubtitleLine(cue);
            status = SubtitleLineStatus.ID;
            continue;
          }
          cue.addLine(textLine);
        }
      }
    }

    // index
    for (Cue line : cues) {
      Range<Long> range = Range.closed(line.getStartTime().getTime(), line.getEndTime().getTime());
      timeRangeMap.put(range, line);
      idMap.put(line.getId(), line);
    }
  }

  public void tokenize() {
    cues.parallelStream().forEach(cue -> cue.getTextLines().forEach(TextLine::tokenize));
  }

  private enum SubtitleLineStatus {
    ID,
    TIME_CODE,
    TEXT
  }
}
