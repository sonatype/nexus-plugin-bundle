<!--

    Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.

    This program is licensed to you under the Apache License Version 2.0,
    and you may not use this file except in compliance with the Apache License Version 2.0.
    You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.

    Unless required by applicable law or agreed to in writing,
    software distributed under the Apache License Version 2.0 is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.

-->
# Usage

Goals of this plugin are not intended to be executed directly, they are enabled via the __nexus-plugin__ packaging lifecycle.

## References

* [Plugin Documentation](plugin-info.html)

## Basic Configuration

Add a plugin definition with __extensions__ enabled:

    <plugin>
        <groupId>org.sonatype.nexus</groupId>
        <artifactId>nexus-plugin-bundle-maven-plugin</artifactId>
        <extensions>true</extensions>
    </plugin>

Set the packaging of the project/module to __nexus-plugin__:

    <packaging>nexus-plugin</packaging>

## Plugin Information

Configure the plugin name and description, which are shown in the Nexus plugin console:

    <plugin>
        <groupId>org.sonatype.nexus</groupId>
        <artifactId>nexus-plugin-bundle-maven-plugin</artifactId>
        <configuration>
            <pluginName>My Nexus Plugin</pluginName>
            <pluginDescription>This is my super-awesome Nexus plugin!</pluginDescription>
        </configuration>
    </plugin>

## Shared Dependencies

If the plugin provides dependencies which are to be made available to other plugins, mark them as shared:

    <plugin>
        <groupId>org.sonatype.nexus</groupId>
        <artifactId>nexus-plugin-bundle-maven-plugin</artifactId>
        <configuration>
            <sharedDependencies>
                <sharedDependency>for:bar</sharedDependency>
            </sharedDependencies>
        </configuration>
    </plugin>
