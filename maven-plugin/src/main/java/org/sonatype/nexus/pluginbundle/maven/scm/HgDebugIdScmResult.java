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
package org.sonatype.nexus.pluginbundle.maven.scm;

import org.apache.maven.scm.ScmResult;

/**
 * Result data from {@link HgDebugIdCommand}.
 *
 * @since 1.0
 */
public class HgDebugIdScmResult
    extends ScmResult
{
    private final String changeSetHash;

    private final String changeSetDate;

    public HgDebugIdScmResult(final String commandLine,
                              final String providerMessage,
                              final String commandOutput,
                              final boolean success,
                              final String changeSetHash,
                              final String changeSetDate)
    {
        super(commandLine, providerMessage, commandOutput, success);

        this.changeSetHash = changeSetHash;
        this.changeSetDate = changeSetDate;
    }

    public String getChangeSetHash() {
        return changeSetHash;
    }

    public String getChangeSetDate() {
        return changeSetDate;
    }
}
