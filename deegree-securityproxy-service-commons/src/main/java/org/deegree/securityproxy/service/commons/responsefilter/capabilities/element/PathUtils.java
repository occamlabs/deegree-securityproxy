//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class PathUtils {

    /**
     * Checks if the passed path matches.
     * 
     * @param path
     *            to match, never <code>null</code>
     * @param visitedElements
     *            list of already visited elements, may be {@link PathUtils} empty but never <code>null</code>
     * @return <code>true</code> if the passed path matches, <code>false</code> otherwise
     */
    public static boolean isPathMatching( List<ElementPathStep> path, List<StartElement> visitedElements ) {
        if ( path != null ) {
            return evaluatePath( path, visitedElements );
        }
        return false;
    }

    private static boolean evaluatePath( List<ElementPathStep> path, List<StartElement> visitedElements ) {
        if ( path.size() != visitedElements.size() )
            return false;
        for ( int pathIndex = 0; pathIndex < path.size(); pathIndex++ ) {
            ElementPathStep ruleElementPathStep = path.get( pathIndex );
            StartElement visitedElement = visitedElements.get( pathIndex );
            if ( !( hasSameName( ruleElementPathStep, visitedElement ) && hasSameAttribute( ruleElementPathStep,
                                                                                            visitedElement ) ) )
                return false;
        }
        return true;
    }

    private static boolean hasSameName( ElementPathStep ruleElementPathStep, StartElement visitedElement ) {
        return ruleElementPathStep.getElementName().equals( visitedElement.getName() );
    }

    private static boolean hasSameAttribute( ElementPathStep ruleElementPathStep, StartElement visitedElement ) {
        QName stepAttributeName = ruleElementPathStep.getAttributeName();
        if ( stepAttributeName == null )
            return true;
        Attribute attributeByName = visitedElement.getAttributeByName( stepAttributeName );
        if ( attributeByName != null ) {
            return hasSameAttributeValueOrSteppAttributeValueIsNull( ruleElementPathStep, attributeByName );
        }
        return false;
    }

    private static boolean hasSameAttributeValueOrSteppAttributeValueIsNull( ElementPathStep ruleElementPathStep,
                                                                             Attribute attributeByName ) {
        if ( ruleElementPathStep.getAttributeValue() != null )
            return ruleElementPathStep.getAttributeValue().equals( attributeByName.getValue() );
        return true;
    }

}
