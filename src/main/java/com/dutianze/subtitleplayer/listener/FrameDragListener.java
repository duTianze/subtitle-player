package com.dutianze.subtitleplayer.listener;

import com.dutianze.subtitleplayer.window.SubtitlePanel;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import lombok.Getter;

/**
 * @author dutianze
 * @date 2022/9/27
 */
@Getter
public class FrameDragListener extends MouseAdapter {

  private final JFrame frame;
  private Point mouseDownCompCords = null;
  SubtitlePanel subtitlePanel;
  private boolean isDrag;

  public FrameDragListener(JFrame window, SubtitlePanel subtitlePanel) {
    this.frame = window;
    this.subtitlePanel = subtitlePanel;
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
    frame.setLocation(location);
  }
}
