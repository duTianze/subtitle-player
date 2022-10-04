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

  private final List<SubtitleLine> lines = new ArrayList<>();
  private final Map<Integer, SubtitleLine> idMap = new HashMap<>();
  private final RangeMap<Long, SubtitleLine> timeRangeMap = TreeRangeMap.create();

  public SubtitleLine getSubtitleLine(long time) {
    return timeRangeMap.get(time);
  }

  public Subtitle(InputStream is) throws IOException, SubtitleParsingException {
    // Read each lines
    BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    String textLine;
    CursorStatus cursorStatus = CursorStatus.NONE;
    SubtitleLine cue = null;
    while ((textLine = br.readLine()) != null) {
      textLine = textLine.trim();
      if (cursorStatus == CursorStatus.NONE) {
        if (textLine.isEmpty()) {
          continue;
        }
        textLine = textLine.replaceAll("\\uFEFF", "");
        cue = new SubtitleLine();
        // First textLine is the cue number
        try {
          Integer.parseInt(textLine);
        } catch (NumberFormatException e) {
          throw new SubtitleParsingException(
              String.format("Unable to parse cue number: %s", textLine));
        }
        cue.setId(Integer.parseInt(textLine));
        cursorStatus = CursorStatus.CUE_ID;
      } else if (cursorStatus == CursorStatus.CUE_ID) {
        // Second textLine defines the start and end time codes
        // 00:01:21,456 --> 00:01:23,417
        if (!textLine.startsWith("-->", 13)) {
          throw new SubtitleParsingException(String.format(
              "Time code textLine is badly formatted: %s", textLine));
        }
        cue.setStartTime(new TimeCode(textLine.substring(0, 12)));
        cue.setEndTime(new TimeCode(textLine.substring(17)));
        cursorStatus = CursorStatus.CUE_TIME_CODE;
      } else if (textLine.isEmpty() && cursorStatus == CursorStatus.CUE_TIME_CODE) {
        cue.addLine("");
        cursorStatus = CursorStatus.CUE_TEXT;
      } else if (!textLine.isEmpty()) {
        // Following lines are the cue lines
        cue.addLine(textLine);
        cursorStatus = CursorStatus.CUE_TEXT;
      } else {
        // End of cue
        lines.add(cue);
        cue = null;
        cursorStatus = CursorStatus.NONE;
      }
    }
    if (cue != null) {
      lines.add(cue);
    }

    for (SubtitleLine subtitleLine : lines) {
      Range<Long> range = Range.closed(subtitleLine.getStartTime().getTime(),
          subtitleLine.getEndTime().getTime());
      timeRangeMap.put(range, subtitleLine);
      idMap.put(subtitleLine.getId(), subtitleLine);
    }
  }

  private enum CursorStatus {
    NONE,
    CUE_ID,
    CUE_TIME_CODE,
    CUE_TEXT
  }
}
