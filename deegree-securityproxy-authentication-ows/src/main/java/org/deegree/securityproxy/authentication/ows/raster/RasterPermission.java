//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.securityproxy.authentication.ows.raster;

import static java.util.Collections.unmodifiableMap;

import java.util.Collections;
import java.util.Map;

import org.deegree.securityproxy.authentication.ows.domain.LimitedOwsServiceVersion;
import org.springframework.security.core.GrantedAuthority;

/**
 * Encapsulates a permission to access a secured WCS.
 * 
 * @author <a href="stenger@lat-lon.de">Dirk Stenger</a>
 * @author <a href="goltz@lat-lon.de">Lyn Goltz</a>
 * @author <a href="erben@lat-lon.de">Alexander Erben</a>
 * @author last edited by: $Author: erben $
 * @version $Revision: $, $Date: $
 */
public class RasterPermission implements GrantedAuthority {

    private static final long serialVersionUID = 5184855468635810194L;

    private final String serviceType;

    private final String operationType;

    private final LimitedOwsServiceVersion serviceVersion;

    private final String layerName;

    private final String serviceName;

    private final String internalServiceUrl;

    private final Map<String, String[]> additionalKeyValuePairs;

    public RasterPermission( String serviceType, String operationType, LimitedOwsServiceVersion serviceVersion,
                             String coverageName, String serviceName, String internalServiceUrl,
                             Map<String, String[]> additionalKeyValuePairs ) {
        this.serviceType = serviceType;
        this.operationType = operationType;
        this.serviceVersion = serviceVersion;
        this.layerName = coverageName;
        this.serviceName = serviceName;
        this.internalServiceUrl = internalServiceUrl;

        if ( additionalKeyValuePairs != null )
            this.additionalKeyValuePairs = unmodifiableMap( additionalKeyValuePairs );
        else
            this.additionalKeyValuePairs = unmodifiableMap( Collections.<String, String[]> emptyMap() );
    }

    @Override
    public String getAuthority() {
        // This permission cannot be represented as a String!
        // Spring Security requires the return value null in this case
        return null;
    }

    /**
     * @return the serviceType
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * @return the operationType
     */
    public String getOperationType() {
        return operationType;
    }

    /**
     * @return the serviceVersion
     */
    public LimitedOwsServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * @return the layerName
     */
    public String getLayerName() {
        return layerName;
    }

    /**
     * @return the serviceName
     */
    public String getServiceName() {
        return serviceName;
    }

    public String getInternalServiceUrl() {
        return internalServiceUrl;
    }

    /**
     * @return the additionalKeyValuePairs in a unmodifiable map, may be empty but never <code>null</code>
     */
    public Map<String, String[]> getAdditionalKeyValuePairs() {
        return additionalKeyValuePairs;
    }

}