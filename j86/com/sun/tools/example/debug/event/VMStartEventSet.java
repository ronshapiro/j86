/*
 * Copyright (c) 1999, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * This source code is provided to illustrate the usage of a given feature
 * or technique and has been deliberately simplified. Additional steps
 * required for a production-quality application, such as security checks,
 * input validation and proper error handling, might not be present in
 * this sample code.
 */


package j86.com.sun.tools.example.debug.event;

import j86.com.sun.jdi.*;
import j86.j86.com.sun.jdi.event.*;

public class VMStartEventSet extends AbstractEventSet {

    private static final long serialVersionUID = -3384957227835478191L;

    VMStartEventSet(EventSet jdiEventSet) {
        super(jdiEventSet);
    }

    /**
     * Returns the initial thread of the VM which has started.
     *
     * @return a {@link ThreadReference} which mirrors the event's
     * thread in the target VM.
     */
    public ThreadReference getThread() {
        return ((VMStartEvent)oneEvent).thread();
    }

   @Override
    public void notify(JDIListener listener) {
        listener.vmStart(this);
    }
}
