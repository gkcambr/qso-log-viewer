
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

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author keithc
 */
public class QsoActionMenu extends QsoMenu {

    QsoActionMenu(QsoLogWindow logWindow) {
        super(logWindow);
        createMenu();
    }

    @Override
    @SuppressWarnings("Convert2Lambda")
    final void createMenu() {
        setText("Action");
        setMnemonic(KeyEvent.VK_A);

        // create the rules map
        if (_rulesMap == null) {
            _rulesMap = new LinkedHashMap<>();
            setRulesToDefaults();
        }

        // create the states map
        if (_statesMap == null) {
            _statesMap = new LinkedHashMap<>();
            initStatesMap();
            setRulesToDefaults();
        }

        // create a column edit item
        _columnEditItem = new JMenuItem("Edit Columns", null);
        _columnEditItem.setMnemonic(KeyEvent.VK_C);
        _columnEditItem.setToolTipText("Edit table columns");
        _columnEditItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                // populate the panel with check boxes for the record fields
                // they can be checked or unchecked to show or hide from the view
                if (_logWindow.getContentPane() != null) {
                    JPanel columnPanel = new JPanel(new GridLayout(16, 8));
                    QsoTable tbl = ((QsoPane) _logWindow.getContentPane()).getTable();
                    TableColumnModel tcm = tbl.getColumnModel();
                    final int noCols = tcm.getColumnCount();

                    // don't put a check box for the hidden index column
                    JCheckBox[] checkBox = new JCheckBox[noCols - 1];
                    for (int c = 0; c < noCols - 1; c++) {
                        checkBox[c] = new JCheckBox((String) tcm.getColumn(c + 1).getHeaderValue());
                        if (tcm.getColumn(c + 1).getMaxWidth() == 0) {
                            checkBox[c].setSelected(false);
                        } else {
                            checkBox[c].setSelected(true);
                        }
                    }
                    ItemListener checkBoxListener = new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            Object source = e.getSource();
                            for (int s = 0; s < noCols - 1; s++) {
                                if (source == checkBox[s]) {
                                    if (tcm.getColumn(s + 1).getWidth() > 0) {
                                        tbl.hideTableColumn(tcm.getColumn(s + 1));
                                    } else {
                                        tbl.showTableColumn(tcm.getColumn(s + 1));
                                    }
                                }
                            }
                        }
                    };
                    for (int c = 0; c < noCols - 1; c++) {
                        checkBox[c].addItemListener(checkBoxListener);
                        columnPanel.add(checkBox[c]);
                    }

                    // add set and clear buttons
                    JPanel buttonPanel = new JPanel();
                    JButton clearButton = new JButton("Clear all");
                    clearButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            for (int c = 0; c < noCols - 1; c++) {
                                checkBox[c].setSelected(false);
                            }
                        }
                    });
                    buttonPanel.add(clearButton);
                    JButton setButton = new JButton("Set all");
                    setButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            for (int c = 0; c < noCols - 1; c++) {
                                checkBox[c].setSelected(true);
                            }
                        }
                    });
                    buttonPanel.add(setButton);
                    columnPanel.add(buttonPanel);

                    int result = JOptionPane.showConfirmDialog(
                            null,
                            columnPanel,
                            "Select columns for display",
                            JOptionPane.OK_CANCEL_OPTION);

                    if (result == 0) {
                        // save the new column list to the init file
                        String fileType = ((QsoPane) _logWindow.getContentPane())._qsoFile.getType();
                        String selectedList = "";
                        for (int c = 0; c < noCols - 1; c++) {
                            String text = checkBox[c].getText();
                            if (checkBox[c].isSelected()) {
                                if (c != 0) {
                                    selectedList = selectedList.concat("@");
                                }
                                selectedList = selectedList.concat(text);
                            }
                        }
                        QsoInitFile.getInstance().set(fileType, selectedList);
                    }
                }
            }
        });
        _columnEditItem.setEnabled(false);
        add(_columnEditItem);

        // create an open and compare Qso file item
        _fileCompareItem = new JMenuItem("Compare Files", null);
        _fileCompareItem.setMnemonic(KeyEvent.VK_O);
        _fileCompareItem.setToolTipText("Compare two QSO files");
        _fileCompareItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                // ask the user to select the file to be compared with
                // the current open QSO file
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
                    QsoFile qsoCompareFile = null;
                    String selectedFile = chooser.getSelectedFile().getName();
                    if (selectedFile.toLowerCase().contains(".adi")) {
                        qsoCompareFile = new QsoAidFile();
                        try {
                            selectedFile = chooser.getSelectedFile().getName();
                            lastFileDir = chooser.getCurrentDirectory().getAbsolutePath();
                            String fullPathName = lastFileDir + File.separator + selectedFile;
                            qsoCompareFile.open(fullPathName);
                            QsoInitFile.getInstance().set(QsoInitFile.QSO_FILE_DIR, lastFileDir);
                        } catch (Exception ex) {
                            Logger.getLogger(QsoLogWindow.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    try {
                        if (qsoCompareFile != null) {
                            compareQsoFile(qsoCompareFile);
                        } else {
                            JOptionPane.showOptionDialog(null, "compare file " + selectedFile + " does not have an adi suffix.", "Aborted",
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                    null, null, null);
                        }
                    } catch (HeadlessException ex) {
                        Logger.getLogger(QsoLogWindow.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        _fileCompareItem.setEnabled(false);
        add(_fileCompareItem);

        // create a duplicate check Qso file item
        _dupeCheckItem = new JMenuItem("Find Duplicates", null);
        _dupeCheckItem.setMnemonic(KeyEvent.VK_D);
        _dupeCheckItem.setToolTipText("Check for duplicates in a QSO file");
        _dupeCheckItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                checkForDuplicates(true);
            }
        });
        _dupeCheckItem.setEnabled(false);
        add(_dupeCheckItem);

        // create a compare rules item
        // users can change the default rules for comparing two records
        _compareRulesItem = new JMenuItem("Show Rules", null);
        _checkBoxList = new LinkedHashMap<>();
        _compareRulesItem.setMnemonic(KeyEvent.VK_R);
        _compareRulesItem.setToolTipText("Edit rules for comparing records");
        _compareRulesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (_logWindow.getContentPane() != null) {
                    JPanel rulesPanel = new JPanel(new GridLayout(10, 1));

                    // add widgets for the panel
                    for (String key : _rulesMap.keySet()) {
                        Integer value = _rulesMap.get(key);
                        if (Objects.equals(value, USE_RULE) || Objects.equals(value, DONT_USE_RULE)) {
                            JCheckBox checkItem = new JCheckBox(key);
                            _checkBoxList.put(key, checkItem);
                            if (Objects.equals(value, USE_RULE)) {
                                checkItem.setSelected(true);
                            } else {
                                checkItem.setSelected(false);
                            }
                            rulesPanel.add(checkItem);
                            if ("TIME_ON".equals(key)) {
                                checkItem.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent event) {
                                        JCheckBox timeOnBox = _checkBoxList.get("TIME_ON");
                                        if (timeOnBox != null) {
                                            if (timeOnBox.isSelected() && _timeSpinner != null) {
                                                _timeSpinner.setEnabled(true);
                                            } else {
                                                _timeSpinner.setEnabled(false);
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            // TIME_DIFF is the time margin for matching two records
                            JPanel spinnerPanel = new JPanel(new GridLayout(1, 2));
                            JTextField spinnerText = new JTextField("Diff(mins)");
                            spinnerText.setEditable(false);
                            spinnerText.setBorder(null);
                            Color bg = spinnerPanel.getBackground();
                            spinnerText.setBackground(bg);
                            Font font = spinnerText.getFont();
                            String name = font.getName();
                            int size = font.getSize();
                            Font newFont = new Font(name, Font.BOLD, size);
                            spinnerText.setFont(newFont);
                            _timeSpinner = new JSpinner();
                            _timeSpinner.getModel().setValue(_rulesMap.get("TIME_DIFF"));
                            spinnerPanel.add(spinnerText);
                            spinnerPanel.add(_timeSpinner);
                            rulesPanel.add(spinnerPanel);
                            JCheckBox timeBox = _checkBoxList.get("TIME_ON");
                            if (timeBox != null) {
                                if (timeBox.isSelected()) {
                                    _timeSpinner.setEnabled(true);
                                } else {
                                    _timeSpinner.setEnabled(false);
                                }
                            }
                        }
                    }

                    // add defaults button to allow users to change back
                    // to the initial match variables
                    JPanel buttonPanel = new JPanel();
                    JButton defaultButton = new JButton("Use Defaults");
                    defaultButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {

                            // update the widgets to default values
                            setRulesToDefaults();
                            for (String boxName : _checkBoxList.keySet()) {
                                Integer value = _rulesMap.get(boxName);
                                JCheckBox box = _checkBoxList.get(boxName);
                                if (box != null) {
                                    if (Objects.equals(value, USE_RULE)) {
                                        box.setSelected(true);
                                    } else {
                                        box.setSelected(false);
                                    }
                                }
                                // TIME_DIFF
                                _timeSpinner.getModel().setValue(_rulesMap.get("TIME_DIFF"));
                                JCheckBox timeOnBox = _checkBoxList.get("TIME_ON");
                                if (timeOnBox != null) {
                                    if (timeOnBox.isSelected() && _timeSpinner != null) {
                                        _timeSpinner.setEnabled(true);
                                    } else {
                                        _timeSpinner.setEnabled(false);
                                    }
                                }
                            }
                        }
                    });
                    buttonPanel.add(defaultButton);
                    rulesPanel.add(buttonPanel);

                    int result = JOptionPane.showConfirmDialog(
                            null,
                            rulesPanel,
                            "Select rules for compare actions",
                            JOptionPane.OK_CANCEL_OPTION);

                    if (result == 0) {
                        // save the new values to the rules map// update the widgets to default values
                        for (String boxName : _checkBoxList.keySet()) {
                            JCheckBox box = _checkBoxList.get(boxName);
                            if (box != null) {
                                if (box.isSelected()) {
                                    _rulesMap.replace(box.getText(), USE_RULE);
                                } else {
                                    _rulesMap.replace(box.getText(), DONT_USE_RULE);
                                }
                            }
                        }
                        // TIME_DIFF
                        _rulesMap.replace("TIME_DIFF", (Integer) _timeSpinner.getValue());
                    }
                }
            }
        }
        );

        // create the states check menu item
        _statesCheckItem = new JMenuItem("Check All States", null);
        _statesCheckItem.setMnemonic(KeyEvent.VK_S);
        _statesCheckItem.setToolTipText("Check all states");
        _statesCheckItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                QsoFile openFile = ((QsoPane) _logWindow.getContentPane()).getQsoFile();

                // check for state and confirmation status fields
                if (!openFile.containsField("STATE")) {
                    // can't check this file
                    JOptionPane.showOptionDialog(null, "This file does not contain state information.\n"
                            + "All States Worked cannot be checked.", "Information",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            null, null, null);
                    return;
                }
                boolean checkStatusInfo = false;
                if (openFile.containsField("APP_QRZLOG_STATUS")) {
                    Object[] options = {"CONFIRMED ONLY", "ALL LOGGED QSOs"};
                    int opt = JOptionPane.showOptionDialog(null, "Click one to continue", "Option",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                            null, options, options[0]);
                    if (opt == 0) {
                        checkStatusInfo = true;
                    }
                }

                // reset the states map
                Iterator<String> it = _statesMap.keySet().iterator();
                while (it.hasNext()) {
                    String state = it.next();
                    _statesMap.replace(state, 0);
                }
                // count occurances of each state
                Iterator<QsoRecord> ofit = openFile._records.iterator();
                while (ofit.hasNext()) {
                    QsoRecord rec = ofit.next();
                    String state, status;
                    try {
                        state = rec.getValue("STATE").trim();
                        if (checkStatusInfo) {
                            status = rec.getValue("APP_QRZLOG_STATUS").trim();
                            if (!status.equalsIgnoreCase("c")) {
                                continue;
                            }
                        }
                    } catch (NullPointerException ex) {
                        // skip this record
                        continue;
                    }
                    if (state.length() > 2) {
                        // lotw uses 'MO //Missouri' for a state format
                        state = state.substring(0, 2);
                    }
                    if (state != null
                            && state.length() == 2
                            && _statesMap.containsKey(state)) {
                        Integer cnt = _statesMap.get(state);
                        cnt += 1;
                        _statesMap.replace(state, cnt);
                    }
                }
                // show the results
                String notice;
                String missing = "";
                int noMissing = 0;
                it = _statesMap.keySet().iterator();
                while (it.hasNext()) {
                    String state = it.next();
                    if (_statesMap.get(state) == 0) {
                        if (missing.length() < 1) {
                            missing = state;
                        } else {
                            missing = missing + "," + state;
                        }
                        noMissing += 1;
                        if (noMissing % 8 == 0) {
                            missing = missing + "\n";
                        }
                    }
                }
                switch (noMissing) {
                    case 0:
                        notice = "All states were found. Congratulations!";
                        break;
                    case 1:
                        notice = "One state is missing.\n" + missing;
                        break;
                    default:
                        notice = "" + noMissing + " states are missing.\n" + missing;
                        break;
                }
                JOptionPane.showOptionDialog(null, notice, "Information",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                        null, null, null);
            }
        }
        );
        _statesCheckItem.setEnabled(false);
        add(_statesCheckItem);
        
         // create a grid update item
        _gridUpdateItem = new JMenuItem("Update Grid Info", null);
        _gridUpdateItem.setMnemonic(KeyEvent.VK_G);
        _gridUpdateItem.setToolTipText("Update QTH grid entry");
        _gridUpdateItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                // open the update grid panel
                if (_logWindow.getContentPane() != null) {
                    QsoUpdateGridFrame gridPane = new QsoUpdateGridFrame(_logWindow);
                    gridPane.update();
                }
            }
        });
        _gridUpdateItem.setEnabled(false);
        add(_gridUpdateItem);
    }

    int checkForDuplicates(boolean markDupes) {
        QsoFile openFile = ((QsoPane) _logWindow.getContentPane()).getQsoFile();

        // find duplicate records in openFile, 
        // use call sign and other variables set by the Choose Rules panel/option
        TreeMap<String, QsoRecord> dupeRecords = new TreeMap<>();
        for (QsoRecord rec : openFile._records) {
            String recCallSign;
            String recBand;
            String recDate;
            String recMode;
            String recTime;
            Stack<QsoRecord> matches;
            try {
                recCallSign = rec.getValue("CALL").trim();
                recBand = rec.getValue("BAND").trim();
                recDate = rec.getValue("QSO_DATE").trim();
                recMode = rec.getValue("MODE").trim();
                recTime = rec.getValue("TIME_ON").trim();
                matches = openFile.getRecords();
            } catch (NullPointerException ex) {
                // skip this record
                continue;
            }

            // filter using call first
            if (_rulesMap.get("CALL").equals(USE_RULE)) {
                matches = openFile.locateRecords("CALL", recCallSign, matches);
                if (matches.size() < 2) {
                    continue;
                }
            }
            // filter using band
            if (_rulesMap.get("BAND").equals(USE_RULE)) {
                matches = openFile.locateRecords("BAND", recBand, matches);
                if (matches.size() < 2) {
                    continue;
                }
            }
            // filter using qso date
            if (_rulesMap.get("QSO_DATE").equals(USE_RULE)) {
                matches = openFile.locateRecords("QSO_DATE", recDate, matches);
                if (matches.size() < 2) {
                    continue;
                }
            }
            // filter using mode
            if (_rulesMap.get("MODE").equals(USE_RULE)) {
                matches = openFile.locateRecords("MODE", recMode, matches);
                if (matches.size() < 2) {
                    continue;
                }
            }
            if (matches.size() < 2) {
                continue;
            }
            // filter using time on & time diff
            if (_rulesMap.get("TIME_ON").equals(USE_RULE)) {
                for (QsoRecord matchRec : matches) {

                    if (matchRec == rec) {
                        continue;
                    }

                    String matchTime = matchRec.getValue("TIME_ON").trim();
                    int timeDiff;
                    try {
                        if (matchTime == null
                                || (matchTime.length() != 6 && matchTime.length() != 4)
                                || recTime == null
                                || (recTime.length() != 6 && recTime.length() != 4)) {
                            continue;
                        }
                        int recMins = Integer.parseInt(recTime.substring(2, 4));
                        int recHours = Integer.parseInt(recTime.substring(0, 2));
                        recMins = recMins + recHours * 60;
                        int matchMins = Integer.parseInt(matchTime.substring(2, 4));
                        int matchHours = Integer.parseInt(matchTime.substring(0, 2));
                        matchMins = matchMins + matchHours * 60;
                        timeDiff = recMins - matchMins;
                    } catch (NumberFormatException ex) {
                        // do nothing; can't use the data
                        continue;
                    }

                    // put duplicate records in the dupeRecords stack
                    if (Math.abs(timeDiff) < _rulesMap.get("TIME_DIFF")) {
                        if (!dupeRecords.containsKey(matchRec.getIndex())) {
                            dupeRecords.put(matchRec.getIndex(), matchRec);
                        }
                        if (!dupeRecords.containsKey(rec.getIndex())) {
                            dupeRecords.put(rec.getIndex(), rec);
                        }
                    }
                }
            } else {
                for (QsoRecord matchRec : matches) {
                    if (!dupeRecords.containsKey(matchRec.getIndex())) {
                        dupeRecords.put(matchRec.getIndex(), matchRec);
                    }
                    if (!dupeRecords.containsKey(rec.getIndex())) {
                        dupeRecords.put(rec.getIndex(), rec);
                    }
                }
            }
        }

        // post the results
        ((QsoPane) _logWindow.getContentPane())._currentTable.getSelectionModel().clearSelection();
        int noDupes = 0;
        for (int i = 0; i < ((QsoPane) _logWindow.getContentPane())._currentTable.getRowCount(); i++) {
            String indx = (String) ((QsoPane) _logWindow.getContentPane())._currentTable.getValueAt(i, 0);
            if (indx == null) {
                continue;
            }
            QsoRecord rec = dupeRecords.get(indx);
            if (rec != null) {
                noDupes += 1;
                if (markDupes) {
                    ((QsoPane) _logWindow.getContentPane())._currentTable.getSelectionModel().addSelectionInterval(i, i);
                }
                dupeRecords.remove(indx);
            }
        }
        if (markDupes) {

            // show the results
            String notice;
            if (noDupes > 0) {
                notice = "" + noDupes + " duplicates were found and highlighted in the table.\n"
                        + "Sort the table by call sign to group them.";
            } else {
                notice = "" + noDupes + " duplicates were found and highlighted in the table.";
            }
            JOptionPane.showOptionDialog(null, notice, "Information",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, null, null);
        }

        return noDupes;
    }

    void compareQsoFile(QsoFile compareFile
    ) {
        QsoFile openFile = ((QsoPane) _logWindow.getContentPane()).getQsoFile();

        // check for duplicates before beginning
        int noDupes = checkForDuplicates(false);
        if (noDupes > 0) {
            int result = JOptionPane.showOptionDialog(null, "" + noDupes + " duplicate records found in " + openFile.getName() + ".\n"
                    + "Strongly recommend you run 'Find Duplicates' and\n"
                    + "remove them before re-running 'Compare'\n"
                    + "\nHit CANCEL to abort Compare.", "Warning",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, null, null);
            if (result != 0) {
                return;
            }
        }

        // find records in openFile that are not in compareFile, 
        // use call sign and other variables from rules map
        TreeMap<String, QsoRecord> missingRecords = new TreeMap<>();
        for (QsoRecord rec : openFile._records) {
            String recCallSign = rec.getValue("CALL").trim();
            String recBand = rec.getValue("BAND").trim();
            String recDate = rec.getValue("QSO_DATE").trim();
            String recMode = rec.getValue("MODE").trim();
            String recTime = rec.getValue("TIME_ON").trim();
            Stack<QsoRecord> compareRecs = compareFile.getRecords();
            Stack<QsoRecord> compareMatches = new Stack<>();

            // filter using call first
            if (_rulesMap.get("CALL").equals(USE_RULE)) {
                compareMatches = openFile.locateRecords("CALL", recCallSign, compareRecs);
                if (0 == compareMatches.size()) {
                    // didn't find open record call sign in compare records
                    missingRecords.put(rec.getIndex(), rec);
                    continue;
                }
            }
            // filter using band
            if (_rulesMap.get("BAND").equals(USE_RULE)) {
                compareMatches = openFile.locateRecords("BAND", recBand, compareMatches);
                if (0 == compareMatches.size()) {
                    // didn't find open record band in matched records
                    missingRecords.put(rec.getIndex(), rec);
                    continue;
                }
            }
            // filter using qso date
            if (_rulesMap.get("QSO_DATE").equals(USE_RULE)) {
                compareMatches = openFile.locateRecords("QSO_DATE", recDate, compareMatches);
                if (0 == compareMatches.size()) {
                    // didn't find open record qsoDate in matched records
                    missingRecords.put(rec.getIndex(), rec);
                    continue;
                }
            }
            // filter using mode
            if (_rulesMap.get("MODE").equals(USE_RULE)) {
                compareMatches = openFile.locateRecords("MODE", recMode, compareMatches);
                if (0 == compareMatches.size()) {
                    // didn't find open record mode in matched records
                    missingRecords.put(rec.getIndex(), rec);
                    continue;
                }
            }
            if (0 == compareMatches.size()) {
                missingRecords.put(rec.getIndex(), rec);
                continue;
            }

            // filter using time on & time diff
            boolean match = false;
            if (_rulesMap.get("TIME_ON").equals(USE_RULE)) {
                for (QsoRecord matchRec : compareMatches) {

                    String matchTime = matchRec.getValue("TIME_ON").trim();
                    int timeDiff;
                    try {
                        if (matchTime == null
                                || (matchTime.length() != 6 && matchTime.length() != 4)
                                || recTime == null
                                || (recTime.length() != 6 && recTime.length() != 4)) {
                            continue;
                        }
                        int recMins = Integer.parseInt(recTime.substring(2, 4));
                        int recHours = Integer.parseInt(recTime.substring(0, 2));
                        recMins = recMins + recHours * 60;
                        int matchMins = Integer.parseInt(matchTime.substring(2, 4));
                        int matchHours = Integer.parseInt(matchTime.substring(0, 2));
                        matchMins = matchMins + matchHours * 60;
                        timeDiff = recMins - matchMins;
                    } catch (NumberFormatException ex) {
                        // do nothing; can't use the data
                        continue;
                    }

                    // put duplicate records in the dupeRecords stack
                    if (Math.abs(timeDiff) < _rulesMap.get("TIME_DIFF")) {
                        // they match! stop the comparison
                        match = true;
                        break;
                    }
                }
            }

            if (!match) {
                missingRecords.put(rec.getIndex(), rec);
            }
        }

        // post the results in the table
        int noMissing = 0;
        TreeMap<String, QsoRecord> updateRecords = new TreeMap<>(missingRecords);
        if (missingRecords.size() > 0) {
            ((QsoPane) _logWindow.getContentPane())._currentTable.getSelectionModel().clearSelection();
            for (int i = 0; i < ((QsoPane) _logWindow.getContentPane())._currentTable.getRowCount(); i++) {
                String indx = (String) ((QsoPane) _logWindow.getContentPane())._currentTable.getValueAt(i, 0);
                if (indx == null) {
                    continue;
                }
                QsoRecord rec = missingRecords.get(indx);
                if (rec != null) {
                    ((QsoPane) _logWindow.getContentPane())._currentTable.getSelectionModel().addSelectionInterval(i, i);
                    missingRecords.remove(indx);
                    noMissing += 1;
                }
            }
        }
        // make sure all rows are displayed in the table
        ((QsoPane) _logWindow.getContentPane()).showAllRows();

        // show the results
        Object[] options = {"OK", "CANCEL"};
        JOptionPane.showOptionDialog(null, "" + noMissing + " records were found and highlighted in the table.\n"
                + "These records are in " + ((QsoPane) _logWindow.getContentPane()).getQsoFile().getName()
                + " but not in " + compareFile.getName() + ".\n"
                + "If you want to prepare an update file edit the selected records\n"
                + "by first selecting the menu item 'View -> Show Selected' . You can then\n"
                + "edit that list and save the edited records using the 'File -> Save'\n"
                + "menu item. Then you can import the updated file into your application.\n",
                "Information",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, 0);
    }

    @Override
    void updateMenusFileOpen(boolean opened
    ) {
        if (opened) {
            _fileCompareItem.setEnabled(true);
            _dupeCheckItem.setEnabled(true);
            _columnEditItem.setEnabled(true);
            _compareRulesItem.setEnabled(true);
            _statesCheckItem.setEnabled(true);
            _gridUpdateItem.setEnabled(true);
        } else {
            _fileCompareItem.setEnabled(false);
            _dupeCheckItem.setEnabled(false);
            _columnEditItem.setEnabled(false);
            _compareRulesItem.setEnabled(true);
            _statesCheckItem.setEnabled(true);
            _gridUpdateItem.setEnabled(false);
        }
    }

    void setRulesToDefaults() {
        _rulesMap.put("CALL", USE_RULE);
        _rulesMap.put("BAND", USE_RULE);
        _rulesMap.put("MODE", DONT_USE_RULE);
        _rulesMap.put("QSO_DATE", USE_RULE);
        _rulesMap.put("TIME_ON", USE_RULE);
        _rulesMap.put("TIME_DIFF", DEFAULT_TIME_DIFF);
    }

    void initStatesMap() {
        String[] stateList = {"AL", "AK", "AZ", "AR", "CA", "CO", "CT",
            "DE", "FL", "GA", "HI", "ID", "IL", "IN", "IA", "KS", "KY",
            "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE",
            "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR",
            "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA",
            "WV", "WI", "WY"};
        for (String state : stateList) {
            _statesMap.put(state, 0);
        }
    }
    // Properties
    private JMenuItem _fileCompareItem;
    private JMenuItem _dupeCheckItem;
    private JMenuItem _columnEditItem;
    private JMenuItem _compareRulesItem;
    private JMenuItem _statesCheckItem;
    private JMenuItem _gridUpdateItem;
    private static LinkedHashMap<String, Integer> _rulesMap;
    private LinkedHashMap<String, Integer> _statesMap;
    private LinkedHashMap<String, JCheckBox> _checkBoxList;
    private static JSpinner _timeSpinner;
    private static final Integer USE_RULE = -1;
    private static final Integer DONT_USE_RULE = -2;
    private static final Integer DEFAULT_TIME_DIFF = 5; // five minutes
}
