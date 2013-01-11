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
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.pluginbundle.maven.scm.GitRevParseCommand;
import org.sonatype.nexus.pluginbundle.maven.scm.GitRevParseScmResult;
import org.sonatype.nexus.pluginbundle.maven.scm.HgDebugIdCommand;
import org.sonatype.nexus.pluginbundle.maven.scm.HgDebugIdScmResult;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates a plugins {@code plugin.xml} descriptor file based on the project's pom and class annotations.
 *
 * @goal generate-metadata
 * @phase process-classes
 * @requiresDependencyResolution test
 *
 * @since 1.0
 */
public class PluginDescriptorMojo
    extends AbstractMojo
{
    /**
     * The output location for the generated plugin descriptor.
     *
     * @parameter
     */
    private File generatedPluginMetadata;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject mavenProject;

    /**
     * The ID of the target application. For example if this plugin was for the Nexus Repository Manager, the ID would be, 'nexus'.
     *
     * @parameter
     */
    private String applicationId;

    /**
     * The edition of the target application. Some applications come in multiple flavors, OSS, PRO, Free, light, etc.
     *
     * @parameter expression="OSS"
     */
    private String applicationEdition;

    /**
     * The minimum product version of the target application.
     *
     * @parameter
     */
    private String applicationMinVersion;

    /**
     * The maximum product version of the target application.
     *
     * @parameter
     */
    private String applicationMaxVersion;

    private ApplicationInformation mapping = new NexusApplicationInformation();

    /**
     * @parameter expression="${project.scm.developerConnection}"
     * @readonly
     */
    private String urlScm;

    /**
     * The username that is used when connecting to the SCM system.
     *
     * @parameter expression="${username}"
     */
    private String username;

    /**
     * The password that is used when connecting to the SCM system.
     *
     * @parameter expression="${password}"
     */
    private String password;

    /**
     * @component
     */
    private ScmManager scmManager;

    /**
     * The list of classpath dependencies to be excluded from bundling for some reason (for example because you are
     * shading it into plugin artifact).
     *
     * @parameter
     */
    private List<String> classpathDependencyExcludes;

    /**
     * A list of groupId:artifactId references to non-plugin dependencies that should be shared along with main plugin
     * JAR to dependants of this plugin.
     *
     * @parameter
     */
    private List<String> sharedDependencies;

    /**
     * Configures the plugin name.  Defaults to maven project name.
     *
     * @parameter expression="${pluginName}"
     */
    private String pluginName;

    /**
     * Configures the plugin description.  Defaults to maven project description.
     *
     * @parameter expression="${pluginDescription}"
     */
    private String pluginDescription;

    /**
     * Configures the plugin site URL.  Defaults to maven project URL.
     *
     * @parameter expression="${pluginSiteUrl}"
     */
    private String pluginSiteUrl;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!mavenProject.getPackaging().equals(mapping.getPluginPackaging())) {
            getLog().info("Project is not of packaging type: " + mapping.getPluginPackaging());
            return;
        }

        initConfig();

        PluginMetadataGenerationRequest request = new PluginMetadataGenerationRequest();
        request.setGroupId(mavenProject.getGroupId());
        request.setArtifactId(mavenProject.getArtifactId());
        request.setVersion(mavenProject.getVersion());

        request.setName(pluginName != null ? pluginName : mavenProject.getName());
        request.setDescription(pluginDescription != null ? pluginDescription : mavenProject.getDescription());
        request.setPluginSiteURL(pluginSiteUrl != null ? pluginSiteUrl : mavenProject.getUrl());

        request.setApplicationId(applicationId);
        request.setApplicationEdition(applicationEdition);
        request.setApplicationMinVersion(applicationMinVersion);
        request.setApplicationMaxVersion(applicationMaxVersion);

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

            artifactLoop:
            for (Artifact artifact : artifacts) {
                if (artifact.getType().equals(mapping.getPluginPackaging())) {
                    if (!Artifact.SCOPE_PROVIDED.equals(artifact.getScope())) {
                        throw new MojoFailureException(
                            "Plugin dependency \"" + artifact.getDependencyConflictId() + "\" must have the \"provided\" scope!");
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
                else if (Artifact.SCOPE_PROVIDED.equals(artifact.getScope())
                    || Artifact.SCOPE_TEST.equals(artifact.getScope())) {
                    excludedArtifactIds.add(artifact.getId());
                }
                else if ((Artifact.SCOPE_COMPILE.equals(artifact.getScope()) || Artifact.SCOPE_RUNTIME.equals(artifact.getScope()))
                    && (!mapping.matchesCoreGroupIds(artifact.getGroupId()))) {
                    if (artifact.getDependencyTrail() != null) {
                        for (String trailId : artifact.getDependencyTrail()) {
                            if (excludedArtifactIds.contains(trailId)) {
                                getLog().debug( "Dependency artifact: " + artifact.getId()
                                        + " is part of the transitive dependency set for a dependency with 'provided' or 'test' scope: "
                                        + trailId + "\nThis artifact will be excluded from the plugin classpath");
                                continue artifactLoop;
                            }
                        }
                    }

                    final String artifactKey = ClasspathUtils.formatArtifactKey(artifact);

                    if (!isExcluded(artifactKey)) {
                        final boolean isShared = sharedDependencies != null &&
                            sharedDependencies.contains(artifact.getGroupId() + ":" + artifact.getArtifactId());

                        // classpath dependencies uses baseVersion, and let PluginManager resolve them runtime
                        // this enables easy development turnaround, by not having recompiling the plugin to drop-in
                        // newer snapshot
                        request.addClasspathDependency(new GAVCoordinate(artifact.getGroupId(),
                            artifact.getArtifactId(), artifact.getBaseVersion(), artifact.getClassifier(),
                            artifact.getType(), isShared));
                        classpathArtifacts.add(artifact);
                    }
                    else {
                        getLog().info(
                            "Classpath dependency [" + artifactKey
                                + "] is excluded from plugin bundle by user configuration");
                    }
                }
            }
        }

        request.setOutputFile(generatedPluginMetadata);

        // do the work
        try {
            new PluginDescriptorGenerator().generatePluginDescriptor(request);
        }
        catch (IOException e) {
            throw new MojoFailureException("Failed to generate plugin xml file: " + e.getMessage(), e);
        }

        try {
            ClasspathUtils.write(classpathArtifacts, mavenProject);
        }
        catch (IOException e) {
            throw new MojoFailureException("Failed to generate classpath properties file: " + e.getMessage(), e);
        }
    }

    private void initConfig() throws MojoFailureException {
        if (generatedPluginMetadata == null) {
            try {
                generatedPluginMetadata = mapping.getPluginMetadataFile(mavenProject);
            }
            catch (InterpolationException e) {
                throw new MojoFailureException("Cannot calculate plugin metadata file location from expression: "
                    + mapping.getPluginMetadataPath(), e);
            }
        }

        applicationId = applicationId == null ? mapping.getApplicationId() : applicationId;
        applicationEdition = applicationEdition == null ? mapping.getApplicationEdition() : applicationEdition;
        applicationMinVersion = applicationMinVersion == null ? mapping.getApplicationMinVersion() : applicationMinVersion;
        applicationMaxVersion = applicationMaxVersion == null ? mapping.getApplicationMaxVersion() : applicationMaxVersion;
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

    protected void fillScmInfo(final PluginMetadataGenerationRequest request) {
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
            getLog().warn("Failed to get scm information: " + e.getMessage());
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

    protected void fillSvnScmInfo(final PluginMetadataGenerationRequest request, final ScmRepository repository) throws ScmException {
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

    protected void fillGitScmInfo(final PluginMetadataGenerationRequest request, final ScmRepository repository) throws ScmException {
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

    protected void fillHgScmInfo(final PluginMetadataGenerationRequest request, final ScmRepository repository) throws ScmException {
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