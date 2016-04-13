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

package org.sonatype.nexus.pluginbundle.maven;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map.Entry;

import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plugins.model.ClasspathDependency;
import org.sonatype.plugins.model.PluginDependency;
import org.sonatype.plugins.model.PluginLicense;
import org.sonatype.plugins.model.PluginMetadata;
import org.sonatype.plugins.model.io.xpp3.PluginModelXpp3Writer;

import org.codehaus.plexus.util.IOUtil;

/**
 * Generates nexus plugin descriptor XML file.
 *
 * @since 1.0
 */
public class PluginDescriptorGenerator
{
  public final static String MODEL_ENCODING = "UTF-8";

  public static final String APPLICATION_ID = "nexus";

  private final BuildContext buildContext;

  public PluginDescriptorGenerator(final BuildContext buildContext) {
    this.buildContext = buildContext;
  }

  public void generate(final PluginDescriptorGenerationRequest request) throws IOException {
    PluginMetadata metadata = new PluginMetadata();

    request.setPluginMetadata(metadata);

    metadata.setModelEncoding(MODEL_ENCODING);
    metadata.setApplicationId(APPLICATION_ID);

    metadata.setGroupId(request.getGroupId());
    metadata.setArtifactId(request.getArtifactId());
    metadata.setVersion(request.getVersion());
    metadata.setName(request.getName());
    metadata.setDescription(request.getDescription());
    metadata.setPluginSite(request.getPluginSiteURL());

    metadata.setScmUri(request.getScmUrl());
    metadata.setScmVersion(request.getScmVersion());
    metadata.setScmTimestamp(request.getScmTimestamp());

    if (request.getLicenses() != null) {
      for (Entry<String, String> licenseEntry : request.getLicenses().entrySet()) {
        PluginLicense entry = new PluginLicense();
        entry.setType(licenseEntry.getKey());
        entry.setUrl(licenseEntry.getValue());
        metadata.addLicense(entry);
      }
    }

    if (request.getClasspathDependencies() != null) {
      for (GAVCoordinate dependency : request.getClasspathDependencies()) {
        ClasspathDependency entry = new ClasspathDependency();
        entry.setGroupId(dependency.getGroupId());
        entry.setArtifactId(dependency.getArtifactId());
        entry.setVersion(dependency.getVersion());
        entry.setClassifier(dependency.getClassifier());
        entry.setType(dependency.getType());
        entry.setShared(dependency.isShared());
        metadata.addClasspathDependency(entry);
      }
    }

    if (request.getPluginDependencies() != null) {
      for (GAVCoordinate dependency : request.getPluginDependencies()) {
        PluginDependency entry = new PluginDependency();
        entry.setGroupId(dependency.getGroupId());
        entry.setArtifactId(dependency.getArtifactId());
        entry.setVersion(dependency.getVersion());
        entry.setOptional(dependency.isOptional());
        metadata.addPluginDependency(entry);
      }
    }

    if (request.getOutputFile() != null) {
      write(metadata, request.getOutputFile());
    }
  }

  private void write(final PluginMetadata metadata, final File outputFile) throws IOException {
    outputFile.getParentFile().mkdirs();
    Writer output = null;
    try {
      output = new BufferedWriter(new OutputStreamWriter(buildContext.newFileOutputStream(outputFile), MODEL_ENCODING));
      PluginModelXpp3Writer writer = new PluginModelXpp3Writer();
      writer.write(output, metadata);
    }
    finally {
      IOUtil.close(output);
    }
  }
}
