package com.dutianze.subtitleplayer.listener;

import com.dutianze.subtitleplayer.window.SubtitlePanel;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.TransferHandler;

/**
 * @author dutianze
 * @date 2022/10/18
 */
public class FileDropHandler extends TransferHandler {

  private final SubtitlePanel subtitlePanel;

  public FileDropHandler(SubtitlePanel subtitlePanel) {
    this.subtitlePanel = subtitlePanel;
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
    files.stream().findFirst().ifPresent(subtitlePanel::loadSrt);
    return true;
  }
}