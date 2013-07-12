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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.assembly.AssemblerConfigurationSource;
import org.apache.maven.plugin.assembly.model.Assembly;
import org.apache.maven.plugin.assembly.utils.AssemblyFormatUtils;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenFileFilter;

/**
 * Supplemental configuration for plugin bundle assemblies, to allow users to customize the finalName, archive
 * configuration (the {@link MavenArchiveConfiguration} instance), and tarLongFileMode. This class is similar to the
 * plugin parameters available in the maven-assembly-plugin itself.
 *
 * @since 1.0
 */
public class BundleConfiguration
    implements AssemblerConfigurationSource
{
    private MavenProject project;

    private MavenSession session;

    private List<String> filters;

    private String finalName;

    private MavenArchiveConfiguration archiveConfiguration;

    private String tarLongFileMode = "gnu";

    public BundleConfiguration() {
        // empty
    }

    public void initDefaults(final MavenProject project, final MavenSession session) {
        this.project = project;
        this.session = session;
        if (finalName == null) {
            finalName = project.getBuild().getFinalName();
        }
    }

    public File getArchiveBaseDirectory() {
        return null;
    }

    public String getArchiverConfig() {
        return null;
    }

    public File getBasedir() {
        return project.getBasedir();
    }

    public String getClassifier() {
        return null;
    }

    public String getDescriptor() {
        return null;
    }

    public String getDescriptorId() {
        return null;
    }

    public String[] getDescriptorReferences() {
        return null;
    }

    public File getDescriptorSourceDirectory() {
        return null;
    }

    public String[] getDescriptors() {
        return null;
    }

    public List getFilters() {
        return filters;
    }

    public void addFilter(final String filter) {
        if (filters == null) {
            filters = new ArrayList<String>();
        }

        filters.add(filter);
    }

    public void setFilters(final List<String> filters) {
        this.filters = filters;
    }

    public String getFinalName() {
        return finalName;
    }

    public void setFinalName(final String finalName) {
        this.finalName = finalName;
    }

    public MavenArchiveConfiguration getJarArchiveConfiguration() {
        return archiveConfiguration;
    }

    public void setArchive(final MavenArchiveConfiguration archiveConfiguration) {
        this.archiveConfiguration = archiveConfiguration;
    }

    public ArtifactRepository getLocalRepository() {
        return session.getLocalRepository();
    }

    public MavenSession getMavenSession() {
        return session;
    }

    public File getOutputDirectory() {
        return new File(project.getBuild().getDirectory());
    }

    public MavenProject getProject() {
        return project;
    }

    public List getReactorProjects() {
        return session.getProjects();
    }

    public List getRemoteRepositories() {
        return project.getRemoteArtifactRepositories();
    }

    public File getSiteDirectory() {
        return new File(project.getReporting().getOutputDirectory());
    }

    public String getTarLongFileMode() {
        return tarLongFileMode;
    }

    public void setTarLongFileMode(final String mode) {
        tarLongFileMode = mode;
    }

    public File getTemporaryRootDirectory() {
        return new File(project.getBuild().getDirectory(), "nexus-plugin-bundle/tmp");
    }

    public File getWorkingDirectory() {
        return new File(project.getBuild().getDirectory(), "nexus-plugin-bundle/work");
    }

    public boolean isAssemblyIdAppended() {
        return true;
    }

    public boolean isDryRun() {
        return false;
    }

    public boolean isIgnoreDirFormatExtensions() {
        return true;
    }

    public boolean isIgnoreMissingDescriptor() {
        return false;
    }

    public boolean isSiteIncluded() {
        return false;
    }

    public String getAssemblyFileName(final Assembly assembly) {
        return AssemblyFormatUtils.getDistributionName(assembly, this);
    }

    public MavenFileFilter getMavenFileFilter() {
        return null;
    }

}
