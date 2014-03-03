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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.assembly.archive.AssemblyArchiver;
import org.apache.maven.plugin.assembly.io.AssemblyReader;
import org.apache.maven.plugin.assembly.model.Assembly;
import org.apache.maven.plugin.assembly.model.FileItem;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProjectHelper;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PACKAGE;

/**
 * Create a plugin bundle artifact attach it to the plugins project.
 * 
 * @since 1.0
 */
@Mojo(name = "create-bundle", defaultPhase = PACKAGE)
public class CreateBundleMojo
    extends MojoSupport
{
  public static final String BUNDLE_TYPE = "zip";

  public static final String BUNDLE_ID = "bundle";

  @Component
  private MavenSession session;

  @Component
  private AssemblyArchiver assemblyArchiver;

  @Component
  private AssemblyReader assemblyReader;

  @Component
  private MavenProjectHelper projectHelper;

  /**
   * Supplemental plugin assembly configuration.
   */
  @Parameter
  private BundleConfiguration bundle;

  /**
   * Optional alternative assembly descriptor.
   * 
   * Generally should avoid using this feature, its here for compatibility reasons.
   */
  @Parameter
  private File assemblyDescriptor;

  public void execute() throws MojoExecutionException, MojoFailureException {
    // skip if wrong packaging
    if (!isNexusPluginPacakging()) {
      return;
    }

    if (bundle == null) {
      bundle = new BundleConfiguration();
    }
    bundle.initDefaults(project, session);

    Assembly assembly = createAssembly();
    assembly.addFormat(BUNDLE_TYPE);
    assembly.setId(BUNDLE_ID);
    assembly.setIncludeBaseDirectory(false);

    List<FileItem> classPathItems = new ArrayList<FileItem>();

    // Write included plugin dependencies into the the /dependencies directory
    try {
      Properties artifacts = ClasspathUtils.read(project);

      // build list of keys and sort for better display
      List<String> artifactKeys = new ArrayList<String>(mapOf(artifacts).keySet());
      Collections.sort(artifactKeys);

      String outputDirectory = String.format("%s-%s/dependencies", project.getArtifactId(), project.getVersion());

      if (!artifacts.isEmpty()) {
        getLog().info("Including " + artifacts.size() + " dependencies:");
        for (String key : artifactKeys) {
          getLog().info(" + " + key);
          FileItem fileItem = ClasspathUtils.createFileItemForKey(key, artifacts);
          fileItem.setOutputDirectory(outputDirectory);
          classPathItems.add(fileItem);
          assembly.addFile(fileItem);
        }
      }
    }
    catch (IOException e) {
      throw new MojoExecutionException("Failed to create plugin bundle: " + e.getMessage(), e);
    }

    // Add the main plugin artifact
    FileItem fileItem = new FileItem();
    fileItem.setSource(project.getArtifact().getFile().getPath());
    fileItem.setOutputDirectory(project.getArtifactId() + "-" + project.getVersion());
    classPathItems.add(fileItem);
    assembly.addFile(fileItem);

    try {
      // Add OSGi metadata (optimized for exploded plugin bundle)
      FileItem osgiItem = new FileItem();
      osgiItem.setSource(OSGiUtils.updateMetadata(project, classPathItems));
      osgiItem.setOutputDirectory(fileItem.getOutputDirectory() + "/META-INF");
      osgiItem.setDestName("MANIFEST.MF");
      assembly.addFile(osgiItem);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Failed to create plugin bundle: " + e.getMessage(), e);
    }

    // Generate the bundle assembly
    File assemblyFile;
    try {
      assemblyFile = assemblyArchiver
          .createArchive(assembly, bundle.getAssemblyFileName(assembly), BUNDLE_TYPE, bundle);
    }
    catch (Exception e) {
      throw new MojoExecutionException("Failed to create plugin bundle: " + e.getMessage(), e);
    }

    // Attach bundle assembly to the project
    projectHelper.attachArtifact(project, BUNDLE_TYPE, assembly.getId(), assemblyFile);
  }

  private static Map<String, String> mapOf(final Properties props) {
    Map<String, String> map = new HashMap<String, String>(props.size());
    for (Object key : props.keySet()) {
      map.put(key.toString(), props.get(key).toString());
    }
    return map;
  }

  private Assembly createAssembly() throws MojoExecutionException {
    Assembly assembly;

    if (assemblyDescriptor != null) {
      getLog().debug("Using custom assembly descriptor: " + assemblyDescriptor.getAbsolutePath());

      try {
        assembly = assemblyReader.getAssemblyFromDescriptorFile(assemblyDescriptor, bundle);
      }
      catch (Exception e) {
        throw new MojoExecutionException("Could not read assembly descriptor: " + assemblyDescriptor.getAbsolutePath(),
            e);
      }
    }
    else {
      assembly = new Assembly();
    }

    return assembly;
  }
}
