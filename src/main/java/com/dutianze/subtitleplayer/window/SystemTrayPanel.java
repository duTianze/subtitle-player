package com.dutianze.subtitleplayer.window;

import com.dutianze.subtitleplayer.subtitle.TimeCode;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.File;
import java.io.Serial;
import java.net.URL;
import java.util.Objects;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
    MenuItem time = new MenuItem("jump to time");
    MenuItem pre = new MenuItem("pre");
    MenuItem pause = new MenuItem("pause");
    MenuItem next = new MenuItem("next");
    MenuItem openFile = new MenuItem("open subtitle");
    MenuItem exitItem = new MenuItem("Exit");

    //Add components to pop-up menu
    popup.add(aboutItem);
    popup.add(time);
    popup.addSeparator();
    popup.add(pre);
    popup.add(pause);
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

    time.addActionListener(e -> {
      DialogPanel dialog = new DialogPanel(subtitlePanel);
      int result = JOptionPane.showConfirmDialog(null,
          dialog, "Test", JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
        String jumpTime = dialog.getIdFieldString();
        log.info("getIdFieldString jumpTime:{}", jumpTime);
        TimeCode timeCode = TimeCode.parseString(jumpTime);
        subtitlePanel.setCurrentTime(timeCode.getTime());
        subtitlePanel.update();
      }
    });
    pre.addActionListener(e -> subtitlePanel.jump(-1));
    pause.addActionListener(
        e -> subtitlePanel.setPlayerState(subtitlePanel.getPlayerState().opposite()));
    next.addActionListener(e -> subtitlePanel.jump(1));
    openFile.addActionListener(e -> {
      int returnVal = fc.showOpenDialog(null);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File file = fc.getSelectedFile();
        log.info("Opening: " + file.getName() + ".");
        try {
          subtitlePanel.loadSrt(file);
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

  public static class DialogPanel extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    private final JTextField idField = new JTextField(20);

    public DialogPanel(SubtitlePanel subtitlePanel) {
      add(new JLabel("Insert something to validate here:"));
      idField.setText(new TimeCode(subtitlePanel.getCurrentTime()).toString());
      add(idField);
    }

    public JTextField getIdField() {
      return idField;
    }

    public String getIdFieldString() {
      return idField.getText();
    }
  }
}
