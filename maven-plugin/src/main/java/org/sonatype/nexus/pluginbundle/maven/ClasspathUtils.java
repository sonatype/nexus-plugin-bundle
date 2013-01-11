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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
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
    private static final String CP_PROPSFILE = "classpath.properties";

    private ClasspathUtils() {
        // empty
    }

    public static String formatArtifactKey(final Artifact artifact) {
        StringBuilder fname = new StringBuilder();

        // Supporting Aether format (see DefaultArtifact in aether-util):
        // <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
        fname.append(artifact.getGroupId()).append(":").append(artifact.getArtifactId()).append(':').append(
            artifact.getArtifactHandler().getExtension());

        if (!StringUtils.isBlank(artifact.getClassifier())) {
            fname.append(':').append(artifact.getClassifier());
        }

        fname.append(":").append(artifact.getVersion());

        return fname.toString();
    }

    private static org.sonatype.aether.artifact.Artifact formatArtifactFromKey(final String key,
                                                                               final Properties cpArtifacts)
    {
        return new DefaultArtifact(key).setFile(new File(cpArtifacts.getProperty(key)));
    }

    public static FileItem createFileItemForKey(final String key, final Properties cpArtifacts) {
        org.sonatype.aether.artifact.Artifact artifact = ClasspathUtils.formatArtifactFromKey(key, cpArtifacts);

        String sourcePath = artifact.getFile().getAbsolutePath(); // cpArtifacts.getProperty( destName );

        FileItem fi = new FileItem();

        fi.setSource(sourcePath);

        StringBuilder artifactFileName = new StringBuilder(artifact.getArtifactId() + "-" + artifact.getVersion());

        if (!StringUtils.isBlank(artifact.getClassifier())) {
            artifactFileName.append('-').append(artifact.getClassifier());
        }

        artifactFileName.append(".").append(artifact.getExtension());

        fi.setDestName(artifactFileName.toString());

        return fi;
    }

    public static Properties read(final MavenProject project)
        throws IOException
    {
        File cpFile = new File(project.getBuild().getDirectory(), CP_PROPSFILE);
        if (!cpFile.exists()) {
            throw new IOException("Cannot find: " + cpFile + ". Did you call 'generate-metadata'?");
        }

        Properties p = new Properties();
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(cpFile);
            p.load(stream);
        }
        finally {
            IOUtil.close(stream);
        }

        return p;
    }

    public static void write(final Set<Artifact> classpathArtifacts, final MavenProject project)
        throws IOException
    {
        Properties p = new Properties();

        for (Artifact artifact : classpathArtifacts) {
            File artifactFile = artifact.getFile();

            String fname = formatArtifactKey(artifact);

            p.setProperty(fname, artifactFile.getAbsolutePath());
        }

        File cpFile = new File(project.getBuild().getDirectory(), CP_PROPSFILE);
        FileOutputStream stream = null;
        try {
            cpFile.getParentFile().mkdirs();
            stream = new FileOutputStream(cpFile);

            p.store(stream, "Written on: " + new Date()
                + " (key format is <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>)");
        }
        finally {
            IOUtil.close(stream);
        }
    }

}
