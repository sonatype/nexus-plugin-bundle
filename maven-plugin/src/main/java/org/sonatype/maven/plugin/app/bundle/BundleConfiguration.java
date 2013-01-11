/*
 * Sonatype Application Build Lifecycle
 * Copyright (C) 2009-2012 Sonatype, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.maven.plugin.app.bundle;

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
 * plugin parameters available in the Assembly plugin itself.
 * 
 * @see {@link http://maven.apache.org/plugins/maven-assembly-plugin/}
 * 
 * @author jdcasey
 * 
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

    public BundleConfiguration()
    {
    }

    public BundleConfiguration( final MavenProject project, final MavenSession session )
    {
        initDefaults( project, session );
    }

    public void initDefaults( final MavenProject project, final MavenSession session )
    {
        this.project = project;
        this.session = session;
        if ( finalName == null )
        {
            finalName = project.getBuild().getFinalName();
        }
    }

    public File getArchiveBaseDirectory()
    {
        return null;
    }

    public String getArchiverConfig()
    {
        return null;
    }

    public File getBasedir()
    {
        return project.getBasedir();
    }

    public String getClassifier()
    {
        return null;
    }

    public String getDescriptor()
    {
        return null;
    }

    public String getDescriptorId()
    {
        return null;
    }

    public String[] getDescriptorReferences()
    {
        return null;
    }

    public File getDescriptorSourceDirectory()
    {
        return null;
    }

    public String[] getDescriptors()
    {
        return null;
    }

    public List getFilters()
    {
        return filters;
    }

    public void addFilter( final String filter )
    {
        if ( filters == null )
        {
            filters = new ArrayList<String>();
        }

        filters.add( filter );
    }

    public void setFilters( final List<String> filters )
    {
        this.filters = filters;
    }

    public String getFinalName()
    {
        return finalName;
    }

    public void setFinalName( final String finalName )
    {
        this.finalName = finalName;
    }

    public MavenArchiveConfiguration getJarArchiveConfiguration()
    {
        return archiveConfiguration;
    }

    public void setArchive( final MavenArchiveConfiguration archiveConfiguration )
    {
        this.archiveConfiguration = archiveConfiguration;
    }

    public ArtifactRepository getLocalRepository()
    {
        return session.getLocalRepository();
    }

    public MavenSession getMavenSession()
    {
        return session;
    }

    public File getOutputDirectory()
    {
        return new File( project.getBuild().getDirectory() );
    }

    public MavenProject getProject()
    {
        return project;
    }

    public List getReactorProjects()
    {
        return session.getProjects();
    }

    @SuppressWarnings( "unchecked" )
    public List getRemoteRepositories()
    {
        return project.getRemoteArtifactRepositories();
    }

    public File getSiteDirectory()
    {
        return new File( project.getReporting().getOutputDirectory() );
    }

    public String getTarLongFileMode()
    {
        return tarLongFileMode;
    }

    public void setTarLongFileMode( final String mode )
    {
        tarLongFileMode = mode;
    }

    public File getTemporaryRootDirectory()
    {
        return new File( project.getBuild().getDirectory(), "nexus-bundle-tmp" );
    }

    public File getWorkingDirectory()
    {
        return new File( project.getBuild().getDirectory(), "nexus-bundle/work" );
    }

    public boolean isAssemblyIdAppended()
    {
        return true;
    }

    public boolean isDryRun()
    {
        return false;
    }

    public boolean isIgnoreDirFormatExtensions()
    {
        return true;
    }

    public boolean isIgnoreMissingDescriptor()
    {
        return false;
    }

    public boolean isSiteIncluded()
    {
        return false;
    }

    public String getAssemblyFileName( final Assembly assembly )
    {
        return AssemblyFormatUtils.getDistributionName( assembly, this );
    }

    public MavenFileFilter getMavenFileFilter()
    {
        return null;
    }

}
