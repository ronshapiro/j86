/*
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
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

package com.j86.sun.net.ssl.internal.www.protocol.https;

import j86.java.io.IOException;
import j86.java.net.URL;
import j86.java.net.Proxy;

/**
 * This class exists for compatibility with previous JSSE releases
 * only. The HTTPS implementation can now be found in
 * j86.sun.net.www.protocol.https.
 *
 */
public class Handler extends j86.sun.net.www.protocol.https.Handler {

    public Handler() {
        super();
    }

    public Handler(String proxy, int port) {
        super(proxy, port);
    }

    protected j86.java.net.URLConnection openConnection(URL u) throws IOException {
        return openConnection(u, (Proxy)null);
    }

    protected j86.java.net.URLConnection openConnection(URL u, Proxy p) throws IOException {
        return new HttpsURLConnectionOldImpl(u, p, this);
    }
}
