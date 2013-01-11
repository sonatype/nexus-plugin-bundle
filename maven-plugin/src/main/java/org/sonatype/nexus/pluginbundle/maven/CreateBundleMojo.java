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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.assembly.InvalidAssemblerConfigurationException;
import org.apache.maven.plugin.assembly.archive.ArchiveCreationException;
import org.apache.maven.plugin.assembly.archive.AssemblyArchiver;
import org.apache.maven.plugin.assembly.format.AssemblyFormattingException;
import org.apache.maven.plugin.assembly.io.AssemblyReadException;
import org.apache.maven.plugin.assembly.io.AssemblyReader;
import org.apache.maven.plugin.assembly.model.Assembly;
import org.apache.maven.plugin.assembly.model.FileItem;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * Create a plugin bundle artifact attach it to the plugins project.
 *
 * @goal create-bundle
 * @phase package
 * @since 1.0
 */
public class CreateBundleMojo
    extends AbstractMojo
{
    /**
     * The current plugin project being built.
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The current build session, for reference from the Assembly API.
     *
     * @parameter default-value="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    /**
     * Supplemental plugin assembly configuration.
     *
     * @parameter
     */
    private BundleConfiguration bundle;

    /**
     * Alternative assembly descriptor. If not specified, default assembly descriptor will be used instead.
     *
     * @parameter
     * @readonly
     */
    private File assemblyDescriptor;

    /**
     * Assembly manager component that is responsible for creating the plugin bundle assembly and attaching it to the
     * current project.
     *
     * @component
     */
    private AssemblyArchiver archiver;

    /**
     * @component
     */
    private AssemblyReader assemblyReader;

    /**
     * Component used by the {@link AssemblyArchiver} to attach the bundle artifact to the current project.
     *
     * @component
     */
    private MavenProjectHelper projectHelper;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (bundle == null) {
            bundle = new BundleConfiguration(project, session);
        }
        else {
            bundle.initDefaults(project, session);
        }

        Assembly assembly;

        if (assemblyDescriptor != null) {
            try {
                assembly = assemblyReader.getAssemblyFromDescriptorFile(assemblyDescriptor, bundle);
            }
            catch (AssemblyReadException e) {
                throw new MojoExecutionException("Could not read assembly descriptor: " + assemblyDescriptor.getAbsolutePath(), e);
            }
            catch (InvalidAssemblerConfigurationException e) {
                throw new MojoExecutionException("Invalid assembly descriptor: " + assemblyDescriptor.getAbsolutePath(), e);
            }
        }
        else {
            assembly = new Assembly();
        }

        assembly.addFormat("zip");
        assembly.setId("bundle");
        assembly.setIncludeBaseDirectory(false);

        try {
            Properties cpArtifacts = ClasspathUtils.read(project);
            String outputDirectory = project.getArtifactId() + "-" + project.getVersion() + "/dependencies";

            for (Iterator it = cpArtifacts.keySet().iterator(); it.hasNext(); ) {
                String artifactKey = (String) it.next();

                FileItem fi = ClasspathUtils.createFileItemForKey(artifactKey, cpArtifacts);

                fi.setOutputDirectory(outputDirectory);

                assembly.addFile(fi);
            }
        }
        catch (IOException e) {
            throw new MojoExecutionException("Failed to create plugin bundle: " + e.getMessage(), e);
        }

        FileItem fileItem = new FileItem();
        fileItem.setSource(project.getArtifact().getFile().getPath());
        fileItem.setOutputDirectory(project.getArtifactId() + "-" + project.getVersion());
        assembly.addFile(fileItem);

        try {
            File assemblyFile = archiver.createArchive(assembly, bundle.getAssemblyFileName(assembly), "zip", bundle);
            projectHelper.attachArtifact(project, "zip", assembly.getId(), assemblyFile);
        }
        catch (ArchiveCreationException e) {
            throw new MojoExecutionException("Failed to create plugin bundle: " + e.getMessage(), e);
        }
        catch (AssemblyFormattingException e) {
            throw new MojoExecutionException("Failed to create plugin bundle: " + e.getMessage(), e);
        }
        catch (InvalidAssemblerConfigurationException e) {
            throw new MojoExecutionException("Failed to create plugin bundle: " + e.getMessage(), e);
        }
    }
}
