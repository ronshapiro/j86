/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
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

package j86.java.lang;

/**
 * An IllegalAccessException is thrown when an application tries
 * to reflectively create an instance (other than an array),
 * set or get a field, or invoke a method, but the currently
 * executing method does not have access to the definition of
 * the specified class, field, method or constructor.
 *
 * @author  unascribed
 * @see     Class#newInstance()
 * @see     j86.java.lang.reflect.Field#set(Object, Object)
 * @see     j86.java.lang.reflect.Field#setBoolean(Object, boolean)
 * @see     j86.java.lang.reflect.Field#setByte(Object, byte)
 * @see     j86.java.lang.reflect.Field#setShort(Object, short)
 * @see     j86.java.lang.reflect.Field#setChar(Object, char)
 * @see     j86.java.lang.reflect.Field#setInt(Object, int)
 * @see     j86.java.lang.reflect.Field#setLong(Object, long)
 * @see     j86.java.lang.reflect.Field#setFloat(Object, float)
 * @see     j86.java.lang.reflect.Field#setDouble(Object, double)
 * @see     j86.java.lang.reflect.Field#get(Object)
 * @see     j86.java.lang.reflect.Field#getBoolean(Object)
 * @see     j86.java.lang.reflect.Field#getByte(Object)
 * @see     j86.java.lang.reflect.Field#getShort(Object)
 * @see     j86.java.lang.reflect.Field#getChar(Object)
 * @see     j86.java.lang.reflect.Field#getInt(Object)
 * @see     j86.java.lang.reflect.Field#getLong(Object)
 * @see     j86.java.lang.reflect.Field#getFloat(Object)
 * @see     j86.java.lang.reflect.Field#getDouble(Object)
 * @see     j86.java.lang.reflect.Method#invoke(Object, Object[])
 * @see     j86.java.lang.reflect.Constructor#newInstance(Object[])
 * @since   JDK1.0
 */
public class IllegalAccessException extends ReflectiveOperationException {
    private static final long serialVersionUID = 6616958222490762034L;

    /**
     * Constructs an <code>IllegalAccessException</code> without a
     * detail message.
     */
    public IllegalAccessException() {
        super();
    }

    /**
     * Constructs an <code>IllegalAccessException</code> with a detail message.
     *
     * @param   s   the detail message.
     */
    public IllegalAccessException(String s) {
        super(s);
    }
}
