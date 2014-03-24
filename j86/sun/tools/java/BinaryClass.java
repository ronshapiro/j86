/*
 * Copyright (c) 1994, 2006, Oracle and/or its affiliates. All rights reserved.
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

package j86.sun.tools.java;

import j86.java.io.IOException;
import j86.java.io.DataInputStream;
import j86.java.io.OutputStream;
import j86.java.io.DataOutputStream;
import j86.java.io.ByteArrayInputStream;
import j86.java.util.Hashtable;
import j86.java.util.Vector;
import j86.java.util.Enumeration;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public final
class BinaryClass extends ClassDefinition implements Constants {
    BinaryConstantPool cpool;
    BinaryAttribute atts;
    Vector dependencies;
    private boolean haveLoadedNested = false;

    /**
     * Constructor
     */
    public BinaryClass(Object source, ClassDeclaration declaration, int modifiers,
                           ClassDeclaration superClass, ClassDeclaration interfaces[],
                           Vector dependencies) {
        super(source, 0, declaration, modifiers, null, null);
        this.dependencies = dependencies;
        this.superClass = superClass;
        this.interfaces = interfaces;
    }

    /**
     * Flags used by basicCheck() to avoid duplicate calls.
     * (Part of fix for 4105911)
     */
    private boolean basicCheckDone = false;
    private boolean basicChecking = false;

    /**
     * Ready a BinaryClass for further checking.  Note that, until recently,
     * BinaryClass relied on the default basicCheck() provided by
     * ClassDefinition.  The definition here has been added to ensure that
     * the information generated by collectInheritedMethods is available
     * for BinaryClasses.
     */
    protected void basicCheck(Environment env) throws ClassNotFound {
        if (tracing) env.dtEnter("BinaryClass.basicCheck: " + getName());

        // We need to guard against duplicate calls to basicCheck().  They
        // can lead to calling collectInheritedMethods() for this class
        // from within a previous call to collectInheritedMethods() for
        // this class.  That is not allowed.
        // (Part of fix for 4105911)
        if (basicChecking || basicCheckDone) {
            if (tracing) env.dtExit("BinaryClass.basicCheck: OK " + getName());
            return;
        }

        if (tracing) env.dtEvent("BinaryClass.basicCheck: CHECKING " + getName());
        basicChecking = true;

        super.basicCheck(env);

        // Collect inheritance information.
        if (doInheritanceChecks) {
            collectInheritedMethods(env);
        }

        basicCheckDone = true;
        basicChecking = false;
        if (tracing) env.dtExit("BinaryClass.basicCheck: " + getName());
    }

    /**
     * Load a binary class
     */
    public static BinaryClass load(Environment env, DataInputStream in) throws IOException {
        return load(env, in, ~(ATT_CODE|ATT_ALLCLASSES));
    }

    public static BinaryClass load(Environment env,
                                   DataInputStream in, int mask) throws IOException {
        // Read the header
        int magic = in.readInt();                    // JVM 4.1 ClassFile.magic
        if (magic != JAVA_MAGIC) {
            throw new ClassFormatError("wrong magic: " + magic + ", expected " + JAVA_MAGIC);
        }
        int minor_version = in.readUnsignedShort();  // JVM 4.1 ClassFile.minor_version
        int version = in.readUnsignedShort();        // JVM 4.1 ClassFile.major_version
        if (version < JAVA_MIN_SUPPORTED_VERSION) {
            throw new ClassFormatError(
                           j86.sun.tools.javac.Main.getText(
                               "javac.err.version.too.old",
                               String.valueOf(version)));
        } else if ((version > JAVA_MAX_SUPPORTED_VERSION)
                     || (version == JAVA_MAX_SUPPORTED_VERSION
                  && minor_version > JAVA_MAX_SUPPORTED_MINOR_VERSION)) {
            throw new ClassFormatError(
                           j86.sun.tools.javac.Main.getText(
                               "javac.err.version.too.recent",
                               version+"."+minor_version));
        }

        // Read the constant pool
        BinaryConstantPool cpool = new BinaryConstantPool(in);

        // The dependencies of this class
        Vector dependencies = cpool.getDependencies(env);

        // Read modifiers
        int classMod = in.readUnsignedShort() & ACCM_CLASS;  // JVM 4.1 ClassFile.access_flags

        // Read the class name - from JVM 4.1 ClassFile.this_class
        ClassDeclaration classDecl = cpool.getDeclaration(env, in.readUnsignedShort());

        // Read the super class name (may be null) - from JVM 4.1 ClassFile.super_class
        ClassDeclaration superClassDecl = cpool.getDeclaration(env, in.readUnsignedShort());

        // Read the interface names - from JVM 4.1 ClassFile.interfaces_count
        ClassDeclaration interfaces[] = new ClassDeclaration[in.readUnsignedShort()];
        for (int i = 0 ; i < interfaces.length ; i++) {
            // JVM 4.1 ClassFile.interfaces[]
            interfaces[i] = cpool.getDeclaration(env, in.readUnsignedShort());
        }

        // Allocate the class
        BinaryClass c = new BinaryClass(null, classDecl, classMod, superClassDecl,
                                        interfaces, dependencies);
        c.cpool = cpool;

        // Add any additional dependencies
        c.addDependency(superClassDecl);

        // Read the fields
        int nfields = in.readUnsignedShort();  // JVM 4.1 ClassFile.fields_count
        for (int i = 0 ; i < nfields ; i++) {
            // JVM 4.5 field_info.access_flags
            int fieldMod = in.readUnsignedShort() & ACCM_FIELD;
            // JVM 4.5 field_info.name_index
            Identifier fieldName = cpool.getIdentifier(in.readUnsignedShort());
            // JVM 4.5 field_info.descriptor_index
            Type fieldType = cpool.getType(in.readUnsignedShort());
            BinaryAttribute atts = BinaryAttribute.load(in, cpool, mask);
            c.addMember(new BinaryMember(c, fieldMod, fieldType, fieldName, atts));
        }

        // Read the methods
        int nmethods = in.readUnsignedShort();  // JVM 4.1 ClassFile.methods_count
        for (int i = 0 ; i < nmethods ; i++) {
            // JVM 4.6 method_info.access_flags
            int methMod = in.readUnsignedShort() & ACCM_METHOD;
            // JVM 4.6 method_info.name_index
            Identifier methName = cpool.getIdentifier(in.readUnsignedShort());
            // JVM 4.6 method_info.descriptor_index
            Type methType = cpool.getType(in.readUnsignedShort());
            BinaryAttribute atts = BinaryAttribute.load(in, cpool, mask);
            c.addMember(new BinaryMember(c, methMod, methType, methName, atts));
        }

        // Read the class attributes
        c.atts = BinaryAttribute.load(in, cpool, mask);

        // See if the SourceFile is known
        byte data[] = c.getAttribute(idSourceFile);
        if (data != null) {
            DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(data));
            // JVM 4.7.2 SourceFile_attribute.sourcefile_index
            c.source = cpool.getString(dataStream.readUnsignedShort());
        }

        // See if the Documentation is know
        data = c.getAttribute(idDocumentation);
        if (data != null) {
            c.documentation = new DataInputStream(new ByteArrayInputStream(data)).readUTF();
        }

        // Was it compiled as deprecated?
        if (c.getAttribute(idDeprecated) != null) {
            c.modifiers |= M_DEPRECATED;
        }

        // Was it synthesized by the compiler?
        if (c.getAttribute(idSynthetic) != null) {
            c.modifiers |= M_SYNTHETIC;
        }

        return c;
    }

    /**
     * Called when an environment ties a binary definition to a declaration.
     * At this point, auxiliary definitions may be loaded.
     */

    public void loadNested(Environment env) {
        loadNested(env, 0);
    }

    public void loadNested(Environment env, int flags) {
        // Sanity check.
        if (haveLoadedNested) {
            // Duplicate calls most likely should not occur, but they do
            // in javap.  Be tolerant of them for the time being.
            // throw new CompilerError("multiple loadNested");
            if (tracing) env.dtEvent("loadNested: DUPLICATE CALL SKIPPED");
            return;
        }
        haveLoadedNested = true;
        // Read class-nesting information.
        try {
            byte data[];
            data = getAttribute(idInnerClasses);
            if (data != null) {
                initInnerClasses(env, data, flags);
            }
        } catch (IOException ee) {
            // The inner classes attribute is not well-formed.
            // It may, for example, contain no data.  Report this.
            // We used to throw a CompilerError here (bug 4095108).
            env.error(0, "malformed.attribute", getClassDeclaration(),
                      idInnerClasses);
            if (tracing)
                env.dtEvent("loadNested: MALFORMED ATTRIBUTE (InnerClasses)");
        }
    }

    private void initInnerClasses(Environment env,
                                  byte data[],
                                  int flags) throws IOException {
        DataInputStream ds = new DataInputStream(new ByteArrayInputStream(data));
        int nrec = ds.readUnsignedShort();  // InnerClasses_attribute.number_of_classes
        for (int i = 0; i < nrec; i++) {
            // For each inner class name transformation, we have a record
            // with the following fields:
            //
            //    u2 inner_class_info_index;   // CONSTANT_Class_info index
            //    u2 outer_class_info_index;   // CONSTANT_Class_info index
            //    u2 inner_name_index;         // CONSTANT_Utf8_info index
            //    u2 inner_class_access_flags; // access_flags bitmask
            //
            // The spec states that outer_class_info_index is 0 iff
            // the inner class is not a member of its enclosing class (i.e.
            // it is a local or anonymous class).  The spec also states
            // that if a class is anonymous then inner_name_index should
            // be 0.
            //
            // Prior to jdk1.2, javac did not implement the spec.  Instead
            // it <em>always</em> set outer_class_info_index to the
            // enclosing outer class and if the class was anonymous,
            // it set inner_name_index to be the index of a CONSTANT_Utf8
            // entry containing the null string "" (idNull).  This code is
            // designed to handle either kind of class file.
            //
            // See also the compileClass() method in SourceClass.java.

            // Read in the inner_class_info
            // InnerClasses_attribute.classes.inner_class_info_index
            int inner_index = ds.readUnsignedShort();
            // could check for zero.
            ClassDeclaration inner = cpool.getDeclaration(env, inner_index);

            // Read in the outer_class_info.  Note that the index will be
            // zero if the class is "not a member".
            ClassDeclaration outer = null;
            // InnerClasses_attribute.classes.outer_class_info_index
            int outer_index = ds.readUnsignedShort();
            if (outer_index != 0) {
                outer = cpool.getDeclaration(env, outer_index);
            }

            // Read in the inner_name_index.  This may be zero.  An anonymous
            // class will either have an inner_nm_index of zero (as the spec
            // dictates) or it will have an inner_nm of idNull (for classes
            // generated by pre-1.2 compilers).  Handle both.
            Identifier inner_nm = idNull;
            // InnerClasses_attribute.classes.inner_name_index
            int inner_nm_index = ds.readUnsignedShort();
            if (inner_nm_index != 0) {
                inner_nm = Identifier.lookup(cpool.getString(inner_nm_index));
            }

            // Read in the modifiers for the inner class.
            // InnerClasses_attribute.classes.inner_name_index
            int mods = ds.readUnsignedShort();

            // Is the class accessible?
            // The old code checked for
            //
            //    (!inner_nm.equals(idNull) && (mods & M_PRIVATE) == 0)
            //
            // which we will preserve to keep it working for class files
            // generated by 1.1 compilers.  In addition we check for
            //
            //    (outer != null)
            //
            // as an additional check that only makes sense with 1.2
            // generated files.  Note that it is entirely possible that
            // the M_PRIVATE bit is always enough.  We are being
            // conservative here.
            //
            // The ATT_ALLCLASSES flag causes the M_PRIVATE modifier
            // to be ignored, and is used by tools such as 'javap' that
            // wish to examine all classes regardless of the normal access
            // controls that apply during compilation.  Note that anonymous
            // and local classes are still not considered accessible, though
            // named local classes in jdk1.1 may slip through.  Note that
            // this accessibility test is an optimization, and it is safe to
            // err on the side of greater accessibility.
            boolean accessible =
                (outer != null) &&
                (!inner_nm.equals(idNull)) &&
                ((mods & M_PRIVATE) == 0 ||
                 (flags & ATT_ALLCLASSES) != 0);

            // The reader should note that there has been a significant change
            // in the way that the InnerClasses attribute is being handled.
            // In particular, previously the compiler called initInner() for
            // <em>every</em> inner class.  Now the compiler does not call
            // initInner() if the inner class is inaccessible.  This means
            // that inaccessible inner classes don't have any of the processing
            // from initInner() done for them: fixing the access flags,
            // setting outerClass, setting outerMember in their outerClass,
            // etc.  We believe this is fine: if the class is inaccessible
            // and binary, then everyone who needs to see its internals
            // has already been compiled.  Hopefully.

            if (accessible) {
                Identifier nm =
                    Identifier.lookupInner(outer.getName(), inner_nm);

                // Tell the type module about the nesting relation:
                Type.tClass(nm);

                if (inner.equals(getClassDeclaration())) {
                    // The inner class in the record is this class.
                    try {
                        ClassDefinition outerClass = outer.getClassDefinition(env);
                        initInner(outerClass, mods);
                    } catch (ClassNotFound e) {
                        // report the error elsewhere
                    }
                } else if (outer.equals(getClassDeclaration())) {
                    // The outer class in the record is this class.
                    try {
                        ClassDefinition innerClass =
                            inner.getClassDefinition(env);
                        initOuter(innerClass, mods);
                    } catch (ClassNotFound e) {
                        // report the error elsewhere
                    }
                }
            }
        }
    }

    private void initInner(ClassDefinition outerClass, int mods) {
        if (getOuterClass() != null)
            return;             // already done
        /******
        // Maybe set static, protected, or private.
        if ((modifiers & M_PUBLIC) != 0)
            mods &= M_STATIC;
        else
            mods &= M_PRIVATE | M_PROTECTED | M_STATIC;
        modifiers |= mods;
        ******/
        // For an inner class, the class access may have been weakened
        // from that originally declared the source.  We must take the
        // actual access permissions against which we check any source
        // we are currently compiling from the InnerClasses attribute.
        // We attempt to guard here against bogus combinations of modifiers.
        if ((mods & M_PRIVATE) != 0) {
            // Private cannot be combined with public or protected.
            mods &= ~(M_PUBLIC | M_PROTECTED);
        } else if ((mods & M_PROTECTED) != 0) {
            // Protected cannot be combined with public.
            mods &= ~M_PUBLIC;
        }
        if ((mods & M_INTERFACE) != 0) {
            // All interfaces are implicitly abstract.
            // All interfaces that are members of a type are implicitly static.
            mods |= (M_ABSTRACT | M_STATIC);
        }
        if (outerClass.isInterface()) {
            // All types that are members of interfaces are implicitly
            // public and static.
            mods |= (M_PUBLIC | M_STATIC);
            mods &= ~(M_PRIVATE | M_PROTECTED);
        }
        modifiers = mods;

        setOuterClass(outerClass);

        for (MemberDefinition field = getFirstMember();
             field != null;
             field = field.getNextMember()) {
            if (field.isUplevelValue()
                    && outerClass.getType().equals(field.getType())
                    && field.getName().toString().startsWith(prefixThis)) {
                setOuterMember(field);
            }
        }
    }

    private void initOuter(ClassDefinition innerClass, int mods) {
        if (innerClass instanceof BinaryClass)
            ((BinaryClass)innerClass).initInner(this, mods);
        addMember(new BinaryMember(innerClass));
    }

    /**
     * Write the class out to a given stream.  This function mirrors the loader.
     */
    public void write(Environment env, OutputStream out) throws IOException {
        DataOutputStream data = new DataOutputStream(out);

        // write out the header
        data.writeInt(JAVA_MAGIC);
        data.writeShort(env.getMinorVersion());
        data.writeShort(env.getMajorVersion());

        // Write out the constant pool
        cpool.write(data, env);

        // Write class information
        data.writeShort(getModifiers() & ACCM_CLASS);
        data.writeShort(cpool.indexObject(getClassDeclaration(), env));
        data.writeShort((getSuperClass() != null)
                        ? cpool.indexObject(getSuperClass(), env) : 0);
        data.writeShort(interfaces.length);
        for (int i = 0 ; i < interfaces.length ; i++) {
            data.writeShort(cpool.indexObject(interfaces[i], env));
        }

        // count the fields and the methods
        int fieldCount = 0, methodCount = 0;
        for (MemberDefinition f = firstMember; f != null; f = f.getNextMember())
            if (f.isMethod()) methodCount++; else fieldCount++;

        // write out each the field count, and then each field
        data.writeShort(fieldCount);
        for (MemberDefinition f = firstMember; f != null; f = f.getNextMember()) {
            if (!f.isMethod()) {
                data.writeShort(f.getModifiers() & ACCM_FIELD);
                String name = f.getName().toString();
                String signature = f.getType().getTypeSignature();
                data.writeShort(cpool.indexString(name, env));
                data.writeShort(cpool.indexString(signature, env));
                BinaryAttribute.write(((BinaryMember)f).atts, data, cpool, env);
            }
        }

        // write out each method count, and then each method
        data.writeShort(methodCount);
        for (MemberDefinition f = firstMember; f != null; f = f.getNextMember()) {
            if (f.isMethod()) {
                data.writeShort(f.getModifiers() & ACCM_METHOD);
                String name = f.getName().toString();
                String signature = f.getType().getTypeSignature();
                data.writeShort(cpool.indexString(name, env));
                data.writeShort(cpool.indexString(signature, env));
                BinaryAttribute.write(((BinaryMember)f).atts, data, cpool, env);
            }
        }

        // write out the class attributes
        BinaryAttribute.write(atts, data, cpool, env);
        data.flush();
    }

    /**
     * Get the dependencies
     */
    public Enumeration getDependencies() {
        return dependencies.elements();
    }

    /**
     * Add a dependency
     */
    public void addDependency(ClassDeclaration c) {
        if ((c != null) && !dependencies.contains(c)) {
            dependencies.addElement(c);
        }
    }

    /**
     * Get the constant pool
     */
    public BinaryConstantPool getConstants() {
        return cpool;
    }

    /**
     * Get a class attribute
     */
    public byte getAttribute(Identifier name)[] {
        for (BinaryAttribute att = atts ; att != null ; att = att.next) {
            if (att.name.equals(name)) {
                return att.data;
            }
        }
        return null;
    }
}
