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
package org.sonatype.maven.plugin.app.descriptor;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sonatype.plugins.model.PluginMetadata;

/**
 * Request for generating plugin metadata.
 * 
 * @author toby
 */
public class PluginMetadataGenerationRequest
{
    private String groupId;

    private String artifactId;

    private String version;

    private String name;

    private String description;

    private String pluginSiteURL;

    private String applicationId;

    private String applicationEdition;

    private String applicationMinVersion;

    private String applicationMaxVersion;

    private String scmUrl;

    private String scmVersion;

    private String scmTimestamp;

    private final Map<String, String> licenses = new HashMap<String, String>();

    private final Set<GAVCoordinate> classpathDependencies = new HashSet<GAVCoordinate>();

    private final Set<GAVCoordinate> pluginDependencies = new HashSet<GAVCoordinate>();

    /** The character encoding of the source files, may be {@code null} or empty to use platform's default encoding. */
    private String sourceEncoding;

    /** The resulting metadata */
    private PluginMetadata pluginMetadata;

    /** Output file for the final component descriptor. */
    private File outputFile;

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getPluginSiteURL()
    {
        return pluginSiteURL;
    }

    public void setPluginSiteURL( String pluginSiteURL )
    {
        this.pluginSiteURL = pluginSiteURL;
    }

    public String getApplicationId()
    {
        return applicationId;
    }

    public void setApplicationId( String applicationId )
    {
        this.applicationId = applicationId;
    }

    public String getApplicationEdition()
    {
        return applicationEdition;
    }

    public void setApplicationEdition( String applicationEdition )
    {
        this.applicationEdition = applicationEdition;
    }

    public String getApplicationMinVersion()
    {
        return applicationMinVersion;
    }

    public void setApplicationMinVersion( String applicationMinVersion )
    {
        this.applicationMinVersion = applicationMinVersion;
    }

    public String getApplicationMaxVersion()
    {
        return applicationMaxVersion;
    }

    public void setApplicationMaxVersion( String applicationMaxVersion )
    {
        this.applicationMaxVersion = applicationMaxVersion;
    }

    public void addLicense( String type, String url )
    {
        getLicenses().put( type, url );
    }

    public Map<String, String> getLicenses()
    {
        return licenses;
    }

    public void addClasspathDependency( GAVCoordinate coordinate )
    {
        getClasspathDependencies().add( coordinate );
    }

    public Set<GAVCoordinate> getClasspathDependencies()
    {
        return classpathDependencies;
    }

    public void addPluginDependency( GAVCoordinate coordinate )
    {
        getPluginDependencies().add( coordinate );
    }

    public Set<GAVCoordinate> getPluginDependencies()
    {
        return pluginDependencies;
    }

    public String getSourceEncoding()
    {
        return sourceEncoding;
    }

    public void setSourceEncoding( String sourceEncoding )
    {
        this.sourceEncoding = sourceEncoding;
    }

    public PluginMetadata getPluginMetadata()
    {
        return pluginMetadata;
    }

    public void setPluginMetadata( PluginMetadata pluginMetadata )
    {
        this.pluginMetadata = pluginMetadata;
    }

    public File getOutputFile()
    {
        return outputFile;
    }

    public void setOutputFile( File outputFile )
    {
        this.outputFile = outputFile;
    }

    public String getScmUrl()
    {
        return scmUrl;
    }

    public void setScmUrl( String scmUrl )
    {
        this.scmUrl = scmUrl;
    }

    public String getScmVersion()
    {
        return scmVersion;
    }

    public void setScmVersion( String scmVersion )
    {
        this.scmVersion = scmVersion;
    }

    public String getScmTimestamp()
    {
        return scmTimestamp;
    }

    public void setScmTimestamp( String scmTimestamp )
    {
        this.scmTimestamp = scmTimestamp;
    }
}
