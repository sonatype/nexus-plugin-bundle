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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.assembly.model.FileItem;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * Utility methods to read and write a mapping document for non-plugin dependency artifacts used in an application plugin.
 *
 * @since 1.0
 */
public final class ClasspathUtils
{
    /**
     * Where detected plugin classpath state is written.
     */
    private static final String CP_PROPSFILE = "nexus-plugin-bundle/plugin.classpath";

    private ClasspathUtils() {
        // empty
    }

    public static String formatArtifactKey(final Artifact artifact) {
        StringBuilder fname = new StringBuilder();

        // Supporting Aether format (see DefaultArtifact in aether-util):
        // <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
        fname.append(artifact.getGroupId())
            .append(":")
            .append(artifact.getArtifactId())
            .append(':')
            .append(artifact.getArtifactHandler().getExtension());

        if (!StringUtils.isBlank(artifact.getClassifier())) {
            fname.append(':').append(artifact.getClassifier());
        }

        fname.append(":").append(artifact.getVersion());

        return fname.toString();
    }

    private static org.sonatype.aether.artifact.Artifact formatArtifactFromKey(final String key, final Properties artifacts) {
        return new DefaultArtifact(key).setFile(new File(artifacts.getProperty(key)));
    }

    public static FileItem createFileItemForKey(final String key, final Properties artifacts) {
        org.sonatype.aether.artifact.Artifact artifact = ClasspathUtils.formatArtifactFromKey(key, artifacts);

        String sourcePath = artifact.getFile().getAbsolutePath(); // cpArtifacts.getProperty( destName );

        FileItem fileItem = new FileItem();
        fileItem.setSource(sourcePath);

        StringBuilder artifactFileName = new StringBuilder(artifact.getArtifactId() + "-" + artifact.getVersion());

        if (!StringUtils.isBlank(artifact.getClassifier())) {
            artifactFileName.append('-').append(artifact.getClassifier());
        }

        artifactFileName.append(".").append(artifact.getExtension());

        fileItem.setDestName(artifactFileName.toString());

        return fileItem;
    }

    public static Properties read(final MavenProject project)
        throws IOException
    {
        File file = new File(project.getBuild().getDirectory(), CP_PROPSFILE);
        if (!file.exists()) {
            throw new IOException("Cannot find: " + file + ". Did you call 'generate-metadata'?");
        }

        Properties props = new Properties();
        InputStream input = null;
        try {
            input = new BufferedInputStream(new FileInputStream(file));
            props.load(input);
        }
        finally {
            IOUtil.close(input);
        }

        return props;
    }

    public static void write(final Set<Artifact> classpathArtifacts, final MavenProject project) throws IOException {
        Properties props = new Properties();

        for (Artifact artifact : classpathArtifacts) {
            File file = artifact.getFile();
            String name = formatArtifactKey(artifact);
            props.setProperty(name, file.getAbsolutePath());
        }

        File file = new File(project.getBuild().getDirectory(), CP_PROPSFILE);
        OutputStream output = null;
        try {
            file.getParentFile().mkdirs();
            output = new BufferedOutputStream(new FileOutputStream(file));
            props.store(output, null);
        }
        finally {
            IOUtil.close(output);
        }
    }
}
