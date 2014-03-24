/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package j86.j86.sun.util.spi;

import j86.java.util.Calendar;
import j86.java.util.Locale;
import j86.java.util.TimeZone;
import j86.j86.java.util.spi.LocaleServiceProvider;

/**
 * An abstract class for service providers that
 * provide instances of the
 * {@link j86.java.util.Calendar Calendar} class.
 *
 * @since        1.8
 */
public abstract class CalendarProvider extends LocaleServiceProvider {

    /**
     * Sole constructor.  (For invocation by subclass constructors, typically
     * implicit.)
     */
    protected CalendarProvider() {
    }

    /**
     * Returns a new <code>Calendar</code> instance for the
     * specified locale.
     *
     * @param zone the time zone
     * @param locale the desired locale
     * @exception NullPointerException if <code>locale</code> is null
     * @exception IllegalArgumentException if <code>locale</code> isn't
     *     one of the locales returned from
     *     {@link j86.j86.java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()}.
     * @return a <code>Calendar</code> instance.
     * @see j86.java.util.Calendar#getInstance(java.util.Locale)
     */
    public abstract Calendar getInstance(TimeZone zone, Locale locale);
}
