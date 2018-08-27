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

import java.io.File;
import java.util.Stack;

/**
 *
 * @author keithc
 */
abstract class QsoFile {

    QsoFile() {
        this._records = new Stack<>();
        _header = "";
        _fileType = "";
        _columnNames = new Stack<>();
    }

    protected abstract int parse() throws Exception;

    public boolean open(String fname) throws Exception {
        boolean ret = false;

        File in = new File(fname);
        _fileName = in.getName();
        _filePathName = in.getCanonicalPath();
        if (in.exists() && in.isFile()) {
            int noRecs;
            noRecs = parse();
            if (noRecs > 0) {
                // create the column stack
                _columnNames.add(QLV_INDEX_KEY);
                for (QsoRecord rec : _records) {
                    String[] keys = rec.getKeys();
                    for (String key : keys) {
                        if (_columnNames.contains(key)) {
                            continue;
                        }
                        _columnNames.add(key);
                    }
                }
                ret = true;
            }
        }
        return ret;
    }

    public String getName() {
        return _fileName;
    }

    public String getType() {
        return _fileType;
    }

    public String getVersion() {
        return _fileVersion;
    }

    public String getHeader() {
        return _header;
    }

    public int getNoRecords() {
        return _records.size();
    }

    public Stack<QsoRecord> getRecords() {
        return _records;
    }

    public Stack<String> getColumnNames() {
        return _columnNames;
    }

    public QsoRecord getRecordByIndex(String indx) {
        QsoRecord rec = null;

        Stack<QsoRecord> locateStack = locateRecords(QLV_INDEX_KEY,
                indx, _records);

        if (locateStack != null && locateStack.size() == 1) {
            rec = locateStack.get(0);
        }
        return rec;
    }
    
    public 
    boolean containsField(String key) {
        boolean ret = false;
        
        String ucKey = key.toUpperCase();
        String lcKey = key.toLowerCase();
        if(_columnNames.contains(ucKey)) {
            ret = true;
        }
        if(_columnNames.contains(lcKey)) {
            ret = true;
        }
        return ret;
    }

    public Stack<QsoRecord> locateRecords(String columnName,
            String columnValue,
            Stack<QsoRecord> records) {

        Stack<QsoRecord> recs = new Stack<>();
        if (columnName != null && columnValue != null) {

            String testValue = columnValue.trim().replaceAll("-", "");
            for (QsoRecord rec : records) {
                String value = rec.getValue(columnName);
                if (value != null && value.trim().replaceAll("-", "").compareToIgnoreCase(testValue) == 0) {
                    recs.add(rec);
                }
            }
        }
        return recs;
    }

    // Properties
    protected String _fileName;
    protected String _filePathName;
    protected String _header = new String();
    protected Stack<QsoRecord> _records;
    protected String _fileType;
    protected String _fileVersion;
    protected Stack<String> _columnNames;
    public static final String QLV_INDEX_KEY = "OlvIndex";
}
