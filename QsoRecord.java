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

import java.io.BufferedWriter;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 *
 * @author keithc
 */
public class QsoRecord {

    private QsoRecord() {
        this._fields = new LinkedHashMap<>();
        // private constructor
    }

    public static QsoRecord create(String recStr) throws NumberFormatException {
        QsoRecord rec = null;
        if (recStr.split("<", 100).length > 3) {
            rec = new QsoRecord();
            rec.parse(recStr);
        }
        return rec;
    }

    QsoRecord parse(String recStr) throws NumberFormatException {
        QsoRecord qsoRec = null;

        // split into records on '<' boundaries
        String[] toks = recStr.split("<", 100);
        ArrayList<String> tokens = new ArrayList<>();
        // don't use '<!--' as a token boundary
        for (int i = 0; i < toks.length; i++) {
            if (toks[i].indexOf("!--") == 0) {
                toks[i] = " " + toks[i].replace('>', ' ');
                String str1 = tokens.remove(tokens.size() - 1);
                str1 += toks[i];
                tokens.add(str1);
            } else {
                tokens.add(toks[i]);
            }
        }
        if (tokens.size() > 3) {
            String[] names = new String[tokens.size()];
            String[] values = new String[tokens.size()];
            for (int n = 0; n < tokens.size(); n++) {
                if (tokens.get(n).length() > 5) {
                    values[n] = tokens.get(n).replaceFirst(".*>", "");
                    names[n] = tokens.get(n).replaceFirst(">.*", "");
                    if (names[n].compareToIgnoreCase("EOR") == 0) {
                        break;
                    }
                    String[] tag = names[n].split(":");
                    int tl = tag.length;
                    tag[tl - 1] = tag[tl - 1].replaceFirst(">.*", "");
                    if (tag.length < 2 || tag.length > 3) {
                        throw (new NumberFormatException("tag " + tag[0] + " has an invalid value length:\n" + recStr));
                    }
                    String id = tag[0].trim();
                    String length = tag[1].trim();
                    Integer len = Integer.parseInt(length);
                    String value4test = values[n];
                    value4test = value4test.replaceFirst(" // .*", "");
                    while (len != value4test.length()) {
                        if (value4test.charAt(value4test.length() - 1) == ' ') {
                            value4test = value4test.substring(0, value4test.length() - 1);
                        } else {
                            break;
                        }
                    }
                    // test against a 5% error
                    if (len != 0) {
                        int lenErr = abs(((len - value4test.length()) * 100) / len);
                        if (lenErr > 5) {
                            throw (new NumberFormatException("tag " + tag[0] + " length does not match value length:\n" + recStr));
                        }
                    }
                    put(id, values[n]);
                }
            }
        }
        return qsoRec;
    }

    public String put(String id, String value) {
        if (id == null || value == null) {
            return null;
        }
        return _fields.put(id.toLowerCase(), value);
    }

    public void remove(String id) {
        if (id != null) {
            _fields.remove(id.toLowerCase());
        }
    }

    public int getSize() {
        return _fields.size();
    }

    public String getValue(int indx) {
        String str = null;

        if (_fields != null) {
            Collection<String> col = _fields.values();
            String[] vals = new String[col.size()];
            Iterator<String> it = col.iterator();
            int i = 0;
            while (it.hasNext()) {
                vals[i++] = it.next();
            }
            str = vals[indx];
        }

        return str;
    }

    public String getValue(String key) {
        String str = null;

        if (_fields != null) {
            str = _fields.get(key.toLowerCase());
            if (str == null) {
                str = _fields.get(key.toUpperCase());
            }
        }

        return str;
    }

    public String[] getKeys() {
        String[] keys = null;

        if (_fields != null) {
            Set<String> set = _fields.keySet();
            keys = new String[set.size()];
            Iterator<String> it = set.iterator();
            int i = 0;
            while (it.hasNext()) {
                keys[i++] = it.next();
            }
        }
        return keys;
    }

    void setIndex(int i) {
        _recIndex = Integer.toString(i);
    }

    String getIndex() {
        return _recIndex;
    }

    boolean containsField(String key) {
        boolean ret = false;

        String ucKey = key.toUpperCase();
        String lcKey = key.toLowerCase();
        if (_fields.containsKey(ucKey)) {
            ret = true;
        }
        if (_fields.containsKey(lcKey)) {
            ret = true;
        }
        return ret;
    }

    // Properties
    private final LinkedHashMap<String, String> _fields;
    private String _recIndex;
    static BufferedWriter _wrtr = null;
}
