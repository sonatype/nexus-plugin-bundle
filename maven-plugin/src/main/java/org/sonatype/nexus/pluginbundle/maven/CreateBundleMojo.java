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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.assembly.archive.AssemblyArchiver;
import org.apache.maven.plugin.assembly.io.AssemblyReader;
import org.apache.maven.plugin.assembly.model.Assembly;
import org.apache.maven.plugin.assembly.model.FileItem;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

/**
 * Create a plugin bundle artifact attach it to the plugins project.
 *
 * @goal create-bundle
 * @phase package
 *
 * @since 1.0
 */
public class CreateBundleMojo
    extends AbstractMojo
{
    /**
     * @parameter property="project"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter property="session"
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
     * @component
     */
    private AssemblyArchiver assemblyArchiver;

    /**
     * @component
     */
    private AssemblyReader assemblyReader;

    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (bundle == null) {
            bundle = new BundleConfiguration();
        }
        bundle.initDefaults(project, session);

        Assembly assembly = new Assembly();
        assembly.addFormat("zip");
        assembly.setId("bundle");
        assembly.setIncludeBaseDirectory(false);

        // Write included plugin dependencies into the the /dependencies directory
        try {
            Properties artifacts = ClasspathUtils.read(project);
            String outputDirectory = project.getArtifactId() + "-" + project.getVersion() + "/dependencies";

            for (Iterator it = artifacts.keySet().iterator(); it.hasNext(); ) {
                String artifactKey = (String) it.next();
                FileItem fileItem = ClasspathUtils.createFileItemForKey(artifactKey, artifacts);
                fileItem.setOutputDirectory(outputDirectory);
                assembly.addFile(fileItem);
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
            assemblyFile = assemblyArchiver.createArchive(assembly, bundle.getAssemblyFileName(assembly), "zip", bundle);
        }
        catch (Exception e) {
            throw new MojoExecutionException("Failed to create plugin bundle: " + e.getMessage(), e);
        }

        // Attach bundle assembly to the project
        projectHelper.attachArtifact(project, "zip", assembly.getId(), assemblyFile);
    }
}
