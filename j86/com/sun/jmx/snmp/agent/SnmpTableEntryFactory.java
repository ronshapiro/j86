/*
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
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

package j86.com.sun.jmx.snmp.agent;

import j86.com.sun.jmx.snmp.SnmpStatusException;
import j86.com.sun.jmx.snmp.SnmpOid;
import j86.com.sun.jmx.snmp.agent.SnmpMibTable;
import j86.com.sun.jmx.snmp.agent.SnmpMibSubRequest;

/**
 * This interface is implemented by mibgen generated table objects
 * inheriting from {@link j86.com.sun.jmx.snmp.agent.SnmpTableSupport}.
 * <p>
 * It is used internally by the metadata whenever a remote SNMP manager
 * requests the creation of a new entry through an SNMP SET.
 * </p>
 * <p>
 * At creation, the mibgen generated table object retrieves its
 * corresponding metadata from the MIB and registers with
 * this metadata as a SnmpTableEntryFactory.
 * </p>
 *
 * <p><b>This API is a Sun Microsystems internal API  and is subject
 * to change without notice.</b></p>
 **/

public interface SnmpTableEntryFactory extends SnmpTableCallbackHandler {

    /**
     * This method is called by the SNMP runtime whenever a new entry
     * creation is requested by a remote manager.
     *
     * The factory is responsible for instantiating the appropriate MBean
     * and for registering it with the appropriate metadata object.
     *
     * Usually this method will:
     * <ul>
     * <li>Check whether the creation can be accepted
     * <li>Instantiate a new entry
     * <li>Possibly register this entry with the MBeanServer, if needed.
     * <li>Call <code>addEntry()</code> on the given <code>meta</code> object.
     * </ul>
     * This method is usually generated by <code>mibgen</code> on table
     * objects (inheriting from
     * {@link j86.com.sun.jmx.snmp.agent.SnmpTableSupport}). <br>
     *
     * <p><b><i>
     * This method is called internally by the SNMP runtime whenever a
     * new entry creation is requested by a remote SNMP manager.
     * You should never need to call this method directlty.
     * </i></b></p>
     *
     * @param request The SNMP subrequest containing the sublist of varbinds
     *                for the new entry.
     * @param rowOid  The OID indexing the conceptual row (entry) for which
     *                the creation was requested.
     * @param depth   The depth reached in the OID tree (the position at
     *                which the columnar object ids start in the OIDs
     *                included in the varbind).
     * @param meta    The metadata object impacted by the subrequest
     *
     * @exception SnmpStatusException The new entry cannot be created.
     *
     **/
    public void createNewEntry(SnmpMibSubRequest request, SnmpOid rowOid,
                               int depth, SnmpMibTable meta)
        throws SnmpStatusException;
}
