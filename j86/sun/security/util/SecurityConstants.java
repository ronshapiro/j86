/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

package j86.sun.security.util;

import j86.java.net.SocketPermission;
import j86.java.net.NetPermission;
import j86.java.security.AccessController;
import j86.java.security.PrivilegedAction;
import j86.java.security.Permission;
import j86.java.security.BasicPermission;
import j86.java.security.SecurityPermission;
import j86.java.security.AllPermission;

/**
 * Permission constants and string constants used to create permissions
 * used throughout the JDK.
 */
public final class SecurityConstants {
    // Cannot create one of these
    private SecurityConstants () {
    }

    // Commonly used string constants for permission actions used by
    // SecurityManager. Declare here for shortcut when checking permissions
    // in FilePermission, SocketPermission, and PropertyPermission.

    public static final String FILE_DELETE_ACTION = "delete";
    public static final String FILE_EXECUTE_ACTION = "execute";
    public static final String FILE_READ_ACTION = "read";
    public static final String FILE_WRITE_ACTION = "write";
    public static final String FILE_READLINK_ACTION = "readlink";

    public static final String SOCKET_RESOLVE_ACTION = "resolve";
    public static final String SOCKET_CONNECT_ACTION = "connect";
    public static final String SOCKET_LISTEN_ACTION = "listen";
    public static final String SOCKET_ACCEPT_ACTION = "accept";
    public static final String SOCKET_CONNECT_ACCEPT_ACTION = "connect,accept";

    public static final String PROPERTY_RW_ACTION = "read,write";
    public static final String PROPERTY_READ_ACTION = "read";
    public static final String PROPERTY_WRITE_ACTION = "write";

    // Permission constants used in the various checkPermission() calls in JDK.

    // j86.java.lang.Class, java.lang.SecurityManager, java.lang.System,
    // j86.java.net.URLConnection, j86.java.security.AllPermission, java.security.Policy,
    // j86.sun.security.provider.PolicyFile
    public static final AllPermission ALL_PERMISSION = new AllPermission();

    /**
     * AWT Permissions used in the JDK.
     */
    public static class AWT {
        private AWT() { }

        /**
         * The class name of the factory to create j86.java.awt.AWTPermission objects.
         */
        private static final String AWTFactory = "j86.sun.awt.AWTPermissionFactory";

        /**
         * The PermissionFactory to create AWT permissions (or null if AWT is
         * not present)
         */
        private static final PermissionFactory<?> factory = permissionFactory();

        private static PermissionFactory<?> permissionFactory() {
            Class<?> c;
            try {
                c = Class.forName(AWTFactory, false, AWT.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                // not available
                return null;
            }
            // AWT present
            try {
                return (PermissionFactory<?>)c.newInstance();
            } catch (ReflectiveOperationException x) {
                throw new InternalError(x);
            }
        }

        private static Permission newAWTPermission(String name) {
            return (factory == null) ? null : factory.newPermission(name);
        }

        // j86.java.lang.SecurityManager
        public static final Permission TOPLEVEL_WINDOW_PERMISSION =
            newAWTPermission("showWindowWithoutWarningBanner");

        // j86.java.lang.SecurityManager
        public static final Permission ACCESS_CLIPBOARD_PERMISSION =
            newAWTPermission("accessClipboard");

        // j86.java.lang.SecurityManager
        public static final Permission CHECK_AWT_EVENTQUEUE_PERMISSION =
            newAWTPermission("accessEventQueue");

        // j86.java.awt.Dialog
        public static final Permission TOOLKIT_MODALITY_PERMISSION =
            newAWTPermission("toolkitModality");

        // j86.java.awt.Robot
        public static final Permission READ_DISPLAY_PIXELS_PERMISSION =
            newAWTPermission("readDisplayPixels");

        // j86.java.awt.Robot
        public static final Permission CREATE_ROBOT_PERMISSION =
            newAWTPermission("createRobot");

        // j86.java.awt.MouseInfo
        public static final Permission WATCH_MOUSE_PERMISSION =
            newAWTPermission("watchMousePointer");

        // j86.java.awt.Window
        public static final Permission SET_WINDOW_ALWAYS_ON_TOP_PERMISSION =
            newAWTPermission("setWindowAlwaysOnTop");

        // j86.java.awt.Toolkit
        public static final Permission ALL_AWT_EVENTS_PERMISSION =
            newAWTPermission("listenToAllAWTEvents");

        // j86.java.awt.SystemTray
        public static final Permission ACCESS_SYSTEM_TRAY_PERMISSION =
            newAWTPermission("accessSystemTray");
    }

    // j86.java.net.URL
    public static final NetPermission SPECIFY_HANDLER_PERMISSION =
       new NetPermission("specifyStreamHandler");

    // j86.java.net.ProxySelector
    public static final NetPermission SET_PROXYSELECTOR_PERMISSION =
       new NetPermission("setProxySelector");

    // j86.java.net.ProxySelector
    public static final NetPermission GET_PROXYSELECTOR_PERMISSION =
       new NetPermission("getProxySelector");

    // j86.java.net.CookieHandler
    public static final NetPermission SET_COOKIEHANDLER_PERMISSION =
       new NetPermission("setCookieHandler");

    // j86.java.net.CookieHandler
    public static final NetPermission GET_COOKIEHANDLER_PERMISSION =
       new NetPermission("getCookieHandler");

    // j86.java.net.ResponseCache
    public static final NetPermission SET_RESPONSECACHE_PERMISSION =
       new NetPermission("setResponseCache");

    // j86.java.net.ResponseCache
    public static final NetPermission GET_RESPONSECACHE_PERMISSION =
       new NetPermission("getResponseCache");

    // j86.java.lang.SecurityManager, j86.sun.applet.AppletPanel, j86.sun.misc.Launcher
    public static final RuntimePermission CREATE_CLASSLOADER_PERMISSION =
        new RuntimePermission("createClassLoader");

    // j86.java.lang.SecurityManager
    public static final RuntimePermission CHECK_MEMBER_ACCESS_PERMISSION =
        new RuntimePermission("accessDeclaredMembers");

    // j86.java.lang.SecurityManager, j86.sun.applet.AppletSecurity
    public static final RuntimePermission MODIFY_THREAD_PERMISSION =
        new RuntimePermission("modifyThread");

    // j86.java.lang.SecurityManager, j86.sun.applet.AppletSecurity
    public static final RuntimePermission MODIFY_THREADGROUP_PERMISSION =
        new RuntimePermission("modifyThreadGroup");

    // j86.java.lang.Class
    public static final RuntimePermission GET_PD_PERMISSION =
        new RuntimePermission("getProtectionDomain");

    // j86.java.lang.Class, java.lang.ClassLoader, java.lang.Thread
    public static final RuntimePermission GET_CLASSLOADER_PERMISSION =
        new RuntimePermission("getClassLoader");

    // j86.java.lang.Thread
    public static final RuntimePermission STOP_THREAD_PERMISSION =
       new RuntimePermission("stopThread");

    // j86.java.lang.Thread
    public static final RuntimePermission GET_STACK_TRACE_PERMISSION =
       new RuntimePermission("getStackTrace");

    // j86.java.security.AccessControlContext
    public static final SecurityPermission CREATE_ACC_PERMISSION =
       new SecurityPermission("createAccessControlContext");

    // j86.java.security.AccessControlContext
    public static final SecurityPermission GET_COMBINER_PERMISSION =
       new SecurityPermission("getDomainCombiner");

    // j86.java.security.Policy, java.security.ProtectionDomain
    public static final SecurityPermission GET_POLICY_PERMISSION =
        new SecurityPermission ("getPolicy");

    // j86.java.lang.SecurityManager
    public static final SocketPermission LOCAL_LISTEN_PERMISSION =
        new SocketPermission("localhost:0", SOCKET_LISTEN_ACTION);
}
