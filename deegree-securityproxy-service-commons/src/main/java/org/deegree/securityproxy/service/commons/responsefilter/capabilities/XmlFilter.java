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
package org.deegree.securityproxy.service.commons.responsefilter.capabilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;
import org.deegree.securityproxy.filter.StatusCodeResponseBodyWrapper;

/**
 * Filters capabilities documents.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class XmlFilter {

    private static final Logger LOG = Logger.getLogger( XmlFilter.class );

    /**
     * Filters the incoming response.
     * 
     * @param servletResponse
     *            containing the response to filter, never <code>null</code>
     * @param xmlModifier
     *            decides if elements should be written or not, never <code>null</code>
     * @throws IOException
     *             if an error occurred during stream handling
     * @throws XMLStreamException
     *             if an error occurred during reading or writing the response
     */
    public void filterXml( StatusCodeResponseBodyWrapper servletResponse, XmlModificationManager xmlModifier )
                            throws IOException, XMLStreamException {
        BufferingXMLEventReader reader = null;
        XMLEventWriter writer = null;
        try {
            reader = createReader( servletResponse );
            writer = createWriter( servletResponse );

            copyResponse( reader, writer, xmlModifier );

        } finally {
            closeQuietly( reader );
            closeQuietly( writer );
        }
    }

    private void copyResponse( BufferingXMLEventReader reader, XMLEventWriter writer, XmlModificationManager xmlModifier )
                            throws XMLStreamException {
        LinkedList<StartElement> visitedElements = new LinkedList<StartElement>();
        while ( reader.hasNext() ) {
            XMLEvent currentEvent = reader.nextEvent();
            if ( currentEvent.isStartElement() ) {
                processStartElement( reader, writer, currentEvent, xmlModifier, visitedElements );
                visitedElements.add( currentEvent.asStartElement() );
            } else {
                if ( currentEvent.isEndElement() )
                    visitedElements.removeLast();
                writer.add( currentEvent );
            }
        }
    }

    private void processStartElement( BufferingXMLEventReader reader, XMLEventWriter writer, XMLEvent currentEvent,
                                      XmlModificationManager xmlModifier, LinkedList<StartElement> visitedElements )
                            throws XMLStreamException {
        LOG.debug( "Found StartElement " + currentEvent );
        if ( ignoreElement( reader, currentEvent, xmlModifier, visitedElements ) ) {
            LOG.info( "Event " + currentEvent + " is ignored." );
            skipElementContent( reader );
            visitedElements.removeLast();
        } else {
            processAttributes( reader, writer, currentEvent.asStartElement(), xmlModifier, visitedElements );
        }
    }

    private void processAttributes( BufferingXMLEventReader reader, XMLEventWriter writer, StartElement startElement,
                                    XmlModificationManager xmlModifier, LinkedList<StartElement> visitedElements )
                            throws XMLStreamException {
        if ( xmlModifier != null ) {
            LOG.debug( "Handle Attribute of StartElement" + startElement );
            List<Attribute> allAttributes = new ArrayList<Attribute>();
            Iterator<?> originalAttributes = startElement.getAttributes();
            while ( originalAttributes.hasNext() ) {
                Attribute processedAttribute = processAttribute( reader, writer, startElement, xmlModifier,
                                                                 visitedElements, originalAttributes );
                allAttributes.add( processedAttribute );
            }
            XMLEventFactory eventFactory = XMLEventFactory.newFactory();
            StartElement copiedStartElement = eventFactory.createStartElement( startElement.getName(),
                                                                               allAttributes.iterator(),
                                                                               startElement.getNamespaces() );
            writer.add( copiedStartElement );
        } else {
            writer.add( startElement );
        }
    }

    private Attribute processAttribute( BufferingXMLEventReader reader, XMLEventWriter writer,
                                        StartElement startElement, XmlModificationManager xmlModifier,
                                        LinkedList<StartElement> visitedElements, Iterator<?> attributes )
                            throws XMLStreamException, FactoryConfigurationError {
        Attribute attribute = (Attribute) attributes.next();
        LOG.debug( "Found Attribute " + attribute );
        String newValue = xmlModifier.determineNewAttributeValue( reader, startElement, attribute, visitedElements );
        if ( newValue != null ) {
            LOG.debug( "New Attribute value " + newValue );
            XMLEventFactory eventFactory = XMLEventFactory.newFactory();
            Attribute newAttribute = eventFactory.createAttribute( attribute.getName(), newValue );
            return newAttribute;
        }
        LOG.debug( "Attribute does not require modification." );
        return attribute;
    }

    private boolean ignoreElement( BufferingXMLEventReader reader, XMLEvent currentEvent, XmlModificationManager xmlModifier,
                                   LinkedList<StartElement> visitedElements )
                            throws XMLStreamException {
        return xmlModifier != null && xmlModifier.ignore( reader, currentEvent, visitedElements );
    }

    private XMLEventWriter createWriter( StatusCodeResponseBodyWrapper servletResponse )
                            throws IOException, FactoryConfigurationError, XMLStreamException {
        ServletOutputStream filteredCapabilitiesStreamToWriteIn = servletResponse.getRealOutputStream();

        XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
        return outFactory.createXMLEventWriter( filteredCapabilitiesStreamToWriteIn );
    }

    private BufferingXMLEventReader createReader( StatusCodeResponseBodyWrapper servletResponse )
                            throws FactoryConfigurationError, XMLStreamException {
        InputStream originalCapabilities = servletResponse.getBufferedStream();
        XMLInputFactory inFactory = XMLInputFactory.newInstance();
        XMLEventReader reader = inFactory.createXMLEventReader( originalCapabilities );
        return new BufferingXMLEventReader( reader );
    }

    private void skipElementContent( XMLEventReader reader )
                            throws XMLStreamException {
        int depth = 0;
        while ( depth >= 0 ) {
            XMLEvent nextEvent = reader.nextEvent();
            if ( nextEvent.isStartElement() ) {
                depth++;
            } else if ( nextEvent.isEndElement() ) {
                depth--;
            }
        }
    }

    private void closeQuietly( XMLEventReader reader ) {
        try {
            if ( reader != null )
                reader.close();
        } catch ( XMLStreamException e ) {
            LOG.warn( "Reader could not be closed: " + e.getMessage() );
        }
    }

    private void closeQuietly( XMLEventWriter writer ) {
        try {
            if ( writer != null )
                writer.close();
        } catch ( XMLStreamException e ) {
            LOG.warn( "Reader could not be closed: " + e.getMessage() );
        }
    }

}