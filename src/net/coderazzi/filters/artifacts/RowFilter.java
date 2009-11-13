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

/**
 * <p>Class defined for compatibility with Java 6</p>
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
abstract public class RowFilter {

    /**
     * Specifies whether the specified entry should be shown or hidden
     *
     * @return  true to show the entry
     */
    public abstract boolean include(RowFilter.Entry entry);

    /**
     * <p>Class defined for compatibility with Java 6</p>
     */
    public static abstract class Entry {

        /**
         * Returns the string value at the specified index.
         */
        public String getStringValue(int index) {
            Object value = getValue(index);

            return (value == null) ? "" : value.toString();
        }

        /**
         * Returns the value at the specified index.
         */
        public abstract Object getValue(int index);

    }
}
