/**
 * Author:  Luis M Pena  ( byteslooser@gmail.com )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  byteslooser@gmail.com
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

package com.byteslooser.filters;

import com.byteslooser.filters.artifacts.RowFilter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * <p>Abstract parent class to support the composition of multiple filters.</p>
 *
 * <p>The exact composition semantics (and / or / not) are not defined.</p>
 *
 * @author  Luis M Pena - byteslooser@gmail.com
 */
abstract public class AbstractComposedFilter extends AbstractObservableRowFilter
    implements IFilterObserver {

    /** Map containing, for each controlled observable filter, the associated filter instance */
    protected Map<IFilterObservable, RowFilter> filters =
        new HashMap<IFilterObservable, RowFilter>();

    /**
     * Constructor built up out of none or more {@link com.byteslooser.filters.IFilterObservable}
     * instances
     */
    protected AbstractComposedFilter(IFilterObservable... observables) {
        addFilterObservable(observables);
    }

    /**
     * Detaches the instance from any observer/observable
     */
    public void detach() {
        filterObservers.clear();
    }

    /**
     * Subscribes one or more {@link com.byteslooser.filters.IFilterObservable} instances to receive
     * filter events from this composition filter.
     */
    public void addFilterObservable(IFilterObservable... observables) {
        for (IFilterObservable observable : observables) {
            if (!filters.containsKey(observable)) {
                filters.put(observable, null);
                observable.addFilterObserver(this);
            }
        }
    }

    /**
     * Unsubscribes a {@link com.byteslooser.filters.IFilterObservable} that was previously
     * subscribed to receibe filter events
     */
    public void removeFilterObservable(IFilterObservable observable) {
        if (filters.containsKey(observable)) {
            observable.removeFilterObserver(this);
            if (null != filters.remove(observable))
                reportFilterUpdatedToObservers();
        }
    }

    /**
     * Returns all {@link com.byteslooser.filters.IFilterObservable} instances previously added.
     */
    public Set<IFilterObservable> getFilterObservables() {
        return new HashSet<IFilterObservable>(filters.keySet());
    }

    /**
     * @see  IFilterObserver#filterUpdated(IFilterObservable, RowFilter)
     */
    public void filterUpdated(IFilterObservable producer, RowFilter newValue) {
        if (!filters.containsKey(producer))
            return;

        RowFilter oldValue = filters.put(producer, newValue);
        if ((oldValue == null) && (newValue == null))
            return;
        reportFilterUpdatedToObservers();
    }

}
