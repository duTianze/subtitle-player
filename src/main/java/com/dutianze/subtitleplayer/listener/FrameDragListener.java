package com.dutianze.subtitleplayer.listener;

import com.dutianze.subtitleplayer.window.SubtitleWindow;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * @author dutianze
 * @date 2022/9/27
 */
@Getter
@Component
public class FrameDragListener extends MouseAdapter {

  private final SubtitleWindow subtitleWindow;

  private Point mouseDownCompCords = null;
  private boolean isDrag;

  public FrameDragListener(SubtitleWindow subtitleWindow) {
    this.subtitleWindow = subtitleWindow;
    this.subtitleWindow.getWindow().addMouseListener(this);
    this.subtitleWindow.getWindow().addMouseMotionListener(this);
  }

  public void mouseReleased(MouseEvent e) {
    mouseDownCompCords = null;
    isDrag = false;
  }

  public void mousePressed(MouseEvent e) {
    mouseDownCompCords = e.getPoint();
    isDrag = true;
  }

  public void mouseDragged(MouseEvent e) {
    Point curryCords = e.getLocationOnScreen();
    Point location = new Point(curryCords.x - mouseDownCompCords.x,
        curryCords.y - mouseDownCompCords.y);
    subtitleWindow.stayTextCenter();
    subtitleWindow.setWindowLocation(location);
  }
}
