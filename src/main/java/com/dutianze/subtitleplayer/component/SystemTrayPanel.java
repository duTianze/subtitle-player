package com.dutianze.subtitleplayer.component;

import com.dutianze.subtitleplayer.subtitle.Subtitle;
import com.dutianze.subtitleplayer.window.SubtitlePanel;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Objects;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dutianze
 * @date 2022/10/3
 */
@Slf4j
public class SystemTrayPanel {

  private final SubtitlePanel subtitlePanel;
  private JFileChooser fc;

  public SystemTrayPanel(SubtitlePanel subtitlePanel) {
    this.subtitlePanel = subtitlePanel;
    //Check the SystemTray is supported
    if (!SystemTray.isSupported()) {
      System.out.println("SystemTray is not supported");
      return;
    }

    fc = new JFileChooser();
    final PopupMenu popup = new PopupMenu();
    final TrayIcon trayIcon =
        new TrayIcon(Objects.requireNonNull(createImage()));
    final SystemTray tray = SystemTray.getSystemTray();

    // Create a pop-up menu components
    MenuItem aboutItem = new MenuItem("About");
    MenuItem pre = new MenuItem("pre subtitle");
    MenuItem next = new MenuItem("next subtitle");
    MenuItem openFile = new MenuItem("open subtitle");
    MenuItem exitItem = new MenuItem("Exit");

    //Add components to pop-up menu
    popup.add(aboutItem);
    popup.addSeparator();
    popup.add(pre);
    popup.add(next);
    popup.add(openFile);
    popup.addSeparator();
    popup.add(exitItem);

    trayIcon.setPopupMenu(popup);

    try {
      tray.add(trayIcon);
    } catch (AWTException e) {
      System.out.println("TrayIcon could not be added.");
    }

    trayIcon.addActionListener(e -> JOptionPane.showMessageDialog(null,
        "This dialog box is run from System Tray"));

    aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(null,
        "This dialog box is run from the About menu item"));

    pre.addActionListener(e -> subtitlePanel.jump(-1));
    next.addActionListener(e -> subtitlePanel.jump(1));
    openFile.addActionListener(e -> {
      int returnVal = fc.showOpenDialog(null);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File file = fc.getSelectedFile();
        log.info("Opening: " + file.getName() + ".");
        try {
          subtitlePanel.setSubtitle(new Subtitle(new FileInputStream(file)));
        } catch (Exception ex) {
          log.error("Open subtitle error.");
        }
        return;
      }
      log.info("Open command cancelled by user.");
    });
    exitItem.addActionListener(e -> {
      tray.remove(trayIcon);
      System.exit(0);
    });
  }

  protected static Image createImage() {
    URL imageURL = SystemTrayPanel.class.getResource("/images/bulb.gif");

    if (imageURL == null) {
      System.err.println("Resource not found: " + "/images/bulb.gif");
      return null;
    } else {
      return (new ImageIcon(imageURL, "tray icon")).getImage();
    }
  }
}
