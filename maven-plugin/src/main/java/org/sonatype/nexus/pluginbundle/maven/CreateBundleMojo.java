/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PACKAGE;

/**
 * Create a plugin bundle artifact attach it to the plugins project.
 *
 * @since 1.0
 */
@Mojo(name="create-bundle", defaultPhase = PACKAGE)
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
     * Alternative assembly descriptor.  If not specified, default assembly descriptor will be used instead.
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

        // Write included plugin dependencies into the the /dependencies directory
        try {
            Properties artifacts = ClasspathUtils.read(project);
            String outputDirectory = project.getArtifactId() + "-" + project.getVersion() + "/dependencies";

            if (!artifacts.isEmpty()) {
                getLog().info("Including dependencies:");

                for (Iterator it = artifacts.keySet().iterator(); it.hasNext(); ) {
                    String artifactKey = (String) it.next();
                    getLog().info("  + " + artifactKey);

                    FileItem fileItem = ClasspathUtils.createFileItemForKey(artifactKey, artifacts);
                    fileItem.setOutputDirectory(outputDirectory);
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
        assembly.addFile(fileItem);

        // Generate the bundle assembly
        File assemblyFile;
        try {
            assemblyFile = assemblyArchiver.createArchive(assembly, bundle.getAssemblyFileName(assembly), BUNDLE_TYPE, bundle);
        }
        catch (Exception e) {
            throw new MojoExecutionException("Failed to create plugin bundle: " + e.getMessage(), e);
        }

        // Attach bundle assembly to the project
        projectHelper.attachArtifact(project, BUNDLE_TYPE, assembly.getId(), assemblyFile);
    }

    private Assembly createAssembly() throws MojoExecutionException {
        Assembly assembly;

        if (assemblyDescriptor != null) {
            getLog().debug("Using custom assembly descriptor: " + assemblyDescriptor.getAbsolutePath());

            try {
                assembly = assemblyReader.getAssemblyFromDescriptorFile(assemblyDescriptor, bundle);
            }
            catch (Exception e) {
                throw new MojoExecutionException("Could not read assembly descriptor: " + assemblyDescriptor.getAbsolutePath(), e);
            }
        }
        else {
            assembly = new Assembly();
        }

        return assembly;
    }
}
