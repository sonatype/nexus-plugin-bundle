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
package org.sonatype.maven.plugin.app;

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
 * Utility methods to read and write a mapping document for non-plugin dependency artifacts used in an application
 * plugin.
 * 
 * @author jdcasey
 */
public final class ClasspathUtils
{

    private static final String CP_PROPSFILE = "classpath.properties";

    private ClasspathUtils()
    {
    }

    public static String formatArtifactKey( final Artifact artifact )
    {
        StringBuilder fname = new StringBuilder();

        // Supporting Aether format (see DefaultArtifact in aether-util):
        // <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
        fname.append( artifact.getGroupId() ).append( ":" ).append( artifact.getArtifactId() ).append( ':' ).append(
            artifact.getArtifactHandler().getExtension() );

        if ( !StringUtils.isBlank( artifact.getClassifier() ) )
        {
            fname.append( ':' ).append( artifact.getClassifier() );
        }

        fname.append( ":" ).append( artifact.getVersion() );

        return fname.toString();
    }

    private static org.sonatype.aether.artifact.Artifact formatArtifactFromKey( final String key,
                                                                                final Properties cpArtifacts )
    {
        return new DefaultArtifact( key ).setFile( new File( cpArtifacts.getProperty( key ) ) );
    }

    public static FileItem createFileItemForKey( final String key, final Properties cpArtifacts )
    {
        org.sonatype.aether.artifact.Artifact artifact = ClasspathUtils.formatArtifactFromKey( key, cpArtifacts );

        String sourcePath = artifact.getFile().getAbsolutePath(); // cpArtifacts.getProperty( destName );

        FileItem fi = new FileItem();

        fi.setSource( sourcePath );

        StringBuilder artifactFileName = new StringBuilder( artifact.getArtifactId() + "-" + artifact.getVersion() );

        if ( !StringUtils.isBlank( artifact.getClassifier() ) )
        {
            artifactFileName.append( '-' ).append( artifact.getClassifier() );
        }

        artifactFileName.append( "." ).append( artifact.getExtension() );

        fi.setDestName( artifactFileName.toString() );

        return fi;
    }

    public static Properties read( final MavenProject project )
        throws IOException
    {
        File cpFile = new File( project.getBuild().getDirectory(), CP_PROPSFILE );
        if ( !cpFile.exists() )
        {
            throw new IOException( "Cannot find: " + cpFile + ". Did you call 'generate-metadata'?" );
        }

        Properties p = new Properties();
        FileInputStream stream = null;
        try
        {
            stream = new FileInputStream( cpFile );
            p.load( stream );
        }
        finally
        {
            IOUtil.close( stream );
        }

        return p;
    }

    public static void write( final Set<Artifact> classpathArtifacts, final MavenProject project )
        throws IOException
    {
        Properties p = new Properties();

        for ( Artifact artifact : classpathArtifacts )
        {
            File artifactFile = artifact.getFile();

            String fname = formatArtifactKey( artifact );

            p.setProperty( fname, artifactFile.getAbsolutePath() );
        }

        File cpFile = new File( project.getBuild().getDirectory(), CP_PROPSFILE );
        FileOutputStream stream = null;
        try
        {
            cpFile.getParentFile().mkdirs();
            stream = new FileOutputStream( cpFile );

            p.store( stream, "Written on: " + new Date()
                + " (key format is <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>)" );
        }
        finally
        {
            IOUtil.close( stream );
        }
    }

}
