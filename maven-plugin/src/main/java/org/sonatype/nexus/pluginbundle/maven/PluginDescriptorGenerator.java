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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map.Entry;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.plugins.model.ClasspathDependency;
import org.sonatype.plugins.model.PluginDependency;
import org.sonatype.plugins.model.PluginLicense;
import org.sonatype.plugins.model.PluginMetadata;
import org.sonatype.plugins.model.io.xpp3.PluginModelXpp3Writer;

/**
 * ???
 *
 * @since 1.0
 */
public class PluginDescriptorGenerator
{
    private final static String MODEL_ENCODING = "UTF-8";

    public void generatePluginDescriptor(final PluginMetadataGenerationRequest request)
        throws IOException
    {
        final PluginMetadata pluginMetadata = new PluginMetadata();

        // put it to request
        request.setPluginMetadata(pluginMetadata);

        pluginMetadata.setModelEncoding(MODEL_ENCODING);

        pluginMetadata.setGroupId(request.getGroupId());
        pluginMetadata.setArtifactId(request.getArtifactId());
        pluginMetadata.setVersion(request.getVersion());
        pluginMetadata.setName(request.getName());
        pluginMetadata.setDescription(request.getDescription());
        pluginMetadata.setPluginSite(request.getPluginSiteURL());

        pluginMetadata.setApplicationId(request.getApplicationId());
        pluginMetadata.setApplicationEdition(request.getApplicationEdition());
        pluginMetadata.setApplicationMinVersion(request.getApplicationMinVersion());
        pluginMetadata.setApplicationMaxVersion(request.getApplicationMaxVersion());

        pluginMetadata.setScmUri(request.getScmUrl());
        pluginMetadata.setScmVersion(request.getScmVersion());
        pluginMetadata.setScmTimestamp(request.getScmTimestamp());

        // set the licenses
        if (request.getLicenses() != null) {
            for (Entry<String, String> licenseEntry : request.getLicenses().entrySet()) {
                PluginLicense license = new PluginLicense();
                license.setType(licenseEntry.getKey());
                license.setUrl(licenseEntry.getValue());
            }
        }

        // set the dependencies
        if (request.getClasspathDependencies() != null) {
            for (GAVCoordinate dependency : request.getClasspathDependencies()) {
                ClasspathDependency classpathDependency = new ClasspathDependency();
                classpathDependency.setGroupId(dependency.getGroupId());
                classpathDependency.setArtifactId(dependency.getArtifactId());
                classpathDependency.setVersion(dependency.getVersion());
                classpathDependency.setClassifier(dependency.getClassifier());
                classpathDependency.setType(dependency.getType());
                classpathDependency.setShared(dependency.isShared());

                pluginMetadata.addClasspathDependency(classpathDependency);
            }
        }

        if (request.getPluginDependencies() != null) {
            for (GAVCoordinate dependency : request.getPluginDependencies()) {
                PluginDependency pluginDependency = new PluginDependency();
                pluginDependency.setGroupId(dependency.getGroupId());
                pluginDependency.setArtifactId(dependency.getArtifactId());
                pluginDependency.setVersion(dependency.getVersion());

                pluginMetadata.addPluginDependency(pluginDependency);
            }
        }

        if (request.getOutputFile() != null) {
            // write file
            writePluginMetadata(pluginMetadata, request.getOutputFile());
        }

    }

    private void writePluginMetadata(final PluginMetadata pluginMetadata, final File outputFile)
        throws IOException
    {
        // make sure the file's parent is created
        outputFile.getParentFile().mkdirs();
        FileOutputStream fos = null;
        OutputStreamWriter streamWriter = null;

        // FIXME: Buffer

        try {
            fos = new FileOutputStream(outputFile);
            streamWriter = new OutputStreamWriter(fos, MODEL_ENCODING);

            PluginModelXpp3Writer writer = new PluginModelXpp3Writer();
            writer.write(streamWriter, pluginMetadata);
        }
        finally {
            IOUtil.close(streamWriter);
            IOUtil.close(fos);
        }
    }
}
