package org.deegree.securityproxy.filter;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.deegree.securityproxy.exception.OwsCommonException.INVALID_PARAMETER;
import static org.deegree.securityproxy.exception.OwsCommonException.MISSING_PARAMETER;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.deegree.securityproxy.authorization.logging.AuthorizationReport;
import org.deegree.securityproxy.exception.OwsServiceExceptionHandler;
import org.deegree.securityproxy.logger.ResponseFilterReportLogger;
import org.deegree.securityproxy.logger.SecurityRequestResponseLogger;
import org.deegree.securityproxy.report.SecurityReport;
import org.deegree.securityproxy.request.KvpNormalizer;
import org.deegree.securityproxy.request.MissingParameterException;
import org.deegree.securityproxy.request.OwsRequest;
import org.deegree.securityproxy.request.UnsupportedRequestTypeException;
import org.deegree.securityproxy.responsefilter.ResponseFilterException;
import org.deegree.securityproxy.responsefilter.logging.ResponseFilterReport;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

/**
 * Servlet Filter that logs all incoming requests and their response and performs access decision.
 * 
 * @author <a href="erben@lat-lon.de">Alexander Erben</a>
 * @author <a href="goltz@lat-lon.de">Lyn Goltz</a>
 * @author <a href="stenger@lat-lon.de">Dirk Stenger</a>
 * @author last edited by: $Author: erben $
 * 
 * @version $Revision: $, $Date: $
 */
public class SecurityFilter implements Filter {

    private static final Logger LOG = Logger.getLogger( SecurityFilter.class );

    private static final String UNSUPPORTED_REQUEST_ERROR_MSG = "Service type is not supported!";

    static final String REQUEST_ATTRIBUTE_SERVICE_URL = "net.sf.j2ep.serviceurl";

    private final List<ServiceManager> serviceManagers;

    private final SecurityRequestResponseLogger proxyReportLogger;

    private final ResponseFilterReportLogger filterReportLogger;

    private final OwsServiceExceptionHandler owsServiceExceptionHandler;

    public SecurityFilter( List<ServiceManager> serviceManagers, SecurityRequestResponseLogger proxyReportLogger,
                           ResponseFilterReportLogger filterReportLogger,
                           OwsServiceExceptionHandler owsServiceExceptionHandler ) {
        this.serviceManagers = serviceManagers;
        this.proxyReportLogger = proxyReportLogger;
        this.filterReportLogger = filterReportLogger;
        this.owsServiceExceptionHandler = owsServiceExceptionHandler;
    }

    @Override
    public void init( FilterConfig filterConfig )
                            throws ServletException {
    }

    @Override
    public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain )
                            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        StatusCodeResponseBodyWrapper wrappedResponse = new StatusCodeResponseBodyWrapper( httpResponse );
        String uuid = createUuidHeader( wrappedResponse );

        try {
            checkServiceType( servletRequest );
            ServiceManager serviceManager = detectServiceManager( httpRequest );
            handleAuthorization( chain, httpRequest, wrappedResponse, uuid, serviceManager );
        } catch ( UnsupportedRequestTypeException e ) {
            owsServiceExceptionHandler.writeException( wrappedResponse, INVALID_PARAMETER, "service" );
            generateAndLogProxyReport( e.getMessage(), uuid, httpRequest, wrappedResponse );
        } catch ( MissingParameterException e ) {
            owsServiceExceptionHandler.writeException( wrappedResponse, MISSING_PARAMETER, e.getParameterName() );
            generateAndLogProxyReport( e.getMessage(), uuid, httpRequest, wrappedResponse );
        }
    }

    @Override
    public void destroy() {
    }

    private void handleAuthorization( FilterChain chain, HttpServletRequest httpRequest,
                                      StatusCodeResponseBodyWrapper wrappedResponse, String uuid,
                                      ServiceManager serviceManager )
                            throws IOException, ServletException {
        AuthorizationReport authorizationReport;
        Authentication authentication = getContext().getAuthentication();
        OwsRequest owsRequest = null;
        try {
            owsRequest = serviceManager.parse( httpRequest );
            authorizationReport = serviceManager.authorize( authentication, owsRequest );
        } catch ( UnsupportedRequestTypeException e ) {
            authorizationReport = new AuthorizationReport( UNSUPPORTED_REQUEST_ERROR_MSG );
        } catch ( IllegalArgumentException e ) {
            authorizationReport = new AuthorizationReport( e.getMessage() );
        }
        if ( authorizationReport.isAuthorized() ) {
            attachServiceUrlAttributeToRequest( httpRequest, authorizationReport );
            Map<String, String[]> additionalKeyValuePairs = authorizationReport.getAdditionalKeyValuePairs();
            KvpRequestWrapper wrappedRequest = new KvpRequestWrapper( httpRequest, additionalKeyValuePairs );
            chain.doFilter( wrappedRequest, wrappedResponse );
            if ( serviceManager.isResponseFilterEnabled( owsRequest ) ) {
                filterResponse( wrappedResponse, uuid, authentication, owsRequest, serviceManager );
            } else {
                LOG.debug( "No filter configured for " + owsRequest.getClass() );
                wrappedResponse.copyBufferedStreamToRealStream();
            }
        }
        handleAuthorizationReport( uuid, httpRequest, wrappedResponse, authorizationReport );
    }

    private void filterResponse( StatusCodeResponseBodyWrapper wrappedResponse, String uuid,
                                 Authentication authentication, OwsRequest owsRequest, ServiceManager serviceManager )
                            throws ServletException {
        try {
            ResponseFilterReport filterResponse = serviceManager.filterResponse( wrappedResponse, authentication,
                                                                                 owsRequest );
            filterReportLogger.logResponseFilterReport( filterResponse, uuid );
            LOG.debug( "Filter was applied. Response: " + filterResponse.getMessage() );
        } catch ( ResponseFilterException e ) {
            LOG.error( "Response filtering failed. " + e.getMessage() );
            LOG.trace( "Response filtering failed!", e );
            throw new ServletException( "Response filtering failed", e );
        }
    }

    private void handleAuthorizationReport( String uuid, HttpServletRequest httpRequest,
                                            StatusCodeResponseBodyWrapper wrappedResponse,
                                            AuthorizationReport authorizationReport ) {
        generateAndLogProxyReport( authorizationReport, uuid, httpRequest, wrappedResponse );
        if ( !authorizationReport.isAuthorized() ) {
            throw new AccessDeniedException( authorizationReport.getMessage() );
        }
    }

    private void generateAndLogProxyReport( AuthorizationReport authorizationReport, String uuid,
                                            HttpServletRequest request, StatusCodeResponseBodyWrapper response ) {

        String message = "";
        if ( authorizationReport.getMessage() != null ) {
            message = authorizationReport.getMessage();
        }
        generateAndLogProxyReport( message, uuid, request, response );
    }

    private void generateAndLogProxyReport( String message, String uuid, HttpServletRequest request,
                                            StatusCodeResponseBodyWrapper response ) {
        int statusCode = response.getStatus();
        boolean isRequestSuccessful = SC_OK == statusCode;
        String targetURI = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        String requestURL = queryString != null ? targetURI + "?" + queryString : targetURI;
        SecurityReport report = new SecurityReport( request.getRemoteAddr(), requestURL, isRequestSuccessful, message );
        proxyReportLogger.logProxyReportInfo( report, uuid );
    }

    private String createUuidHeader( StatusCodeResponseBodyWrapper wrappedResponse ) {
        String uuid = UUID.randomUUID().toString();
        wrappedResponse.addHeader( "serial_uuid", uuid );
        return uuid;
    }

    private void attachServiceUrlAttributeToRequest( HttpServletRequest httpRequest,
                                                     AuthorizationReport authorizationReport ) {
        String serviceUrl = authorizationReport.getServiceUrl();
        if ( serviceUrl != null )
            httpRequest.setAttribute( REQUEST_ATTRIBUTE_SERVICE_URL, serviceUrl );
    }

    private void checkServiceType( ServletRequest request )
                            throws MissingParameterException {
        @SuppressWarnings("unchecked")
        Map<String, String[]> kvpMap = KvpNormalizer.normalizeKvpMap( request.getParameterMap() );
        String[] serviceTypes = kvpMap.get( "service" );
        if ( serviceTypes == null || serviceTypes.length < 1 )
            throw new MissingParameterException( "service" );
    }

    private ServiceManager detectServiceManager( HttpServletRequest request )
                            throws UnsupportedRequestTypeException {
        if ( serviceManagers != null ) {
            for ( ServiceManager serviceManager : serviceManagers ) {
                if ( serviceManager.isServiceTypeSupported( request ) )
                    return serviceManager;
            }
        }
        throw new UnsupportedRequestTypeException( UNSUPPORTED_REQUEST_ERROR_MSG );
    }

}