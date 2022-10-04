package com.dutianze.subtitleplayer.subtitle;

import lombok.Data;

/**
 * @author dutianze
 * @date 2022/10/3
 */
@Data
public class TimeCode {

  private final int MS_HOUR = 3600000;
  private final int MS_MINUTE = 60000;
  private final int MS_SECOND = 1000;
  private int hour;
  private int minute;
  private int second;
  private int millisecond;

  public TimeCode(String str) throws SubtitleParsingException {
    try {
      this.hour = Integer.parseInt(str.substring(0, 2));
      this.minute = Integer.parseInt(str.substring(3, 5));
      this.second = Integer.parseInt(str.substring(6, 8));
      this.millisecond = Integer.parseInt(str.substring(9, 12));
    } catch (NumberFormatException e) {
      throw new SubtitleParsingException("Error parse " + str, e);
    }
  }

  public TimeCode(int hour, int minute, int second, int millisecond) {
    this.hour = hour;
    this.minute = minute;
    this.second = second;
    this.millisecond = millisecond;
  }

  public TimeCode(long time) {
    this.hour = (int) (time / MS_HOUR);
    this.minute = (int) ((time - (this.hour * MS_HOUR)) / MS_MINUTE);
    this.second = (int) ((time - (this.hour * MS_HOUR + this.minute * MS_MINUTE)) / MS_SECOND);
    this.millisecond = (int) (time - (this.hour * MS_HOUR + this.minute * MS_MINUTE
        + this.second * MS_SECOND));
  }

  public long getTime() {
    return (long) this.hour * MS_HOUR + (long) this.minute * MS_MINUTE
        + (long) this.second * MS_SECOND + this.getMillisecond();
  }

  public int compareTo(TimeCode toCompare) {
    return Long.compare(this.getTime(), toCompare.getTime());
  }
}
