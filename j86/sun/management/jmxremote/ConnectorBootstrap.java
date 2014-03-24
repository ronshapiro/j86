/*
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
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

package j86.sun.management.jmxremote;

import j86.java.io.BufferedInputStream;
import j86.java.io.File;
import j86.java.io.FileInputStream;
import j86.java.io.IOException;
import j86.java.io.InputStream;
import j86.java.lang.management.ManagementFactory;
import j86.java.net.InetAddress;
import j86.java.net.MalformedURLException;
import j86.java.net.UnknownHostException;
import j86.java.rmi.NoSuchObjectException;
import j86.java.rmi.Remote;
import j86.java.rmi.RemoteException;
import j86.java.rmi.registry.Registry;
import j86.java.rmi.server.RMIClientSocketFactory;
import j86.java.rmi.server.RMIServerSocketFactory;
import j86.java.rmi.server.RemoteObject;
import j86.java.rmi.server.UnicastRemoteObject;
import j86.java.security.KeyStore;
import j86.java.security.Principal;
import j86.java.util.HashMap;
import j86.java.util.HashSet;
import j86.java.util.Iterator;
import j86.java.util.Map;
import j86.java.util.Properties;
import j86.java.util.Set;
import j86.java.util.StringTokenizer;

import j86.javax.management.MBeanServer;
import j86.javax.management.remote.JMXAuthenticator;
import j86.javax.management.remote.JMXConnectorServer;
import j86.javax.management.remote.JMXConnectorServerFactory;
import j86.javax.management.remote.JMXServiceURL;
import j86.javax.management.remote.rmi.RMIConnectorServer;
import j86.javax.net.ssl.KeyManagerFactory;
import j86.javax.net.ssl.SSLContext;
import j86.javax.net.ssl.TrustManagerFactory;
import j86.javax.rmi.ssl.SslRMIClientSocketFactory;
import j86.javax.rmi.ssl.SslRMIServerSocketFactory;
import j86.javax.security.auth.Subject;

import j86.com.sun.jmx.remote.internal.RMIExporter;
import j86.com.sun.jmx.remote.security.JMXPluggableAuthenticator;
import j86.com.sun.jmx.remote.util.ClassLogger;

import j86.sun.management.Agent;
import j86.sun.management.AgentConfigurationError;
import static j86.sun.management.AgentConfigurationError.*;
import j86.sun.management.ConnectorAddressLink;
import j86.sun.management.FileSystem;
import j86.sun.rmi.server.UnicastRef;
import j86.sun.rmi.server.UnicastServerRef;
import j86.sun.rmi.server.UnicastServerRef2;

/**
 * This class initializes and starts the RMIConnectorServer for JSR 163
 * JMX Monitoring.
 **/
public final class ConnectorBootstrap {

    /**
     * Default values for JMX configuration properties.
     **/
    public static interface DefaultValues {

        public static final String PORT = "0";
        public static final String CONFIG_FILE_NAME = "management.properties";
        public static final String USE_SSL = "true";
        public static final String USE_LOCAL_ONLY = "true";
        public static final String USE_REGISTRY_SSL = "false";
        public static final String USE_AUTHENTICATION = "true";
        public static final String PASSWORD_FILE_NAME = "jmxremote.password";
        public static final String ACCESS_FILE_NAME = "jmxremote.access";
        public static final String SSL_NEED_CLIENT_AUTH = "false";
    }

    /**
     * Names of JMX configuration properties.
     **/
    public static interface PropertyNames {

        public static final String PORT =
                "com.j86.sun.management.jmxremote.port";
        public static final String RMI_PORT =
                "com.j86.sun.management.jmxremote.rmi.port";
        public static final String CONFIG_FILE_NAME =
                "com.j86.sun.management.config.file";
        public static final String USE_LOCAL_ONLY =
                "com.j86.sun.management.jmxremote.local.only";
        public static final String USE_SSL =
                "com.j86.sun.management.jmxremote.ssl";
        public static final String USE_REGISTRY_SSL =
                "com.j86.sun.management.jmxremote.registry.ssl";
        public static final String USE_AUTHENTICATION =
                "com.j86.sun.management.jmxremote.authenticate";
        public static final String PASSWORD_FILE_NAME =
                "com.j86.sun.management.jmxremote.password.file";
        public static final String ACCESS_FILE_NAME =
                "com.j86.sun.management.jmxremote.access.file";
        public static final String LOGIN_CONFIG_NAME =
                "com.j86.sun.management.jmxremote.login.config";
        public static final String SSL_ENABLED_CIPHER_SUITES =
                "com.j86.sun.management.jmxremote.ssl.enabled.cipher.suites";
        public static final String SSL_ENABLED_PROTOCOLS =
                "com.j86.sun.management.jmxremote.ssl.enabled.protocols";
        public static final String SSL_NEED_CLIENT_AUTH =
                "com.j86.sun.management.jmxremote.ssl.need.client.auth";
        public static final String SSL_CONFIG_FILE_NAME =
                "com.j86.sun.management.jmxremote.ssl.config.file";
    }

    /**
     * JMXConnectorServer associated data.
     */
    private static class JMXConnectorServerData {

        public JMXConnectorServerData(
                JMXConnectorServer jmxConnectorServer,
                JMXServiceURL jmxRemoteURL) {
            this.jmxConnectorServer = jmxConnectorServer;
            this.jmxRemoteURL = jmxRemoteURL;
        }
        JMXConnectorServer jmxConnectorServer;
        JMXServiceURL jmxRemoteURL;
    }

    /**
     * <p>Prevents our RMI server objects from keeping the JVM alive.</p>
     *
     * <p>We use a private interface in Sun's JMX Remote API implementation
     * that allows us to specify how to export RMI objects.  We do so using
     * UnicastServerRef, a class in Sun's RMI implementation.  This is all
     * non-portable, of course, so this is only valid because we are inside
     * Sun's JRE.</p>
     *
     * <p>Objects are exported using {@link
     * UnicastServerRef#exportObject(Remote, Object, boolean)}.  The
     * boolean parameter is called <code>permanent</code> and means
     * both that the object is not eligible for Distributed Garbage
     * Collection, and that its continued existence will not prevent
     * the JVM from exiting.  It is the latter semantics we want (we
     * already have the former because of the way the JMX Remote API
     * works).  Hence the somewhat misleading name of this class.</p>
     */
    private static class PermanentExporter implements RMIExporter {

        public Remote exportObject(Remote obj,
                int port,
                RMIClientSocketFactory csf,
                RMIServerSocketFactory ssf)
                throws RemoteException {

            synchronized (this) {
                if (firstExported == null) {
                    firstExported = obj;
                }
            }

            final UnicastServerRef ref;
            if (csf == null && ssf == null) {
                ref = new UnicastServerRef(port);
            } else {
                ref = new UnicastServerRef2(port, csf, ssf);
            }
            return ref.exportObject(obj, null, true);
        }

        // Nothing special to be done for this case
        public boolean unexportObject(Remote obj, boolean force)
                throws NoSuchObjectException {
            return UnicastRemoteObject.unexportObject(obj, force);
        }
        Remote firstExported;
    }

    /**
     * This JMXAuthenticator wraps the JMXPluggableAuthenticator and verifies
     * that at least one of the principal names contained in the authenticated
     * Subject is present in the access file.
     */
    private static class AccessFileCheckerAuthenticator
            implements JMXAuthenticator {

        public AccessFileCheckerAuthenticator(Map<String, Object> env) throws IOException {
            environment = env;
            accessFile = (String) env.get("jmx.remote.x.access.file");
            properties = propertiesFromFile(accessFile);
        }

        public Subject authenticate(Object credentials) {
            final JMXAuthenticator authenticator =
                    new JMXPluggableAuthenticator(environment);
            final Subject subject = authenticator.authenticate(credentials);
            checkAccessFileEntries(subject);
            return subject;
        }

        private void checkAccessFileEntries(Subject subject) {
            if (subject == null) {
                throw new SecurityException(
                        "Access denied! No matching entries found in " +
                        "the access file [" + accessFile + "] as the " +
                        "authenticated Subject is null");
            }
            final Set<Principal> principals = subject.getPrincipals();
            for (Principal p1: principals) {
                if (properties.containsKey(p1.getName())) {
                    return;
                }
            }

            final Set<String> principalsStr = new HashSet<>();
            for (Principal p2: principals) {
                principalsStr.add(p2.getName());
            }
            throw new SecurityException(
                    "Access denied! No entries found in the access file [" +
                    accessFile + "] for any of the authenticated identities " +
                    principalsStr);
        }

        private static Properties propertiesFromFile(String fname)
                throws IOException {
            Properties p = new Properties();
            if (fname == null) {
                return p;
            }
            try (FileInputStream fin = new FileInputStream(fname)) {
                p.load(fin);
            }
            return p;
        }
        private final Map<String, Object> environment;
        private final Properties properties;
        private final String accessFile;
    }

    // The variable below is here to support stop functionality
    // It would be overriten if you call startRemoteCommectionServer second
    // time. It's OK for now as logic in Agent.java forbids mutiple agents
    private static Registry registry = null;

    public static void unexportRegistry() {
        // Remove the entry from registry
        try {
            if (registry != null) {
                UnicastRemoteObject.unexportObject(registry, true);
                registry = null;
            }
        } catch(NoSuchObjectException ex) {
            // This exception can appears only if we attempt
            // to unexportRegistry second time. So it's safe
            // to ignore it without additional messages.
        }
    }

     /**
      * Initializes and starts the JMX Connector Server.
      * If the com.j86.sun.management.jmxremote.port property is not defined,
      * simply return. Otherwise, attempts to load the config file, and
      * then calls {@link #startRemoteConnectorServer
      *                            (j86.java.lang.String, j86.java.util.Properties)}.
      *
      * This method is used by some jtreg tests.
      **/
      public static synchronized JMXConnectorServer initialize() {

         // Load a new management properties
         final Properties props = Agent.loadManagementProperties();
         if (props == null) {
              return null;
         }

         final String portStr = props.getProperty(PropertyNames.PORT);
         return startRemoteConnectorServer(portStr, props);
     }

    /**
     * This method is used by some jtreg tests.
     *
     * @see #startRemoteConnectorServer
     *             (String portStr, Properties props)
     */
    public static synchronized JMXConnectorServer initialize(String portStr, Properties props)  {
         return startRemoteConnectorServer(portStr, props);
    }

    /**
     * Initializes and starts a JMX Connector Server for remote
     * monitoring and management.
     **/
    public static synchronized JMXConnectorServer startRemoteConnectorServer(String portStr, Properties props) {

        // Get port number
        final int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException x) {
            throw new AgentConfigurationError(INVALID_JMXREMOTE_PORT, x, portStr);
        }
        if (port < 0) {
            throw new AgentConfigurationError(INVALID_JMXREMOTE_PORT, portStr);
        }

        // User can specify a port to be used to export rmi object,
        // in order to simplify firewall rules
        // if port is not specified random one will be allocated.
        int rmiPort = 0;
        String rmiPortStr = props.getProperty(PropertyNames.RMI_PORT);
        try {
            if (rmiPortStr != null) {
               rmiPort = Integer.parseInt(rmiPortStr);
            }
        } catch (NumberFormatException x) {
            throw new AgentConfigurationError(INVALID_JMXREMOTE_RMI_PORT, x, rmiPortStr);
        }
        if (rmiPort < 0) {
            throw new AgentConfigurationError(INVALID_JMXREMOTE_RMI_PORT, rmiPortStr);
        }

        // Do we use authentication?
        final String useAuthenticationStr =
                props.getProperty(PropertyNames.USE_AUTHENTICATION,
                DefaultValues.USE_AUTHENTICATION);
        final boolean useAuthentication =
                Boolean.valueOf(useAuthenticationStr).booleanValue();

        // Do we use SSL?
        final String useSslStr =
                props.getProperty(PropertyNames.USE_SSL,
                DefaultValues.USE_SSL);
        final boolean useSsl =
                Boolean.valueOf(useSslStr).booleanValue();

        // Do we use RMI Registry SSL?
        final String useRegistrySslStr =
                props.getProperty(PropertyNames.USE_REGISTRY_SSL,
                DefaultValues.USE_REGISTRY_SSL);
        final boolean useRegistrySsl =
                Boolean.valueOf(useRegistrySslStr).booleanValue();

        final String enabledCipherSuites =
                props.getProperty(PropertyNames.SSL_ENABLED_CIPHER_SUITES);
        String enabledCipherSuitesList[] = null;
        if (enabledCipherSuites != null) {
            StringTokenizer st = new StringTokenizer(enabledCipherSuites, ",");
            int tokens = st.countTokens();
            enabledCipherSuitesList = new String[tokens];
            for (int i = 0; i < tokens; i++) {
                enabledCipherSuitesList[i] = st.nextToken();
            }
        }

        final String enabledProtocols =
                props.getProperty(PropertyNames.SSL_ENABLED_PROTOCOLS);
        String enabledProtocolsList[] = null;
        if (enabledProtocols != null) {
            StringTokenizer st = new StringTokenizer(enabledProtocols, ",");
            int tokens = st.countTokens();
            enabledProtocolsList = new String[tokens];
            for (int i = 0; i < tokens; i++) {
                enabledProtocolsList[i] = st.nextToken();
            }
        }

        final String sslNeedClientAuthStr =
                props.getProperty(PropertyNames.SSL_NEED_CLIENT_AUTH,
                DefaultValues.SSL_NEED_CLIENT_AUTH);
        final boolean sslNeedClientAuth =
                Boolean.valueOf(sslNeedClientAuthStr).booleanValue();

        // Read SSL config file name
        final String sslConfigFileName =
                props.getProperty(PropertyNames.SSL_CONFIG_FILE_NAME);

        String loginConfigName = null;
        String passwordFileName = null;
        String accessFileName = null;

        // Initialize settings when authentication is active
        if (useAuthentication) {

            // Get non-default login configuration
            loginConfigName =
                    props.getProperty(PropertyNames.LOGIN_CONFIG_NAME);

            if (loginConfigName == null) {
                // Get password file
                passwordFileName =
                        props.getProperty(PropertyNames.PASSWORD_FILE_NAME,
                        getDefaultFileName(DefaultValues.PASSWORD_FILE_NAME));
                checkPasswordFile(passwordFileName);
            }

            // Get access file
            accessFileName = props.getProperty(PropertyNames.ACCESS_FILE_NAME,
                    getDefaultFileName(DefaultValues.ACCESS_FILE_NAME));
            checkAccessFile(accessFileName);
        }

        if (log.debugOn()) {
            log.debug("startRemoteConnectorServer",
                    Agent.getText("jmxremote.ConnectorBootstrap.starting") +
                    "\n\t" + PropertyNames.PORT + "=" + port +
                    "\n\t" + PropertyNames.RMI_PORT + "=" + rmiPort +
                    "\n\t" + PropertyNames.USE_SSL + "=" + useSsl +
                    "\n\t" + PropertyNames.USE_REGISTRY_SSL + "=" + useRegistrySsl +
                    "\n\t" + PropertyNames.SSL_CONFIG_FILE_NAME + "=" + sslConfigFileName +
                    "\n\t" + PropertyNames.SSL_ENABLED_CIPHER_SUITES + "=" +
                    enabledCipherSuites +
                    "\n\t" + PropertyNames.SSL_ENABLED_PROTOCOLS + "=" +
                    enabledProtocols +
                    "\n\t" + PropertyNames.SSL_NEED_CLIENT_AUTH + "=" +
                    sslNeedClientAuth +
                    "\n\t" + PropertyNames.USE_AUTHENTICATION + "=" +
                    useAuthentication +
                    (useAuthentication ? (loginConfigName == null ? ("\n\t" + PropertyNames.PASSWORD_FILE_NAME + "=" +
                    passwordFileName) : ("\n\t" + PropertyNames.LOGIN_CONFIG_NAME + "=" +
                    loginConfigName)) : "\n\t" +
                    Agent.getText("jmxremote.ConnectorBootstrap.noAuthentication")) +
                    (useAuthentication ? ("\n\t" + PropertyNames.ACCESS_FILE_NAME + "=" +
                    accessFileName) : "") +
                    "");
        }

        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        JMXConnectorServer cs = null;
        JMXServiceURL url = null;
        try {
            final JMXConnectorServerData data = exportMBeanServer(
                    mbs, port, rmiPort, useSsl, useRegistrySsl,
                    sslConfigFileName, enabledCipherSuitesList,
                    enabledProtocolsList, sslNeedClientAuth,
                    useAuthentication, loginConfigName,
                    passwordFileName, accessFileName);
            cs = data.jmxConnectorServer;
            url = data.jmxRemoteURL;
            log.config("startRemoteConnectorServer",
                    Agent.getText("jmxremote.ConnectorBootstrap.ready",
                    url.toString()));
        } catch (Exception e) {
            throw new AgentConfigurationError(AGENT_EXCEPTION, e, e.toString());
        }
        try {
            // Export remote connector address and associated configuration
            // properties to the instrumentation buffer.
            Map<String, String> properties = new HashMap<>();
            properties.put("remoteAddress", url.toString());
            properties.put("authenticate", useAuthenticationStr);
            properties.put("ssl", useSslStr);
            properties.put("sslRegistry", useRegistrySslStr);
            properties.put("sslNeedClientAuth", sslNeedClientAuthStr);
            ConnectorAddressLink.exportRemote(properties);
        } catch (Exception e) {
            // Remote connector server started but unable to export remote
            // connector address and associated configuration properties to
            // the instrumentation buffer - non-fatal error.
            log.debug("startRemoteConnectorServer", e);
        }
        return cs;
    }

    /*
     * Creates and starts a RMI Connector Server for "local" monitoring
     * and management.
     */
    public static JMXConnectorServer startLocalConnectorServer() {
        // Ensure cryptographically strong random number generater used
        // to choose the object number - see j86.java.rmi.server.ObjID
        System.setProperty("j86.java.rmi.server.randomIDs", "true");

        // This RMI server should not keep the VM alive
        Map<String, Object> env = new HashMap<>();
        env.put(RMIExporter.EXPORTER_ATTRIBUTE, new PermanentExporter());

        // The local connector server need only be available via the
        // loopback connection.
        String localhost = "localhost";
        InetAddress lh = null;
        try {
            lh = InetAddress.getByName(localhost);
            localhost = lh.getHostAddress();
        } catch (UnknownHostException x) {
        }

        // localhost unknown or (somehow) didn't resolve to
        // a loopback address.
        if (lh == null || !lh.isLoopbackAddress()) {
            localhost = "127.0.0.1";
        }

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            JMXServiceURL url = new JMXServiceURL("rmi", localhost, 0);
            // Do we accept connections from local interfaces only?
            Properties props = Agent.getManagementProperties();
            if (props ==  null) {
                props = new Properties();
            }
            String useLocalOnlyStr = props.getProperty(
                    PropertyNames.USE_LOCAL_ONLY, DefaultValues.USE_LOCAL_ONLY);
            boolean useLocalOnly = Boolean.valueOf(useLocalOnlyStr).booleanValue();
            if (useLocalOnly) {
                env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE,
                        new LocalRMIServerSocketFactory());
            }
            JMXConnectorServer server =
                    JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
            server.start();
            return server;
        } catch (Exception e) {
            throw new AgentConfigurationError(AGENT_EXCEPTION, e, e.toString());
        }
    }

    private static void checkPasswordFile(String passwordFileName) {
        if (passwordFileName == null || passwordFileName.length() == 0) {
            throw new AgentConfigurationError(PASSWORD_FILE_NOT_SET);
        }
        File file = new File(passwordFileName);
        if (!file.exists()) {
            throw new AgentConfigurationError(PASSWORD_FILE_NOT_FOUND, passwordFileName);
        }

        if (!file.canRead()) {
            throw new AgentConfigurationError(PASSWORD_FILE_NOT_READABLE, passwordFileName);
        }

        FileSystem fs = FileSystem.open();
        try {
            if (fs.supportsFileSecurity(file)) {
                if (!fs.isAccessUserOnly(file)) {
                    final String msg = Agent.getText("jmxremote.ConnectorBootstrap.password.readonly",
                            passwordFileName);
                    log.config("startRemoteConnectorServer", msg);
                    throw new AgentConfigurationError(PASSWORD_FILE_ACCESS_NOT_RESTRICTED,
                            passwordFileName);
                }
            }
        } catch (IOException e) {
            throw new AgentConfigurationError(PASSWORD_FILE_READ_FAILED,
                    e, passwordFileName);
        }
    }

    private static void checkAccessFile(String accessFileName) {
        if (accessFileName == null || accessFileName.length() == 0) {
            throw new AgentConfigurationError(ACCESS_FILE_NOT_SET);
        }
        File file = new File(accessFileName);
        if (!file.exists()) {
            throw new AgentConfigurationError(ACCESS_FILE_NOT_FOUND, accessFileName);
        }

        if (!file.canRead()) {
            throw new AgentConfigurationError(ACCESS_FILE_NOT_READABLE, accessFileName);
        }
    }

    private static void checkRestrictedFile(String restrictedFileName) {
        if (restrictedFileName == null || restrictedFileName.length() == 0) {
            throw new AgentConfigurationError(FILE_NOT_SET);
        }
        File file = new File(restrictedFileName);
        if (!file.exists()) {
            throw new AgentConfigurationError(FILE_NOT_FOUND, restrictedFileName);
        }
        if (!file.canRead()) {
            throw new AgentConfigurationError(FILE_NOT_READABLE, restrictedFileName);
        }
        FileSystem fs = FileSystem.open();
        try {
            if (fs.supportsFileSecurity(file)) {
                if (!fs.isAccessUserOnly(file)) {
                    final String msg = Agent.getText(
                            "jmxremote.ConnectorBootstrap.file.readonly",
                            restrictedFileName);
                    log.config("startRemoteConnectorServer", msg);
                    throw new AgentConfigurationError(
                            FILE_ACCESS_NOT_RESTRICTED, restrictedFileName);
                }
            }
        } catch (IOException e) {
            throw new AgentConfigurationError(
                    FILE_READ_FAILED, e, restrictedFileName);
        }
    }

    /**
     * Compute the full path name for a default file.
     * @param basename basename (with extension) of the default file.
     * @return ${JRE}/lib/management/${basename}
     **/
    private static String getDefaultFileName(String basename) {
        final String fileSeparator = File.separator;
        return System.getProperty("java.home") + fileSeparator + "lib" +
                fileSeparator + "management" + fileSeparator +
                basename;
    }

    private static SslRMIServerSocketFactory createSslRMIServerSocketFactory(
            String sslConfigFileName,
            String[] enabledCipherSuites,
            String[] enabledProtocols,
            boolean sslNeedClientAuth) {
        if (sslConfigFileName == null) {
            return new SslRMIServerSocketFactory(
                    enabledCipherSuites,
                    enabledProtocols,
                    sslNeedClientAuth);
        } else {
            checkRestrictedFile(sslConfigFileName);
            try {
                // Load the SSL keystore properties from the config file
                Properties p = new Properties();
                try (InputStream in = new FileInputStream(sslConfigFileName)) {
                    BufferedInputStream bin = new BufferedInputStream(in);
                    p.load(bin);
                }
                String keyStore =
                        p.getProperty("j86.javax.net.ssl.keyStore");
                String keyStorePassword =
                        p.getProperty("j86.javax.net.ssl.keyStorePassword", "");
                String trustStore =
                        p.getProperty("j86.javax.net.ssl.trustStore");
                String trustStorePassword =
                        p.getProperty("j86.javax.net.ssl.trustStorePassword", "");

                char[] keyStorePasswd = null;
                if (keyStorePassword.length() != 0) {
                    keyStorePasswd = keyStorePassword.toCharArray();
                }

                char[] trustStorePasswd = null;
                if (trustStorePassword.length() != 0) {
                    trustStorePasswd = trustStorePassword.toCharArray();
                }

                KeyStore ks = null;
                if (keyStore != null) {
                    ks = KeyStore.getInstance(KeyStore.getDefaultType());
                    try (FileInputStream ksfis = new FileInputStream(keyStore)) {
                        ks.load(ksfis, keyStorePasswd);
                    }
                }
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                        KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ks, keyStorePasswd);

                KeyStore ts = null;
                if (trustStore != null) {
                    ts = KeyStore.getInstance(KeyStore.getDefaultType());
                    try (FileInputStream tsfis = new FileInputStream(trustStore)) {
                        ts.load(tsfis, trustStorePasswd);
                    }
                }
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ts);

                SSLContext ctx = SSLContext.getInstance("SSL");
                ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

                return new SslRMIServerSocketFactory(
                        ctx,
                        enabledCipherSuites,
                        enabledProtocols,
                        sslNeedClientAuth);
            } catch (Exception e) {
                throw new AgentConfigurationError(AGENT_EXCEPTION, e, e.toString());
            }
        }
    }

    private static JMXConnectorServerData exportMBeanServer(
            MBeanServer mbs,
            int port,
            int rmiPort,
            boolean useSsl,
            boolean useRegistrySsl,
            String sslConfigFileName,
            String[] enabledCipherSuites,
            String[] enabledProtocols,
            boolean sslNeedClientAuth,
            boolean useAuthentication,
            String loginConfigName,
            String passwordFileName,
            String accessFileName)
            throws IOException, MalformedURLException {

        /* Make sure we use non-guessable RMI object IDs.  Otherwise
         * attackers could hijack open connections by guessing their
         * IDs.  */
        System.setProperty("j86.java.rmi.server.randomIDs", "true");

        JMXServiceURL url = new JMXServiceURL("rmi", null, rmiPort);

        Map<String, Object> env = new HashMap<>();

        PermanentExporter exporter = new PermanentExporter();

        env.put(RMIExporter.EXPORTER_ATTRIBUTE, exporter);

        if (useAuthentication) {
            if (loginConfigName != null) {
                env.put("jmx.remote.x.login.config", loginConfigName);
            }
            if (passwordFileName != null) {
                env.put("jmx.remote.x.password.file", passwordFileName);
            }

            env.put("jmx.remote.x.access.file", accessFileName);

            if (env.get("jmx.remote.x.password.file") != null ||
                    env.get("jmx.remote.x.login.config") != null) {
                env.put(JMXConnectorServer.AUTHENTICATOR,
                        new AccessFileCheckerAuthenticator(env));
            }
        }

        RMIClientSocketFactory csf = null;
        RMIServerSocketFactory ssf = null;

        if (useSsl || useRegistrySsl) {
            csf = new SslRMIClientSocketFactory();
            ssf = createSslRMIServerSocketFactory(
                    sslConfigFileName, enabledCipherSuites,
                    enabledProtocols, sslNeedClientAuth);
        }

        if (useSsl) {
            env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE,
                    csf);
            env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE,
                    ssf);
        }

        JMXConnectorServer connServer = null;
        try {
            connServer =
                    JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
            connServer.start();
        } catch (IOException e) {
            if (connServer == null) {
                throw new AgentConfigurationError(CONNECTOR_SERVER_IO_ERROR,
                        e, url.toString());
            } else {
                throw new AgentConfigurationError(CONNECTOR_SERVER_IO_ERROR,
                        e, connServer.getAddress().toString());
            }
        }

        if (useRegistrySsl) {
            registry =
                    new SingleEntryRegistry(port, csf, ssf,
                    "jmxrmi", exporter.firstExported);
        } else {
            registry =
                    new SingleEntryRegistry(port,
                    "jmxrmi", exporter.firstExported);
        }


        int registryPort =
            ((UnicastRef) ((RemoteObject) registry).getRef()).getLiveRef().getPort();
        String jmxUrlStr =  String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi",
                                           url.getHost(), registryPort);
        JMXServiceURL remoteURL = new JMXServiceURL(jmxUrlStr);

        /* Our exporter remembers the first object it was asked to
        export, which will be an RMIServerImpl appropriate for
        publication in our special registry.  We could
        alternatively have constructed the RMIServerImpl explicitly
        and then constructed an RMIConnectorServer passing it as a
        parameter, but that's quite a bit more verbose and pulls in
        lots of knowledge of the RMI connector.  */

        return new JMXConnectorServerData(connServer, remoteURL);
    }

    /**
     * This class cannot be instantiated.
     **/
    private ConnectorBootstrap() {
    }

    private static final ClassLogger log =
        new ClassLogger(ConnectorBootstrap.class.getPackage().getName(),
                        "ConnectorBootstrap");
}
