package com.dutianze.subtitleplayer.window;

import com.dutianze.subtitleplayer.subtitle.CueTiming;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author dutianze
 * @date 2022/10/3
 */
@Slf4j
@Component
public class TrayPanel {

  @Autowired
  private SubtitleWindow subtitleWindow;
  private JFileChooser fc;

  public TrayPanel() {
    //Check the SystemTray is supported
    if (!SystemTray.isSupported()) {
      log.info("SystemTray is not supported");
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
      log.info("TrayIcon could not be added.");
    }

    trayIcon.addActionListener(e -> JOptionPane.showMessageDialog(null,
        "This dialog box is run from System Tray"));

    aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(null,
        "This dialog box is run from the About menu item"));

    time.addActionListener(e -> {
      DialogPanel dialog = new DialogPanel(subtitleWindow);
      int result = JOptionPane.showConfirmDialog(subtitleWindow,
          dialog, "Test", JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
        String jumpTime = dialog.getIdFieldString();
        log.info("getIdFieldString jumpTime:{}", jumpTime);
        CueTiming cueTiming = CueTiming.parseString(jumpTime);
        subtitleWindow.getCurrentTime().set(cueTiming.getTime());
        subtitleWindow.getSubtitle()
            .getCues().stream()
            .filter(cue -> cueTiming.getTime() < cue.getStartTime().getTime())
            .findFirst()
            .ifPresent(cue -> subtitleWindow.setPreId(cue.getId()));
      }
    });
    pre.addActionListener(e -> subtitleWindow.jump(-1));
    pause.addActionListener(
        e -> subtitleWindow.setPlayerState(subtitleWindow.getPlayerState().opposite()));
    next.addActionListener(e -> subtitleWindow.jump(1));
    openFile.addActionListener(e -> {
      int returnVal = fc.showOpenDialog(null);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File file = fc.getSelectedFile();
        log.info("Opening: " + file.getName() + ".");
        try {
          subtitleWindow.loadSrt(file);
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
    URL imageURL = TrayPanel.class.getResource("/images/bulb.gif");

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

    public DialogPanel(SubtitleWindow subtitleWindow) {
      add(new JLabel("Insert something to validate here:"));
      idField.setText(new CueTiming(subtitleWindow.getCurrentTime().get()).toString());
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
