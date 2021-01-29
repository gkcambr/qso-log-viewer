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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author keithc
 */
public class QsoAidFile extends QsoFile {

    QsoAidFile() {
    }

    @Override
    protected int parse() throws Exception {
        String line;
        boolean eoh = false;
        BufferedWriter wrtr = null;
        int ret = 0;

        BufferedReader rdr;
        rdr = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(_filePathName), "ISO-8859-1"));
        String record = "";
        _header = "";
        _fileType = "";
        while ((line = rdr.readLine()) != null) {
            String trimLine = line.trim();
            if (eoh == false) {
                
                // concatenate lines until header is found
                _header = _header.concat(line + "\n");
                int indx = trimLine.toUpperCase().indexOf("PROGRAMID");
                if (indx > -1) {
                    String[] tokens = trimLine.substring(indx).split(">");
                    if (tokens.length >= 2) {
                        _fileType = tokens[1];
                        int eostr = _fileType.indexOf("<");
                        if(eostr > -1) {
                            _fileType = _fileType.substring(0, eostr);
                            _fileType = _fileType.trim();
                        }
                    }
                }
                indx = trimLine.toUpperCase().indexOf("ADIF_VER");
                if (indx > -1) {
                    String[] tokens = trimLine.substring(indx).split(">");
                    if (tokens.length >= 2) {
                        _fileVersion = tokens[1];
                        int eostr = _fileVersion.indexOf("<");
                        if(eostr > -1) {
                            _fileVersion = _fileVersion.substring(0, eostr);
                            _fileVersion = _fileVersion.trim();
                        }
                    }
                }
                // look for the end of header
                indx = trimLine.toUpperCase().indexOf("<EOH>");
                if (indx > -1) {
                    eoh = true;
                }
                continue;
            }

            // concatenate lines until end of record is found
            record = record.concat(trimLine);
            if (trimLine.endsWith("<EOR>") || trimLine.endsWith("<eor>")) {
                QsoRecord rec = null;
                String listStr = "";
                rec = QsoRecord.create(record);
                if(rec != null & rec.getError()) {
                    String[] errMsg = rec.getErrorStr().split("\n");
                    if (errMsg.length > 1) {
                        String[] recList = errMsg[1].split("<");
                        for (int i = 0; i < recList.length; i++) {
                            if (i != 0) {
                                listStr = listStr + "<";
                            }
                            listStr = listStr + recList[i];
                            if (i > 0 && i % 4 == 0) {
                                listStr = listStr + "\n";
                            }
                        }
                    }
                    if (wrtr == null && _warningIssued == false) {
                        int opt = JOptionPane.showConfirmDialog(null, "Error found in " + _fileName + ".\n"
                                + "check '" + _errFileName + "' for details\n\nIgnore warnings?", "Warning", JOptionPane.YES_NO_OPTION);
                        if(opt == JOptionPane.NO_OPTION) {
                            _ignoreWarnings = false;
                        }
                        _warningIssued = true;
                        QsoInitFile qsf = QsoInitFile.getInstance();
                        String qsfName = null;
                        if(qsf != null) {
                            qsfName = qsf.getLastFileDir() + File.separator + QsoFile.getErrFileName();
                        }
                        if(qsfName == null) {
                            qsfName = System.getProperty("user.home") + File.separator + QsoFile.getErrFileName();
                        }
                        if(qsfName != null) {
                        try {
                            wrtr = new BufferedWriter(
                                    new OutputStreamWriter(
                                            new FileOutputStream(
                                                    qsfName, true),
                                            "ISO-8859-1"));

                        } catch (UnsupportedEncodingException
                                | FileNotFoundException ex1) {
                            Logger.getLogger(QsoRecord.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                        }
                    }
                    try {
                        if (wrtr != null) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String timeStamp = simpleDateFormat.format(new Date());
                            if (errMsg[0] != null) {
                                wrtr.append(timeStamp + ": " + errMsg[0] + "\n");
                            }
                            if (errMsg.length > 0) {
                                wrtr.append(listStr + "\n\n");
                            }
                            wrtr.flush();
                        }
                    } catch (IOException ex1) {
                        Logger.getLogger(QsoRecord.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
                if (rec.getError() == false || _ignoreWarnings) {
                    rec.setIndex(_records.size());
                    _records.add(rec);
                    ret += 1;
                } else if (eoh == false) {
                    _header = _header.concat(line + "\n");
                }
                record = "";
            }
        }

        rdr.close();
        if (wrtr != null) {
            wrtr.close();
        }
        return ret;
    }

    // Properties
    boolean _ignoreWarnings = true;
    boolean _warningIssued = false;
}
