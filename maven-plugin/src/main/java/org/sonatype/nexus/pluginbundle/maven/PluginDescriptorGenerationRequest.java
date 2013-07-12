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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.pluginbundle.maven.GAVCoordinate;
import org.sonatype.plugins.model.PluginMetadata;

/**
 * Request for generating plugin metadata.
 *
 * @since 1.0
 *
 * @see PluginDescriptorGenerator
 */
public class PluginDescriptorGenerationRequest
{
    private String groupId;

    private String artifactId;

    private String version;

    private String name;

    private String description;

    private String pluginSiteURL;

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
