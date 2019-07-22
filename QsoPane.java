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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author keithc
 */
public class QsoPane extends JPanel {

    QsoPane(QsoFile qsoFile) {
        super(new GridLayout(1, 0));

        _qsoFile = qsoFile;
        populateTable();
    }

    QsoFile getQsoFile() {
        return _qsoFile;
    }

    QsoTable getTable() {
        return _currentTable;
    }

    void showSelectedRows() {
        _selectedTable = createTableWithSelectedRows(_currentTable);
        _openTable = _currentTable;
        _currentTable = _selectedTable;
        _currentTable.setShowGrid(true);
        Component[] comp = this.getComponents();
        if(comp.length > 0) {
            remove(comp[0]);
        }
        JScrollPane tableScrollPane = new JScrollPane(_currentTable);
        add(tableScrollPane);
        doLayout();
        repaint();
    }

    void showAllRows() {
        _selectedTable = _currentTable;
        _currentTable = _openTable;
        Component[] comp = this.getComponents();
        if(comp.length > 0) {
            remove(comp[0]);
        }
        JScrollPane tableScrollPane = new JScrollPane(_currentTable);
        add(tableScrollPane);
        doLayout();
        repaint();
    }

    private QsoTable createTableWithSelectedRows(QsoTable srcTbl) {

        // populate the table
        int noCols = srcTbl.getColumnCount();
        int[] row = srcTbl.getSelectedRows();
        QsoTable newTbl = new QsoTable(row.length, noCols);
        newTbl.setAutoCreateRowSorter(true);
        newTbl.setPreferredScrollableViewportSize(new Dimension(500, 300));
        newTbl.setFillsViewportHeight(true);

        TableColumnModel srcTcm = srcTbl.getColumnModel();
        TableColumnModel newTcm = newTbl.getColumnModel();
        for (int c = 0; c < noCols; c++) {
            TableColumn newTc = newTcm.getColumn(c);
            String hdr = (String) newTc.getHeaderValue();
            if (hdr != null) {
                newTc.setHeaderValue(srcTbl.getColumnName(c).toUpperCase());
            }
            int width = srcTcm.getColumn(c).getWidth();
            if(width < 2) {
                newTbl.hideTableColumn(newTc);
            }
        }
        for (int r = 0; r < row.length; r++) {
            for (int c = 0; c < noCols; c++) {
                newTbl.setValueAt((String) srcTbl.getValueAt(row[r], c), r, c);
            }
        }
        newTbl.hideTableColumn(newTbl.getColumnModel().getColumn(0));
        return newTbl;
    }

    private void populateTable() {

        // populate the table
        DefaultTableModel model = new DefaultTableModel(
                _qsoFile.getColumnNames().toArray(),
                _qsoFile.getRecords().size());
        _currentTable = new QsoTable(model);
        _openTable = _currentTable;
        _currentTable.setAutoCreateRowSorter(true);
        _currentTable.setPreferredScrollableViewportSize(new Dimension(500, 300));
        _currentTable.setFillsViewportHeight(true);

        // change the headers to upper case
        // we can't change columnNames because they are keys
        // to the QsoFile
        JTableHeader th = _currentTable.getTableHeader();
        TableColumnModel tcm = th.getColumnModel();
        for (int c = 1; c < _qsoFile.getColumnNames().size(); c++) {
            TableColumn tc = tcm.getColumn(c);
            String hdr = (String) tc.getHeaderValue();
            if (hdr != null) {
                tc.setHeaderValue(hdr.toUpperCase());
            }
        }

        // build the rows
        for (int recNo = 0; recNo < _qsoFile.getRecords().size(); recNo++) {
            QsoRecord rec = _qsoFile.getRecords().get(recNo);
            if (rec != null) {
                // set the index as the first column value
                String entry = rec.getIndex();
                model.setValueAt(entry, recNo, 0);
                for (int colNo = 1; colNo < _qsoFile.getColumnNames().size(); colNo++) {
                    entry = rec.getValue(_qsoFile.getColumnNames().get(colNo));
                    if (entry != null) {
                        model.setValueAt(entry, recNo, colNo);
                    }
                }
            }
        }
        _currentTable.setShowGrid(true);
        JScrollPane tableScrollPane = new JScrollPane(_currentTable);
        add(tableScrollPane);
    }

    // Properties
    final QsoFile _qsoFile;
    QsoTable _currentTable;
    QsoTable _selectedTable;
    QsoTable _openTable;
    private static final long serialVersionUID = 100L;
}
