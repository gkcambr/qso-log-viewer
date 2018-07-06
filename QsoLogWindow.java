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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

/**
 *
 * @author keithc
 */
public class QsoLogWindow extends JFrame {

    public QsoLogWindow(String[] args) {
        super();
        init();
    }

    private void init() {

        // create the main window
        setTitle(TITLE);
        setSize(600, 400);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                QsoInitFile initFile = QsoInitFile.getInstance();
                initFile.close();
                System.exit(0);
            }
        });

        createMenuBar();
        getRootPane().setJMenuBar(_menuBar);
        setVisible(true);
        ArrayList<Image> imageList = getImages();
        setIconImages(imageList);
    }

    public void run() {
        this.setVisible(true);
    }

    void createMenuBar() {

        _menuBar = new JMenuBar();
        _fileMenu = new QsoFileMenu(this);
        _menuBar.add(_fileMenu);
        _editMenu = new QsoEditMenu(this);
        _menuBar.add(_editMenu);
        _viewMenu = new QsoViewMenu(this);
        _menuBar.add(_viewMenu);
        _actionMenu = new QsoActionMenu(this);
        _menuBar.add(_actionMenu);
        JMenu helpMenu = createHelpMenu();
        _menuBar.add(helpMenu);
    }

    void updateMenusFileOpen(boolean opened) {

        if (opened) {
            if (_fileMenu != null) {
                _fileMenu.updateMenusFileOpen(true);
            }
            if (_viewMenu != null) {
                _viewMenu.updateMenusFileOpen(true);
            }
            if (_actionMenu != null) {
                _actionMenu.updateMenusFileOpen(true);
            }
            if (_editMenu != null) {
                _editMenu.updateMenusFileOpen(true);
            }
        } else {
            if (_fileMenu != null) {
                _fileMenu.updateMenusFileOpen(false);
            }
            if (_viewMenu != null) {
                _viewMenu.updateMenusFileOpen(false);
            }
            if (_actionMenu != null) {
                _actionMenu.updateMenusFileOpen(false);
            }
            if (_editMenu != null) {
                _editMenu.updateMenusFileOpen(false);
            }
        }
    }

    JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        JMenuItem aboutMenu = new JMenuItem("About", null);
        aboutMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JOptionPane.showInternalMessageDialog(getContentPane(),
                        QsoLogViewer.VERSION + "\n" + QsoLogViewer.AUTHOR + "\n" + QsoLogViewer.WEBSITE,
                        "About", JOptionPane.PLAIN_MESSAGE);
            }
        });
        aboutMenu.setEnabled(true);
        helpMenu.add(aboutMenu);

        JMenuItem helpFileMenu = new JMenuItem("Help file", null);
        helpFileMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    URL helpUrl = this.getClass().getResource("/qsologviewer");
                    if (helpUrl == null) {
                        throw (new IOException("help not supported on this host"));
                    }
                    File hf = new File(helpUrl.getFile());
                    String docPath = hf.toString().replaceFirst("QsoLogViewer\\.jar.*", "");
                    if (docPath == null) {
                        throw (new IOException("help not supported on this host"));
                    }
                    docPath = docPath.concat("docs" + File.separator);
                    docPath = docPath.concat("QsoLogViewerHelp.html");
                    docPath = docPath.replaceFirst("file:.", "");
                    docPath = docPath.replaceAll("%20", " ");
                    File helpFile = new File(docPath);
                    if (helpFile.exists() && helpFile.isFile()) {
                        // create the help window
                        JFrame helpFrame = new JFrame();
                        ArrayList<Image> imageList = getImages();
                        helpFrame.setIconImages(imageList);
                        helpFrame.setTitle("Help File");
                        helpFrame.setSize(600, 400);
                        JEditorPane ep = new JEditorPane();
                        ep.setPage(helpFile.toURI().toURL());
                        ep.setEditable(false);
                        JScrollPane epScrollPane = new JScrollPane(ep);
                        epScrollPane.setVerticalScrollBarPolicy(
                                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                        epScrollPane.setHorizontalScrollBarPolicy(
                                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                        helpFrame.getContentPane().add(epScrollPane);
                        helpFrame.setVisible(true);
                    } else {
                        throw (new IOException("file not found"));
                    }
                } catch (IOException ex) {
                    JOptionPane.showInternalMessageDialog(getContentPane(),
                            "Help is not supported on this host\n" + ex,
                            "Help file", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        helpFileMenu.setEnabled(true);
        helpMenu.add(helpFileMenu);

        return helpMenu;
    }

    QsoFile getQsoFile() {
        QsoFile file = null;

        if (getContentPane() != null) {
            file = ((QsoPane) getContentPane()).getQsoFile();
        }
        return file;
    }

    ArrayList<Image> getImages() {
        ArrayList<Image> imageList = new ArrayList<>();
        try {
            BufferedImage image;
            URL iconUrl = this.getClass().getResource("/QsoViewer_16.png");
            image = ImageIO.read(iconUrl);
            imageList.add(image);
            iconUrl = this.getClass().getResource("/QsoViewer_32.png");
            image = ImageIO.read(iconUrl);
            imageList.add(image);
            iconUrl = this.getClass().getResource("/QsoViewer_64.png");
            image = ImageIO.read(iconUrl);
            imageList.add(image);
            iconUrl = this.getClass().getResource("/QsoViewer_128.png");
            image = ImageIO.read(iconUrl);
            imageList.add(image);
            iconUrl = this.getClass().getResource("/QsoLogViewer_48.ico");
            image = ImageIO.read(iconUrl);
            imageList.add(image);
        } catch (IOException ex) {
            Logger.getLogger(QsoLogWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        return imageList;
    }

    // Properties
    static final String TITLE = "QSO Log Viewer";
    private JMenuBar _menuBar;
    QsoViewMenu _viewMenu;
    QsoFileMenu _fileMenu;
    QsoActionMenu _actionMenu;
    QsoEditMenu _editMenu;
}
