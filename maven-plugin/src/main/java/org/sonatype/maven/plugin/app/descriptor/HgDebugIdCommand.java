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
 * @author cstamas
 */
public class HgDebugIdCommand
    extends AbstractCommand
{
    @Override
    protected HgDebugIdScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                 CommandParameters parameters )
        throws ScmException
    {
        HgOutputConsumer consumer;
        ScmResult result;

        consumer = new HgOutputConsumer( getLogger() );
        result = HgUtils.execute( consumer, getLogger(), fileSet.getBasedir(), new String[] { "id", "-i", "--debug" } );
        checkResult( result );

        String changeSetHash = consumer.getOutput();

        // trim off the possible "+"
        if ( changeSetHash.endsWith( "+" ) )
        {
            changeSetHash = changeSetHash.substring( 0, changeSetHash.length() - 1 );
        }

        consumer = new HgOutputConsumer( getLogger() );
        result =
            HgUtils.execute( consumer, getLogger(), fileSet.getBasedir(),
                new String[] { "log", "-r", String.valueOf( changeSetHash ), "--template", "\"{date|isodate}\"" } );
        checkResult( result );

        final String changeSetDate = consumer.getOutput();

        return new HgDebugIdScmResult( null, null, null, true, changeSetHash, changeSetDate );
    }

    private void checkResult( ScmResult result )
        throws ScmException
    {
        if ( !result.isSuccess() )
        {
            getLogger().debug( "Provider message:" );
            getLogger().debug( result.getProviderMessage() == null ? "" : result.getProviderMessage() );
            getLogger().debug( "Command output:" );
            getLogger().debug( result.getCommandOutput() == null ? "" : result.getCommandOutput() );
            throw new ScmException( "Command failed." + StringUtils.defaultString( result.getProviderMessage() ) );
        }
    }

    private static class HgOutputConsumer
        extends HgConsumer
    {

        private String output;

        private HgOutputConsumer( ScmLogger logger )
        {
            super( logger );
        }

        public void doConsume( ScmFileStatus status, String line )
        {
            output = line;
        }

        private String getOutput()
        {
            return output;
        }
    }
}
