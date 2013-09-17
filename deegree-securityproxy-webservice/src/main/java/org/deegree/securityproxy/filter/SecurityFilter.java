package org.deegree.securityproxy.filter;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.securityproxy.authorization.logging.AuthorizationReport;
import org.deegree.securityproxy.authorization.wcs.RequestAuthorizationManager;
import org.deegree.securityproxy.logger.SecurityRequestResposeLogger;
import org.deegree.securityproxy.report.SecurityReport;
import org.deegree.securityproxy.request.OwsRequest;
import org.deegree.securityproxy.request.UnsupportedRequestTypeException;
import org.deegree.securityproxy.request.WcsRequestParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

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

    private static final String UNSUPPORTED_REQUEST_ERROR_MSG = "Could not parse request.";

    private static final String UNKNOWN_ERROR_MSG = "Unknown error. See application log for details.";

    @Autowired
    private RequestAuthorizationManager requestAuthorizationManager;

    @Autowired
    private WcsRequestParser parser;

    @Autowired
    private SecurityRequestResposeLogger proxyReportLogger;

    @Override
    public void init( FilterConfig filterConfig )
                            throws ServletException {
    }

    @Override
    public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain )
                            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        FilterResponseWrapper wrappedResponse = new FilterResponseWrapper( httpResponse );
        String uuid = createUuidHeader( wrappedResponse );
        AuthorizationReport authorizationReport;
        try {
            OwsRequest wcsRequest = parser.parse( httpRequest );
            authorizationReport = requestAuthorizationManager.decide( SecurityContextHolder.getContext().getAuthentication(),
                                                                      wcsRequest );
        } catch ( UnsupportedRequestTypeException e ) {
            authorizationReport = new AuthorizationReport( UNSUPPORTED_REQUEST_ERROR_MSG, false );
        } catch ( IllegalArgumentException e ) {
            authorizationReport = new AuthorizationReport( UNKNOWN_ERROR_MSG, false );
        }
        if ( authorizationReport.isAuthorized() ) {
            chain.doFilter( httpRequest, wrappedResponse );
        }
        handleAuthorizationReport( uuid, httpRequest, wrappedResponse, authorizationReport );
    }

    @Override
    public void destroy() {
    }

    /**
     * For testing purposes only. Set authorization manager manually.
     * 
     * @param requestAuthorizationManager
     */
    protected void setRequestAuthorizationManager( RequestAuthorizationManager requestAuthorizationManager ) {
        this.requestAuthorizationManager = requestAuthorizationManager;
    }

    private void handleAuthorizationReport( String uuid, HttpServletRequest httpRequest,
                                            FilterResponseWrapper wrappedResponse,
                                            AuthorizationReport authorizationReport ) {
        generateAndLogProxyReport( authorizationReport, uuid, httpRequest, wrappedResponse );
        if ( !authorizationReport.isAuthorized() ) {
            throw new AccessDeniedException( authorizationReport.getMessage() );
        }
    }

    private void generateAndLogProxyReport( AuthorizationReport authorizationReport, String uuid,
                                            HttpServletRequest request, FilterResponseWrapper response ) {
        int statusCode = response.getStatus();
        boolean isRequestSuccessful = SC_OK == statusCode ? true : false;
        String targetURI = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        String requestURL = queryString != null ? targetURI + "?" + queryString : targetURI;
        String message = "";
        if ( authorizationReport.getMessage() != null ) {
            message = authorizationReport.getMessage();
        }
        SecurityReport report = new SecurityReport( uuid, request.getRemoteAddr(), requestURL, isRequestSuccessful,
                                                    message );
        proxyReportLogger.logProxyReportInfo( report );
    }

    private String createUuidHeader( FilterResponseWrapper wrappedResponse ) {
        String uuid = UUID.randomUUID().toString();
        wrappedResponse.addHeader( "serial_uuid", uuid );
        return uuid;
    }

}
