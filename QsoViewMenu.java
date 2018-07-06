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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;

/**
 *
 * @author keithc
 */
public class QsoViewMenu extends QsoMenu {

    QsoViewMenu(QsoLogWindow logWindow) {
        super(logWindow);
        createMenu();
    }

    @Override
    final void createMenu() {
        setText("View");
        setMnemonic(KeyEvent.VK_V);

        // create a header item
        _headerItem = new JMenuItem("File Header", null);
        _headerItem.setMnemonic(KeyEvent.VK_H);
        _headerItem.setToolTipText("View qso file header");
        _headerItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                QsoFile qsoFile = _logWindow.getQsoFile();
                if (qsoFile != null) {
                    String header = qsoFile.getHeader();
                    JRootPane root = _logWindow.getRootPane();
                    JPanel panel = (JPanel) root.getContentPane();
                    JOptionPane.showInternalMessageDialog(panel, header,
                            "Header - " + qsoFile.getName(), JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        _headerItem.setEnabled(false);
        add(_headerItem);

        // create a show selected rows item
        _showSelectedItem = new JMenuItem("Show Selected Rows", null);
        _showSelectedItem.setMnemonic(KeyEvent.VK_R);
        _showSelectedItem.setToolTipText("View only selected rows");
        _showSelectedItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                QsoPane pane = (QsoPane) _logWindow.getContentPane();
                if (pane != null) {
                    pane.showSelectedRows();
                    String title = QsoLogWindow.TITLE + " - " + "Update list - "
                            + ((QsoPane) _logWindow.getContentPane()).getTable().getRowCount()
                            + " records.";
                    _logWindow.setTitle(title);
                    _showAllRowsItem.setEnabled(true);
                    _showSelectedItem.setEnabled(false);
                }
            }
        });
        _showSelectedItem.setEnabled(false);
        add(_showSelectedItem);

        // create a show all rows item
        _showAllRowsItem = new JMenuItem("Show All Rows", null);
        _showAllRowsItem.setMnemonic(KeyEvent.VK_S);
        _showAllRowsItem.setToolTipText("View all rows");
        _showAllRowsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                QsoPane pane = (QsoPane) _logWindow.getContentPane();
                if (pane != null) {
                    pane.showAllRows();
                    QsoFile qsoFile = _logWindow.getQsoFile();
                    if (qsoFile != null) {
                        int noRecs = qsoFile.getNoRecords();
                        String title = QsoLogWindow.TITLE + " - " + qsoFile.getName() 
                                + " - " + noRecs + " records";
                        _logWindow.setTitle(title);
                    }
                    _showAllRowsItem.setEnabled(false);
                    _showSelectedItem.setEnabled(true);
                }
            }
        });
        _showAllRowsItem.setEnabled(false);
        add(_showAllRowsItem);
    }

    @Override
    void updateMenusFileOpen(boolean opened) {
        if (opened) {
            _headerItem.setEnabled(true);
            _showSelectedItem.setEnabled(true);
            _showAllRowsItem.setEnabled(false);
        } else {
            _headerItem.setEnabled(false);
            _showSelectedItem.setEnabled(false);
            _showAllRowsItem.setEnabled(false);
        }
    }

    // Properties
    JMenuItem _headerItem;
    JMenuItem _showSelectedItem;
    JMenuItem _showAllRowsItem;
}
