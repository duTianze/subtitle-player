package com.dutianze.subtitleplayer.listener;

import com.dutianze.subtitleplayer.window.SubtitleWindow;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.TransferHandler;
import org.springframework.stereotype.Component;

/**
 * @author dutianze
 * @date 2022/10/18
 */
@Component
public class FileDropHandler extends TransferHandler {

  private final SubtitleWindow subtitleWindow;

  public FileDropHandler(SubtitleWindow subtitleWindow) {
    this.subtitleWindow = subtitleWindow;
    subtitleWindow.setTransferHandler(this);
  }

  @Override
  public boolean canImport(TransferHandler.TransferSupport support) {
    for (DataFlavor flavor : support.getDataFlavors()) {
      if (flavor.isFlavorJavaFileListType()) {
        return true;
      }
    }
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean importData(TransferHandler.TransferSupport support) {
    if (!this.canImport(support)) {
      return false;
    }
    List<File> files;
    try {
      files = (List<File>) support.getTransferable()
          .getTransferData(DataFlavor.javaFileListFlavor);
    } catch (UnsupportedFlavorException | IOException ex) {
      return false;
    }
    files.stream().findFirst().ifPresent(subtitleWindow::loadSrt);
    return true;
  }
}