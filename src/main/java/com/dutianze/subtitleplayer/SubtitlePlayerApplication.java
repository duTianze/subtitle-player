package com.dutianze.subtitleplayer;

import com.dutianze.subtitleplayer.window.SystemTrayPanel;
import com.dutianze.subtitleplayer.window.SubtitlePanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import javax.swing.JFrame;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class SubtitlePlayerApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(SubtitlePlayerApplication.class)
        .headless(false)
        .web(WebApplicationType.NONE)
        .run(args);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void initUI() {
    EventQueue.invokeLater(() -> {
      JFrame window = new JFrame();
      window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      window.setUndecorated(true);
      window.setResizable(false);
      window.setTitle("Subtitle Player");
      window.setBackground(new Color(0, 0, 0, 0));
      // panel
      SubtitlePanel subtitlePanel = new SubtitlePanel(window);
      window.add(subtitlePanel);
      window.pack();
      window.setAlwaysOnTop(true);
      Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
      subtitlePanel.setCenter(new Point((int) (dimension.getWidth() / 2),
          (int) (dimension.getHeight() - 200)));
      // system tray
      new SystemTrayPanel(subtitlePanel);
      // visible
      subtitlePanel.startGameThread();
      window.setVisible(true);
    });
  }
}
