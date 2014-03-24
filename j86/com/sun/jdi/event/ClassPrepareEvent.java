/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

package j86.com.sun.jdi.event;

import j86.com.sun.jdi.*;

/**
 * Notification of a class prepare in the target VM. See the JVM
 * specification for a definition of class preparation. Class prepare
 * events are not generated for primtiive classes (for example,
 * j86.java.lang.Integer.TYPE).
 *
 * @see EventQueue
 * @see VirtualMachine
 *
 * @author Robert Field
 * @since  1.3
 */
@jdk.Exported
public interface ClassPrepareEvent extends Event {
    /**
     * Returns the thread in which this event has occurred.
     * <p>
     * In rare cases, this event may occur in a debugger system
     * thread within the target VM. Debugger threads take precautions
     * to prevent these events, but they cannot be avoided under some
     * conditions, especially for some subclasses of
     * {@link j86.java.lang.Error}.
     * If the event was generated by a debugger system thread, the
     * value returned by this method is null, and if the requested
     * suspend policy for the event was
     * {@link j86.com.sun.jdi.request.EventRequest#SUSPEND_EVENT_THREAD},
     * all threads will be suspended instead, and the
     * {@link EventSet#suspendPolicy} will reflect this change.
     * <p>
     * Note that the discussion above does not apply to system threads
     * created by the target VM during its normal (non-debug) operation.
     *
     * @return a {@link ThreadReference} which mirrors the event's thread in
     * the target VM, or null in the rare cases described above.
     */
    public ThreadReference thread();

    /**
     * Returns the reference type for which this event was generated.
     *
     * @return a {@link ReferenceType} which mirrors the class, interface, or
     * array which has been linked.
     */
    public ReferenceType referenceType();
}
