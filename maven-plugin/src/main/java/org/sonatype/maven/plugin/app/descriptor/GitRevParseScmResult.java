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

import org.apache.maven.scm.ScmResult;

public class GitRevParseScmResult
    extends ScmResult
{
    private final String changeSetHash;

    private final String changeSetDate;

    public GitRevParseScmResult( String commandLine, String providerMessage, String commandOutput, boolean success,
                                 String changeSetHash, String changeSetDate )
    {
        super( commandLine, providerMessage, commandOutput, success );

        this.changeSetHash = changeSetHash;

        this.changeSetDate = changeSetDate;
    }

    public String getChangeSetHash()
    {
        return changeSetHash;
    }

    public String getChangeSetDate()
    {
        return changeSetDate;
    }
}
