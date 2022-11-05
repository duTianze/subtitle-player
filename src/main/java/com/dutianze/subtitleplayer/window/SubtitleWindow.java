package com.dutianze.subtitleplayer.window;

import com.dutianze.subtitleplayer.subtitle.Cue;
import com.dutianze.subtitleplayer.subtitle.Subtitle;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.JFrame;
import javax.swing.JPanel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author dutianze
 * @date 2022/9/27
 */
@Slf4j
@Getter
@Setter
@Component
public class SubtitleWindow extends JPanel implements Runnable, InitializingBean {

  private JFrame window;
  private final int FPS = 20;
  public int screenWidth = 1000;
  public int screenHeight = 150;
  private int minScreenWith = 80;
  private Thread thread;
  private Font purisaB;
  private Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
  private Point center;
  public static Float BIG_FONT_SIZE = 40F;
  public static Float SMALL_FONT_SIZE = 18F;
  public static List<Color> COLORS = List.of(
      new Color(236, 64, 81),
      new Color(5, 29, 33)
  );
  public static final String PAUSE_ICON = "▶";

  // time millisecond
  private AtomicLong currentTime;
  private long startTime;
  private long endTime;

  // subtitle
  private Subtitle subtitle = null;
  private Cue currentCue = Cue.EMPTY;
  private volatile int preId = 0;

  // state
  private PlayerState playerState = PlayerState.PLAY_STATE;

  public SubtitleWindow() throws Exception {
    this.window = new JFrame();
    this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.window.setUndecorated(true);
    this.window.setResizable(false);
    this.window.setTitle("Subtitle Player");
    this.window.setBackground(new Color(0, 0, 0, 0));
    this.window.add(this);
    this.window.pack();
    this.window.setAlwaysOnTop(true);
    this.setPreferredSize(new Dimension(screenWidth, screenHeight));
    this.setBackground(new Color(0.1f, 0.1f, 0.1f, 0.1f));
    this.setDoubleBuffered(true);
    this.setFocusable(true);

    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
    center = (new Point((int) (dimension.getWidth() / 2), (int) (dimension.getHeight() - 200)));

    InputStream is = getClass().getResourceAsStream("/font/Purisa Bold.ttf");
    Objects.requireNonNull(is);
    purisaB = Font.createFont(Font.TRUETYPE_FONT, is);

    // load test subtitle
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
      log.info("loadSrt");
      subtitle = new Subtitle(inputStream, fileName);
      subtitle.tokenize();
      List<Cue> cues = subtitle.getCues();
      startTime = cues.get(0).getStartTime().getTime();
      endTime = cues.get(cues.size() - 1).getEndTime().getTime();
      currentCue = cues.get(0);
      currentTime = new AtomicLong(0);
      preId = 0;
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
      currentTime.addAndGet(1_000 / FPS);
    }

    Cue cue = subtitle.getSubtitleLine(currentTime.get());
    Optional.ofNullable(cue)
        .map(Cue::getText)
        .ifPresentOrElse(text -> {
          this.currentCue = cue;
          preId = cue.getId();
        }, () -> {
          // before the first subtitle, show file name
          if (currentTime.get() < startTime) {
            this.currentCue = Cue.SIMPLE_CUE.apply(subtitle.getFileName());
          } else {
            this.currentCue = Cue.EMPTY;
          }
        });
  }

  public void paintComponent(Graphics g) {
    // set graphics
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setFont(g2.getFont().deriveFont(Font.BOLD, BIG_FONT_SIZE));

    // calc screenWidth and textHeight
    List<String> texts = currentCue.getTexts();
    screenWidth = getMaxTextLength(texts, g2);
    int textHeight = (int) g2.getFontMetrics().getStringBounds(texts.get(0), g2).getHeight() + 30;

    // draw cue
    screenWidth = Math.max(screenWidth, minScreenWith);
    int textY = currentCue.draw(this, screenWidth, textHeight, g2);
    screenHeight = Math.max(textY + 30, 80);

    // reset size
    this.setSize(screenWidth, screenHeight);
    window.setSize(screenWidth, screenHeight);

    // reset location
    window.setLocation((int) (center.getX() - screenWidth / 2), (int) center.getY());

    // dispose
    g2.dispose();
  }

  public void stayTextCenter() {
    Point location = window.getLocation();
    center = new Point((int) (location.getX() + screenWidth / 2), (int) location.getY());
  }

  private int getMaxTextLength(List<String> texts, Graphics2D g2) {
    return texts.stream()
        .map(e -> (int) g2.getFontMetrics().getStringBounds(e, g2).getWidth())
        .max(Integer::compare).orElse(800);
  }

  public void jump(int num) {
    if (subtitle == null) {
      throw new RuntimeException("jump error, subtitle or currentCue is null");
    }
    Map<Integer, Cue> idMap = subtitle.getIdMap();
    if (idMap == null) {
      throw new RuntimeException("jump error, idMap is null");
    }
    Cue next = idMap.get(preId + num);
    if (next == null) {
      log.info("jump error, next is null");
      return;
    }
    // jump to next different subtitle
    if (next.getText().equals(currentCue.getText())) {
      jump(num + (num < 0 ? -1 : 1));
      return;
    }
    currentTime.set(next.getStartTime().getTime());
  }

  public void setWindowLocation(Point p) {
    window.setLocation(p);
  }

  @Override
  public void afterPropertiesSet() {
    this.startGameThread();
    window.setVisible(true);
  }
}
