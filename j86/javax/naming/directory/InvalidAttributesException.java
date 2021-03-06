/*
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
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

package j86.javax.naming.directory;

import j86.javax.naming.NamingException;

/**
  * This exception is thrown when an attempt is
  * made to add or modify an attribute set that has been specified
  * incompletely or incorrectly. This could happen, for example,
  * when attempting to add or modify a binding, or to create a new
  * subcontext without specifying all the mandatory attributes
  * required for creation of the object.  Another situation in
  * which this exception is thrown is by specification of incompatible
  * attributes within the same attribute set, or attributes in conflict
  * with that specified by the object's schema.
  * <p>
  * Synchronization and serialization issues that apply to NamingException
  * apply directly here.
  *
  * @author Rosanna Lee
  * @author Scott Seligman
  * @since 1.3
  */

public class InvalidAttributesException extends NamingException {
    /**
     * Constructs a new instance of InvalidAttributesException using an
     * explanation. All other fields are set to null.
     * @param   explanation     Additional detail about this exception. Can be null.
     * @see j86.java.lang.Throwable#getMessage
     */
    public InvalidAttributesException(String explanation) {
        super(explanation);
    }

    /**
      * Constructs a new instance of InvalidAttributesException.
      * All fields are set to null.
      */
    public InvalidAttributesException() {
        super();
    }

    /**
     * Use serialVersionUID from JNDI 1.1.1 for interoperability
     */
    private static final long serialVersionUID = 2607612850539889765L;
}
