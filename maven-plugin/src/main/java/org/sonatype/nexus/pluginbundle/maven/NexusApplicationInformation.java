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

/**
 * Implementation of {@link ApplicationInformation}, which supplies Nexus-specific default configurations, plugin
 * packaging, and application-core groupIds. This is simpler to maintain for now than an XML configuration.
 *
 * @since 1.0
 */
public class NexusApplicationInformation
    extends ApplicationInformation
{
    public NexusApplicationInformation() {
        addCoreGroupIdPattern("org.sonatype.nexus");
        addCoreGroupIdPattern("com.sonatype.nexus");

        setPluginPackaging("nexus-plugin");

        setApplicationId("nexus");
        setPluginMetadataPath("${project.build.outputDirectory}/META-INF/nexus/plugin.xml");

        setApplicationMinVersion("1.10.0");

        setApplicationEdition("OSS");
    }
}
