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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Check that any application plugin dependencies are specified with 'provided' scope,
 * along with any dependencies that have a groupId that belongs in the application's core.
 *
 * Specifying these as 'provided' scope means that the plugin expects its runtime environment to provide them,
 * which in the case of core dependencies and other plugins, is appropriate.
 *
 * @goal check-dependencies
 * @phase initialize
 * @requiresDependencyResolution runtime
 *
 * @since 1.0
 */
public class CheckDependenciesMojo
    extends AbstractMojo
{
    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    private ApplicationInformation mapping = new NexusApplicationInformation();

    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException, MojoFailureException {
        Set<Artifact> dependencies = project.getDependencyArtifacts();

        if (dependencies != null) {
            List<String> failures = new ArrayList<String>();
            for (Artifact dep : dependencies) {
                if (Artifact.SCOPE_PROVIDED.equals(dep.getScope())) {
                    getLog().info("Found dependency with 'provided' scope: " + dep.getDependencyConflictId() + "; ignoring");
                    continue;
                }
                else if (Artifact.SCOPE_TEST.equals(dep.getScope())) {
                    getLog().info("Found dependency with 'test' scope: " + dep.getDependencyConflictId() + "; ignoring");
                    continue;
                }

                // TODO: Sort out WTF this is for and why its needed
                if (mapping.matchesCoreGroupIds(dep.getGroupId())
                    || mapping.getPluginPackaging().equals(dep.getArtifactHandler().getPackaging())) {
                    failures.add(dep.getId());
                }
            }

            if (!failures.isEmpty()) {
                StringBuilder message = new StringBuilder();
                message.append("The following dependencies should be changed to use 'provided' scope:\n");

                for (String id : failures) {
                    message.append("\n  - ").append(id);
                }

                throw new MojoExecutionException(message.toString());
            }
            else {
                getLog().info("All Nexus dependencies in this project seem to have correct scope");
            }
        }
    }
}
