package com.dutianze.subtitleplayer.window;

import com.dutianze.subtitleplayer.listener.FileDropHandler;
import com.dutianze.subtitleplayer.listener.FrameDragListener;
import com.dutianze.subtitleplayer.listener.KeyHandler;
import com.dutianze.subtitleplayer.subtitle.Subtitle;
import com.dutianze.subtitleplayer.subtitle.SubtitleLine;
import com.dutianze.subtitleplayer.subtitle.TimeCode;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JFrame;
import javax.swing.JPanel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dutianze
 * @date 2022/9/27
 */
@Slf4j
@Getter
@Setter
public class SubtitlePanel extends JPanel implements Runnable {

  private static final String EMPTY_TEXT = "  ";

  private final int FPS = 20;
  public int screenWidth = 1000;
  public int screenHeight = 150;
  private Thread thread;
  private Font purisaB;
  private JFrame window;
  private FrameDragListener frameDragListener;
  private Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
  private Point center;
  private Color FONT_BORDER_COLOR = new Color(236, 64, 81);
  private Float fontSize = 40F;

  // time
  private long currentTime;
  private long startTime;
  private long endTime;

  // subtitle
  private Subtitle subtitle = null;
  private SubtitleLine subtitleLine = null;
  private String currentText = EMPTY_TEXT;

  // state
  private PlayerState playerState = PlayerState.PLAY_STATE;

  public SubtitlePanel(JFrame window) {
    // init
    this.window = window;
    this.setPreferredSize(new Dimension(screenWidth, screenHeight));
    this.setBackground(new Color(0.1f, 0.1f, 0.1f, 0.1f));
    this.setDoubleBuffered(true);
    this.setFocusable(true);
    try {
      InputStream is = getClass().getResourceAsStream("/font/Purisa Bold.ttf");
      Objects.requireNonNull(is);
      purisaB = Font.createFont(Font.TRUETYPE_FONT, is);
    } catch (Exception e) {
      log.error("load font error", e);
    }
    // listener
    frameDragListener = new FrameDragListener(window, this);
    KeyHandler keyHandler = new KeyHandler(this);
    this.addKeyListener(keyHandler);
    window.addMouseListener(frameDragListener);
    window.addMouseMotionListener(frameDragListener);
    FileDropHandler fileDropHandler = new FileDropHandler(this);
    this.setTransferHandler(fileDropHandler);

    // subtitle
    InputStream testIn = Subtitle.class.getResourceAsStream("/Kanojo_Mo_Kanojo_001.srt");
    loadSrt(testIn, "Kanojo_Mo_Kanojo_001.srt");
  }

  public void loadSrt(File file) {
    try {
      loadSrt(new FileInputStream(file), file.getName());
    } catch (FileNotFoundException e) {
      log.error("loadSrt error", e);
    }
  }

  private void loadSrt(InputStream inputStream, String fileName) {
    try {
      subtitle = new Subtitle(inputStream, fileName);
      List<SubtitleLine> subtitleLines = subtitle.getSubtitleLines();
      startTime = subtitleLines.get(0).getStartTime().getTime();
      endTime = subtitleLines.get(subtitleLines.size() - 1).getEndTime().getTime();
      subtitleLine = subtitleLines.get(0);
      currentTime = 0;
      currentText = fileName;
    } catch (Exception e) {
      log.error("loadSrt error", e);
    }
  }

  public void startGameThread() {
    thread = new Thread(this);
    thread.start();
  }

  @Override
  public void run() {
    double drawInterval = 1_000_000_000.0 / this.FPS;
    double delta = 0.0;
    long lastTime = System.nanoTime();
    long currentTime;

    while (thread != null) {
      currentTime = System.nanoTime();
      delta += (currentTime - lastTime) / drawInterval;
      lastTime = currentTime;

      if (delta >= 1.0) {
        update();
        repaint();
        delta--;
      }
    }
  }

  public void update() {
    if (subtitle == null) {
      return;
    }

    if (playerState == PlayerState.PLAY_STATE) {
      currentTime = currentTime + 1_000 / FPS;
    }

    SubtitleLine subtitleLine = subtitle.getSubtitleLine(currentTime);
    Optional.ofNullable(subtitleLine).map(SubtitleLine::getText).ifPresentOrElse(text -> {
      this.subtitleLine = subtitleLine;
      this.currentText = text;
    }, () -> {
      if (currentTime > startTime) {
        this.currentText = EMPTY_TEXT;
      }
    });

    if (playerState == PlayerState.PAUSE_STATE) {
      this.currentText = "暂停: " + new TimeCode(currentTime);
    }
  }

  public void paintComponent(Graphics g) {
    // set graphics
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setFont(g2.getFont().deriveFont(Font.BOLD, fontSize));

    // size
    screenWidth = getMaxTextLength(currentText, g2);
    int textHeight = (int) g2.getFontMetrics().getStringBounds(currentText, g2).getHeight() + 10;

    int textX;
    int textY = textHeight;

    for (String text : currentText.split("\n")) {
      textX = getXForCenteredText(text, screenWidth, g2);

      g2.setColor(FONT_BORDER_COLOR);
      g2.drawString(text, textX, textY);

      g2.setColor(FONT_BORDER_COLOR);
      g2.drawString(text, textX + 2, textY + 2);

      g2.setColor(Color.white);
      g2.drawString(text, textX + 1, textY + 1);

      textY += textHeight;
    }

    screenHeight = Math.max(textY - textHeight + 20, 80);
    screenWidth = Math.max(screenWidth, 80);

    // reset size
    this.setSize(screenWidth, screenHeight);
    window.setSize(screenWidth, screenHeight);

    // reset location
    Point location = window.getLocation();
    if (frameDragListener.isDrag()) {
      center = new Point((int) (location.getX() + screenWidth / 2), (int) location.getY());
    }
    window.setLocation((int) (center.getX() - screenWidth / 2), (int) center.getY());

    // dispose
    g2.dispose();
  }

  private int getMaxTextLength(String line, Graphics2D g2) {
    return Arrays.stream(line.split("\n"))
        .map(e -> (int) g2.getFontMetrics().getStringBounds(e, g2).getWidth())
        .max(Integer::compare).orElse(800);
  }

  private int getXForCenteredText(String text, int width, Graphics2D g2) {
    int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
    return width / 2 - length / 2;
  }

  public void jump(int num) {
    if (subtitleLine == null || subtitle == null) {
      throw new RuntimeException("jump error, subtitle or subtitleLine is null");
    }
    Map<Integer, SubtitleLine> idMap = subtitle.getIdMap();
    if (idMap == null) {
      throw new RuntimeException("jump error, idMap is null");
    }
    SubtitleLine next = idMap.get(subtitleLine.getId() + num);
    if (next == null) {
      throw new RuntimeException("jump error, next is null");
    }
    currentTime = next.getStartTime().getTime();
    update();
  }
}
