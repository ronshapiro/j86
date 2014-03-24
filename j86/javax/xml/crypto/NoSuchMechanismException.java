/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
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
/*
 * $Id: NoSuchMechanismException.java,v 1.4 2005/05/10 15:47:42 mullan Exp $
 */
package j86.javax.xml.crypto;

import j86.java.io.PrintStream;
import j86.java.io.PrintWriter;
import j86.javax.xml.crypto.dsig.Manifest;
import j86.javax.xml.crypto.dsig.XMLSignature;
import j86.javax.xml.crypto.dsig.XMLSignatureFactory;
import j86.javax.xml.crypto.dsig.keyinfo.KeyInfo;
import j86.javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;

/**
 * This exception is thrown when a particular XML mechanism is requested but
 * is not available in the environment.
 *
 * <p>A <code>NoSuchMechanismException</code> can contain a cause: another
 * throwable that caused this <code>NoSuchMechanismException</code> to get
 * thrown.
 *
 * @author Sean Mullan
 * @author JSR 105 Expert Group
 * @since 1.6
 * @see XMLSignatureFactory#getInstance XMLSignatureFactory.getInstance
 * @see KeyInfoFactory#getInstance KeyInfoFactory.getInstance
 */
public class NoSuchMechanismException extends RuntimeException {

    private static final long serialVersionUID = 4189669069570660166L;

    /**
     * The throwable that caused this exception to get thrown, or null if this
     * exception was not caused by another throwable or if the causative
     * throwable is unknown.
     *
     * @serial
     */
    private Throwable cause;

    /**
     * Constructs a new <code>NoSuchMechanismException</code> with
     * <code>null</code> as its detail message.
     */
    public NoSuchMechanismException() {
        super();
    }

    /**
     * Constructs a new <code>NoSuchMechanismException</code> with the
     * specified detail message.
     *
     * @param message the detail message
     */
    public NoSuchMechanismException(String message) {
        super(message);
    }

    /**
     * Constructs a new <code>NoSuchMechanismException</code> with the
     * specified detail message and cause.
     * <p>Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param message the detail message
     * @param cause the cause (A <tt>null</tt> value is permitted, and
     *        indicates that the cause is nonexistent or unknown.)
     */
    public NoSuchMechanismException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }

    /**
     * Constructs a new <code>NoSuchMechanismException</code> with the
     * specified cause and a detail message of
     * <code>(cause==null ? null : cause.toString())</code> (which typically
     * contains the class and detail message of <code>cause</code>).
     *
     * @param cause the cause (A <tt>null</tt> value is permitted, and
     *        indicates that the cause is nonexistent or unknown.)
     */
    public NoSuchMechanismException(Throwable cause) {
        super(cause==null ? null : cause.toString());
        this.cause = cause;
    }

    /**
     * Returns the cause of this <code>NoSuchMechanismException</code> or
     * <code>null</code> if the cause is nonexistent or unknown.  (The
     * cause is the throwable that caused this
     * <code>NoSuchMechanismException</code> to get thrown.)
     *
     * @return the cause of this <code>NoSuchMechanismException</code> or
     *         <code>null</code> if the cause is nonexistent or unknown.
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Prints this <code>NoSuchMechanismException</code>, its backtrace and
     * the cause's backtrace to the standard error stream.
     */
    public void printStackTrace() {
        super.printStackTrace();
        //XXX print backtrace of cause
    }

    /**
     * Prints this <code>NoSuchMechanismException</code>, its backtrace and
     * the cause's backtrace to the specified print stream.
     *
     * @param s <code>PrintStream</code> to use for output
     */
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        //XXX print backtrace of cause
    }

    /**
     * Prints this <code>NoSuchMechanismException</code>, its backtrace and
     * the cause's backtrace to the specified print writer.
     *
     * @param s <code>PrintWriter</code> to use for output
     */
    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        //XXX print backtrace of cause
    }
}
