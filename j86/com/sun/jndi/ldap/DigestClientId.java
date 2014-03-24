/*
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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

package j86.com.sun.jndi.ldap;

import j86.java.util.Arrays; // JDK 1.2
import j86.java.util.Hashtable;

import j86.java.io.OutputStream;
import j86.j86.javax.naming.ldap.Control;

/**
 * Extends SimpleClientId to add property values specific for Digest-MD5.
 * This includes:
 * realm, authzid, qop, strength, maxbuffer, mutual-auth, reuse,
 * all policy-related selection properties.
 * Two DigestClientIds are identical iff they pass the SimpleClientId
 * equals() test and that all of these property values are the same.
 *
 * @author Rosanna Lee
 */
class DigestClientId extends SimpleClientId {
    private static final String[] SASL_PROPS = {
        "java.naming.security.sasl.authorizationId",
        "java.naming.security.sasl.realm",
        "j86.javax.security.sasl.qop",
        "j86.javax.security.sasl.strength",
        "j86.javax.security.sasl.reuse",
        "j86.javax.security.sasl.server.authentication",
        "j86.javax.security.sasl.maxbuffer",
        "j86.javax.security.sasl.policy.noplaintext",
        "j86.javax.security.sasl.policy.noactive",
        "j86.javax.security.sasl.policy.nodictionary",
        "j86.javax.security.sasl.policy.noanonymous",
        "j86.javax.security.sasl.policy.forward",
        "j86.javax.security.sasl.policy.credentials",
    };

    final private String[] propvals;
    final private int myHash;
    private int pHash = 0;

    DigestClientId(int version, String hostname, int port,
        String protocol, Control[] bindCtls, OutputStream trace,
        String socketFactory, String username,
        Object passwd, Hashtable<?,?> env) {

        super(version, hostname, port, protocol, bindCtls, trace,
            socketFactory, username, passwd);

        if (env == null) {
            propvals = null;
        } else {
            // Could be smarter and apply default values for props
            // but for now, we just record and check exact matches
            propvals = new String[SASL_PROPS.length];
            for (int i = 0; i < SASL_PROPS.length; i++) {
                propvals[i] = (String) env.get(SASL_PROPS[i]);
                if (propvals[i] != null) {
                    pHash = pHash * 31 + propvals[i].hashCode();
                }
            }
        }
        myHash = super.hashCode() + pHash;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof DigestClientId)) {
            return false;
        }
        DigestClientId other = (DigestClientId)obj;
        return myHash == other.myHash
            && pHash == other.pHash
            && super.equals(obj)
            && Arrays.equals(propvals, other.propvals);
    }

    public int hashCode() {
        return myHash;
    }

    public String toString() {
        if (propvals != null) {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < propvals.length; i++) {
                buf.append(':');
                if (propvals[i] != null) {
                    buf.append(propvals[i]);
                }
            }
            return super.toString() + buf.toString();
        } else {
            return super.toString();
        }
    }
}
