/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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

package org.sonatype.nexus.pluginbundle.maven.scm;

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
 * @since 1.0
 */
public class GitRevParseCommand
    extends AbstractCommand
    implements GitCommand
{
  @Override
  protected GitRevParseScmResult executeCommand(final ScmProviderRepository repository,
                                                final ScmFileSet fileSet,
                                                final CommandParameters parameters)
      throws ScmException
  {
    Commandline cl = createCommandLine((GitScmProviderRepository) repository, fileSet.getBasedir(), "HEAD");
    CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
    CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

    int exitCode = GitCommandLineUtils.execute(cl, stdout, stderr, getLogger());

    if (exitCode != 0) {
      return new GitRevParseScmResult(
          cl.toString(),
          "The git-rev-parse command failed.",
          stderr.getOutput(),
          false,
          null,
          null
      );
    }

    return new GitRevParseScmResult(
        cl.toString(),
        "The git-rev-parse command succeeded.",
        stderr.getOutput(),
        true,
        StringUtils.chomp(stdout.getOutput()),
        null
    );
  }

  public static Commandline createCommandLine(final GitScmProviderRepository repository,
                                              final File workingDirectory,
                                              final String revPtr)
  {
    Commandline cl = GitCommandLineUtils.getBaseGitCommandLine(workingDirectory, "rev-parse");

    cl.createArg().setValue(revPtr);

    return cl;
  }
}
