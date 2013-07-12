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

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.codehaus.plexus.util.StringUtils;

/**
 * This is Hg "id" command but with "--debug" switch, to force Hg show full checksum, not just the part of it as it
 * usually does.
 *
 * @since 1.0
 */
public class HgDebugIdCommand
    extends AbstractCommand
{
    @Override
    protected HgDebugIdScmResult executeCommand(final ScmProviderRepository repository,
                                                final ScmFileSet fileSet,
                                                final CommandParameters parameters)
        throws ScmException
    {
        HgOutputConsumer consumer;
        ScmResult result;

        consumer = new HgOutputConsumer(getLogger());
        result = HgUtils.execute(consumer, getLogger(), fileSet.getBasedir(), new String[]{"id", "-i", "--debug"});
        checkResult(result);

        String changeSetHash = consumer.getOutput();

        // trim off the possible "+"
        if (changeSetHash.endsWith("+")) {
            changeSetHash = changeSetHash.substring(0, changeSetHash.length() - 1);
        }

        consumer = new HgOutputConsumer(getLogger());
        result = HgUtils.execute(consumer, getLogger(), fileSet.getBasedir(), new String[]{
            "log",
            "-r",
            String.valueOf(changeSetHash),
            "--template",
            "\"{date|isodate}\""
        });
        checkResult(result);

        return new HgDebugIdScmResult(null, null, null, true, changeSetHash, consumer.getOutput());
    }

    private void checkResult(final ScmResult result)
        throws ScmException
    {
        if (!result.isSuccess()) {
            getLogger().debug("Provider message:");
            getLogger().debug(result.getProviderMessage() == null ? "" : result.getProviderMessage());
            getLogger().debug("Command output:");
            getLogger().debug(result.getCommandOutput() == null ? "" : result.getCommandOutput());
            throw new ScmException("Command failed: " + StringUtils.defaultString(result.getProviderMessage()));
        }
    }

    private static class HgOutputConsumer
        extends HgConsumer
    {
        private String output;

        private HgOutputConsumer(final ScmLogger logger) {
            super(logger);
        }

        public void doConsume(final ScmFileStatus status, final String line) {
            output = line;
        }

        private String getOutput() {
            return output;
        }
    }
}
