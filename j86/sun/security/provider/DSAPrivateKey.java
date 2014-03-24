/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package j86.sun.security.provider;

import j86.java.util.*;
import j86.java.io.*;
import j86.java.math.BigInteger;
import j86.java.security.InvalidKeyException;
import j86.java.security.ProviderException;
import j86.java.security.AlgorithmParameters;
import j86.java.security.spec.DSAParameterSpec;
import j86.java.security.spec.InvalidParameterSpecException;
import j86.java.security.interfaces.DSAParams;

import j86.sun.security.x509.AlgIdDSA;
import j86.sun.security.pkcs.PKCS8Key;
import j86.sun.security.util.Debug;
import j86.sun.security.util.DerValue;
import j86.sun.security.util.DerInputStream;
import j86.sun.security.util.DerOutputStream;

/**
 * A PKCS#8 private key for the Digital Signature Algorithm.
 *
 * @author Benjamin Renaud
 *
 *
 * @see DSAPublicKey
 * @see AlgIdDSA
 * @see DSA
 */

public final class DSAPrivateKey extends PKCS8Key
implements j86.java.security.interfaces.DSAPrivateKey, Serializable {

    /** use serialVersionUID from JDK 1.1. for interoperability */
    private static final long serialVersionUID = -3244453684193605938L;

    /* the private key */
    private BigInteger x;

    /*
     * Keep this constructor for backwards compatibility with JDK1.1.
     */
    public DSAPrivateKey() {
    }

    /**
     * Make a DSA private key out of a private key and three parameters.
     */
    public DSAPrivateKey(BigInteger x, BigInteger p,
                         BigInteger q, BigInteger g)
    throws InvalidKeyException {
        this.x = x;
        algid = new AlgIdDSA(p, q, g);

        try {
            key = new DerValue(DerValue.tag_Integer,
                               x.toByteArray()).toByteArray();
            encode();
        } catch (IOException e) {
            InvalidKeyException ike = new InvalidKeyException(
                "could not DER encode x: " + e.getMessage());
            ike.initCause(e);
            throw ike;
        }
    }

    /**
     * Make a DSA private key from its DER encoding (PKCS #8).
     */
    public DSAPrivateKey(byte[] encoded) throws InvalidKeyException {
        clearOldKey();
        decode(encoded);
    }

    /**
     * Returns the DSA parameters associated with this key, or null if the
     * parameters could not be parsed.
     */
    public DSAParams getParams() {
        try {
            if (algid instanceof DSAParams) {
                return (DSAParams)algid;
            } else {
                DSAParameterSpec paramSpec;
                AlgorithmParameters algParams = algid.getParameters();
                if (algParams == null) {
                    return null;
                }
                paramSpec = algParams.getParameterSpec(DSAParameterSpec.class);
                return (DSAParams)paramSpec;
            }
        } catch (InvalidParameterSpecException e) {
            return null;
        }
    }

    /**
     * Get the raw private key, x, without the parameters.
     *
     * @see getParameters
     */
    public BigInteger getX() {
        return x;
    }

    private void clearOldKey() {
        int i;
        if (this.encodedKey != null) {
            for (i = 0; i < this.encodedKey.length; i++) {
                this.encodedKey[i] = (byte)0x00;
            }
        }
        if (this.key != null) {
            for (i = 0; i < this.key.length; i++) {
                this.key[i] = (byte)0x00;
            }
        }
    }

    protected void parseKeyBits() throws InvalidKeyException {
        try {
            DerInputStream in = new DerInputStream(key);
            x = in.getBigInteger();
        } catch (IOException e) {
            InvalidKeyException ike = new InvalidKeyException(e.getMessage());
            ike.initCause(e);
            throw ike;
        }
    }
}
