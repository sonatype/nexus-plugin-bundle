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

import org.codehaus.plexus.util.StringUtils;

/**
 * GAV configuration.
 *
 * @since 1.0
 */
public class GAVCoordinate
{
    public static final String DEFAULT_TYPE = "jar";

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String classifier;

    private final String type;

    private final boolean shared;

    public GAVCoordinate(final String groupId,
                         final String artifactId,
                         final String version,
                         final String classifier,
                         final String type,
                         final boolean shared)
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;

        if (!StringUtils.isEmpty(classifier)) {
            this.classifier = classifier;
        }
        else {
            this.classifier = null;
        }

        if (!StringUtils.isEmpty(type)) {
            this.type = type;
        }
        else {
            this.type = DEFAULT_TYPE;
        }

        this.shared = shared;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getType() {
        return type;
    }

    public boolean isShared() {
        return shared;
    }

    public String toCompositeForm() {
        StringBuilder buff = new StringBuilder();

        buff.append(String.valueOf(getGroupId()))
            .append(":")
            .append(String.valueOf(getArtifactId()))
            .append(":")
            .append(String.valueOf(getVersion()));

        if (!StringUtils.isEmpty(getClassifier())) {
            buff.append(":").append(getClassifier());
        }

        if (!StringUtils.isEmpty(getType()) && !StringUtils.equals(DEFAULT_TYPE, getType())) {
            if (StringUtils.isEmpty(getClassifier())) {
                buff.append(":");
            }

            buff.append(":").append(getType());
        }

        return buff.toString();
    }

    // ==

    public String toString() {
        return toCompositeForm();
    }

    public int hashCode() {
        int hash = 7;

        hash = 31 * hash + (getGroupId() != null ? getGroupId().hashCode() : 0);

        hash = 31 * hash + (getArtifactId() != null ? getArtifactId().hashCode() : 0);

        hash = 31 * hash + (getVersion() != null ? getVersion().hashCode() : 0);

        hash = 31 * hash + (getClassifier() != null ? getClassifier().hashCode() : 0);

        hash = 31 * hash + (getType() != null ? getType().hashCode() : 0);

        return hash;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

        GAVCoordinate other = (GAVCoordinate) obj;

        return StringUtils.equals(getGroupId(), other.getGroupId())
            && StringUtils.equals(getArtifactId(), other.getArtifactId())
            && StringUtils.equals(getVersion(), other.getVersion())
            && StringUtils.equals(getClassifier(), other.getClassifier())
            && StringUtils.equals(getType(), other.getType());
    }

}
