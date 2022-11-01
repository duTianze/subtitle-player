package com.dutianze.subtitleplayer.subtitle;

import com.dutianze.subtitleplayer.subtitle.line.BlockUnit;
import com.dutianze.subtitleplayer.subtitle.line.SimpleTextLine;
import com.dutianze.subtitleplayer.subtitle.line.TextBlock;
import com.dutianze.subtitleplayer.subtitle.line.TextLine;
import com.dutianze.subtitleplayer.subtitle.line.TokenizeTextLine;
import com.dutianze.subtitleplayer.window.PlayerState;
import com.dutianze.subtitleplayer.window.SubtitlePanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author dutianze
 * @date 2022/10/3
 */
@Data
public class Cue {

  private static final String PAUSE_ICON = "▶";
  public static Cue EMPTY = new Cue();
  public static Function<String, Cue> SIMPLE_CUE = (text) -> {
    Cue cue = new Cue();
    cue.textLines = List.of(new SimpleTextLine(text));
    cue.texts = List.of(text);
    return cue;
  };

  private int id;
  private CueTiming startTime;
  private CueTiming endTime;
  private List<TextLine> textLines;
  private List<String> texts;

  public Cue() {
    textLines = new ArrayList<>();
    texts = new ArrayList<>();
    texts.add("");
  }

  public boolean isEmpty() {
    return textLines == null || textLines.isEmpty();
  }

  public void addLine(String text) {
    if (textLines == null) {
      textLines = new ArrayList<>();
    }
    textLines.add(new TokenizeTextLine(text));
    texts.add(text);
  }

  public String getText() {
    return String.join("", texts);
  }

  public int draw(SubtitlePanel subtitlePanel, int screenWidth, int textHeight, Graphics2D g2) {
    int textY = textHeight;
    for (TextLine textLine : textLines) {
      int textX = getXForCenteredText(textLine.getText(), screenWidth, g2);
      for (TextBlock textBlock : textLine.getTextBlock()) {
        for (BlockUnit blockUnit : textBlock.getBlockUnits()) {
          String surface = blockUnit.getSurface();
          // reading
          String reading = blockUnit.getReading();
          g2.setFont(g2.getFont().deriveFont(Font.PLAIN, SubtitlePanel.SMALL_FONT_SIZE));
          drawString(textX, (int) (textY - SubtitlePanel.BIG_FONT_SIZE), reading, g2);
          g2.setFont(g2.getFont().deriveFont(Font.BOLD, SubtitlePanel.BIG_FONT_SIZE));
          // surface
          drawString(textX, textY, surface, g2);
          textX += getTextWidth(surface, g2);
        }
      }
      textY += textHeight;
    }

    // pause tip
    if (subtitlePanel.getPlayerState() == PlayerState.PAUSE_STATE) {
      String text = PAUSE_ICON + new CueTiming(subtitlePanel.getCurrentTime().get());
      int textX = getXForCenteredText(text, screenWidth, g2);
      drawString(textX, textY, text, g2);
    } else {
      textY -= textHeight;
    }
    return textY;
  }

  private void drawString(int textX, int textY, String text, Graphics2D g2) {
    if (StringUtils.isEmpty(text)) {
      return;
    }
    g2.setColor(SubtitlePanel.FONT_BORDER_COLOR);
    g2.drawString(text, textX, textY);

    g2.setColor(SubtitlePanel.FONT_BORDER_COLOR);
    g2.drawString(text, textX + 2, textY + 2);

    g2.setColor(Color.white);
    g2.drawString(text, textX + 1, textY + 1);
  }

  private int getXForCenteredText(String text, int width, Graphics2D g2) {
    int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
    return width / 2 - length / 2;
  }

  private int getTextWidth(String text, Graphics2D g2) {
    return (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
  }
}
