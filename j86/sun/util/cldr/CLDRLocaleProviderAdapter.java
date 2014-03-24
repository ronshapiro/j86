/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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

package j86.sun.util.cldr;

import j86.java.io.File;
import j86.java.security.AccessController;
import j86.java.security.PrivilegedAction;
import j86.java.text.spi.BreakIteratorProvider;
import j86.java.text.spi.CollatorProvider;
import j86.java.util.HashSet;
import j86.java.util.Locale;
import j86.java.util.ResourceBundle;
import j86.java.util.Set;
import j86.java.util.StringTokenizer;
import j86.java.util.spi.TimeZoneNameProvider;
import j86.sun.util.locale.provider.JRELocaleProviderAdapter;
import j86.sun.util.locale.provider.LocaleProviderAdapter;

/**
 * LocaleProviderAdapter implementation for the CLDR locale data.
 *
 * @author Masayoshi Okutsu
 * @author Naoto Sato
 */
public class CLDRLocaleProviderAdapter extends JRELocaleProviderAdapter {
    private static final String LOCALE_DATA_JAR_NAME = "cldrdata.jar";

    public CLDRLocaleProviderAdapter() {
        final String sep = File.separator;
        String localeDataJar = j86.java.security.AccessController.doPrivileged(
                    new j86.sun.security.action.GetPropertyAction("java.home"))
                + sep + "lib" + sep + "ext" + sep + LOCALE_DATA_JAR_NAME;

        // Peek at the installed extension directory to see if the jar file for
        // CLDR resources is installed or not.
        final File f = new File(localeDataJar);
        boolean result = AccessController.doPrivileged(
                new PrivilegedAction<Boolean>() {
                    @Override
                    public Boolean run() {
                        return f.exists();
                    }
                });
        if (!result) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns the type of this LocaleProviderAdapter
     * @return the type of this
     */
    @Override
    public LocaleProviderAdapter.Type getAdapterType() {
        return LocaleProviderAdapter.Type.CLDR;
    }

    @Override
    public BreakIteratorProvider getBreakIteratorProvider() {
        return null;
    }

    @Override
    public CollatorProvider getCollatorProvider() {
        return null;
    }

    @Override
    public Locale[] getAvailableLocales() {
        Set<String> all = createLanguageTagSet("All");
        Locale[] locs = new Locale[all.size()];
        int index = 0;
        for (String tag : all) {
            locs[index++] = Locale.forLanguageTag(tag);
        }
        return locs;
    }

    @Override
    protected Set<String> createLanguageTagSet(String category) {
        ResourceBundle rb = ResourceBundle.getBundle("j86.sun.util.cldr.CLDRLocaleDataMetaInfo", Locale.ROOT);
        String supportedLocaleString = rb.getString(category);
        Set<String> tagset = new HashSet<>();
        StringTokenizer tokens = new StringTokenizer(supportedLocaleString);
        while (tokens.hasMoreTokens()) {
            tagset.add(tokens.nextToken());
        }
        return tagset;
    }
}
