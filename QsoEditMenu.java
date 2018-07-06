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
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author keithc
 */
public class QsoEditMenu extends QsoMenu {

    QsoEditMenu(QsoLogWindow logWindow) {
        super(logWindow);
        createMenu();
    }

    @Override
    final void createMenu() {
        setText("Edit");
        setMnemonic(KeyEvent.VK_E);

        // create a delete item
        _deleteItem = new JMenuItem("Delete", null);
        _deleteItem.setMnemonic(KeyEvent.VK_D);
        _deleteItem.setToolTipText("Delete highlighted rows");
        _deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                QsoPane panel = (QsoPane) _logWindow.getContentPane();
                QsoTable tbl = panel.getTable();
                if (tbl != null) {
                    int[] row = tbl.getSelectedRows();
                    for (int i = row.length - 1; i >= 0; i--) {
                        RowSorter rs = tbl.getRowSorter();
                        int tmIndex = rs.convertRowIndexToModel(row[i]);
                        ((DefaultTableModel) tbl.getModel()).removeRow(tmIndex);
                    }
                    String title = QsoLogWindow.TITLE + " - " + "Update list - "
                            + ((QsoPane) _logWindow.getContentPane()).getTable().getRowCount()
                            + " records.";
                    _logWindow.setTitle(title);
                } else {
                    // null qso file
                }
            }
        });
        _deleteItem.setEnabled(
                false);
        add(_deleteItem);
    }

    @Override
    void updateMenusFileOpen(boolean opened) {
        if (opened) {
            _deleteItem.setEnabled(true);
        } else {
            _deleteItem.setEnabled(false);
        }
    }

    // Properties
    JMenuItem _deleteItem;
}
