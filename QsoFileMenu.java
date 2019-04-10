/*
 * Copyright (C) 2018 G. Keith Cambron
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package qsologviewer;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author keithc
 */
public class QsoFileMenu extends QsoMenu {

    QsoFileMenu(QsoLogWindow logWindow) {
        super(logWindow);
        createMenu();
    }

    @Override
    @SuppressWarnings("Convert2Lambda")
    final void createMenu() {
        setMnemonic(KeyEvent.VK_F);
        setText("File");

        // create an exit item
        JMenuItem exitItem = new JMenuItem("Exit", null);
        exitItem.setMnemonic(KeyEvent.VK_E);
        exitItem.setToolTipText("Exit application");
        exitItem.addActionListener((ActionEvent event) -> {
            _logWindow.dispatchEvent(
                    new WindowEvent(_logWindow, WindowEvent.WINDOW_CLOSING));
        });
        add(exitItem);

        // create an open Qso file item
        _fileOpenItem = new JMenuItem("Open", null);
        _fileOpenItem.setMnemonic(KeyEvent.VK_O);
        _fileOpenItem.setToolTipText("Open QSO file");
        _fileOpenItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFileChooser chooser;
                String lastFileDir = QsoInitFile.getInstance().getLastFileDir();
                if (lastFileDir != null && lastFileDir.length() > 5) {
                    chooser = new JFileChooser(lastFileDir);
                } else {
                    chooser = new JFileChooser();
                }
                FileNameExtensionFilter filter = new FileNameExtensionFilter("ADIF files", "adi");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(_logWindow);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    QsoFile qsoFile = null;
                    if (chooser.getSelectedFile().getName().toLowerCase().endsWith(".adi")) {
                        qsoFile = new QsoAidFile();
                        try {
                            String selectedFile = chooser.getSelectedFile().getName();
                            lastFileDir = chooser.getCurrentDirectory().getAbsolutePath();
                            String fullPathName = lastFileDir + File.separator + selectedFile;
                            qsoFile.open(fullPathName);
                            QsoInitFile.getInstance().set(QsoInitFile.QSO_FILE_DIR, lastFileDir);
                        } catch (Exception ex) {
                            Logger.getLogger(QsoLogWindow.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    try {
                        if (qsoFile != null) {
                            int noRecs = qsoFile.getNoRecords();
                            _logWindow.setTitle(QsoLogWindow.TITLE + " - " + chooser.getSelectedFile().getName() + " - " + noRecs + " records");
                            QsoPane panel = new QsoPane(qsoFile);
                            panel.setOpaque(true);
                            JPanel oldPanel = (JPanel) _logWindow.getContentPane();
                            if (oldPanel != null) {
                                _logWindow.remove(oldPanel);
                            }
                            _logWindow.setContentPane(panel);
                            // initialize the table
                            String fileTypeName = qsoFile.getType();
                            String checkedBoxs = (QsoInitFile.getInstance().get(fileTypeName));
                            if (checkedBoxs != null) {
                                if (checkedBoxs.length() < 2) {
                                    // all the columns are hidden
                                    JOptionPane.showOptionDialog(null, "All of the columns in this file have been hidden.\nGo to Action->Edit Columns to unhide them.", "Warning",
                                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                            null, null, null);
                                }
                                String[] checkedBoxList = checkedBoxs.split("@");
                                QsoTable tbl = panel.getTable();
                                for (int i = 0; i < tbl.getColumnCount(); i++) {
                                    String colName = tbl.getColumnName(i).toUpperCase();
                                    tbl.hideTableColumn(tbl.getColumnModel().getColumn(i));
                                    for (String checkedEntry : checkedBoxList) {
                                        checkedEntry = checkedEntry.toUpperCase();
                                        if (colName.equals(checkedEntry)) {
                                            tbl.showTableColumn(tbl.getColumnModel().getColumn(i));
                                        }
                                    }
                                }
                            } else {
                                // first time to view this file
                                // hide the index column for new file types
                                QsoTable tbl = panel.getTable();
                                tbl.hideTableColumn(tbl.getColumnModel().getColumn(0));
                                for (int i = 0; i < tbl.getColumnCount(); i++) {
                                    tbl.hideTableColumn(tbl.getColumnModel().getColumn(i));
                                    String colName = tbl.getColumnName(i).toUpperCase();
                                    for (String name : DEFAULT_COLUMNS) {
                                        if (name.equals(colName)) {
                                            tbl.showTableColumn(tbl.getColumnModel().getColumn(i));
                                        }
                                    }
                                }
                                JOptionPane.showOptionDialog(null, "" + tbl.getColumnCount() + " columns are in this table.\n"
                                        + "To view them use the Action -> Edit Columns menus.", "Information",
                                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                        null, null, null);
                            }
                            _logWindow.pack();
                            _logWindow.setVisible(true);
                            _logWindow.updateMenusFileOpen(true);
                            _logWindow.repaint();
                        }
                    } catch (HeadlessException ex) {
                        Logger.getLogger(QsoLogWindow.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        // create a close Qso file item
        _fileCloseItem = new JMenuItem("Close", null);
        _fileCloseItem.setMnemonic(KeyEvent.VK_C);
        _fileCloseItem.setToolTipText("Close QSO file");
        _fileCloseItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                QsoPane panel = (QsoPane) _logWindow.getContentPane();
                if (panel != null) {
                    panel.removeAll();
                }
                updateMenusFileOpen(false);
                // update the init file; view columns may have changed
                QsoInitFile initFile = QsoInitFile.getInstance();
                initFile.close();
                _logWindow.updateMenusFileOpen(false);
                _logWindow.setTitle(QsoLogWindow.TITLE);
                _logWindow.repaint();
            }
        });
        _fileCloseItem.setEnabled(false);

        // create a close Qso file item
        _fileSaveItem = new JMenuItem("Save", null);
        _fileSaveItem.setMnemonic(KeyEvent.VK_S);
        _fileSaveItem.setToolTipText("Save records to file");
        _fileSaveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFileChooser chooser;
                String lastFileDir = QsoInitFile.getInstance().getLastFileDir();
                if (lastFileDir != null && lastFileDir.length() > 5) {
                    chooser = new JFileChooser(lastFileDir);
                } else {
                    chooser = new JFileChooser();
                }
                FileNameExtensionFilter filter = new FileNameExtensionFilter("AIDF images", "adi");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(_logWindow);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    QsoFile qsoFile = null;
                    if (chooser.getSelectedFile().getName().toLowerCase().endsWith(".adi")) {
                        qsoFile = new QsoAidFile();
                        try {
                            String selectedFile = chooser.getSelectedFile().getName();
                            lastFileDir = chooser.getCurrentDirectory().getAbsolutePath();
                            String fullPathName = lastFileDir + File.separator + selectedFile;
                            qsoFile.open(fullPathName);
                            QsoInitFile.getInstance().set(QsoInitFile.QSO_FILE_DIR, lastFileDir);
                        } catch (Exception ex) {
                            Logger.getLogger(QsoLogWindow.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        JOptionPane.showOptionDialog(null, "save file must have a '.adi or .ADI' extension.", "Aborted",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                null, null, null);
                    }
                    try {
                        if (qsoFile != null) {
                            // create a map with the selected table rows
                            QsoPane panel = (QsoPane) _logWindow.getContentPane();
                            if (panel != null) {
                                QsoTable tbl = panel.getTable();
                                if (tbl != null) {
                                    Stack<QsoRecord> saveRecords = new Stack<>();
                                    ArrayList<Integer> rowList = new ArrayList<>();
                                    
                                    for( int i = 0; i < tbl.getRowCount(); i++) {
                                        String indx = (String)tbl.getValueAt(i, 0);
                                        rowList.add(Integer.valueOf(indx));
                                    }
                                    QsoFile openFile = _logWindow.getQsoFile();
                                    Stack<QsoRecord> qsoRecs = openFile.getRecords();
                                    for (QsoRecord rec : qsoRecs) {
                                      if(rowList.contains(Integer.valueOf(rec.getIndex()))) {
                                          saveRecords.add(rec);
                                          rowList.remove(Integer.valueOf(rec.getIndex()));
                                      }
                                    }
                                    saveRecords(qsoFile, saveRecords);
                                }
                            }
                        } else {
                            // null qso file
                        }
                    } catch (HeadlessException ex) {
                        Logger.getLogger(QsoLogWindow.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        _fileSaveItem.setEnabled(false);

        add(_fileOpenItem);
        add(_fileSaveItem);
        add(_fileCloseItem);
    }

    void saveRecords(QsoFile saveFile,
            Stack<QsoRecord> records) {

        BufferedWriter wrtr = null;
        String fullPathName = QsoInitFile.getInstance().getLastFileDir() + "/" + saveFile.getName();
        try {
            File out = new File(fullPathName);
            if (out.exists()) {
                JOptionPane.showOptionDialog(null, "" + fullPathName + " exists.\nDelete or rename the file and try again.", "Aborted",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, null, null);
                return;
            }
            wrtr = new BufferedWriter(new FileWriter(fullPathName));

            //write the header - use AIDF 3.0.6 format to include FT8
            wrtr.write("ADIF Export from QsoLogViewer\n");
            String timeStamp = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
            wrtr.write("Generated on " + timeStamp + "\n");
            String id = "QsoLogViwer UploadADIF";
            wrtr.write("<PROGRAMID:" + id.length() + ">" + id + "\n");
            wrtr.write("<aidf_ver:" + "3.0.6".length()
                    + ">" + "3.0.6" + "\n");
            wrtr.write("<EOH>\n");
            // write the records

            int writeCnt = 0;
            for (QsoRecord rec : records) {
                if (rec != null && rec.getSize() > 0) {
                    String[] keys = rec.getKeys();
                    for (String key : keys) {
                        String value = rec.getValue(key).trim();
                        wrtr.write("<" + key + ":" + value.length() + ">" + value);
                    }
                    wrtr.write("<EOR>\n");
                    writeCnt += 1;
                }
            }
            JOptionPane.showOptionDialog(null, "saved " + writeCnt + " records to " + saveFile.getName() 
                    + ".", "Records Saved",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, null, null);

        } catch (IOException ex) {
            Logger.getLogger(QsoActionMenu.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showOptionDialog(null, "Unspecified error.\n"
                    + "Cannot write to " + fullPathName + ".", "Aborted",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, null, null);
        } finally {
            try {
                if (wrtr != null) {
                    wrtr.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(QsoActionMenu.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showOptionDialog(null, "Unspecified error.\n"
                        + "Cannot write to " + fullPathName + ".", "Aborted",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, null, null);
            }
        }
    }

    @Override
    void updateMenusFileOpen(boolean opened
    ) {

        if (opened) {
            _fileCloseItem.setEnabled(true);
            _fileSaveItem.setEnabled(true);
        } else {
            _fileCloseItem.setEnabled(false);
            _fileSaveItem.setEnabled(false);
        }
    }

    // Properties
    JMenuItem _fileOpenItem;
    JMenuItem _fileCloseItem;
    JMenuItem _fileSaveItem;
    final String[] DEFAULT_COLUMNS = {"BAND", "QSO_DATE", "TIME_ON",
        "MODE", "CALL"};
}
