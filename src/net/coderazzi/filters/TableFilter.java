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

package net.coderazzi.filters;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import net.coderazzi.filters.artifacts.ITableModelFilter;
import net.coderazzi.filters.artifacts.RowFilter;
import net.coderazzi.filters.artifacts.TableModelFilter;



/**
 * <p>TableFilter represents a {@link RowFilter} instance that can be attached to a
 * {@link javax.swing.JTable} to compose dynamically the outcome of one or more filter editors. As
 * such, it is a dynamic filter, which updates the table when there are changes in any of the
 * composed sub filters.</p>
 *
 * <p>Users require instancing TableFilter instances only when managing their own filter editors.
 * Note that the {@link net.coderazzi.filters.gui.TableFilterHeader} already handles its own
 * TableFilter, and keeps track of any table changes, updating automatically the editors.</p>
 * </p>
 *
 * <p>When users instanciate directly TableFilter objects, care must be taken to update the
 * associated editors when the table model changes.</p>
 *
 * <p>In Java 5, the {@link javax.swing.JTable} has no sorting or filtering capabilities, so the
 * implementation of the TableFilter is slightly different in Java 5 and 6. When the table is
 * attached, it should contain already the model to filter, and the model itself should be an
 * instance of {@link ITableModelFilter}. If this is not the case, the TableFilter automatically
 * creates a (@link ITableModelFilter} and attaches it to the table.</p>
 *
 * <p>It is important, therefore, not to override afterwards the model in the table, or, if this is
 * done, it is needed to invoke {@link TableFilter#setModel(TableModel)} on the TableFilter.</p>
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
public class TableFilter extends AndFilter {

    /**
     * sendNotifications is used internally as a semaphore to disable temporarily notifications to
     * the filter observers. Notifications are only sent to the observers when this variable is non
     * negative.
     */
    int sendNotifications = 0;

    /**
     * pendingNotifications keeps track of notifications to be sent to the observers, but were
     * discarded because the variable sendNotifications was false.
     */
    private boolean pendingNotifications;

    /** The associated table, if any. */
    JTable table;

    /**
     * Default constructor
     */
    public TableFilter() {

        // create an observer instance to notify the associated table when there
        // are filter changes.
        addFilterObserver(new IFilterObserver() {
                public void filterUpdated(IFilter obs) {
                    notifyUpdatedFilter(false);
                }
            });
    }

    /**
     * Basic constructor, setting directly the controlled table
     */
    public TableFilter(JTable table) {
        this();
        setTable(table);
    }

    /**
     * Method to set the associated table. If the table had not defined its own
     * {@link javax.swing.RowSorter}, the default one is automatically created.
     */
    public void setTable(JTable table) {
    	if (this.table!=null){
    		TableModel tm = this.table.getModel();
    		if (tm instanceof ITableModelFilter){
    			this.table.setModel(((ITableModelFilter) tm).getModel());
    		}
    	}
        this.table = table;
        if (table!=null){
        	setModel(table.getModel());
        }
    }

    /**
     * Returns the associated table
     */
    public JTable getTable() {
        return table;
    }


    /**
     * Method to set the associated model. If the tableModel associated to the table does not
     * implement the {@link ITableModelFilter} interface, one is automatically created
     */
    public void setModel(TableModel tableModel) {
        getRowFilter(tableModel);
    }


    /**
     * <p>Temporarily enable/disable notifications to the observers, including the registered
     * {@link javax.swing.JTable}.</p>
     *
     * <p>Multiple calls to this method can be issued, but the caller must ensure that there are as
     * many calls with true parameter as with false parameter, as the notifications are only
     * re-enabled when the zero balance is reached.</p>
     */
    public boolean enableNotifications(boolean enable) {
        sendNotifications += enable ? 1 : -1;
        if (enable && (sendNotifications == 0) && pendingNotifications) {
            notifyUpdatedFilter(false);
        }

        return sendNotifications >= 0;
    }

    /**
     * <p>Method to force the sending of notifications, even if they are currently temporarily
     * disabled.</p>
     *
     * <p>Note that, in any case, the update notification is only sent if there is any pending
     * notifications.</p>
     */
    public void sendPendingNotifications() {
        if (pendingNotifications) {
            notifyUpdatedFilter(true);
        }
    }

    /**
     * Internal method to send a notification to the observers, verifying first if the notifications
     * are currently enabled.
     */
    void notifyUpdatedFilter(boolean forced) {
        if (forced || (sendNotifications < 0)) {
            pendingNotifications = true;

            return;
        }
        pendingNotifications = sendFilterUpdateNotification();
    }

    /**
     * Internal method to send without further checks a notification to the observers.
     */
    private boolean sendFilterUpdateNotification() {
    	if (table!=null){
	    	TableModel model = table.getModel();
	    	if (model!=null){
	    		getRowFilter(model).setRowFilter(this);
	    	}
    	}
        return false;
    }

    /**
     * Returns the row filter associated to the current table, creating a default one if none.
     */
    private ITableModelFilter getRowFilter(TableModel tableModel) {
        if (tableModel instanceof ITableModelFilter) {
            return (TableModelFilter) tableModel;
        }

        TableModelFilter modelFilter = new TableModelFilter(tableModel);
        table.setModel(modelFilter);

        return modelFilter;
    }


}