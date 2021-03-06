/*
 *  Copyright (C) 2010-2014 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.gui.hexview.HexView;
import com.jpexs.decompiler.flash.treenodes.TagNode;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

/**
 *
 * @author JPEXS
 */
public final class BinaryPanel extends JPanel implements ComponentListener {

    public HexView hexEditor = new HexView();
    private byte[] data;
    private JPanel swfInsidePanel;
    private TagNode node = null;
    private final MainPanel mainPanel;

    public BinaryPanel(final MainPanel mainPanel) {
        super(new BorderLayout());
        this.mainPanel = mainPanel;

        add(new JScrollPane(hexEditor), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
        addComponentListener(this);
        swfInsidePanel = new JPanel();
        swfInsidePanel.setBackground(new Color(253, 205, 137));
        swfInsidePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        swfInsidePanel.add(new JLabel(AppStrings.translate("binarydata.swfInside")));
        swfInsidePanel.setFocusable(true);
        swfInsidePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        swfInsidePanel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                mainPanel.loadFromBinaryTag(node);
                swfInsidePanel.setVisible(false);
            }

        });
        add(swfInsidePanel, BorderLayout.NORTH);
        swfInsidePanel.setVisible(false);
    }

    public void setBinaryData(byte[] data, TagNode node) {
        this.node = node;
        this.data = data;
        if (data != null) {
            hexEditor.setData(data, null, null);

            if (node != null) {
                if (node.subNodes.isEmpty() && data.length > 8) {
                    try {
                        String signature = new String(data, 0, 3, Utf8Helper.charset);
                        if (Arrays.asList(
                                "FWS", //Uncompressed Flash
                                "CWS", //ZLib compressed Flash
                                "ZWS", //LZMA compressed Flash
                                "GFX", //Uncompressed ScaleForm GFx
                                "CFX" //Compressed ScaleForm GFx
                        ).contains(signature)) {
                            swfInsidePanel.setVisible(true);
                        }
                    } catch (Exception ex) {
                        swfInsidePanel.setVisible(false);
                    }
                } else {
                    swfInsidePanel.setVisible(false);
                }
            } else {
                swfInsidePanel.setVisible(false);
            }

        } else {
            hexEditor.setData(new byte[0], null, null);
            swfInsidePanel.setVisible(false);
        }
        hexEditor.revalidate();
        hexEditor.repaint();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        setBinaryData(data, node);
    }

    @Override
    public void componentMoved(ComponentEvent ce) {
    }

    @Override
    public void componentShown(ComponentEvent ce) {
    }

    @Override
    public void componentHidden(ComponentEvent ce) {
    }
}
