package com.dutianze.subtitleplayer.listener;

import com.dutianze.subtitleplayer.window.SubtitlePanel;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author dutianze
 * @date 2022/10/4
 */
public class KeyHandler implements KeyListener {

  private final SubtitlePanel subtitlePanel;

  public KeyHandler(SubtitlePanel subtitlePanel) {
    this.subtitlePanel = subtitlePanel;
  }

  @Override
  public void keyTyped(KeyEvent e) {

  }

  @Override
  public void keyPressed(KeyEvent e) {
    int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_UP) {
      subtitlePanel.jump(-1);
    }
    if (keyCode == KeyEvent.VK_DOWN) {
      subtitlePanel.jump(1);
    }
    if (keyCode == KeyEvent.VK_LEFT) {
      subtitlePanel.setCurrentTime(subtitlePanel.getCurrentTime() - 250);
    }
    if (keyCode == KeyEvent.VK_RIGHT) {
      subtitlePanel.setCurrentTime(subtitlePanel.getCurrentTime() + 250);
    }
    if (keyCode == KeyEvent.VK_SPACE) {
      subtitlePanel.setPlayerState(subtitlePanel.getPlayerState().opposite());
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }
}
