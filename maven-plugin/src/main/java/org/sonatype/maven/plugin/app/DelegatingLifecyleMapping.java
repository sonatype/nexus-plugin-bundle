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
package org.sonatype.maven.plugin.app;


import org.apache.maven.lifecycle.mapping.DefaultLifecycleMapping;
import org.apache.maven.lifecycle.mapping.LifecycleMapping;

import java.util.List;
import java.util.Map;

/**
 * Maven {@link LifecycleMapping} implementation which delegates to another {@link LifecycleMapping} instance. This
 * allows the aliasing of one mapping by another. In our case, it allows us to specify an abstract lifecycle mapping for
 * application plugin builds, then reference the abstract case with an application-specific packaging name.
 * 
 * @author jdcasey
 * 
 */
public class DelegatingLifecyleMapping
    extends DefaultLifecycleMapping
{

    private LifecycleMapping delegate;

    @SuppressWarnings( "unchecked" )
    @Override
    public List getOptionalMojos( final String lifecycle )
    {
        return delegate.getOptionalMojos( lifecycle );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public Map getPhases( final String lifecycle )
    {
        return delegate.getPhases( lifecycle );
    }

}
