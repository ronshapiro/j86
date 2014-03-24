/*
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
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

package j86.sun.management;

import j86.sun.management.counter.Counter;

/**
 * Hotspot internal management interface for the runtime system.
 *
 * This management interface is internal and uncommitted
 * and subject to change without notice.
 */
public interface HotspotRuntimeMBean {

    /**
     * Returns the number of safepoints taken place since the Java
     * virtual machine started.
     *
     * @return the number of safepoints taken place since the Java
     * virtual machine started.
     */
    public long getSafepointCount();

    /**
     * Returns the accumulated time spent at safepoints in milliseconds.
     * This is the accumulated elapsed time that the application has
     * been stopped for safepoint operations.
     *
     * @return the accumulated time spent at safepoints in milliseconds.
     */
    public long getTotalSafepointTime();

    /**
     * Returns the accumulated time spent getting to safepoints in milliseconds.
     *
     * @return the accumulated time spent getting to safepoints in milliseconds.
     */
    public long getSafepointSyncTime();

    /**
     * Returns a list of internal counters maintained in the Java
     * virtual machine for the runtime system.
     *
     * @return a <tt>List</tt> of internal counters maintained in the VM
     * for the runtime system.
     */
    public j86.java.util.List<Counter> getInternalRuntimeCounters();
}
