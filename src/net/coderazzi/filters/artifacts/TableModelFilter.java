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

package net.coderazzi.filters.artifacts;

import java.util.Arrays;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;


/**
 * <p>Implementation of a {@link ITableModelFilter}</p>
 *
 * <p>It tries to mimic the behaviour in the Java 6 model. In special, updates to the model do not
 * imply reapplying the filter in the modified rows.</p>
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
public class TableModelFilter extends AbstractTableModel implements ITableModelFilter {

	private static final long serialVersionUID = 8464799419424149436L;

	/** Mapping from view rows to model rows */
    private int[] rowsMapper = new int[0];

    /** Number of valid elements in rowsMapper */
    private int validRows = 0;

    /**
     * Mapping from model rows to view rows; it only contains valid information if viewMap is true
     */
    private int[] viewsMapper = new int[0];

    /** Whether the information in viewsMapper is up to date */
    private boolean viewMap;

    /** Listener to the original tableModel events */
    private TableModelListener tableModelListener = new TableListener();

    /** The RowFilter.Entry instance passed to the filter */
    private RowFilterEntry rowFilterEntry = new RowFilterEntry();

    /** The current filter, if any */
    RowFilter tableFilter;

    /** The attached table model */
    TableModel tableModel;


    /**
     * Constructor
     */
    public TableModelFilter(TableModel model) {

        setModel(model);
    }


    /**
     * Updates the model used beneath the TableModelFilter
     */
    public void setModel(TableModel tableModel) {

        if (this.tableModel != null) {
            this.tableModel.removeTableModelListener(tableModelListener);
        }

        this.tableModel = tableModel;

        if (this.tableModel != null) {
            this.tableModel.addTableModelListener(tableModelListener);
        }

        handleModifiedModel();
        fireTableStructureChanged();
    }

    /**
     * Handles structure modifications in the model
     */
    void handleModifiedModel() {
        rowFilterEntry.columns = tableModel.getColumnCount();
        reapplyFilter();
    }


    /**
     * @see  ITableModelFilter#getModel()
     */
    public TableModel getModel() {
        return tableModel;
    }


    /**
     * @see  ITableModelFilter#setRowFilter(RowFilter)
     */
    public void setRowFilter(RowFilter newValue) {
        this.tableFilter = newValue;
        reapplyFilter();
        fireTableDataChanged();
    }


    /**
     * Returns the location of index in terms of the underlying model.
     */
    public int convertRowIndexToModel(int index) {
        return rowsMapper[index];
    }

    /**
     * Returns the location of index in terms of the view.
     */
    public int convertRowIndexToView(int index) {
        if (!viewMap) {
            viewMap = true;
            viewsMapper = resizeArray(viewsMapper, tableModel.getRowCount());
            for (int i = 0; i < validRows; i++)
                viewsMapper[rowsMapper[i]] = i;
        }

        return viewsMapper[index];
    }

    /**
     * Resizes a buffer to have the desired size. Whole array is set to -1
     */
    private int[] resizeArray(int[] original, int size) {
        int[] ret;
        if ((original.length >= size) && (original.length <= ((size / 4) * 5))) {
            ret = original;
        } else {
            ret = new int[size];
        }
        Arrays.fill(ret, -1);

        return ret;
    }


    /**
     * Reapplies the filter, updating the rowsMapper and validRows private variables
     */
    void reapplyFilter() {
        viewMap = false;

        int rows = tableModel.getRowCount();
        rowsMapper = resizeArray(rowsMapper, rows);
        validRows = 0;

        for (int j = 0; j < rows; j++) {
            rowFilterEntry.modelRow = j;
            if ((tableFilter == null) || tableFilter.include(rowFilterEntry)) {
                rowsMapper[validRows++] = j;
            }
        }
    }


    @Override public Class<?> getColumnClass(int columnIndex) {
        return tableModel.getColumnClass(columnIndex);
    }


    @Override public String getColumnName(int column) {
        return tableModel.getColumnName(column);
    }


    public int getColumnCount() {
        return tableModel.getColumnCount();
    }


    public int getRowCount() {
        return validRows;
    }


    public Object getValueAt(int rowIndex, int columnIndex) {
        return tableModel.getValueAt(convertRowIndexToModel(rowIndex), columnIndex);
    }


    @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
        return tableModel.isCellEditable(convertRowIndexToModel(rowIndex), columnIndex);
    }


    @Override public void setValueAt(Object value, int rowIndex, int columnIndex) {
        tableModel.setValueAt(value, convertRowIndexToModel(rowIndex), columnIndex);
    }


    /**
     * Private implementation of the {@link RowFilter.Entry}, to access the elements in the table
     * row
     */
    class RowFilterEntry extends RowFilter.Entry {
        int columns;
        int modelRow;

        @Override public Object getValue(int index) {
            return tableModel.getValueAt(modelRow, index);
        }
    }


    /**
     * Private {@link TableModelListener} to follow the original table model changes.
     */
    class TableListener implements TableModelListener {
        public void tableChanged(TableModelEvent e) {
            if (tableFilter == null) {
                fireTableChanged(e);
            } else if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
                handleModifiedModel();
                fireTableChanged(e);
            } else if (e.getType() == TableModelEvent.UPDATE && e.getLastRow()!=Integer.MAX_VALUE) {
                fireTableChanged(e);
            } else {
                reapplyFilter();
                fireTableDataChanged();
            }
        }
    }
}
