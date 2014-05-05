package org.deegree.securityproxy.wfs;

import org.deegree.securityproxy.authorization.logging.AuthorizationReport;
import org.deegree.securityproxy.exception.ServiceExceptionManager;
import org.deegree.securityproxy.exception.ServiceExceptionWrapper;
import org.deegree.securityproxy.filter.ServiceManager;
import org.deegree.securityproxy.filter.StatusCodeResponseBodyWrapper;
import org.deegree.securityproxy.request.KvpNormalizer;
import org.deegree.securityproxy.request.OwsRequest;
import org.deegree.securityproxy.request.OwsRequestParser;
import org.deegree.securityproxy.request.UnsupportedRequestTypeException;
import org.deegree.securityproxy.responsefilter.ResponseFilterException;
import org.deegree.securityproxy.responsefilter.ResponseFilterManager;
import org.deegree.securityproxy.responsefilter.logging.DefaultResponseFilterReport;
import org.deegree.securityproxy.responsefilter.logging.ResponseFilterReport;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * This is an implementation of a {@link ServiceManager} for wfs requests. It contains a wfs specific parser, filter
 * manager and a service exception wrapper.
 * 
 * @author <a href="stenger@lat-lon.de">Dirk Stenger</a>
 * @author last edited by: $Author: stenger $
 * @version $Revision: $, $Date: $
 */
public class WfsServiceManager implements ServiceManager, ServiceExceptionManager {

    private final OwsRequestParser parser;

    private final List<ResponseFilterManager> filterManagers;

    private final ServiceExceptionWrapper serviceExceptionWrapper;

    public WfsServiceManager( OwsRequestParser parser, List<ResponseFilterManager> filterManagers,
                              ServiceExceptionWrapper serviceExceptionWrapper ) {
        this.parser = parser;
        this.filterManagers = filterManagers;
        if ( serviceExceptionWrapper != null )
            this.serviceExceptionWrapper = serviceExceptionWrapper;
        else
            this.serviceExceptionWrapper = new ServiceExceptionWrapper();
    }

    @Override
    public OwsRequest parse( HttpServletRequest httpRequest )
                            throws UnsupportedRequestTypeException {
        return parser.parse( httpRequest );
    }

    @Override
    public AuthorizationReport authorize( Authentication authentication, OwsRequest owsRequest ) {
        return new AuthorizationReport();
    }

    @Override
    public boolean isResponseFilterEnabled( OwsRequest owsRequest ) {
        for ( ResponseFilterManager filterManager : filterManagers ) {
            if ( filterManager.canBeFiltered( owsRequest ) )
                return true;
        }
        return false;
    }

    @Override
    public ResponseFilterReport filterResponse( StatusCodeResponseBodyWrapper wrappedResponse,
                                                Authentication authentication, OwsRequest owsRequest )
                            throws ResponseFilterException {
        for ( ResponseFilterManager filterManager : filterManagers ) {
            if ( filterManager.canBeFiltered( owsRequest ) )
                return filterManager.filterResponse( wrappedResponse, owsRequest, authentication );
        }
        return createEmptyFilterReport();
    }

    @Override
    public ServiceExceptionWrapper retrieveServiceExceptionWrapper() {
        return serviceExceptionWrapper;
    }

    @Override
    public boolean isServiceTypeSupported( HttpServletRequest request ) {
        @SuppressWarnings("unchecked")
        Map<String, String[]> kvpMap = KvpNormalizer.normalizeKvpMap( request.getParameterMap() );
        return "wfs".equalsIgnoreCase( kvpMap.get( "service" )[0] );
    }

    private ResponseFilterReport createEmptyFilterReport() {
        return new DefaultResponseFilterReport( "Response was not filtered! No response filter manager was found!" );
    }

}
