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
package org.sonatype.maven.plugin.app.descriptor;

import java.io.File;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * A simple command to present checksum of current repo.
 * 
 * @author cstamas
 */
public class GitRevParseCommand
    extends AbstractCommand
    implements GitCommand
{
    @Override
    protected GitRevParseScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                   CommandParameters parameters )
        throws ScmException
    {
        // wire
        Commandline cl = createCommandLine( (GitScmProviderRepository) repository, fileSet.getBasedir(), "HEAD" );

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        exitCode = GitCommandLineUtils.execute( cl, stdout, stderr, getLogger() );

        if ( exitCode != 0 )
        {
            return new GitRevParseScmResult( cl.toString(), "The git-rev-parse command failed.", stderr.getOutput(),
                false, null, null );
        }

        GitRevParseScmResult result =
            new GitRevParseScmResult( cl.toString(), "The git-rev-parse command succeeded.", stderr.getOutput(), true,
                StringUtils.chomp( stdout.getOutput() ), null );

        return result;
    }

    public static Commandline createCommandLine( GitScmProviderRepository repository, File workingDirectory,
                                                 String revPtr )
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory, "rev-parse" );

        cl.createArg().setValue( revPtr );

        return cl;
    }
}
