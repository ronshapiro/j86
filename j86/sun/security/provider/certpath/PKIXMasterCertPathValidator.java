/*
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
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

package j86.sun.security.provider.certpath;

import j86.sun.security.util.Debug;

import j86.java.util.Collections;
import j86.java.util.List;
import j86.java.util.Set;
import j86.java.security.cert.CertPath;
import j86.java.security.cert.CertPathValidatorException;
import j86.java.security.cert.PKIXCertPathChecker;
import j86.java.security.cert.PKIXReason;
import j86.java.security.cert.X509Certificate;

/**
 * This class is initialized with a list of <code>PKIXCertPathChecker</code>s
 * and is used to verify the certificates in a <code>CertPath</code> by
 * feeding each certificate to each <code>PKIXCertPathChecker</code>.
 *
 * @since       1.4
 * @author      Yassir Elley
 */
class PKIXMasterCertPathValidator {

    private static final Debug debug = Debug.getInstance("certpath");

    /**
     * Validates a certification path consisting exclusively of
     * <code>X509Certificate</code>s using the specified
     * <code>PKIXCertPathChecker</code>s. It is assumed that the
     * <code>PKIXCertPathChecker</code>s
     * have been initialized with any input parameters they may need.
     *
     * @param cpOriginal the original X509 CertPath passed in by the user
     * @param reversedCertList the reversed X509 CertPath (as a List)
     * @param certPathCheckers the PKIXCertPathCheckers
     * @throws CertPathValidatorException if cert path does not validate
     */
    static void validate(CertPath cpOriginal,
                         List<X509Certificate> reversedCertList,
                         List<PKIXCertPathChecker> certPathCheckers)
        throws CertPathValidatorException
    {
        // we actually process reversedCertList, but we keep cpOriginal because
        // we need to return the original certPath when we throw an exception.
        // we will also need to modify the index appropriately when we
        // throw an exception.

        int cpSize = reversedCertList.size();

        if (debug != null) {
            debug.println("--------------------------------------------------"
                  + "------------");
            debug.println("Executing PKIX certification path validation "
                  + "algorithm.");
        }

        for (int i = 0; i < cpSize; i++) {

            /* The basic loop algorithm is that we get the
             * current certificate, we verify the current certificate using
             * information from the previous certificate and from the state,
             * and we modify the state for the next loop by setting the
             * current certificate of this loop to be the previous certificate
             * of the next loop. The state is initialized during first loop.
             */
            if (debug != null)
                debug.println("Checking cert" + (i+1) + " ...");

            X509Certificate currCert = reversedCertList.get(i);
            Set<String> unresCritExts = currCert.getCriticalExtensionOIDs();
            if (unresCritExts == null) {
                unresCritExts = Collections.<String>emptySet();
            }

            if (debug != null && !unresCritExts.isEmpty()) {
                debug.println("Set of critical extensions:");
                for (String oid : unresCritExts) {
                    debug.println(oid);
                }
            }

            for (int j = 0; j < certPathCheckers.size(); j++) {

                PKIXCertPathChecker currChecker = certPathCheckers.get(j);
                if (debug != null) {
                    debug.println("-Using checker" + (j + 1) + " ... [" +
                        currChecker.getClass().getName() + "]");
                }

                if (i == 0)
                    currChecker.init(false);

                try {
                    currChecker.check(currCert, unresCritExts);

                    if (debug != null) {
                        debug.println("-checker" + (j + 1) +
                            " validation succeeded");
                    }

                } catch (CertPathValidatorException cpve) {
                    throw new CertPathValidatorException(cpve.getMessage(),
                        cpve.getCause(), cpOriginal, cpSize - (i + 1),
                        cpve.getReason());
                }
            }

            if (!unresCritExts.isEmpty()) {
                throw new CertPathValidatorException("unrecognized " +
                    "critical extension(s)", null, cpOriginal, cpSize-(i+1),
                    PKIXReason.UNRECOGNIZED_CRIT_EXT);
            }

            if (debug != null)
                debug.println("\ncert" + (i+1) + " validation succeeded.\n");
        }

        if (debug != null) {
            debug.println("Cert path validation succeeded. (PKIX validation "
                          + "algorithm)");
            debug.println("-------------------------------------------------"
                          + "-------------");
        }
    }
}
