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

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 *
 * @author keithc
 */
public class QsoTable extends JTable {
    
    QsoTable() {
        super();
    }
    
    QsoTable(TableModel model) {
        super(model);
    }
    
    QsoTable(int noRows, int noCols) {
        super(noRows, noCols);
    }
    
    void hideTableColumn(TableColumn col) {
        col.setMinWidth(0);
        col.setMaxWidth(0);
        col.setPreferredWidth(0);
    }

    void showTableColumn(TableColumn col) {
        if (((String) col.getHeaderValue()).equals(QsoFile.QLV_INDEX_KEY)) {
            return;
        }
        col.setMinWidth(COL_DEFAULT_MIN_WIDTH);
        col.setMaxWidth(COL_DEFAULT_MAX_WIDTH);
        col.setPreferredWidth(COL_DEFAULT_PREFER_WIDTH);
    }
    
    // Properties
    private static final int COL_DEFAULT_MIN_WIDTH = 15;
    private static final int COL_DEFAULT_MAX_WIDTH = 200000;
    private static final int COL_DEFAULT_PREFER_WIDTH = 75;   
    private static final long serialVersionUID = 100L;
}
