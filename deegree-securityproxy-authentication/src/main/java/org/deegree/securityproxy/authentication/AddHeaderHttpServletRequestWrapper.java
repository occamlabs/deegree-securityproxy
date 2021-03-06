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
package org.deegree.securityproxy.authentication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * {@link HttpServletRequestWrapper} to add a new header.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class AddHeaderHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String> additionalHeader = new HashMap<String, String>();

    /**
     * @param request
     *            to wrap, never <code>null</code>
     */
    public AddHeaderHttpServletRequestWrapper( HttpServletRequest request ) {
        super( request );
    }

    public void addHeader( String name, String value ) {
        additionalHeader.put( name, value );
    }

    @Override
    public String getHeader( String name ) {
        if ( additionalHeader.containsKey( name ) )
            return additionalHeader.get( name );
        return super.getHeader( name );
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getHeaderNames() {
        List<String> names = initHeaderNamesFrom();
        names.addAll( additionalHeader.keySet() );
        return Collections.enumeration( names );
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<String> initHeaderNamesFrom() {
        Enumeration originalHeaderNames = super.getHeaderNames();
        List<String> names = new ArrayList<String>();
        if ( originalHeaderNames != null )
            names.addAll( Collections.list( originalHeaderNames ) );
        return names;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getHeaders( String name ) {
        List<String> values = new ArrayList<String>();
        addOriginalHeader( name, values );
        addAdditionalHeader( name, values );
        return Collections.enumeration( values );
    }

    private void addAdditionalHeader( String name, List<String> values ) {
        String additionalHeaderValue = additionalHeader.get( name );
        if ( additionalHeaderValue != null )
            values.add( additionalHeaderValue );
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void addOriginalHeader( String name, List<String> values ) {
        Enumeration headers = super.getHeaders( name );
        if ( headers != null )
            values.addAll( Collections.list( headers ) );
    }

}