package net.sf.jabref.gui.preftabs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import net.sf.jabref.logic.l10n.Localization;

public class FontSelectorDialog extends JDialog {

    private static final String PLAIN = "plain";
    private static final String BOLD = "bold";
    private static final String BOLD_ITALIC = "bold-italic";
    private static final String ITALIC = "italic";

    private static final String[] styles = {PLAIN, BOLD, ITALIC, BOLD_ITALIC};

    private static final String[] sizes = {"9", "10", "12", "14", "16", "18", "24"};

    private boolean isOK;
    private final JTextField familyField = new JTextField();
    private final JList<String> familyList;
    private final JTextField sizeField = new JTextField();
    private final JList<String> sizeList = new JList<>(sizes);
    private final JTextField styleField = new JTextField();
    private final JList<String> styleList = new JList<>(styles);
    private final JLabel preview;

    /**
     * For some reason the default Java fonts show up in the
     * list with .bold, .bolditalic, and .italic extensions.
     */
    private static final String[] HIDEFONTS = {".bold", ".italic"};


    public FontSelectorDialog(Component comp, Font font) {

        super(JOptionPane.getFrameForComponent(comp), Localization.lang("Font selection"), true); //
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(content);

        JPanel listPanel = new JPanel(new GridLayout(1, 3, 6, 6));

        familyList = new JList<>(getFontList());

        JPanel familyPanel = createTextFieldAndListPanel(Localization.lang("Font family"), familyField, familyList);
        listPanel.add(familyPanel);

        JPanel sizePanel = createTextFieldAndListPanel(Localization.lang("Font size"), sizeField, sizeList);
        listPanel.add(sizePanel);

        JPanel stylePanel = createTextFieldAndListPanel(Localization.lang("Font style"), styleField, styleList);
        styleField.setEditable(false);
        listPanel.add(stylePanel);

        familyList.setSelectedValue(font.getFamily(), true);
        familyField.setText(font.getFamily());
        sizeList.setSelectedValue(String.valueOf(font.getSize()), true);
        sizeField.setText(String.valueOf(font.getSize()));
        styleList.setSelectedIndex(font.getStyle());
        styleField.setText(styleList.getSelectedValue());

        familyList.addListSelectionListener(evt -> {
            String family = familyList.getSelectedValue();
            if (family != null) {
                familyField.setText(family);
            }
            updatePreview();
        });
        sizeList.addListSelectionListener(evt -> {
            String size = sizeList.getSelectedValue();
            if (size != null) {
                sizeField.setText(size);
            }
            updatePreview();
        });
        styleList.addListSelectionListener(evt -> {
            String style = styleList.getSelectedValue();
            if (style != null) {
                styleField.setText(style);
            }
            updatePreview();
        });

        content.add(BorderLayout.NORTH, listPanel);

        /* --------------------------------------------------------
           |  Experimental addition by Morten Alver. I want to    |
           |  enable antialiasing in the preview field, since I'm |
           |  working on introducing this in the table view.      |
           -------------------------------------------------------- */
        preview = new JLabel(Localization.lang("Font preview")) {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint
                (RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                super.paint(g2);
            }

        };

        preview.setBorder(new TitledBorder(Localization.lang("Font preview")));

        updatePreview();

        Dimension prefSize = preview.getPreferredSize();
        prefSize.height = 50;
        preview.setPreferredSize(prefSize);

        content.add(BorderLayout.CENTER, preview);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.setBorder(new EmptyBorder(12, 0, 0, 0));
        buttons.add(Box.createGlue());

        JButton ok = new JButton(Localization.lang("OK"));
        ok.addActionListener(e -> {
            isOK = true;
            dispose();
        });
        getRootPane().setDefaultButton(ok);
        buttons.add(ok);

        buttons.add(Box.createHorizontalStrut(6));

        JButton cancel = new JButton(Localization.lang("Cancel"));
        cancel.addActionListener(e -> dispose());
        buttons.add(cancel);

        buttons.add(Box.createGlue());

        content.add(BorderLayout.SOUTH, buttons);

        pack();
        setLocationRelativeTo(JOptionPane.getFrameForComponent(comp));
        setVisible(true);
    }

    public Optional<Font> getSelectedFont() {
        if (!isOK) {
            return Optional.empty();
        }

        int size;
        try {
            size = Integer.parseInt(sizeField.getText());
        } catch (NumberFormatException e) {
            size = 14;
        }

        return Optional.of(new Font(familyField.getText(), styleList.getSelectedIndex(), size));
    }



    private static String[] getFontList() {
        try {
            String[] nameArray = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            List<String> nameList = new ArrayList<>(nameArray.length);
            for (String fontName : nameArray) {
                boolean hidden = false;
                for (String hiddenName : FontSelectorDialog.HIDEFONTS) {
                    if (fontName.contains(hiddenName)) {
                        hidden = true;
                        break;
                    }
                }

                if (!hidden) {
                    nameList.add(fontName);
                }
            }
            String[] resultArray = new String[nameList.size()];
            return nameList.toArray(resultArray);
        } catch (SecurityException | IllegalArgumentException ex) {
            return new String[0];
        }
    }

    private static JPanel createTextFieldAndListPanel(String labelString, JTextField textField, JList<String> list) {
        GridBagLayout layout = new GridBagLayout();
        JPanel panel = new JPanel(layout);

        GridBagConstraints cons = new GridBagConstraints();
        cons.gridx = cons.gridy = 0;
        cons.gridwidth = cons.gridheight = 1;
        cons.fill = GridBagConstraints.BOTH;
        cons.weightx = 1.0f;

        JLabel label = new JLabel(labelString);
        layout.setConstraints(label, cons);
        panel.add(label);

        cons.gridy = 1;
        Component vs = Box.createVerticalStrut(6);
        layout.setConstraints(vs, cons);
        panel.add(vs);

        cons.gridy = 2;
        layout.setConstraints(textField, cons);
        panel.add(textField);

        cons.gridy = 3;
        vs = Box.createVerticalStrut(6);
        layout.setConstraints(vs, cons);
        panel.add(vs);

        cons.gridy = 4;
        cons.gridheight = GridBagConstraints.REMAINDER;
        cons.weighty = 1.0f;
        JScrollPane scroller = new JScrollPane(list);
        layout.setConstraints(scroller, cons);
        panel.add(scroller);

        return panel;
    }

    private void updatePreview() {
        String family = familyField.getText();
        int size;
        try {
            size = Integer.parseInt(sizeField.getText());
        } catch (NumberFormatException e) {
            size = 14;
        }
        int style = styleList.getSelectedIndex();
        preview.setFont(new Font(family, style, size));
    }
}
