/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package org.sonatype.nexus.pluginbundle.maven;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plugins.model.ClasspathDependency;
import org.sonatype.plugins.model.PluginDependency;
import org.sonatype.plugins.model.PluginMetadata;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Jar;
import aQute.bnd.version.Version;
import com.google.common.io.Closeables;
import org.apache.maven.plugin.assembly.model.FileItem;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.osgi.framework.Constants;

/**
 * Utility methods to manage basic OSGi metadata for exploded Nexus plugin bundles.
 * 
 * @since 1.2
 */
public class OSGiUtils
{
  private static final String FILE_NAME = "nexus-plugin-bundle/osgi.metadata";

  private OSGiUtils() {
    // empty
  }

  /**
   * Updates the OSGi metadata wrt the assembled content.
   * 
   * @return Path to OSGi manifest
   */
  public static String updateMetadata(final MavenProject project, final List<FileItem> content) throws IOException {

    File file = new File(project.getBuild().getDirectory(), FILE_NAME);
    if (!file.exists()) {
      throw new IOException("Missing metadata file: " + file.getAbsolutePath());
    }

    Manifest mf = new Manifest();

    InputStream input = null;
    try {
      input = new BufferedInputStream(new FileInputStream(file));
      mf.read(input);
    }
    finally {
      IOUtil.close(input);
    }

    Attributes attributes = mf.getMainAttributes();

    String exportedPackages = getExportedPackages(content);
    if (exportedPackages.length() > 0) {
      attributes.putValue(Constants.EXPORT_PACKAGE, exportedPackages);
    }

    OutputStream output = null;
    try {
      output = new BufferedOutputStream(new FileOutputStream(file));
      mf.write(output);
    }
    finally {
      IOUtil.close(output);
    }

    return file.getPath();
  }

  /**
   * Generates basic OSGi metadata for the Nexus plugin.
   */
  public static void write(final BuildContext buildContext, final PluginMetadata metadata, final MavenProject project)
      throws IOException
  {
    Manifest mf = new Manifest();

    Attributes attributes = mf.getMainAttributes();
    attributes.put(Name.MANIFEST_VERSION, "1.0");

    attributes.putValue(Constants.BUNDLE_MANIFESTVERSION, "2");
    attributes.putValue(Constants.BUNDLE_SYMBOLICNAME, metadata.getGroupId() + '.' + metadata.getArtifactId());
    attributes.putValue(Constants.BUNDLE_VERSION, Analyzer.cleanupVersion(metadata.getVersion()));
    attributes.putValue(Constants.BUNDLE_CLASSPATH, getBundleClassPath(project, metadata));

    String requiredBundles = getRequiredBundles(metadata);
    if (requiredBundles.length() > 0) {
      attributes.putValue(Constants.REQUIRE_BUNDLE, requiredBundles);
    }

    File file = new File(project.getBuild().getDirectory(), FILE_NAME);
    file.getParentFile().mkdirs();

    OutputStream output = null;
    try {
      output = new BufferedOutputStream(buildContext.newFileOutputStream(file));
      mf.write(output);
    }
    finally {
      IOUtil.close(output);
    }
  }

  /**
   * Generates a Bundle-ClassPath header for the Nexus plugin.
   */
  private static String getBundleClassPath(final MavenProject project, final PluginMetadata metadata) {
    StringBuilder buf = new StringBuilder(project.getBuild().getFinalName()).append(".jar");
    for (ClasspathDependency d : metadata.getClasspathDependencies()) {
      buf.append(",dependencies/").append(d.getArtifactId()).append('-').append(d.getVersion());
      if (!StringUtils.isBlank(d.getClassifier())) {
        buf.append('-').append(d.getClassifier());
      }
      buf.append('.').append(d.getType());
    }
    return buf.toString();
  }

  /**
   * Generates a Require-Bundle header for the Nexus plugin.
   */
  private static String getRequiredBundles(final PluginMetadata metadata) {
    StringBuilder buf = new StringBuilder("org.sonatype.nexus.plugin-api;resolution:=optional");
    for (PluginDependency d : metadata.getPluginDependencies()) {
      if (buf.length() > 0) {
        buf.append(',');
      }
      buf.append(d.getGroupId()).append('.').append(d.getArtifactId()).append(";bundle-version=")
          .append(Version.parseVersion(Analyzer.cleanupVersion(d.getVersion())).getWithoutQualifier());
    }
    return buf.toString();
  }

  /**
   * Generates an Export-Package header for the given content.
   */
  private static String getExportedPackages(final List<FileItem> content) throws IOException {
    StringBuilder buf = new StringBuilder();
    for (FileItem i : content) {
      Jar jar = null;
      try {
        jar = new Jar(new File(i.getSource()));
        for (String pkg : jar.getPackages()) {
          if (pkg.length() > 0 &&
              !pkg.startsWith("META") &&
              !pkg.startsWith("OSGI") &&
              !pkg.startsWith("docs") &&
              !pkg.startsWith("static")) {
            if (buf.length() > 0) {
              buf.append(',');
            }
            buf.append(pkg);
          }
        }
      }
      finally {
        Closeables.closeQuietly(jar);
      }
    }
    return buf.toString();
  }
}
