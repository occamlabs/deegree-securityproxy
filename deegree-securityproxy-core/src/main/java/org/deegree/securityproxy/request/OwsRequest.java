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
package org.deegree.securityproxy.request;

/**
 * Encapsulates OWS request.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class OwsRequest {

    private final String serviceType;

    private final OwsServiceVersion serviceVersion;

    private final String operationType;

    /**
     * Instantiates a new {@link OwsRequest}.
     * 
     * @param operationType
     *            the type of the operation, never <code>null</code>
     * @param serviceVersion
     *            the version of the service, never <code>null</code>
     * @param serviceType
     *            the type of the service (wms, wcs, ...), never <code>null</code>
     */
    public OwsRequest( String serviceType, String operationType, OwsServiceVersion serviceVersion ) {
        this.operationType = operationType;
        this.serviceVersion = serviceVersion;
        this.serviceType = serviceType;
    }

    /**
     * @return the serviceType in lower cases(wms, wcs, ...), never <code>null</code>
     */
    public String getServiceType() {
        return serviceType.toLowerCase();
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

}