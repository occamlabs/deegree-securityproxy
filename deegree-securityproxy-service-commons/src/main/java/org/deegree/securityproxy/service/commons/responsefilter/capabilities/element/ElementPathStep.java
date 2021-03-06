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
package org.deegree.securityproxy.service.commons.responsefilter.capabilities.element;

import javax.xml.namespace.QName;

/**
 * Encapsulates an element, may contain one attribute and text to check as path hierarchy.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class ElementPathStep {

    private final QName elementName;

    private final String attributeValue;

    private final QName attributeName;

    /**
     * @param elementName
     *            never <code>null</code>
     */
    public ElementPathStep( QName elementName ) {
        this( elementName, null, null );
    }

    /**
     * 
     * @param elementName
     *            never <code>null</code>
     * @param attributeName
     *            may be <code>null</code>
     * @param attributeValue
     *            may be <code>null</code>, but not when attributeName is not <code>null</code>
     */
    public ElementPathStep( QName elementName, QName attributeName ) {
        this( elementName, attributeName, null );
    }

    /**
     * 
     * @param elementName
     *            never <code>null</code>
     * @param attributeName
     *            may be <code>null</code>
     * @param attributeValue
     *            may be <code>null</code>
     */
    public ElementPathStep( QName elementName, QName attributeName, String attributeValue ) {
        this.elementName = elementName;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    /**
     * @return the elementName never <code>null</code>
     */
    public QName getElementName() {
        return elementName;
    }

    /**
     * @return the attributeName may be <code>null</code>
     */
    public QName getAttributeName() {
        return attributeName;
    }

    /**
     * @return the attributeValue may be <code>null</code> if attributeName is <code>null</code>
     */
    public String getAttributeValue() {
        return attributeValue;
    }

}