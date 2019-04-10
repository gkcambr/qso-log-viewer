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

import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author keithc
 */
public class QsoHelpMenu extends QsoMenu {

    QsoHelpMenu(QsoLogWindow logWindow) {
        super(logWindow);
        createMenu();
    }

    @Override
    @SuppressWarnings("Convert2Lambda")
    final void createMenu() {
        setText("Help");
        setMnemonic(KeyEvent.VK_E);

        setMnemonic(KeyEvent.VK_H);
        _aboutMenu = new JMenuItem("About", null);
        _aboutMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JOptionPane.showInternalMessageDialog(_logWindow.getContentPane(),
                        QsoLogViewer.VERSION + "\n" + QsoLogViewer.AUTHOR + "\n" + QsoLogViewer.WEBSITE,
                        "About", JOptionPane.PLAIN_MESSAGE);
            }
        });
        _aboutMenu.setEnabled(true);
        add(_aboutMenu);

        _helpFileMenu = new JMenuItem("Help file", null);
        _helpFileMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {

                // try to launch in the local browser
                if (Desktop.isDesktopSupported()) {
                    try {
                        URI helpUri = new URI("http://radiocheck.us/QsoLogViewerHelp.html");
                        Desktop.getDesktop().browse(helpUri);
                    } catch (IOException | URISyntaxException ex) {
                            JOptionPane.showInternalMessageDialog(_logWindow.getContentPane(),
                                    "Help is not supported on this host\n" + ex,
                                    "Help file", JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        });
        _helpFileMenu.setEnabled(true);
        add(_helpFileMenu);
    }
    
    @Override
    void updateMenusFileOpen(boolean opened) {
        _helpFileMenu.setEnabled(true);
        _aboutMenu.setEnabled(true);
    }

    // Properties
    JMenuItem _helpFileMenu;
    JMenuItem _aboutMenu;
}
