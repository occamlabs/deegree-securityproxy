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
package org.deegree.securityproxy.wms.request;

import org.deegree.securityproxy.authentication.ows.domain.OwsServiceVersion;
import org.deegree.securityproxy.request.OwsRequest;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Encapsulates a WMS request.
 *
 * @author <a href="stenger@lat-lon.de">Dirk Stenger</a>
 * @author last edited by: $Author: stenger $
 * @version $Revision: $, $Date: $
 */
public class WmsRequest implements OwsRequest {

    private final String operationType;

    private final OwsServiceVersion serviceVersion;

    private final List<String> layerNames;

    private final String serviceName;

    /**
     * Instantiates a new {@link WmsRequest} with an empty {@link List} of layer names.
     *
     * @param operationType  the type of the operation, never <code>null</code>
     * @param serviceVersion the version of the service, never <code>null</code>
     * @param serviceName    the name of the service, never <code>null</code>
     */
    public WmsRequest( String operationType, OwsServiceVersion serviceVersion, String serviceName ) {
        this( operationType, serviceVersion, Collections.<String>emptyList(), serviceName );
    }

    /**
     * Instantiates a new {@link WmsRequest} with an single layer name.
     *
     * @param operationType  the type of the operation, never <code>null</code>
     * @param serviceVersion the version of the service, never <code>null</code>
     * @param layerName      the name of the layer, never <code>null</code>
     * @param serviceName    the name of the service, never <code>null</code>
     */
    public WmsRequest( String operationType, OwsServiceVersion serviceVersion, String layerName, String serviceName ) {
        this( operationType, serviceVersion, singletonList( layerName ), serviceName );
    }

    /**
     * Instantiates a new {@link WmsRequest}.
     *
     * @param operationType  the type of the operation, never <code>null</code>
     * @param serviceVersion the version of the service, never <code>null</code>
     * @param layerNames     a {@link List} of layer names, may be empty but never <code>null</code>
     * @param serviceName    the name of the service, never <code>null</code>
     */
    public WmsRequest( String operationType, OwsServiceVersion serviceVersion, List<String> layerNames,
                       String serviceName ) {
        this.operationType = operationType;
        this.serviceVersion = serviceVersion;
        this.layerNames = layerNames;
        this.serviceName = serviceName;
    }

    /**
     * @return the operationType, never <code>null</code>
     */
    public String getOperationType() {
        return operationType;
    }

    /**
     * @return the serviceVersion, never <code>null</code>
     */
    public OwsServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * @return the layerNames as unmodifiable list, may be empty but never <code>null</code>
     */
    public List<String> getLayerNames() {
        return Collections.unmodifiableList( layerNames );
    }

    /**
     * @return the serviceName, never <code>null</code>
     */
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String toString() {
        return "WmsRequest [operationType=" + operationType + ", serviceVersion=" + serviceVersion + ", layerNames="
               + layerNames + ", serviceName=" + serviceName + "]";
    }

}