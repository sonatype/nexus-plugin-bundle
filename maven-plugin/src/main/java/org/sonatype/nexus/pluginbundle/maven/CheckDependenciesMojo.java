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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.apache.maven.plugins.annotations.LifecyclePhase.INITIALIZE;
import static org.apache.maven.plugins.annotations.ResolutionScope.RUNTIME;

/**
 * Check that any 'nexus-plugin' dependencies are specified with 'provided' scope.
 *
 * @since 1.0
 */
@Mojo(name = "check-dependencies", defaultPhase = INITIALIZE, requiresDependencyResolution = RUNTIME)
public class CheckDependenciesMojo
    extends MojoSupport
{
    public void execute() throws MojoExecutionException, MojoFailureException {
        // skip if wrong packaging
        if (!isNexusPluginPacakging()) {
            return;
        }

        Set<Artifact> dependencies = project.getDependencyArtifacts();

        // skip if no dependencies
        if (dependencies == null) {
            return;
        }

        List<String> failures = new ArrayList<String>();

        // find any nexus-plugin deps which are not scope=provided
        for (Artifact dep : dependencies) {
            if (NEXUS_PLUGIN.equals(dep.getArtifactHandler().getPackaging())) {
                if (!Artifact.SCOPE_PROVIDED.equals(dep.getScope())) {
                    failures.add(dep.getId());
                }
            }
        }

        if (failures.isEmpty()) {
            getLog().info("No dependency problems detected");
        }
        else {
            StringBuilder message = new StringBuilder();
            message.append("The following dependencies should be changed to use 'provided' scope:\n");

            for (String id : failures) {
                message.append("\n  - ").append(id);
            }

            throw new MojoExecutionException(message.toString());
        }
    }
}
