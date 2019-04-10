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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author keithc
 */
public class QsoUpdateGridFrame extends JFrame {

    QsoUpdateGridFrame(QsoLogWindow logWindow) {
        super();
        _gridUpdateFrame = this;
        _gridUpdateFrame.setTitle(TITLE);
        _gridLayout = new GridBagLayout();
        _panel = (JPanel) this.getContentPane();
        _logWindow = logWindow;
        _qsoFile = ((QsoPane) _logWindow.getContentPane())._qsoFile;
        _missingRecs = new Stack<>();
        ArrayList<Image> imageList = _logWindow.getImages();
        setIconImages(imageList);
    }

    int[] getGridCount(Stack<QsoRecord> records) {
        int noGridRecs = 0;
        int noMissingRecs = 0;
        Iterator<QsoRecord> it = records.iterator();
        while (it.hasNext()) {
            QsoRecord rec = it.next();
            if (rec.containsField("GRIDSQUARE")) {
                noGridRecs += 1;
            } else {
                noMissingRecs += 1;
                _missingRecs.add(rec);
            }
        }

        int[] ret = {noGridRecs, noMissingRecs};
        return ret;
    }

    void update() {
        // construct the dialog
        this.setSize(480, 280);
        this.setResizable(false);
        Font font = _panel.getFont();
        Font newFont = font.deriveFont(14);
        _panel.setFont(newFont);
        _panel.setLayout(_gridLayout);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

        // file name field
        JTextField fileName = new JTextField("File: " + _qsoFile.getName());
        fileName.setEditable(false);
        fileName.setBorder(new EmptyBorder(0, 0, 0, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 0.0;
        c.insets = new Insets(0, 40, 5, 10);
        _gridLayout.setConstraints(fileName, c);
        this.add(fileName);

        // record count fields
        int[] gridCnt = getGridCount(_qsoFile.getRecords());
        c = new GridBagConstraints();
        JTextField noGridRecText = new JTextField("number of records with grid info: " + gridCnt[0]);
        noGridRecText.setEditable(false);
        noGridRecText.setBorder(new EmptyBorder(0, 0, 0, 0));
        JTextField noGridMissingText = new JTextField("number of records without grid info: " + gridCnt[1]);
        noGridMissingText.setEditable(false);
        noGridMissingText.setBorder(new EmptyBorder(0, 0, 0, 0));
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        c.weightx = 1.0;
        c.insets = new Insets(0, 40, 5, 10);
        _gridLayout.setConstraints(noGridRecText, c);
        this.add(noGridRecText);
        _gridLayout.setConstraints(noGridMissingText, c);
        this.add(noGridMissingText);

        // update site fields
        JTextField siteNotice = new JTextField("You must registered for updates at one of the following:");
        siteNotice.setEditable(false);
        siteNotice.setBorder(new EmptyBorder(0, 0, 0, 0));
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        c.weightx = 1.0;
        c.insets = new Insets(0, 40, 5, 10);
        _gridLayout.setConstraints(siteNotice, c);
        this.add(siteNotice);

        JTextField updateSiteText = new JTextField("Grid update site:");
        updateSiteText.setColumns(20);
        updateSiteText.setHorizontalAlignment(JTextField.RIGHT);
        updateSiteText.setEditable(false);
        updateSiteText.setBorder(new EmptyBorder(0, 0, 0, 0));
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.weightx = 1.0;
        _gridLayout.setConstraints(updateSiteText, c);
        c.insets = new Insets(0, 40, 5, 10);
        this.add(updateSiteText);

        _updateGrp = new ButtonGroup();
        Box updateBox = Box.createHorizontalBox();

        JRadioButton hamQthBtn = new JRadioButton("hamqth.com", true);
        hamQthBtn.setActionCommand("hamqth");
        _updateGrp.add(hamQthBtn);
        updateBox.add(hamQthBtn);

        JRadioButton qrzBtn = new JRadioButton("qrz.com", false);
        qrzBtn.setActionCommand("qrz");
        _updateGrp.add(qrzBtn);
        updateBox.add(qrzBtn);

        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        c.weightx = 1.0;
        c.insets = new Insets(0, 40, 5, 10);
        _gridLayout.setConstraints(updateBox, c);
        this.add(updateBox);

        // user id and password
        JTextField userIdHdr = new JTextField("user ID:");
        userIdHdr.setEditable(false);
        userIdHdr.setBorder(new EmptyBorder(0, 0, 0, 0));
        userIdHdr.setHorizontalAlignment(JTextField.RIGHT);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.RELATIVE; // end row
        c.weightx = 1.0;
        c.insets = new Insets(0, 40, 5, 10);
        _gridLayout.setConstraints(userIdHdr, c);
        this.add(userIdHdr);

        _userId = new JTextField();
        _userId.setColumns(30);
        _userId.setEditable(true);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        c.weightx = 1.0;
        c.insets = new Insets(0, 10, 5, 80);
        _gridLayout.setConstraints(_userId, c);
        this.add(_userId);

        JTextField passwdHdr = new JTextField("password:");
        passwdHdr.setEditable(false);
        passwdHdr.setBorder(new EmptyBorder(0, 0, 0, 0));
        passwdHdr.setHorizontalAlignment(JTextField.RIGHT);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.RELATIVE; // end row
        c.weightx = 1.0;
        c.insets = new Insets(0, 40, 5, 10);
        _gridLayout.setConstraints(passwdHdr, c);
        this.add(passwdHdr);

        _passwd = new JPasswordField();
        _passwd.setColumns(30);
        _passwd.setEditable(true);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        c.weightx = 1.0;
        c.insets = new Insets(0, 10, 5, 80);
        _gridLayout.setConstraints(_passwd, c);
        this.add(_passwd);

        _loginBtn = new JButton("Update records");
        _loginBtn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (_session == null) {

                    // update the siteID (qrz or hamqth)                    
                    String site = _updateGrp.getSelection().getActionCommand();
                    setSiteId(site);

                    // start the session
                    _session = new XMLsession();
                    _session.start();
                    _loginBtn.setEnabled(false);
                    _loginBtn.setEnabled(true);
                    _loginBtn.setText("Cancel");
                } else {
                    // stop the session
                    _session.abort();
                    try {
                        _session.join(1000);
                    } catch (InterruptedException ex) {
                        // do nothing
                    }
                    _session = null;
                    cancelSession();
                }
            }
        });
        _loginBtn.setSize(120, 40);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.RELATIVE; // end row
        c.weightx = 1.0;
        c.insets = new Insets(0, 60, 5, 10);
        _gridLayout.setConstraints(_loginBtn, c);
        this.add(_loginBtn);

        _showPasswd = new JCheckBox("Show Password");
        _showPasswd.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    _passwd.setEchoChar((char) 0);
                } else {
                    _passwd.setEchoChar('*');
                }
            }          
        });
        _showPasswd.setBorder(new EmptyBorder(0, 0, 0, 0));
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        c.weightx = 1.0;
        c.insets = new Insets(0, 40, 5, 10);
        _gridLayout.setConstraints(_showPasswd, c);
        this.add(_showPasswd);

        // record update count
        JTextField updateCntHdr = new JTextField("No. recs updated:");
        updateCntHdr.setEditable(false);
        updateCntHdr.setBorder(new EmptyBorder(0, 0, 0, 0));
        updateCntHdr.setHorizontalAlignment(JTextField.RIGHT);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.RELATIVE; // end row
        c.weightx = 1.0;
        c.insets = new Insets(0, 40, 5, 10);
        _gridLayout.setConstraints(updateCntHdr, c);
        this.add(updateCntHdr);

        _updateRecCnt = new JTextField();
        _updateRecCnt.setColumns(10);
        _updateRecCnt.setEditable(false);
        _updateRecCnt.setBorder(new EmptyBorder(0, 0, 0, 0));
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        c.weightx = 1.0;
        c.insets = new Insets(0, 10, 5, 80);
        _gridLayout.setConstraints(_updateRecCnt, c);
        this.add(_updateRecCnt);
        _updateRecCnt.setText("0");

        this.setVisible(true);
    }

    void updateRecordCount(int cnt) {

        // user id and password
        _updateRecCnt.setText("" + cnt);
        _upgradeRecordCount = cnt;
    }

    void showSessionResults() {

        String msg;
        if (_sessionCanceled) {
            msg = "Grid data upgrade canceled.\n"
                    + _upgradeRecordCount + " records were upgraded.\n"
                    + "You must save them now if you wish\n"
                    + "to keep the upgrades.";
        } else {
            msg = "Grid data upgrade completed.\n"
                    + _upgradeRecordCount + " records were upgraded.\n"
                    + "You must save them after you close these\n"
                    + "dialogs if you wish to keep the upgrades.";
        }
        JOptionPane.showInternalMessageDialog(_panel, msg,
                "information", JOptionPane.INFORMATION_MESSAGE);
    }

    void cancelSession() {
        _sessionCanceled = true;
    }

    void setSiteId(String clue) {
        if (clue.contains("qrz")) {
            _siteId = SITE_QRZ;
            _siteName = "qrz.com";
        } else {
            _siteId = SITE_HAMQTH;
            _siteName = "hamqth.com";
        }
    }

    int getSiteId() {
        return _siteId;
    }

    String getSiteName() {
        return _siteName;
    }

    void setSessionKey(String key) {
        _sessionKey = key;
    }

    String getSessionKey() {
        return _sessionKey;
    }

    boolean sessionIdOK() {
        boolean ret = true;

        // test for correct length
        if (_siteId == SITE_QRZ) {
            if (_sessionKey == null || _sessionKey.length() != 32) {
                ret = false;
            }
        } else {
            if (_sessionKey == null || _sessionKey.length() != 40) {
                ret = false;
            }
        }

        // test for all hex chars
        String keyStr = _sessionKey.toUpperCase();
        char key[] = keyStr.toCharArray();
        for (int c = 0; c < _sessionKey.length(); c++) {
            if (key[c] < '0' || key[c] > '9') {
                if (key[c] < 'A' || key[c] > 'F') {
                    ret = false;
                    break;
                }
            }
        }
        return ret;
    }

    // Properties
    QsoUpdateGridFrame _gridUpdateFrame;
    static final String TITLE = "Update Grid Information";
    final JPanel _panel;
    final GridBagLayout _gridLayout;
    final QsoLogWindow _logWindow;
    final QsoFile _qsoFile;
    Stack<QsoRecord> _missingRecs;
    JTextField _userId;
    JPasswordField _passwd;
    JCheckBox _showPasswd;
    JTextField _updateRecCnt;
    ButtonGroup _updateGrp;
    JButton _loginBtn;
    String _sessionKey;
    XMLsession _session = null;
    boolean _sessionCanceled = false;
    int _upgradeRecordCount = 0;
    final int SITE_HAMQTH = 0;
    final int SITE_QRZ = 1;
    final int SITE_UNDEFINED = -1;
    final int MSG_BUF_SIZE = 2000;
    int _siteId = SITE_UNDEFINED;
    String _siteName;
    final String QRZ_VERSION = "1.31";

    class XMLsession extends Thread {

        @Override
        public void run() {
            HttpURLConnection connection = openSession();
            if (connection != null) {
                int updateCnt = updateRecords(connection);
                updateRecordCount(updateCnt);
                showSessionResults();
            }
        }

        HttpURLConnection openSession() {
            HttpURLConnection connection = null;
            String userId = _userId.getText();
            String passwd = _passwd.getText();
            if (userId == null || userId.length() < 2
                    || passwd == null || passwd.length() < 2) {
                JOptionPane.showMessageDialog(_panel,
                        "user id or password was not entered.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                cancelSession();
                return connection;
            }
            URL url;
            String resp;
            String inMsg;
            int readCnt = 0;
            int totalReadCnt = 0;
            InputStream inStream;
            byte[] inBuf = new byte[MSG_BUF_SIZE];
            try {
                String urlStr;
                if (getSiteId() == SITE_QRZ) {
                    urlStr = "http://xmldata.qrz.com/xml/" + QRZ_VERSION + "/?username=" + userId
                            + ";password=" + passwd + ";agent=kc";
                    url = new URL(urlStr);
                    connection = (HttpURLConnection) url.openConnection();
                } else {
                    urlStr = "https://www.hamqth.com/xml.php?u=" + userId
                            + "&p=" + passwd;
                    url = new URL(urlStr);
                    connection = (HttpsURLConnection) url.openConnection();
                }
            } catch (MalformedURLException ex) {
                JOptionPane.showMessageDialog(_panel,
                        "cannot open session to " + getSiteName() + " .\nmalformed url.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                connection = null;
                cancelSession();
                return connection;
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(_panel,
                        "cannot open session to " + getSiteName() + " .\nIO exception.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                cancelSession();
                connection = null;
                return connection;
            }
            try {
                resp = connection.getResponseMessage();
                if (resp.contains("OK")) {
                    inStream = connection.getInputStream();
                    totalReadCnt = 0;
                    do {
                        readCnt = inStream.read(inBuf, totalReadCnt, MSG_BUF_SIZE - totalReadCnt);
                        totalReadCnt += readCnt;
                    } while (readCnt > 0);
                    if (totalReadCnt > 0) {
                        inMsg = new String(inBuf, 0, totalReadCnt);
                        if (inMsg.length() > 30) {
                            String keyDelim = "session_id";
                            if (getSiteId() == SITE_QRZ) {
                                keyDelim = "Key";
                            }
                            int indx = inMsg.indexOf("<" + keyDelim + ">");
                            if (indx > 0) {
                                inMsg = inMsg.substring(indx + ("<" + keyDelim + ">").length());
                                indx = inMsg.indexOf("</" + keyDelim + ">");
                                if (indx > 0) {
                                    setSessionKey(inMsg.substring(0, indx));
                                } else {
                                    setSessionKey("");
                                }
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(_panel,
                        "invalid response from " + getSiteName() + " .\nIO exception.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                connection = null;
                cancelSession();
                return connection;
            }
            if (!sessionIdOK()) {
                JOptionPane.showMessageDialog(_panel,
                        "invalid session key from " + getSiteName() + " .\nIO exception.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                connection = null;
                cancelSession();
                return connection;
            }
            return connection;
        }

        int updateRecords(HttpURLConnection connection) {
            int noUpdatedRecs = 0;

            if (connection == null) {
                return noUpdatedRecs;
            }
            URL url;
            String resp;
            String inMsg;
            int readCnt = 0;
            int totalReadCnt = 0;
            byte[] inBuf = new byte[MSG_BUF_SIZE];
            InputStream inStream;
            Iterator<QsoRecord> it = _missingRecs.iterator();
            while (it.hasNext() && _abort == false) {
                QsoRecord rec = it.next();
                String callSign = rec.getValue("call");
                String urlStr;
                try {
                    if (getSiteId() == SITE_QRZ) {
                        urlStr = "http://xmldata.qrz.com/xml/" + QRZ_VERSION + "/?s=" + getSessionKey() + ";callsign=" + callSign;
                        url = new URL(urlStr);
                        connection = (HttpURLConnection) url.openConnection();
                    } else {
                        urlStr = "https://www.hamqth.com/xml.php?id=" + getSessionKey() + "&callsign=" + callSign + "&prg=QsoLogViewer";
                        url = new URL(urlStr);
                        connection = (HttpsURLConnection) url.openConnection();
                    }
                } catch (MalformedURLException ex) {
                    JOptionPane.showMessageDialog(_panel,
                            "failed on request to " + getSiteName() + " .\nmalformed url.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    cancelSession();
                    return noUpdatedRecs;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(_panel,
                            "failed on request to " + getSiteName() + " .\nIO exception.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    cancelSession();
                    return noUpdatedRecs;
                }
                try {
                    resp = connection.getResponseMessage();
                    if (resp.contains("OK")) {
                        inStream = connection.getInputStream();
                        totalReadCnt = 0;
                        do {
                            readCnt = inStream.read(inBuf, totalReadCnt, MSG_BUF_SIZE - totalReadCnt);
                            totalReadCnt += readCnt;
                        } while (readCnt > 0);
                        if (totalReadCnt > 0) {
                            inMsg = new String(inBuf, 0, totalReadCnt);
                            if (inMsg.length() > 30) {
                                int indx = inMsg.indexOf("<grid>");
                                if (indx > 0) {
                                    inMsg = inMsg.substring(indx + "<grid>".length());
                                    indx = inMsg.indexOf("</grid>");
                                    if (indx > 0) {
                                        String grid = inMsg.substring(0, indx);
                                        if (grid.length() >= 4 && grid.length() <= 8) {
                                            // adif format uses gridsquare, not grid
                                            rec.put("gridsquare", grid);
                                            noUpdatedRecs += 1;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(_panel,
                            "invalid response from " + getSiteName() + " .\nIO exception.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    cancelSession();
                    return noUpdatedRecs;
                }
                updateRecordCount(noUpdatedRecs);
            }
            return noUpdatedRecs;
        }

        void abort() {
            _abort = true;
        }

        // Properties
        boolean _abort = false;
    }
}
