/**
 * Author:  Luis M Pena  ( sen@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  sen@coderazzi.net
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

import net.coderazzi.filters.artifacts.RowFilter;


/**
 * Composed set of filters, added via logical OR
 *
 * @author  Luis M Pena - sen@coderazzi.net
 */
public class OrFilter extends AbstractComposedFilter {

    /**
     * Constructor built up out of none or more {@link net.coderazzi.filters.IFilterObservable}
     * instances
     */
    public OrFilter(IFilterObservable... observables) {
        super(observables);
    }

    /**
     * @see  RowFilter#include(RowFilter.Entry)
     */
    @Override public boolean include(RowFilter.Entry rowEntry) {
        boolean ret = true;
        for (RowFilter filter : filters.values()) {
            if (filter != null) {
                if (filter.include(rowEntry))
                    return true;
                ret = false;
            }
        }

        return ret;
    }
}
