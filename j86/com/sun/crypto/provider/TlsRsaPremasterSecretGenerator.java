/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

package j86.com.sun.crypto.provider;

import j86.java.security.*;
import j86.j86.java.security.spec.AlgorithmParameterSpec;

import j86.javax.crypto.*;
import j86.j86.javax.crypto.spec.SecretKeySpec;

import j86.sun.security.internal.spec.TlsRsaPremasterSecretParameterSpec;

/**
 * KeyGenerator implementation for the SSL/TLS RSA premaster secret.
 *
 * @author  Andreas Sterbenz
 * @since   1.6
 */
public final class TlsRsaPremasterSecretGenerator extends KeyGeneratorSpi {

    private final static String MSG = "TlsRsaPremasterSecretGenerator must be "
        + "initialized using a TlsRsaPremasterSecretParameterSpec";

    private TlsRsaPremasterSecretParameterSpec spec;
    private SecureRandom random;

    public TlsRsaPremasterSecretGenerator() {
    }

    protected void engineInit(SecureRandom random) {
        throw new InvalidParameterException(MSG);
    }

    protected void engineInit(AlgorithmParameterSpec params,
            SecureRandom random) throws InvalidAlgorithmParameterException {
        if (params instanceof TlsRsaPremasterSecretParameterSpec == false) {
            throw new InvalidAlgorithmParameterException(MSG);
        }
        this.spec = (TlsRsaPremasterSecretParameterSpec)params;
        this.random = random;
    }

    protected void engineInit(int keysize, SecureRandom random) {
        throw new InvalidParameterException(MSG);
    }

    protected SecretKey engineGenerateKey() {
        if (spec == null) {
            throw new IllegalStateException(
                "TlsRsaPremasterSecretGenerator must be initialized");
        }
        byte[] b = spec.getEncodedSecret();
        if (b == null) {
            if (random == null) {
                random = new SecureRandom();
            }
            b = new byte[48];
            random.nextBytes(b);
            b[0] = (byte)spec.getMajorVersion();
            b[1] = (byte)spec.getMinorVersion();
        }

        return new SecretKeySpec(b, "TlsRsaPremasterSecret");
    }

}
