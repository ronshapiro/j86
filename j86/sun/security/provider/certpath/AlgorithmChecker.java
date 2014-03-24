/*
 * Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
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

package j86.j86.sun.security.provider.certpath;

import j86.java.security.AlgorithmConstraints;
import j86.java.security.CryptoPrimitive;
import j86.java.util.Collection;
import j86.java.util.Collections;
import j86.java.util.Set;
import j86.java.util.EnumSet;
import j86.java.util.HashSet;
import j86.java.math.BigInteger;
import j86.java.security.PublicKey;
import j86.java.security.KeyFactory;
import j86.java.security.AlgorithmParameters;
import j86.java.security.NoSuchAlgorithmException;
import j86.java.security.GeneralSecurityException;
import j86.j86.java.security.cert.Certificate;
import j86.j86.java.security.cert.X509CRL;
import j86.j86.java.security.cert.X509Certificate;
import j86.j86.java.security.cert.PKIXCertPathChecker;
import j86.j86.java.security.cert.TrustAnchor;
import j86.j86.java.security.cert.CRLException;
import j86.j86.java.security.cert.CertificateException;
import j86.j86.java.security.cert.CertPathValidatorException;
import j86.j86.java.security.cert.CertPathValidatorException.BasicReason;
import j86.j86.java.security.cert.PKIXReason;
import j86.java.io.IOException;
import j86.j86.java.security.interfaces.*;
import j86.j86.java.security.spec.*;

import j86.sun.security.util.DisabledAlgorithmConstraints;
import j86.sun.security.x509.X509CertImpl;
import j86.sun.security.x509.X509CRLImpl;
import j86.sun.security.x509.AlgorithmId;

/**
 * A <code>PKIXCertPathChecker</code> implementation to check whether a
 * specified certificate contains the required algorithm constraints.
 * <p>
 * Certificate fields such as the subject public key, the signature
 * algorithm, key usage, extended key usage, etc. need to conform to
 * the specified algorithm constraints.
 *
 * @see PKIXCertPathChecker
 * @see PKIXParameters
 */
final public class AlgorithmChecker extends PKIXCertPathChecker {

    private final AlgorithmConstraints constraints;
    private final PublicKey trustedPubKey;
    private PublicKey prevPubKey;

    private final static Set<CryptoPrimitive> SIGNATURE_PRIMITIVE_SET =
                                    EnumSet.of(CryptoPrimitive.SIGNATURE);

    private final static DisabledAlgorithmConstraints
        certPathDefaultConstraints = new DisabledAlgorithmConstraints(
            DisabledAlgorithmConstraints.PROPERTY_CERTPATH_DISABLED_ALGS);

    /**
     * Create a new <code>AlgorithmChecker</code> with the algorithm
     * constraints specified in security property
     * "jdk.certpath.disabledAlgorithms".
     *
     * @param anchor the trust anchor selected to validate the target
     *     certificate
     */
    public AlgorithmChecker(TrustAnchor anchor) {
        this(anchor, certPathDefaultConstraints);
    }

    /**
     * Create a new <code>AlgorithmChecker</code> with the
     * given {@code AlgorithmConstraints}.
     * <p>
     * Note that this constructor will be used to check a certification
     * path where the trust anchor is unknown, or a certificate list which may
     * contain the trust anchor. This constructor is used by SunJSSE.
     *
     * @param constraints the algorithm constraints (or null)
     */
    public AlgorithmChecker(AlgorithmConstraints constraints) {
        this.prevPubKey = null;
        this.trustedPubKey = null;
        this.constraints = constraints;
    }

    /**
     * Create a new <code>AlgorithmChecker</code> with the
     * given <code>TrustAnchor</code> and <code>AlgorithmConstraints</code>.
     *
     * @param anchor the trust anchor selected to validate the target
     *     certificate
     * @param constraints the algorithm constraints (or null)
     *
     * @throws IllegalArgumentException if the <code>anchor</code> is null
     */
    public AlgorithmChecker(TrustAnchor anchor,
            AlgorithmConstraints constraints) {

        if (anchor == null) {
            throw new IllegalArgumentException(
                        "The trust anchor cannot be null");
        }

        if (anchor.getTrustedCert() != null) {
            this.trustedPubKey = anchor.getTrustedCert().getPublicKey();
        } else {
            this.trustedPubKey = anchor.getCAPublicKey();
        }

        this.prevPubKey = trustedPubKey;
        this.constraints = constraints;
    }

    @Override
    public void init(boolean forward) throws CertPathValidatorException {
        //  Note that this class does not support forward mode.
        if (!forward) {
            if (trustedPubKey != null) {
                prevPubKey = trustedPubKey;
            } else {
                prevPubKey = null;
            }
        } else {
            throw new
                CertPathValidatorException("forward checking not supported");
        }
    }

    @Override
    public boolean isForwardCheckingSupported() {
        //  Note that as this class does not support forward mode, the method
        //  will always returns false.
        return false;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return null;
    }

    @Override
    public void check(Certificate cert,
            Collection<String> unresolvedCritExts)
            throws CertPathValidatorException {

        if (!(cert instanceof X509Certificate) || constraints == null) {
            // ignore the check for non-x.509 certificate or null constraints
            return;
        }

        X509CertImpl x509Cert = null;
        try {
            x509Cert = X509CertImpl.toImpl((X509Certificate)cert);
        } catch (CertificateException ce) {
            throw new CertPathValidatorException(ce);
        }

        PublicKey currPubKey = x509Cert.getPublicKey();
        String currSigAlg = x509Cert.getSigAlgName();

        AlgorithmId algorithmId = null;
        try {
            algorithmId = (AlgorithmId)x509Cert.get(X509CertImpl.SIG_ALG);
        } catch (CertificateException ce) {
            throw new CertPathValidatorException(ce);
        }

        AlgorithmParameters currSigAlgParams = algorithmId.getParameters();

        // Check the current signature algorithm
        if (!constraints.permits(
                SIGNATURE_PRIMITIVE_SET,
                currSigAlg, currSigAlgParams)) {
            throw new CertPathValidatorException(
                "Algorithm constraints check failed: " + currSigAlg,
                null, null, -1, BasicReason.ALGORITHM_CONSTRAINED);
        }

        // check the key usage and key size
        boolean[] keyUsage = x509Cert.getKeyUsage();
        if (keyUsage != null && keyUsage.length < 9) {
            throw new CertPathValidatorException(
                "incorrect KeyUsage extension",
                null, null, -1, PKIXReason.INVALID_KEY_USAGE);
        }

        if (keyUsage != null) {
            Set<CryptoPrimitive> primitives =
                        EnumSet.noneOf(CryptoPrimitive.class);

            if (keyUsage[0] || keyUsage[1] || keyUsage[5] || keyUsage[6]) {
                // keyUsage[0]: KeyUsage.digitalSignature
                // keyUsage[1]: KeyUsage.nonRepudiation
                // keyUsage[5]: KeyUsage.keyCertSign
                // keyUsage[6]: KeyUsage.cRLSign
                primitives.add(CryptoPrimitive.SIGNATURE);
            }

            if (keyUsage[2]) {      // KeyUsage.keyEncipherment
                primitives.add(CryptoPrimitive.KEY_ENCAPSULATION);
            }

            if (keyUsage[3]) {      // KeyUsage.dataEncipherment
                primitives.add(CryptoPrimitive.PUBLIC_KEY_ENCRYPTION);
            }

            if (keyUsage[4]) {      // KeyUsage.keyAgreement
                primitives.add(CryptoPrimitive.KEY_AGREEMENT);
            }

            // KeyUsage.encipherOnly and KeyUsage.decipherOnly are
            // undefined in the absence of the keyAgreement bit.

            if (!primitives.isEmpty()) {
                if (!constraints.permits(primitives, currPubKey)) {
                    throw new CertPathValidatorException(
                        "algorithm constraints check failed",
                        null, null, -1, BasicReason.ALGORITHM_CONSTRAINED);
                }
            }
        }

        // Check with previous cert for signature algorithm and public key
        if (prevPubKey != null) {
            if (currSigAlg != null) {
                if (!constraints.permits(
                        SIGNATURE_PRIMITIVE_SET,
                        currSigAlg, prevPubKey, currSigAlgParams)) {
                    throw new CertPathValidatorException(
                        "Algorithm constraints check failed: " + currSigAlg,
                        null, null, -1, BasicReason.ALGORITHM_CONSTRAINED);
                }
            }

            // Inherit key parameters from previous key
            if (PKIX.isDSAPublicKeyWithoutParams(currPubKey)) {
                // Inherit DSA parameters from previous key
                if (!(prevPubKey instanceof DSAPublicKey)) {
                    throw new CertPathValidatorException("Input key is not " +
                        "of a appropriate type for inheriting parameters");
                }

                DSAParams params = ((DSAPublicKey)prevPubKey).getParams();
                if (params == null) {
                    throw new CertPathValidatorException(
                                    "Key parameters missing");
                }

                try {
                    BigInteger y = ((DSAPublicKey)currPubKey).getY();
                    KeyFactory kf = KeyFactory.getInstance("DSA");
                    DSAPublicKeySpec ks = new DSAPublicKeySpec(y,
                                                       params.getP(),
                                                       params.getQ(),
                                                       params.getG());
                    currPubKey = kf.generatePublic(ks);
                } catch (GeneralSecurityException e) {
                    throw new CertPathValidatorException("Unable to generate " +
                        "key with inherited parameters: " + e.getMessage(), e);
                }
            }
        }

        // reset the previous public key
        prevPubKey = currPubKey;

        // check the extended key usage, ignore the check now
        // List<String> extendedKeyUsages = x509Cert.getExtendedKeyUsage();

        // DO NOT remove any unresolved critical extensions
    }

    /**
     * Try to set the trust anchor of the checker.
     * <p>
     * If there is no trust anchor specified and the checker has not started,
     * set the trust anchor.
     *
     * @param anchor the trust anchor selected to validate the target
     *     certificate
     */
    void trySetTrustAnchor(TrustAnchor anchor) {
        // Don't bother if the check has started or trust anchor has already
        // specified.
        if (prevPubKey == null) {
            if (anchor == null) {
                throw new IllegalArgumentException(
                        "The trust anchor cannot be null");
            }

            // Don't bother to change the trustedPubKey.
            if (anchor.getTrustedCert() != null) {
                prevPubKey = anchor.getTrustedCert().getPublicKey();
            } else {
                prevPubKey = anchor.getCAPublicKey();
            }
        }
    }

    /**
     * Check the signature algorithm with the specified public key.
     *
     * @param key the public key to verify the CRL signature
     * @param crl the target CRL
     */
    static void check(PublicKey key, X509CRL crl)
                        throws CertPathValidatorException {

        X509CRLImpl x509CRLImpl = null;
        try {
            x509CRLImpl = X509CRLImpl.toImpl(crl);
        } catch (CRLException ce) {
            throw new CertPathValidatorException(ce);
        }

        AlgorithmId algorithmId = x509CRLImpl.getSigAlgId();
        check(key, algorithmId);
    }

    /**
     * Check the signature algorithm with the specified public key.
     *
     * @param key the public key to verify the CRL signature
     * @param crl the target CRL
     */
    static void check(PublicKey key, AlgorithmId algorithmId)
                        throws CertPathValidatorException {
        String sigAlgName = algorithmId.getName();
        AlgorithmParameters sigAlgParams = algorithmId.getParameters();

        if (!certPathDefaultConstraints.permits(
                SIGNATURE_PRIMITIVE_SET, sigAlgName, key, sigAlgParams)) {
            throw new CertPathValidatorException(
                "algorithm check failed: " + sigAlgName + " is disabled",
                null, null, -1, BasicReason.ALGORITHM_CONSTRAINED);
        }
    }

}

