package com.dutianze.subtitleplayer.window;

/**
 * @author dutianze
 * @date 2022/10/18
 */
public enum PlayerState {

  PLAY_STATE {
    @Override
    public PlayerState opposite() {
      return PlayerState.PAUSE_STATE;
    }
  },
  PAUSE_STATE {
    @Override
    public PlayerState opposite() {
      return PlayerState.PLAY_STATE;
    }
  };

  public abstract PlayerState opposite();
}
