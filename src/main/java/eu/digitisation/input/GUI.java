/*
 * Copyright (C) 2014 Universidad de Alicante
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package eu.digitisation.input;

import eu.digitisation.log.Messages;
import eu.digitisation.output.Browser;
import eu.digitisation.output.OutputFileSelector;
import eu.digitisation.output.Report;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InvalidObjectException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author R.C.C
 */
public class GUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Color green = Color.decode("#4C501E");
    private static final Color white = Color.decode("#FAFAFA");
    private static final Color gray = Color.decode("#EEEEEE");
    // Frame components
    FileSelector gtselector;
    FileSelector ocrselector;
    JPanel advanced;
    Link info;
    JPanel actions;

    /**
     * Show a warning message
     *
     * @param text the text to be displayed
     */
    public void warn(String message) {
        JOptionPane.showMessageDialog(super.getRootPane(), message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Ask for confirmation
     */
    public boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(super.getRootPane(),
                message, message, JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    // The unique constructor
    public GUI() {
        init();
    }

    /**
     * Build advanced options panel
     *
     * @param ignoreCase
     * @param ignoreDiacritics
     * @param ignorePunctuation
     * @param compatibilty
     * @param eqfile
     * @return
     */
    private JPanel advancedOptionsPanel(Parameters pars) {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JPanel subpanel = new JPanel(new GridLayout(0, 2));
        Color fg = getForeground();
        Color bg = getBackground();

        subpanel.setForeground(fg);
        subpanel.setBackground(bg);
        subpanel.add(new BooleanSelector(pars.ignoreCase, fg, bg));
        subpanel.add(new BooleanSelector(pars.ignoreDiacritics, fg, bg));
        subpanel.add(new BooleanSelector(pars.ignorePunctuation, fg, bg));
        subpanel.add(new BooleanSelector(pars.compatibility, fg, bg));

        panel.setForeground(fg);
        panel.setBackground(bg);
        panel.setVisible(false);
        panel.add(subpanel);
        panel.add(new FileSelector(pars.swfile, fg, bg));
        panel.add(new FileSelector(pars.eqfile, fg, bg));
        return panel;
    }

    /**
     * Creates a subpanel with two actions: "show advanced options" & "generate
     * report"
     *
     * @param gui
     * @return
     */
    private JPanel actionsPanel(final GUI gui, final Parameters pars) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        final JCheckBox more = new JCheckBox("Show advanced options");
        more.setForeground(getForeground());
        more.setBackground(Color.LIGHT_GRAY);
        more.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dimension dframe = gui.getSize();
                Dimension dadvanced = gui.advanced.getPreferredSize();
                if (more.isSelected()) {
                    gui.setSize(new Dimension(dframe.width, dframe.height + dadvanced.height));
                } else {
                    gui.setSize(new Dimension(dframe.width, dframe.height - dadvanced.height));
                }
                gui.advanced.setVisible(more.isSelected());
            }
        });

        JButton reset = new JButton("Reset");
        reset.setForeground(getForeground());
        reset.setBackground(getBackground());
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pars.clear();
                gui.remove(gtselector);
                gui.remove(ocrselector);
                gui.remove(info);
                gui.remove(advanced);
                gui.remove(actions);
                gui.repaint();
                gui.setVisible(true);
                gui.init();
            }
        });

        // Go for it! button with inverted colors 
        JButton trigger = new JButton("Generate report");
        trigger.setForeground(getBackground());
        trigger.setBackground(getForeground());
        trigger.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launch(pars);
            }
        });

        panel.add(more, BorderLayout.WEST);
        panel.add(Box.createHorizontalGlue());
        panel.add(reset, BorderLayout.CENTER);
        panel.add(Box.createHorizontalGlue());
        panel.add(trigger, BorderLayout.EAST);
        return panel;
    }

    public void launch(Parameters pars) {
        try {
            if (gtselector.ready() && ocrselector.ready()) {
                File ocrfile = pars.ocrfile.getValue();
                String name = ocrfile.getName().replaceAll("\\.\\w+", "")
                        + "_report.html";
                File dir = ocrfile.getParentFile();
                File preselected = new File(name);
                OutputFileSelector selector = new OutputFileSelector();
                File outfile = selector.choose(dir, preselected);
                pars.outfile.setValue(outfile);

                if (outfile != null) {
                    try {
                        Batch batch = new Batch(pars.gtfile.value, pars.ocrfile.value);
                        Report report = new Report(batch, pars);
                        report.write(outfile);
                        Messages.info("Report dumped to " + outfile);
                        Browser.open(outfile.toURI());
                    } catch (InvalidObjectException ex) {
                        warn(ex.getMessage());
                    } catch (SchemaLocationException ex) {
                        boolean ans = confirm("Unknown schema location: "
                                + ex.getSchemaLocation()
                                + "\nAdd it to the list of valid schemas?");
                        if (ans) {
                            StartUp.addValue("schemaLocation." + ex.getFileType().toString(),
                                    ex.getSchemaLocation());
                        }
                    }
                }
            } else {
                gtselector.checkout();
                ocrselector.checkout();
            }
        } catch (WarningException ex) {
            warn(ex.getMessage());
        }
    }

    public final void init() {
        // Main container
        Container pane = getContentPane();
        // Initialization settings
        setForeground(green);
        setBackground(gray);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        setLocationRelativeTo(null);

        // Define program parameters: input files 
        Parameters pars = new Parameters();

        // Define content 
        gtselector = new FileSelector(pars.gtfile, getForeground(), white);
        ocrselector = new FileSelector(pars.ocrfile, getForeground(), white);
        advanced = advancedOptionsPanel(pars);
        info = new Link("Info:",
                "https://sites.google.com/site/textdigitisation/ocrevaluation",
                getForeground());
        actions = actionsPanel(this, pars);

        // Put all content together
        pane.add(gtselector);
        pane.add(ocrselector);
        pane.add(advanced);
        pane.add(info);
        pane.add(actions);

        // Show
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        new GUI();
    }
}
