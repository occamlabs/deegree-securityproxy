package org.deegree.securityproxy.wms;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.deegree.securityproxy.authorization.RequestAuthorizationManager;
import org.deegree.securityproxy.filter.StatusCodeResponseBodyWrapper;
import org.deegree.securityproxy.request.OwsRequest;
import org.deegree.securityproxy.request.OwsRequestParser;
import org.deegree.securityproxy.responsefilter.ResponseFilterManager;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;


/**
 * @author <a href="wanhoff@lat-lon.de">Jeronimo Wanhoff</a>
 * @author last edited by: $Author: stenger $
 * @version $Revision: $, $Date: $
 */
public class WmsServiceManagerTest {

    private WmsServiceManager wmsServiceManager;

    private OwsRequestParser parser;

    private RequestAuthorizationManager requestAuthorizationManager;

    private ResponseFilterManager filterManager;

    @Before
    public void reset() {
        parser = mockOwsRequestParser();
        requestAuthorizationManager = mockRequestAuthorizationManager();
        filterManager = mockResponseFilterManager();
        wmsServiceManager = new WmsServiceManager( parser, requestAuthorizationManager, filterManager );
    }

    @Test
    public void testParse()
            throws Exception {
        HttpServletRequest request = mockHttpServletRequest();
        wmsServiceManager.parse(request);

        verify(parser).parse(request);
    }

    @Test
    public void testAuthorize()
            throws Exception {
        Authentication authentication = mockAuthentication();
        OwsRequest owsRequest = mockOwsRequest();
        wmsServiceManager.authorize(authentication, owsRequest);

        verify(requestAuthorizationManager).decide(authentication, owsRequest);
    }

    @Test
    public void testIsResponseFilterEnabled()
            throws Exception {
        OwsRequest owsRequest = mockOwsRequest();
        wmsServiceManager.isResponseFilterEnabled(owsRequest);

        verify(filterManager).supports(owsRequest.getClass());
    }

    @Test
    public void testFilterResponse()
            throws Exception {
        StatusCodeResponseBodyWrapper wrappedResponse = mockStatusCodeResponseBodyWrapper();
        Authentication authentication = mockAuthentication();
        OwsRequest owsRequest = mockOwsRequest();
        wmsServiceManager.filterResponse(wrappedResponse, authentication, owsRequest);

        verify(filterManager).filterResponse(wrappedResponse, owsRequest, authentication);
    }

    @Test
    public void testIsServiceTypeSupportedWithWmsServiceParameterShouldReturnTrue()
            throws Exception {
        HttpServletRequest request = mockHttpServletRequestWithWmsServiceParameter();
        boolean isSupported = wmsServiceManager.isServiceTypeSupported(request);

        assertThat(isSupported, is(true));
    }

    @Test
    public void testIsServiceTypeSupportedWithWmsServiceParameterShouldReturnFalse()
            throws Exception {
        HttpServletRequest request = mockHttpServletRequestWithWmsServiceParameter();
        boolean isSupported = wmsServiceManager.isServiceTypeSupported(request);

        assertThat(isSupported, is(false));
    }

    private ResponseFilterManager mockResponseFilterManager() {
        return mock(ResponseFilterManager.class);
    }

    private RequestAuthorizationManager mockRequestAuthorizationManager() {
        return mock(RequestAuthorizationManager.class);
    }

    private OwsRequestParser mockOwsRequestParser() {
        return mock(OwsRequestParser.class);
    }

    private HttpServletRequest mockHttpServletRequest() {
        return mock(HttpServletRequest.class);
    }

    private OwsRequest mockOwsRequest() {
        return mock(OwsRequest.class);
    }

    private Authentication mockAuthentication() {
        return mock(Authentication.class);
    }

    private StatusCodeResponseBodyWrapper mockStatusCodeResponseBodyWrapper() {
        return mock(StatusCodeResponseBodyWrapper.class);
    }

    private HttpServletRequest mockHttpServletRequestWithWmsServiceParameter() {
        return mockHttpServletRequestWithServiceParameter("wms");
    }

    private HttpServletRequest mockHttpServletRequestWithServiceParameter(String serviceValue) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, String[]> kvpMap = createKvpMapWithServiceParameter(serviceValue);
        doReturn(kvpMap).when(request).getParameterMap();
        return request;
    }

    private Map<String, String[]> createKvpMapWithServiceParameter(String serviceValue) {
        Map<String, String[]> kvpMap = new HashMap<String, String[]>();
        String[] serviceTypes = {serviceValue};
        kvpMap.put("service", serviceTypes);
        return kvpMap;
    }


}
