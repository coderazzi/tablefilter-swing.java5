/**
 * Author:  Luis M Pena  ( lu@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  lu@coderazzi.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.coderazzi.filters.gui.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.CellRendererPane;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Special renderer used on the history and options list, and to render the content
 * when is not text-based (the user has specified a {@link ListCellRenderer}<br>
 * This FilterListCellRenderer encapsulated the user's {@link ListCellRenderer}, to
 * have some specific appearance: it differentiates between selected cells, selected focused cells,
 * and other cells; a cell that is selected is considered focused if the current focus lies
 * on the Popup menu (and not on the editor itself):<ul>
 * <li>A selected and focused cells is displayed as usual (selected background).</li>
 * <li>A selected, not focused cell, only gets an arrow, shown on its left side.</li></ul>
 * To avoid inconsistencies, the space required for the arrow is left blank on the unselected cells 
 * <br>
 */
class FilterListCellRenderer extends JComponent implements ListCellRenderer {

	private static final long serialVersionUID = 6736940091246039334L;
	private final static int X_MARGIN_ARROW = 1;
	private final static int WIDTH_ARROW = 5;
	private final static int HEIGHT_ARROW = 6; // must be even
	private final static int X[] = { 0, WIDTH_ARROW, 0 };
	private final static int Y[] = { 0, HEIGHT_ARROW / 2, HEIGHT_ARROW };

	private ListCellRenderer userRenderer;
	private ListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	private CellRendererPane painter = new CellRendererPane();
	private JList referenceList;
	private Component inner;
	private Color arrowColor;

	private boolean selected;
	private boolean focusOnList;
	private int xDeltaBase;
	private int width;

	public FilterListCellRenderer(JList mainList) {
		setUserRenderer(null);
		setDoubleBuffered(true);
		this.referenceList = mainList;
	}

	/** Indicates that the focus is currenly on the list. Selected cells are selected-focused cells */
	public void setFocusOnList(boolean set) {
		this.focusOnList = set;
	}

	public boolean isFocusOnList() {
		return focusOnList;
	}

	public void setUserRenderer(ListCellRenderer renderer) {
		userRenderer = renderer == null ? defaultRenderer : renderer;
	}

	public ListCellRenderer getUserRenderer() {
		return userRenderer==defaultRenderer? null : userRenderer;
	}

	public Component getListCellRendererComponent(JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus) {
		setupRenderer(list, value, index, focusOnList && isSelected, cellHasFocus);
		width = referenceList.isShowing() ? referenceList.getWidth() : list.getWidth();
		selected = isSelected;
		xDeltaBase = WIDTH_ARROW + 2 * X_MARGIN_ARROW;
		return this;
	}

	/** Method used to render the content on the rendered editor */
	public Component getCellRendererComponent(Object value, int finalWidth) {
		setupRenderer(referenceList, value, -1, false, false);
		width = finalWidth;
		selected = false;
		xDeltaBase = 0;
		return this;
	}

	private void setupRenderer(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		try {
			inner = userRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		} catch (Exception ex) {
			inner = null;
		}
		if (inner == null) {
			inner = defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
		inner.setFont(list.getFont());
		inner.setEnabled(isEnabled());
		arrowColor = list.getSelectionBackground();
	}

	@Override
	protected void paintComponent(Graphics g) {
		int height = getHeight();
		int xDelta = xDeltaBase;
		int yDelta = 0;
		if (selected) {
			Object old = ((Graphics2D) g).getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			yDelta = height / 2 + 1 - HEIGHT_ARROW / 2;
			xDelta-=X_MARGIN_ARROW; 
			g.translate(X_MARGIN_ARROW, yDelta);
			g.setColor(arrowColor);
			g.fillPolygon(X, Y, X.length);
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, old);
		} 
		g.translate(xDelta, -yDelta);
		painter.paintComponent(g, inner, this, 0, 0, width-xDeltaBase, height);
	}

	@Override
	public Dimension getPreferredSize() {
		return inner.getPreferredSize();
	}

	@Override
	public boolean isShowing() {
		return true;
	}

}