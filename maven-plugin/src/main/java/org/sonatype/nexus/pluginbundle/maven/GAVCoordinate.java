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

import javax.annotation.Nullable;

import org.codehaus.plexus.util.StringUtils;

/**
 * GAV configuration.
 *
 * @since 1.0
 */
public class GAVCoordinate
{
  public static final String DEFAULT_TYPE = "jar";

  public static final String COLON = ":";

  private final String groupId;

  private final String artifactId;

  private final String version;

  private final String classifier;

  private final String type;

  private final boolean optional;

  private final boolean shared;

  public GAVCoordinate(final String groupId,
                       final String artifactId,
                       final String version,
                       final @Nullable String classifier,
                       final @Nullable String type,
                       final boolean optional,
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

    this.optional = optional;
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

  public boolean isOptional() {
    return optional;
  }

  public boolean isShared() {
    return shared;
  }

  public String toString() {
    StringBuilder buff = new StringBuilder();

    buff.append(String.valueOf(groupId))
        .append(COLON)
        .append(String.valueOf(artifactId))
        .append(COLON)
        .append(String.valueOf(version));

    if (!StringUtils.isEmpty(classifier)) {
      buff.append(COLON).append(classifier);
    }

    if (!StringUtils.isEmpty(type) && !StringUtils.equals(DEFAULT_TYPE, type)) {
      if (StringUtils.isEmpty(classifier)) {
        buff.append(COLON);
      }

      buff.append(COLON).append(type);
    }

    return buff.toString();
  }

  public int hashCode() {
    int hash = 7;

    hash = 31 * hash + (groupId != null ? groupId.hashCode() : 0);
    hash = 31 * hash + (artifactId != null ? artifactId.hashCode() : 0);
    hash = 31 * hash + (version != null ? version.hashCode() : 0);
    hash = 31 * hash + (classifier != null ? classifier.hashCode() : 0);
    hash = 31 * hash + (type != null ? type.hashCode() : 0);

    return hash;
  }

  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if ((obj == null) || (obj.getClass() != this.getClass())) {
      return false;
    }

    GAVCoordinate other = (GAVCoordinate) obj;

    return StringUtils.equals(groupId, other.groupId)
        && StringUtils.equals(artifactId, other.artifactId)
        && StringUtils.equals(version, other.version)
        && StringUtils.equals(classifier, other.classifier)
        && StringUtils.equals(type, other.type);
  }
}
