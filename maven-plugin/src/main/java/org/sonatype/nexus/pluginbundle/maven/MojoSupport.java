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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;

/**
 * Support for mojo implementations.
 *
 * @since 1.0
 */
public abstract class MojoSupport
    extends AbstractMojo
{
    public static final String NEXUS_PLUGIN = "nexus-plugin";

    @Component
    protected MavenProject project;

    protected boolean isNexusPluginPacakging() {
        if (!project.getPackaging().equals(NEXUS_PLUGIN)) {
            getLog().warn("Project is not of packaging type: " + NEXUS_PLUGIN);
            return false;
        }
        return true;
    }
}
