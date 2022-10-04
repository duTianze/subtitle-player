package com.dutianze.subtitleplayer.subtitle;

/**
 * @author dutianze
 * @date 2022/10/4
 */
public class SubtitleParsingException extends Exception {

  public SubtitleParsingException(String message) {
    super(message);
  }

  public SubtitleParsingException(String message, Throwable cause) {
    super(message, cause);
  }
}
