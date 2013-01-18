# Usage

Goals of this plugin are not intended to be executed directly.  They are enabled via the __nexus-plugin__ packaging lifecycle.

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
