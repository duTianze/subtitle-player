package com.dutianze.subtitleplayer.listener;

import com.dutianze.subtitleplayer.window.SubtitleWindow;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.springframework.stereotype.Component;

/**
 * @author dutianze
 * @date 2022/10/4
 */
@Component
public class KeyHandler extends KeyAdapter {

  private final SubtitleWindow subtitleWindow;

  public KeyHandler(SubtitleWindow subtitleWindow) {
    this.subtitleWindow = subtitleWindow;
    subtitleWindow.addKeyListener(this);
  }

  @Override
  public void keyTyped(KeyEvent e) {

  }

  @Override
  public void keyPressed(KeyEvent e) {
    int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_UP) {
      subtitleWindow.jump(-1);
    }
    if (keyCode == KeyEvent.VK_DOWN) {
      subtitleWindow.jump(1);
    }
    if (keyCode == KeyEvent.VK_LEFT) {
      subtitleWindow.getCurrentTime().addAndGet(-250);
    }
    if (keyCode == KeyEvent.VK_RIGHT) {
      subtitleWindow.getCurrentTime().addAndGet(250);
    }
    if (keyCode == KeyEvent.VK_SPACE) {
      subtitleWindow.setPlayerState(subtitleWindow.getPlayerState().opposite());
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }
}
