/*
 * #%L
 * Native ARchive plugin for Maven
 * %%
 * Copyright (C) 2002 - 2014 NAR Maven Plugin developers.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.maven_nar.cpptasks.hp;

import java.io.File;
import java.util.Vector;

import com.github.maven_nar.cpptasks.CUtil;
import com.github.maven_nar.cpptasks.compiler.LinkType;
import com.github.maven_nar.cpptasks.compiler.Linker;
import com.github.maven_nar.cpptasks.gcc.AbstractLdLinker;

/**
 * Adapter for Sun (r) Forte(tm) C++ Linker
 *
 * @author Curt Arnold
 */
public final class aCCLinker extends AbstractLdLinker {
  private static final String[] discardFiles = new String[0];
  private static final String[] objFiles = new String[] {
      ".o", ".a", ".lib", ".dll", ".so", ".sl"
  };
  private static final aCCLinker arLinker = new aCCLinker("aCC", objFiles, discardFiles, "", ".a");
  private static final aCCLinker dllLinker = new aCCLinker("aCC", objFiles, discardFiles, "lib", ".sl");
  private static final aCCLinker instance = new aCCLinker("aCC", objFiles, discardFiles, "", "");

  public static aCCLinker getInstance() {
    return instance;
  }

  private File[] libDirs;

  private aCCLinker(final String command, final String[] extensions, final String[] ignoredExtensions,
      final String outputPrefix, final String outputSuffix) {
    super(command, "-help", extensions, ignoredExtensions, outputPrefix, outputSuffix, false, null);
  }

  public void addImpliedArgs(final boolean debug, final LinkType linkType, final Vector<String> args) {
    if (debug) {
      args.addElement("-g");
    }
    /*
     * if(linkType.isStaticRuntime()) { args.addElement("-static"); }
     */
    if (linkType.isSharedLibrary()) {
      args.addElement("-b");
    }
    /*
     * if (linkType.isStaticLibrary()) { args.addElement("-Wl,-noshared"); }
     */
  }

  public void addIncremental(final boolean incremental, final Vector<String> args) {
    /*
     * if (incremental) { args.addElement("-xidlon"); } else {
     * args.addElement("-xidloff"); }
     */
  }

  /**
   * Returns library path.
   * 
   */
  @Override
  public File[] getLibraryPath() {
    if (this.libDirs == null) {
      final File CCloc = CUtil.getExecutableLocation("aCC");
      if (CCloc != null) {
        final File compilerLib = new File(new File(CCloc, "../lib").getAbsolutePath());
        if (compilerLib.exists()) {
          this.libDirs = new File[2];
          this.libDirs[0] = compilerLib;
        }
      }
      if (this.libDirs == null) {
        this.libDirs = new File[1];
      }
    }
    this.libDirs[this.libDirs.length - 1] = new File("/usr/lib");
    return this.libDirs;
  }

  @Override
  public Linker getLinker(final LinkType type) {
    if (type.isStaticLibrary()) {
      return arLinker;
    }
    if (type.isSharedLibrary()) {
      return dllLinker;
    }
    return instance;
  }
}
