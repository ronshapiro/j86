/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
package j86.com.sun.org.apache.xml.internal.security.keys.keyresolver.implementations;

import j86.java.security.PrivateKey;
import j86.java.security.PublicKey;
import j86.java.security.cert.X509Certificate;

import j86.javax.crypto.SecretKey;

import j86.com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import j86.com.sun.org.apache.xml.internal.security.keys.content.DEREncodedKeyValue;
import j86.com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolverException;
import j86.com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolverSpi;
import j86.com.sun.org.apache.xml.internal.security.keys.storage.StorageResolver;
import j86.com.sun.org.apache.xml.internal.security.utils.Constants;
import j86.com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import org.w3c.dom.Element;

/**
 * KeyResolverSpi implementation which resolves public keys from a
 * <code>dsig11:DEREncodedKeyValue</code> element.
 *
 * @author Brent Putman (putmanb@georgetown.edu)
 */
public class DEREncodedKeyValueResolver extends KeyResolverSpi {

    /** {@link org.apache.commons.logging} logging facility */
    private static j86.java.util.logging.Logger log =
        j86.java.util.logging.Logger.getLogger(DEREncodedKeyValueResolver.class.getName());

    /** {@inheritDoc}. */
    public boolean engineCanResolve(Element element, String baseURI, StorageResolver storage) {
        return XMLUtils.elementIsInSignature11Space(element, Constants._TAG_DERENCODEDKEYVALUE);
    }

    /** {@inheritDoc}. */
    public PublicKey engineLookupAndResolvePublicKey(Element element, String baseURI, StorageResolver storage)
        throws KeyResolverException {

        if (log.isLoggable(j86.java.util.logging.Level.FINE)) {
            log.log(j86.java.util.logging.Level.FINE, "Can I resolve " + element.getTagName());
        }

        if (!engineCanResolve(element, baseURI, storage)) {
            return null;
        }

        try {
            DEREncodedKeyValue derKeyValue = new DEREncodedKeyValue(element, baseURI);
            return derKeyValue.getPublicKey();
        } catch (XMLSecurityException e) {
            if (log.isLoggable(j86.java.util.logging.Level.FINE)) {
                log.log(j86.java.util.logging.Level.FINE, "XMLSecurityException", e);
            }
        }

        return null;
    }

    /** {@inheritDoc}. */
    public X509Certificate engineLookupResolveX509Certificate(Element element, String baseURI, StorageResolver storage)
        throws KeyResolverException {
        return null;
    }

    /** {@inheritDoc}. */
    public SecretKey engineLookupAndResolveSecretKey(Element element, String baseURI, StorageResolver storage)
        throws KeyResolverException {
        return null;
    }

    /** {@inheritDoc}. */
    public PrivateKey engineLookupAndResolvePrivateKey(Element element, String baseURI, StorageResolver storage)
        throws KeyResolverException {
        return null;
    }



}
