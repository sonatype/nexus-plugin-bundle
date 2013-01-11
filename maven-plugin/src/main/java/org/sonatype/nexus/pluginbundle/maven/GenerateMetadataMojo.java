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
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.AbstractGitScmProvider;
import org.apache.maven.scm.provider.hg.HgScmProvider;
import org.apache.maven.scm.provider.svn.AbstractSvnScmProvider;
import org.apache.maven.scm.provider.svn.command.info.SvnInfoItem;
import org.apache.maven.scm.provider.svn.command.info.SvnInfoScmResult;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.pluginbundle.maven.scm.GitRevParseCommand;
import org.sonatype.nexus.pluginbundle.maven.scm.GitRevParseScmResult;
import org.sonatype.nexus.pluginbundle.maven.scm.HgDebugIdCommand;
import org.sonatype.nexus.pluginbundle.maven.scm.HgDebugIdScmResult;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.maven.artifact.Artifact.SCOPE_COMPILE;
import static org.apache.maven.artifact.Artifact.SCOPE_PROVIDED;
import static org.apache.maven.artifact.Artifact.SCOPE_RUNTIME;
import static org.apache.maven.artifact.Artifact.SCOPE_TEST;

/**
 * Generates a plugins {@code plugin.xml} descriptor file based on the project's pom and class annotations.
 *
 * @goal generate-metadata
 * @phase process-classes
 * @requiresDependencyResolution test
 * @since 1.0
 */
public class GenerateMetadataMojo
    extends AbstractMojo
{
    /**
     * @component
     */
    private ScmManager scmManager;

    /**
     * @parameter property="project"
     * @required
     * @readonly
     */
    private MavenProject mavenProject;

    /**
     * @parameter property="project.scm.developerConnection"
     * @readonly
     */
    private String urlScm;

    /**
     * The username that is used when connecting to the SCM system.
     *
     * @parameter property="username"
     */
    private String username;

    /**
     * The password that is used when connecting to the SCM system.
     *
     * @parameter property="password"
     */
    private String password;

    /**
     * The list of classpath dependencies to be excluded from bundling for some reason (for example because you are shading it into plugin artifact).
     *
     * @parameter
     */
    private List<String> classpathDependencyExcludes;

    /**
     * A list of groupId:artifactId references to non-plugin dependencies that should be shared along with main plugin JAR to dependants of this
     * plugin.
     *
     * @parameter
     */
    private List<String> sharedDependencies;

    // TODO: Consider using nexusPlugin.{name|description|siteUrl} for properties

    /**
     * Configures the plugin name.  Defaults to maven project name.
     *
     * @parameter property="pluginName"
     */
    private String pluginName;

    /**
     * Configures the plugin description.  Defaults to maven project description.
     *
     * @parameter property="pluginDescription"
     */
    private String pluginDescription;

    /**
     * Configures the plugin site URL.  Defaults to maven project URL.
     *
     * @parameter property="pluginSiteUrl"
     */
    private String pluginSiteUrl;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!mavenProject.getPackaging().equals("nexus-plugin")) {
            getLog().info("Project is not of packaging type: nexus-plugin");
            return;
        }

        PluginDescriptorGenerationRequest request = new PluginDescriptorGenerationRequest();
        request.setGroupId(mavenProject.getGroupId());
        request.setArtifactId(mavenProject.getArtifactId());
        request.setVersion(mavenProject.getVersion());

        request.setName(pluginName != null ? pluginName : mavenProject.getName());
        request.setDescription(pluginDescription != null ? pluginDescription : mavenProject.getDescription());
        request.setPluginSiteURL(pluginSiteUrl != null ? pluginSiteUrl : mavenProject.getUrl());

        // licenses
        if (mavenProject.getLicenses() != null) {
            for (License mavenLicenseModel : mavenProject.getLicenses()) {
                request.addLicense(mavenLicenseModel.getName(), mavenLicenseModel.getUrl());
            }
        }

        // scm information
        fillScmInfo(request);

        // dependencies
        List<Artifact> artifacts = mavenProject.getTestArtifacts();
        Set<Artifact> classpathArtifacts = new HashSet<Artifact>();
        if (artifacts != null) {
            Set<String> excludedArtifactIds = new HashSet<String>();

            // FIXME: Drop need for label, the following is already complex and hard to comprehend
            artifactLoop:
            for (Artifact artifact : artifacts) {
                if (artifact.getType().equals("nexus-plugin")) {
                    if (!SCOPE_PROVIDED.equals(artifact.getScope())) {
                        throw new MojoFailureException("Nexus plugin dependency must use 'provided' scope: " + artifact.getDependencyConflictId());
                    }

                    excludedArtifactIds.add(artifact.getId());

                    // plugin inter-dependencies will use baseVersion, and let PluginManager resolve them runtime
                    request.addPluginDependency(new GAVCoordinate(
                        artifact.getGroupId(),
                        artifact.getArtifactId(),
                        artifact.getBaseVersion(),
                        artifact.getClassifier(),
                        artifact.getType(),
                        false));
                }
                else if (SCOPE_PROVIDED.equals(artifact.getScope()) || SCOPE_TEST.equals(artifact.getScope())) {
                    excludedArtifactIds.add(artifact.getId());
                }
                else if (SCOPE_COMPILE.equals(artifact.getScope()) || SCOPE_RUNTIME.equals(artifact.getScope())) {
                    if (artifact.getDependencyTrail() != null) {
                        for (String trailId : artifact.getDependencyTrail()) {
                            if (excludedArtifactIds.contains(trailId)) {
                                getLog().debug(String.format(
                                    "Dependency artifact: %s is part of the transitive dependency set for a dependency with 'provided' or 'test' scope: %s\n" +
                                        "This artifact will be excluded from the plugin classpath", artifact.getId(), trailId));
                                continue artifactLoop;
                            }
                        }
                    }

                    final String artifactKey = ClasspathUtils.formatArtifactKey(artifact);

                    if (!isExcluded(artifactKey)) {
                        boolean isShared =
                            sharedDependencies != null && sharedDependencies.contains(artifact.getGroupId() + ":" + artifact.getArtifactId());

                        // classpath dependencies uses baseVersion, and let PluginManager resolve them runtime
                        // this enables easy development turnaround, by not having recompiling the plugin to drop-in newer snapshot
                        request.addClasspathDependency(new GAVCoordinate(
                            artifact.getGroupId(),
                            artifact.getArtifactId(),
                            artifact.getBaseVersion(),
                            artifact.getClassifier(),
                            artifact.getType(),
                            isShared));
                        classpathArtifacts.add(artifact);
                    }
                    else {
                        getLog().info("Classpath dependency excluded from plugin bundle by user configuration: " + artifactKey);
                    }
                }
            }
        }

        File outputDir = new File(mavenProject.getBuild().getOutputDirectory());
        request.setOutputFile(new File(outputDir, "META-INF/nexus/plugin.xml"));

        try {
            new PluginDescriptorGenerator().generate(request);
        }
        catch (Exception e) {
            throw new MojoFailureException("Failed to generate plugin metadata file: " + e, e);
        }

        try {
            ClasspathUtils.write(classpathArtifacts, mavenProject);
        }
        catch (Exception e) {
            throw new MojoFailureException("Failed to generate plugin classpath file: " + e, e);
        }
    }

    protected boolean isExcluded(final String key) {
        if (classpathDependencyExcludes == null) {
            return false;
        }

        for (String exclude : classpathDependencyExcludes) {
            if (key.startsWith(exclude)) {
                return true;
            }
        }

        return false;
    }

    // SCM

    protected void fillScmInfo(final PluginDescriptorGenerationRequest request) {
        try {
            final ScmRepository repository = getScmRepository();
            request.setScmUrl(urlScm);

            final String provider = repository.getProvider();

            if ("svn".equals(provider)) {
                fillSvnScmInfo(request, repository);
            }
            else if ("git".equals(provider)) {
                fillGitScmInfo(request, repository);
            }
            else if ("hg".equals(provider)) {
                fillHgScmInfo(request, repository);
            }
        }
        catch (ScmException e) {
            getLog().warn("Failed to get SCM information: " + e.getMessage());
            getLog().debug(e);
        }
    }

    protected ScmRepository getScmRepository() throws ScmException {
        if (StringUtils.isEmpty(urlScm)) {
            throw new ScmException("No SCM URL found");
        }

        ScmRepository repository = scmManager.makeScmRepository(urlScm);

        ScmProviderRepository scmRepo = repository.getProviderRepository();

        if (!StringUtils.isEmpty(username)) {
            scmRepo.setUser(username);
        }

        if (!StringUtils.isEmpty(password)) {
            scmRepo.setPassword(password);
        }

        return repository;
    }

    protected void fillSvnScmInfo(final PluginDescriptorGenerationRequest request, final ScmRepository repository) throws ScmException {
        AbstractSvnScmProvider provider = (AbstractSvnScmProvider) scmManager.getProviderByType("svn");

        SvnInfoScmResult result = provider.info(
            repository.getProviderRepository(), new ScmFileSet(mavenProject.getBasedir()), null);

        if (!result.isSuccess()) {
            throw new ScmException(result.getCommandOutput());
        }

        SvnInfoItem info = (SvnInfoItem) result.getInfoItems().get(0);

        request.setScmVersion(info.getLastChangedRevision());
        request.setScmTimestamp(info.getLastChangedDate());
    }

    protected void fillGitScmInfo(final PluginDescriptorGenerationRequest request, final ScmRepository repository) throws ScmException {
        AbstractGitScmProvider provider = (AbstractGitScmProvider) scmManager.getProviderByType("git");

        GitRevParseCommand cmd = new GitRevParseCommand();

        cmd.setLogger(provider.getLogger());

        GitRevParseScmResult result = (GitRevParseScmResult)
            cmd.execute(repository.getProviderRepository(), new ScmFileSet(mavenProject.getBasedir()), null);

        if (!result.isSuccess()) {
            throw new ScmException(result.getCommandOutput());
        }

        request.setScmVersion(result.getChangeSetHash());
        request.setScmTimestamp(result.getChangeSetDate());
    }

    protected void fillHgScmInfo(final PluginDescriptorGenerationRequest request, final ScmRepository repository) throws ScmException {
        HgScmProvider provider = (HgScmProvider) scmManager.getProviderByType("hg");

        HgDebugIdCommand cmd = new HgDebugIdCommand();

        cmd.setLogger(provider.getLogger());

        HgDebugIdScmResult result = (HgDebugIdScmResult)
            cmd.execute(repository.getProviderRepository(), new ScmFileSet(mavenProject.getBasedir()), null);

        if (!result.isSuccess()) {
            throw new ScmException(result.getCommandOutput());
        }

        request.setScmVersion(result.getChangeSetHash());
        request.setScmTimestamp(result.getChangeSetDate());
    }
}