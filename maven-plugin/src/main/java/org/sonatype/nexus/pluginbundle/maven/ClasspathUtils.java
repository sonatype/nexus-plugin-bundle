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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.assembly.model.FileItem;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Utility methods to read and write a mapping document for non-plugin dependency artifacts used in an application plugin.
 *
 * @since 1.0
 */
public class ClasspathUtils
{
    /**
     * Where detected plugin classpath state is written (under project.build.directory).
     */
    private static final String FILE_NAME = "nexus-plugin-bundle/plugin.classpath";

    public static final String COLON = ":";

    public static final String DASH = "-";

    public static final String DOT = ".";

    private ClasspathUtils() {
        // empty
    }

    /**
     * {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}
     */
    public static String formatArtifactKey(final Artifact artifact) {
        StringBuilder buff = new StringBuilder();

        buff.append(artifact.getGroupId())
            .append(COLON)
            .append(artifact.getArtifactId())
            .append(COLON)
            .append(artifact.getArtifactHandler().getExtension());

        if (!StringUtils.isBlank(artifact.getClassifier())) {
            buff.append(COLON).append(artifact.getClassifier());
        }

        buff.append(COLON).append(artifact.getVersion());

        return buff.toString();
    }

    private static Artifact formatArtifactFromKey(final String key, final Properties artifacts) {
        Pattern p = Pattern.compile( "([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?:([^: ]+)" );
        Matcher m = p.matcher( key );
        if ( !m.matches() )
        {
            throw new IllegalArgumentException( "Bad artifact coordinates " + key
                    + ", expected format is <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>" );
        }
        String groupId = m.group( 1 );
        String artifactId = m.group( 2 );
        String extension = m.group( 4 );
        extension = (extension == null || extension.length() < 1 ) ? "jar" : extension ;
        String classifier = m.group( 6 );
        classifier = (classifier == null || classifier.length() < 1 ) ? "" : classifier ;
        String version = m.group( 7 );
        final DefaultArtifact result = new DefaultArtifact(groupId, artifactId, version, null, extension, classifier, null);
        result.setFile(new File(artifacts.getProperty(key)));
        return result;
    }

    public static FileItem createFileItemForKey(final String key, final Properties artifacts) {
        Artifact artifact = ClasspathUtils.formatArtifactFromKey(key, artifacts);

        String sourcePath = artifact.getFile().getAbsolutePath();

        FileItem fileItem = new FileItem();
        fileItem.setSource(sourcePath);

        StringBuilder buff = new StringBuilder();

        buff.append(artifact.getArtifactId())
            .append(DASH)
            .append(artifact.getVersion());

        if (!StringUtils.isBlank(artifact.getClassifier())) {
            buff.append(DASH).append(artifact.getClassifier());
        }

        buff.append(DOT).append(artifact.getType());

        fileItem.setDestName(buff.toString());

        return fileItem;
    }

    public static Properties read(final MavenProject project)
        throws IOException
    {
        File file = new File(project.getBuild().getDirectory(), FILE_NAME);
        if (!file.exists()) {
            throw new IOException("Missing classpath file: " + file.getAbsolutePath());
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

    public static void write(final BuildContext buildContext, final Set<Artifact> classpathArtifacts, final MavenProject project) throws IOException {
        Properties props = new Properties();

        for (Artifact artifact : classpathArtifacts) {
            File file = artifact.getFile();
            String name = formatArtifactKey(artifact);
            props.setProperty(name, file.getAbsolutePath());
        }

        File file = new File(project.getBuild().getDirectory(), FILE_NAME);
        file.getParentFile().mkdirs();

        OutputStream output = null;
        try {
            output = new BufferedOutputStream(buildContext.newFileOutputStream(file));
            props.store(output, null);
        }
        finally {
            IOUtil.close(output);
        }
    }
}
