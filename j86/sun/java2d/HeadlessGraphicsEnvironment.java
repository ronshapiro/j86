/*
 * Copyright (c) 2000, 2010, Oracle and/or its affiliates. All rights reserved.
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

package j86.sun.java2d;

import j86.java.awt.GraphicsEnvironment;
import j86.java.awt.GraphicsDevice;
import j86.java.awt.Graphics2D;
import j86.java.awt.HeadlessException;
import j86.java.awt.image.BufferedImage;
import j86.java.awt.Font;
import j86.java.text.AttributedCharacterIterator;
import j86.java.awt.print.PrinterJob;
import j86.java.util.Map;
import j86.java.util.Hashtable;
import j86.java.util.Locale;
import j86.java.util.Vector;
import j86.java.util.StringTokenizer;
import j86.java.util.ResourceBundle;
import j86.java.util.MissingResourceException;
import j86.java.io.IOException;
import j86.java.io.FilenameFilter;
import j86.java.io.File;
import j86.java.util.NoSuchElementException;
import j86.sun.awt.FontConfiguration;
import j86.java.util.TreeMap;
import j86.java.util.Set;
import j86.java.awt.font.TextAttribute;
import j86.java.io.InputStream;
import j86.java.io.FileInputStream;
import j86.java.io.BufferedInputStream;
import j86.java.util.Properties;
import j86.java.awt.Point;
import j86.java.awt.Rectangle;

/**
 * Headless decorator implementation of a SunGraphicsEnvironment
 */

public class HeadlessGraphicsEnvironment extends GraphicsEnvironment {

    private GraphicsEnvironment ge;

    public HeadlessGraphicsEnvironment(GraphicsEnvironment ge) {
        this.ge = ge;
    }

    public GraphicsDevice[] getScreenDevices()
        throws HeadlessException {
        throw new HeadlessException();
    }

    public GraphicsDevice getDefaultScreenDevice()
        throws HeadlessException {
        throw new HeadlessException();
    }

    public Point getCenterPoint() throws HeadlessException {
        throw new HeadlessException();
    }

    public Rectangle getMaximumWindowBounds() throws HeadlessException {
        throw new HeadlessException();
    }

    public Graphics2D createGraphics(BufferedImage img) {
        return ge.createGraphics(img); }

    public Font[] getAllFonts() { return ge.getAllFonts(); }

    public String[] getAvailableFontFamilyNames() {
        return ge.getAvailableFontFamilyNames(); }

    public String[] getAvailableFontFamilyNames(Locale l) {
        return ge.getAvailableFontFamilyNames(l); }

    /* Used by FontManager : internal API */
    public GraphicsEnvironment getSunGraphicsEnvironment() {
        return ge;
    }
}
