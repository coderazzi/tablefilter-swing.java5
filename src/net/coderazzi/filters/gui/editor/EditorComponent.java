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

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.text.ParseException;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import net.coderazzi.filters.IFilterTextParser;
import net.coderazzi.filters.artifacts.RowFilter;


/**
 * Private interface, defining the editor component [usually a text field]<br>
 * There are two such implementations, the usual one, represented by a
 * {@link JTextField}, and a non-text-based one, which renders the content using
 * a {@link ListCellRenderer} component.<br>
 */
interface EditorComponent {

    public static final String EMPTY_FILTER = "";

    /** Returns the swing component associated to the editor */
    public JComponent getComponent();

    /** 
     * Call always before {@link #getFilter()} to verify if the filter
     * has been updated. 
     * @param forceUpdate set to true if the filte must been updated, even when the editor's
     *   content is not
     * @return true if the filter was updated  
     */
    public boolean checkFilterUpdate(boolean forceUpdate);

    /**
     * Returns the filter associated to the current content. Always invoked after 
     * {@link #checkFilterUpdate(boolean)}
     */
    public RowFilter getFilter();

    /** 
     * Informs that the editor has received the focus.
     * @return true if the associated popup should be shown 
     */
    public boolean focusGained(boolean gained);
    
    /** Enables/disables the editor */
    public void setEnabled(boolean enabled);

    /** Sets the {@link IFilterTextParser} associated to the editor */
    public void setTextParser(IFilterTextParser parser);

    /** Return the associated {@link IFilterTextParser}*/
    public IFilterTextParser getTextParser();

    /** Sets the content of the editor */
    public void setContent(Object content);

    /** Returns the definition associated to the current editor */
    public Object getContent();
    
    /** Defines the filter position associated to this editor. It corresponds to the table's model*/
    public void setPosition(int position);

    /** Returns the filter position associated to this editor*/
    public int getPosition();

    /** Sets the editable flag. The editor can be edited if the user can enter any content */
    public void setEditable(boolean set);

    /** Returns the editable flag*/
    public boolean isEditable();

    /** Sets the color used to show filter's errors (invalid syntax) */
    public void setErrorForeground(Color fg);

    /** Returns the color used to show filter's errors */
    public Color getErrorForeground();

    /** Sets the color used to represent disabled state */
    public void setDisabledForeground(Color fg);

    /** Returns the color used to represent disabled state */
    public Color getDisabledForeground();

    /** Sets the foreground color used*/
    public void setForeground(Color fg);

    /** Returns the foreground color used*/
    public Color getForeground();

    /**
     * EditorComponent for text edition, backed up with a {@link JTextField}<br>
     * It is editable by default.
     */
    static final class Text extends DocumentFilter implements DocumentListener, EditorComponent {

        private JTextField textField = new JTextField(15);
        private IFilterTextParser parser;
        private RowFilter cachedFilter;
        private String cachedContent;
        private int filterPosition;
        private boolean editable;
        private boolean enabled;
        /** set to true if the content is being set from inside, as to not to raise some events */
        private boolean controlledSet;
        private Color errorColor = Color.red;
        private Color foreground;
        private Color disabledColor;
        PopupComponent popup;

        public Text(PopupComponent popupComponent) {
            this.popup = popupComponent;
            setEditable(true);
            //if the user moves the cursor on the editor, the focus passes automatically
            // back to the editor (from the popup)
            textField.addCaretListener(new CaretListener() {
                    public void caretUpdate(CaretEvent e) {
                        popup.setPopupFocused(false);
                    }
                });
        }

        public JComponent getComponent() {
            return textField;
        }

        public boolean checkFilterUpdate(boolean forceUpdate) {
            String content = textField.getText().trim();
            if (!forceUpdate && content.equals(cachedContent)) {
                return false;
            }
            RowFilter old = cachedFilter;
            cachedContent = content;
            cachedFilter = parseText(content);
            return cachedFilter != old;
        }
        
        public RowFilter getFilter() {
            return cachedFilter;
        }

        public boolean focusGained(boolean gained) {
    		textField.setCaretPosition(0);
        	if (gained){
        		textField.moveCaretPosition(textField.getText().length());
        	}
            return !editable;
        }

        public void setEnabled(boolean enabled) {
        	this.enabled=enabled;
        	textField.setFocusable(enabled);
        	ensureCorrectForegroundColor();
        }

        public void setTextParser(IFilterTextParser parser) {
            this.parser = parser;
            if (textField.isEnabled()) {
                checkFilterUpdate(true);
            }
        }
        
        public IFilterTextParser getTextParser() {
            return parser;
        }

        public void setContent(Object content) {
            setControlledSet();
            // the filterEditor verifies already that the content is a string
            textField.setText((String) content);
        }

        public void setPosition(int position) {
            this.filterPosition = position;
        }

        public int getPosition() {
            return filterPosition;
        }

        public Object getContent() {
            return textField.getText();
        }

        public void setEditable(boolean set) {
        	//dispose first the current listeners
            if (isEditable()) {
                textField.getDocument().removeDocumentListener(this);
            } else {
                ((AbstractDocument) textField.getDocument()).setDocumentFilter(null);
            }
            editable = set;
            if (set) {
                textField.getDocument().addDocumentListener(this);
            } else {
                // ensure that the text contains something okay
                String proposal = getProposalOnEdition(textField.getText(), false);
                textField.setText((proposal == null) ? EMPTY_FILTER : proposal);
                ((AbstractDocument) textField.getDocument()).setDocumentFilter(this);
            }
            controlledSet = false;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setErrorForeground(Color fg) {
            errorColor = fg;
        	ensureCorrectForegroundColor();
        }

        public Color getErrorForeground() {
            return errorColor;
        }

        public void setDisabledForeground(Color fg) {
        	disabledColor=fg;
        	ensureCorrectForegroundColor();        	
        }

        public Color getDisabledForeground() {
            return disabledColor;
        }

        public void setForeground(Color fg) {
            this.foreground = fg;
        	ensureCorrectForegroundColor();
        }

        public Color getForeground() {
            return foreground;
        }

        /** Ensures that the correct foreground is on use*/
        private void ensureCorrectForegroundColor(){
        	if (enabled){
        		parseText(textField.getText());
        	} else {
        		textField.setForeground(disabledColor);
        	}
        }

        /** Returns the filter associated to the current content, setting the foreground color*/
        private RowFilter parseText(String content) {
        	RowFilter ret;
            Color color = getForeground();
            if (content.length() == 0) {
                ret = null;
            } else {
                try {
                    ret = parser.parseText(cachedContent, filterPosition);
                } catch (ParseException pex) {
                    ret = null;
                    color = getErrorForeground();
                }
            }
            textField.setForeground(color);
            return ret;
        }

        /** set if the content is being set from inside, as to not to raise some events */
        public void setControlledSet() {
            controlledSet = true;
        }

        /** Returns a proposal for the current edition*/
        private String getProposalOnEdition(String hint, boolean perfectMatch) {
            String ret = popup.selectBestMatch(hint, perfectMatch);
            popup.setPopupFocused(false);
            return ret;
        }

        /** {@link DocumentFilter}: method called when handler is not editable */
        @Override
        public void insertString(FilterBypass fb,
                                 int offset,
                                 String string,
                                 AttributeSet attr) {
            // we never use it, we never invoke Document.insertString
            // note that normal (non programmatically) editing only invokes
            // replace/remove
        }

        /** {@link DocumentFilter}: method called when handler is not editable */
        @Override
        public void replace(FilterBypass fb,
                            int offset,
                            int length,
                            String text,
                            AttributeSet attrs) throws BadLocationException {
            String buffer = textField.getText();
            String newContentBegin = buffer.substring(0, offset) + text;
            String newContent = newContentBegin + buffer.substring(offset + length);
            String proposal = getProposalOnEdition(newContent, true);
            if (proposal == null) {
                // why this part? Imagine having text "se|cond" with the cursor
                // at "|". Nothing is selected.
                // if the user presses now 'c', the code above would imply
                // getting "seccond", which is probably wrong, so we try now
                // to get a proposal starting at 'sec' ['sec|ond']
                proposal = getProposalOnEdition(newContentBegin, true);
                if (proposal == null) {
                    return;
                }
                newContent = newContentBegin;
            }
            int caret;
            if (controlledSet) {
                controlledSet = false;
                caret = 0;
            } else {
                caret = 1 + Math.min(textField.getCaret().getDot(), textField.getCaret().getMark());
            }
            super.replace(fb, 0, buffer.length(), proposal, attrs);
            textField.setCaretPosition(proposal.length());
            textField.moveCaretPosition(caret);
        }

        /** {@link DocumentFilter}: method called when handler is not editable */
        @Override
        public void remove(FilterBypass fb,
                           int offset,
                           int length) throws BadLocationException {
            int caret = textField.getCaret().getDot();
            int mark = textField.getCaret().getMark();
            String buffer = textField.getText();
            String newContent = buffer.substring(0, offset) + buffer.substring(offset + length);
            String proposal = getProposalOnEdition(newContent, true);
            if (!newContent.equals(proposal)) {
                if (proposal == null) {
                    proposal = getProposalOnEdition(newContent, false);
                    if (proposal == null) {
                        return;
                    }
                }
                if (matchCount(proposal, newContent) <= matchCount(buffer, newContent)) {
                    proposal = buffer;
                }
            }
            super.replace(fb, 0, buffer.length(), proposal, null);
            
            //special case if the removal is due to BACK SPACE
    		AWTEvent ev = EventQueue.getCurrentEvent();
    		if ((ev instanceof KeyEvent) && ((KeyEvent)ev).getKeyCode() == KeyEvent.VK_BACK_SPACE){
                if (caret > mark) {
                    caret = mark;
                } else if (buffer == proposal) {
                    --caret;
                } else if (caret == mark) {
                    caret = offset;
                }    			
    		} 
            textField.setCaretPosition(proposal.length());
            textField.moveCaretPosition(caret);
        }

        /** returns the number of starting characters matching among both parameters */
        private int matchCount(String a,
                               String b) {
            int max = Math.min(a.length(), b.length());
            for (int i = 0; i < max; i++) {
                if (a.charAt(i) != b.charAt(i)) {
                    return i;
                }
            }
            return max;
        }

        /** {@link DocumentListener}: method called when handler is editable */
        public void changedUpdate(DocumentEvent e) {
            // no need to handle updates
        }

        /** {@link DocumentListener}: method called when handler is editable */
        public void removeUpdate(DocumentEvent e) {
            getProposalOnEdition(textField.getText(), false);
        }

        /** {@link DocumentListener}: method called when handler is editable */
        public void insertUpdate(DocumentEvent e) {
            getProposalOnEdition(textField.getText(), false);
        }

    }

    /**
     * Editor component for cell rendering
     */
    static final class Rendered extends JComponent implements EditorComponent {

        private static final long serialVersionUID = -7162028817569553287L;

        private Object content = EMPTY_FILTER;
        private FilterListCellRenderer renderer;
        private CellRendererPane painter = new CellRendererPane();
        private IFilterTextParser parser;
        private RowFilter filter;
        private Color errorColor;
        private Color disabledColor;
        Object cachedContent;
        int filterPosition;

        public Rendered(FilterListCellRenderer renderer) {
            this.renderer = renderer;
        }
        
        public JComponent getComponent() {
            return this;
        }

        public boolean checkFilterUpdate(boolean forceUpdate) {
            Object currentContent = getContent();
            if (!forceUpdate && (currentContent == cachedContent)) {
                return false;
            }
            cachedContent = currentContent;
            RowFilter old = filter;
            if (EMPTY_FILTER.equals(cachedContent)) {
                filter = null;
            } else {
                filter = new RowFilter() {
                        @Override
                        public boolean include(RowFilter.Entry entry) {
                            Object val = entry.getValue(filterPosition);
                            return (val == null) ? (cachedContent == null) : val.equals(cachedContent);
                        }
                    };
            }
            return old != filter;
        }

        public RowFilter getFilter() {
            return filter;
        }

        public boolean focusGained(boolean gained) {
            return true;
        }

        @Override public void setEnabled(boolean enabled) {
        	renderer.setEnabled(enabled);
        }

        public void setTextParser(IFilterTextParser parser) {
            this.parser = parser;
            cachedContent = null;
        }

        public IFilterTextParser getTextParser() {
            return parser;
        }
      
        public void setContent(Object content) {
            this.content = (content == null) ? EMPTY_FILTER : content;
            repaint();
        }

        public Object getContent() {
            return content;
        }

        public void setPosition(int position) {
            this.filterPosition = position;
        }

        public int getPosition() {
            return filterPosition;
        }

        public void setEditable(boolean set) {
            // cannot apply to the rendered component -at least, not currently
        }

        public boolean isEditable() {
            return false;
        }

        public void setErrorForeground(Color fg) {
            this.errorColor = fg;
        }

        public Color getErrorForeground() {
            return errorColor;
        }

        public void setDisabledForeground(Color fg) {
            disabledColor = fg;
        }

        public Color getDisabledForeground() {
            return disabledColor;
        }

        @Override public boolean isShowing() {
            return true;
        }

        @Override protected void paintComponent(Graphics g) {
            Component c = renderer.getCellRendererComponent(content, getWidth());
            painter.paintComponent(g, c, this, 0, 0, getWidth(), getHeight());
        }
    }
}