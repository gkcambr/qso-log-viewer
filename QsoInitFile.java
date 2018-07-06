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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author keithc
 */
public final class QsoInitFile {

    private QsoInitFile() {
        _initFilePath = System.getProperty("user.home") + File.separator + ".qsoViewer.ini";
        _isDirty = false;
        init();
    }
    
    static QsoInitFile getInstance() {
        if(_onlyOne != null) {
            return _onlyOne;
        }
        _onlyOne = new QsoInitFile();
        return _onlyOne;
    }

    void init() {
        _tokenMap = new TreeMap<>();
        _tokenMap.put("QSO_FILE_DIR", _qsoFileDirectory);
    }

    void open() {
        File in = new File(_initFilePath);
        if (in.exists() && in.isFile() && in.length() > 1) {
            parse(_initFilePath);
        } else {
            _isDirty = true;
        }
    }

    void close() {
        if (!_isDirty) {
            return;
        }
        write(_initFilePath);
    }

    void parse(String fname) {
        BufferedReader rdr;
        try {
            rdr = new BufferedReader(new FileReader(fname));
            String line;
            while ((line = rdr.readLine()) != null) {
                String trimLine = line.trim();
                int delim = trimLine.indexOf(DELIM);
                if(delim < 0) {
                    break;
                }
                String key = trimLine.substring(0, delim);
                String value = trimLine.substring(delim + 1, trimLine.length());
                if(key.equals(QSO_FILE_DIR)) {
                    _qsoFileDirectory = value;
                    _lastFileDirectory = value;
                }
                _tokenMap.put(key, value);
            }
            rdr.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(QsoInitFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | IndexOutOfBoundsException ex) {
            Logger.getLogger(QsoInitFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void write(String fname) {
        BufferedWriter wrtr;

        try {
            wrtr = new BufferedWriter(new FileWriter(fname));
            for (String key : _tokenMap.navigableKeySet()) {
                String value = _tokenMap.get(key);
                if(value != null) {
                    String outLine =  key + DELIM + value + "\n";
                    wrtr.write(outLine, 0, outLine.length());
                }
            }
            wrtr.close();
        } catch (IOException ex) {
            Logger.getLogger(QsoInitFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void set(String key, String value) {
        _tokenMap.put(key, value);
        _isDirty = true;
        if(key.equals(QSO_FILE_DIR)) {
            _lastFileDirectory = value;
        }
    }

    String get(String key) {
        String ret = null;
        
        if(key != null) {
            ret = _tokenMap.get(key);
        }
        return ret;
    }
    
    String getLastFileDir() {
        return _lastFileDirectory;
    }
    
    void setLastFileDir(String dir) {
        _lastFileDirectory = dir;
    }

    // Properties
    private static final String DELIM = "^";
    private static QsoInitFile _onlyOne = null;
    private final String _initFilePath;
    private boolean _isDirty;
    private TreeMap<String, String> _tokenMap;

    // init items - token values
    final static String QSO_FILE_DIR = "QSO_FILE_DIR";
    private String _qsoFileDirectory;
    private String _lastFileDirectory;
}