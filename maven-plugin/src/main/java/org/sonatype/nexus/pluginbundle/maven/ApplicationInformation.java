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

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.PrefixAwareRecursionInterceptor;
import org.codehaus.plexus.interpolation.PrefixedObjectValueSource;
import org.codehaus.plexus.interpolation.RecursionInterceptor;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ???
 *
 * @since 1.0
 */
public abstract class ApplicationInformation
{
    private static final List<String> PROJECT_PREFIXES;

    static {
        List<String> prefixes = new ArrayList<String>();

        prefixes.add("project.");
        prefixes.add("pom.");

        PROJECT_PREFIXES = Collections.unmodifiableList(prefixes);
    }

    private Set<String> coreGroupIdPatterns;

    private String pluginPackaging;

    private String pluginMetadataPath;

    private String applicationId;

    private String applicationMinVersion;

    private String applicationMaxVersion;

    private String applicationEdition;

    /**
     * Interpolate any project references in the plugin metadata output path, returning a {@link File} reference to the
     * interpolated path.
     *
     * @see ApplicationInformation#setPluginMetadataPath(String)
     */
    public File getPluginMetadataFile(final MavenProject project)
        throws InterpolationException
    {
        return interpolateToFile(getPluginMetadataPath(), project);
    }

    /**
     * Determine whether the specified groupId matches any of those specified as core groupIds for this application. If
     * there are no application core groupIds, return false. If the groupId matches one of the core groupIds using
     * {@link Object#equals(Object)}, or using {@link String#matches(String)}, then return true.
     */
    public boolean matchesCoreGroupIds(final String groupId) {
        boolean matchedCoreGroupId = false;
        if (getCoreGroupIdPatterns() != null) {
            for (String pattern : getCoreGroupIdPatterns()) {
                if (groupId.equals(pattern) || groupId.matches(pattern)) {
                    matchedCoreGroupId = true;
                    break;
                }
            }
        }

        return matchedCoreGroupId;
    }

    /**
     * @see ApplicationInformation#setCoreGroupIdPatterns(Set)
     */
    public void addCoreGroupIdPattern(final String coreGroupIdPattern) {
        if (coreGroupIdPatterns == null) {
            coreGroupIdPatterns = new HashSet<String>();
        }

        coreGroupIdPatterns.add(coreGroupIdPattern);
    }

    /**
     * @see ApplicationInformation#setCoreGroupIdPatterns(Set)
     */
    public Set<String> getCoreGroupIdPatterns() {
        return coreGroupIdPatterns;
    }

    /**
     * These are the groupId patterns that are meant to be present ONLY in the application's core. They can be either
     * groupId prefixes (or whole groupIds), or they can be regular expressions. <br/>
     * The mojos in the app-lifecycle-maven-plugin will require that any plugin dependency with a matching groupId be
     * declared with the 'provided' scope. These dependencies will be excluded from the plugin descriptor, and the
     * plugin bundle itself.
     */
    public void setCoreGroupIdPatterns(final Set<String> coreGroupIdPatterns) {
        this.coreGroupIdPatterns = coreGroupIdPatterns;
    }

    /**
     * @see ApplicationInformation#setPluginPackaging(String)
     */
    public String getPluginPackaging() {
        return pluginPackaging;
    }

    /**
     * This is the POM packaging (also, the dependency type) used for plugins in this application. Plugin dependencies
     * with this type specification MUST be declared with 'provided' scope, and will be included in a separate section
     * of the plugin descriptor from its external dependencies. Inter-plugin dependencies will later be resolved using
     * the application's plugin manager.
     */
    public void setPluginPackaging(final String pluginPackaging) {
        this.pluginPackaging = pluginPackaging;
    }

    /**
     * @see ApplicationInformation#setPluginMetadataPath(String)
     */
    public String getPluginMetadataPath() {
        return pluginMetadataPath;
    }

    /**
     * Path where the plugin descriptor should be written during the build. This path may make reference to Maven
     * project expressions just like any plugin or POM would. <br/>
     * Normally, this path will start with ${project.build.outputDirectory/META-INF/.
     */
    public void setPluginMetadataPath(final String pluginMetadataFile) {
        this.pluginMetadataPath = pluginMetadataFile;
    }

    /**
     * Default application ID.
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Default application ID.
     */
    public void setApplicationId(final String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * @see ApplicationInformation#setApplicationMinVersion(String)
     */
    public String getApplicationMinVersion() {
        return applicationMinVersion;
    }

    /**
     * The default minimum application version with which this plugin being built is compatible.
     */
    public void setApplicationMinVersion(final String applicationMinVersion) {
        this.applicationMinVersion = applicationMinVersion;
    }

    /**
     * @see ApplicationInformation#setApplicationMaxVersion(String)
     */
    public String getApplicationMaxVersion() {
        return applicationMaxVersion;
    }

    /**
     * The default maximum application version with which this plugin being built is compatible.
     */
    public void setApplicationMaxVersion(final String applicationMaxVersion) {
        this.applicationMaxVersion = applicationMaxVersion;
    }

    /**
     * @see ApplicationInformation#setApplicationEdition(String)
     */
    public String getApplicationEdition() {
        return applicationEdition;
    }

    /**
     * The default edition of this application (OSS, Pro, etc.) with which this plugin is meant to work.
     */
    public void setApplicationEdition(final String applicationEdition) {
        this.applicationEdition = applicationEdition;
    }

    private File interpolateToFile(final String pattern, final MavenProject project)
        throws InterpolationException
    {
        if (pattern == null) {
            return null;
        }

        Interpolator interpolator = new StringSearchInterpolator();
        interpolator.addValueSource(new PrefixedObjectValueSource(PROJECT_PREFIXES, project, false));

        RecursionInterceptor ri = new PrefixAwareRecursionInterceptor(PROJECT_PREFIXES);

        return new File(interpolator.interpolate(pattern, ri));
    }
}
