package com.dutianze.subtitleplayer.subtitle.line;

import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

/**
 * @author dutianze
 * @date 2022/11/1
 */
@Data
public class TextBlock {

  private List<BlockUnit> blockUnits;

  public TextBlock(String text) {
    blockUnits = List.of(new BlockUnit(text));
  }

  public TextBlock(String surface, String reading, boolean isKnown) {
    if (surface.equals(reading) || !isKnown || surface.chars()
        .allMatch(c -> UnicodeBlock.of(c).equals(UnicodeBlock.KATAKANA))) {
      blockUnits = List.of(new BlockUnit(surface));
      return;
    }

    List<BlockUnit> blockUnitList = new ArrayList<>();
    UnicodeBlock preUnicodeBlock = UnicodeBlock.of(surface.charAt(0));

    StringBuilder tempStr = new StringBuilder();
    for (char c : surface.toCharArray()) {
      UnicodeBlock currentUnicodeBlock = UnicodeBlock.of(c);
      if (preUnicodeBlock == UnicodeBlock.HIRAGANA
          && currentUnicodeBlock == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
        blockUnitList.add(new BlockUnit(tempStr.toString()));
        tempStr.setLength(0);
      }
      tempStr.append(c);
      preUnicodeBlock = currentUnicodeBlock;
    }

    if (blockUnitList.size() == 0) {
      // 汉字 + 平假名
      blockUnitList.add(new BlockUnit(surface, reading));
    } else {
      // 多个 [ 汉字 + 平假名 ]
      BlockUnit blockUnit = new BlockUnit(tempStr.toString());
      blockUnitList.add(blockUnit);
      blockUnitList.forEach(BlockUnit::tokenize);
    }
    blockUnits = blockUnitList;
  }

  public String getText() {
    return blockUnits.stream().map(BlockUnit::getSurface).collect(Collectors.joining());
  }
}
