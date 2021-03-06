package org.deegree.securityproxy.wcs.request;

import static java.util.Arrays.asList;
import static org.deegree.securityproxy.request.GetOwsRequestParserUtils.checkSingleRequiredParameter;
import static org.deegree.securityproxy.request.GetOwsRequestParserUtils.isNotSet;
import static org.deegree.securityproxy.request.GetOwsRequestParserUtils.isNotSingle;
import static org.deegree.securityproxy.request.GetOwsRequestParserUtils.throwException;
import static org.deegree.securityproxy.request.KvpNormalizer.normalizeKvpMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.deegree.securityproxy.request.OwsRequestParser;
import org.deegree.securityproxy.request.OwsServiceVersion;
import org.deegree.securityproxy.request.UnsupportedRequestTypeException;

/**
 * Parses an incoming {@link HttpServletRequest} into a {@link WcsRequest}.
 * 
 * @author <a href="erben@lat-lon.de">Alexander Erben</a>
 * @author last edited by: $Author: erben $
 * @version $Revision: $, $Date: $
 */
public class WcsRequestParser implements OwsRequestParser {

    private static final String REQUEST = "request";

    private static final String SERVICE = "service";

    private static final String VERSION = "version";

    private static final String COVERAGE = "coverage";

    private static final String CRS = "crs";

    private static final String BBOX = "bbox";

    private static final String TIME = "time";

    private static final String WIDTH = "width";

    private static final String HEIGHT = "height";

    private static final String RESX = "resx";

    private static final String RESY = "resy";

    private static final String FORMAT = "format";

    public static final OwsServiceVersion VERSION_100 = new OwsServiceVersion( 1, 0, 0 );

    public static final OwsServiceVersion VERSION_110 = new OwsServiceVersion( 1, 1, 0 );

    public static final OwsServiceVersion VERSION_200 = new OwsServiceVersion( 2, 0, 0 );

    public static final String GETCAPABILITIES = "GetCapabilities";

    public static final String DESCRIBECOVERAGE = "DescribeCoverage";

    public static final String GETCOVERAGE = "GetCoverage";

    private final List<OwsServiceVersion> supportedVersion = asList( VERSION_100, VERSION_110, VERSION_200 );

    @Override
    @SuppressWarnings("unchecked")
    public WcsRequest parse( HttpServletRequest request )
                            throws UnsupportedRequestTypeException {
        if ( request == null )
            throw new IllegalArgumentException( "Request must not be null!" );
        String serviceName = evaluateServiceName( request );
        Map<String, String[]> normalizedParameterMap = normalizeKvpMap( request.getParameterMap() );
        checkParameters( normalizedParameterMap );
        return parseRequest( serviceName, normalizedParameterMap );
    }

    private WcsRequest parseRequest( String serviceName, Map<String, String[]> normalizedParameterMap ) {
        String type = normalizedParameterMap.get( REQUEST )[0];
        if ( GETCAPABILITIES.equalsIgnoreCase( type ) )
            return parseGetCapabilitiesRequest( serviceName, normalizedParameterMap );
        if ( DESCRIBECOVERAGE.equalsIgnoreCase( type ) )
            return parseDescribeCoverageRequest( serviceName, normalizedParameterMap );
        if ( GETCOVERAGE.equalsIgnoreCase( type ) )
            return parseGetCoverageRequest( serviceName, normalizedParameterMap );
        throw new IllegalArgumentException( "Unrecognized operation type: " + type );
    }

    private WcsRequest parseGetCoverageRequest( String serviceName, Map<String, String[]> normalizedParameterMap ) {
        checkGetCoverageParameters( normalizedParameterMap );
        OwsServiceVersion version = evaluateVersion( normalizedParameterMap );
        String[] coverageParameter = normalizedParameterMap.get( COVERAGE );
        if ( isNotSet( coverageParameter ) )
            return new WcsRequest( GETCOVERAGE, version, serviceName );

        List<String> separatedCoverages = extractCoverages( coverageParameter );
        if ( separatedCoverages.size() != 1 )
            throw new IllegalArgumentException( "GetCoverage requires exactly one coverage parameter!" );
        return new WcsRequest( GETCOVERAGE, version, separatedCoverages.get( 0 ), serviceName );
    }

    private WcsRequest parseDescribeCoverageRequest( String serviceName, Map<String, String[]> normalizedParamMap ) {
        OwsServiceVersion version = evaluateVersion( normalizedParamMap );
        String[] coverageParameter = normalizedParamMap.get( COVERAGE );
        if ( isNotSet( coverageParameter ) )
            return new WcsRequest( DESCRIBECOVERAGE, version, serviceName );
        else {
            List<String> separatedCoverages = extractCoverages( coverageParameter );
            return new WcsRequest( DESCRIBECOVERAGE, version, separatedCoverages, serviceName );
        }
    }

    private List<String> extractCoverages( String[] coverageParameter ) {
        String firstCoverageParameter = coverageParameter[0];
        List<String> separatedCoverages = new ArrayList<String>();
        Collections.addAll( separatedCoverages, firstCoverageParameter.split( "," ) );
        return separatedCoverages;
    }

    private WcsRequest parseGetCapabilitiesRequest( String serviceName, Map<String, String[]> normalizedParameterMap ) {
        OwsServiceVersion version = evaluateVersion( normalizedParameterMap );
        return new WcsRequest( GETCAPABILITIES, version, serviceName );
    }

    private OwsServiceVersion evaluateVersion( Map<String, String[]> normalizedParameterMap ) {
        String[] versionParameters = normalizedParameterMap.get( VERSION );
        if ( versionParameters == null || versionParameters.length == 0 )
            return null;
        String versionParam = versionParameters[0];
        if ( versionParam != null && !versionParam.isEmpty() ) {
            OwsServiceVersion version = new OwsServiceVersion( versionParam );
            if ( supportedVersion.contains( version ) )
                return version;
        }
        throw new IllegalArgumentException( "Unrecognized version " + versionParam );
    }

    private String evaluateServiceName( HttpServletRequest request ) {
        String servletPath = request.getServletPath();
        if ( servletPath == null )
            throw new IllegalArgumentException( "Service name must not be null!" );
        if ( servletPath.contains( "/" ) ) {
            String[] splittedServletPath = servletPath.split( "/" );
            return splittedServletPath[splittedServletPath.length - 1];
        }
        return servletPath;
    }

    private void checkParameters( Map<String, String[]> normalizedParameterMap )
                            throws UnsupportedRequestTypeException {
        checkServiceParameter( normalizedParameterMap );
        checkRequestParameter( normalizedParameterMap );
    }

    private void checkServiceParameter( Map<String, String[]> normalizedParameterMap )
                            throws UnsupportedRequestTypeException {
        String serviceType = checkSingleRequiredParameter( normalizedParameterMap, SERVICE );
        if ( !"wcs".equalsIgnoreCase( serviceType ) ) {
            String msg = "Request must contain a \"service\" parameter with value \"wcs\"";
            throw new UnsupportedRequestTypeException( msg );
        }
    }

    private void checkRequestParameter( Map<String, String[]> normalizedParameterMap ) {
        checkSingleRequiredParameter( normalizedParameterMap, REQUEST );
    }

    private void checkGetCoverageParameters( Map<String, String[]> normalizedParameterMap ) {
        checkCoverageParameter( normalizedParameterMap );
        checkCrsParameter( normalizedParameterMap );
        checkBboxOrTimeParameter( normalizedParameterMap );
        checkWidthAndHeightOrResXAndResYParameter( normalizedParameterMap );
        checkFormatParameter( normalizedParameterMap );
    }

    private void checkCoverageParameter( Map<String, String[]> normalizedParameterMap ) {
        checkSingleRequiredParameter( normalizedParameterMap, COVERAGE );
    }

    private void checkCrsParameter( Map<String, String[]> normalizedParameterMap ) {
        checkSingleRequiredParameter( normalizedParameterMap, CRS );
    }

    private void checkBboxOrTimeParameter( Map<String, String[]> normalizedParameterMap ) {
        String[] bboxParameter = normalizedParameterMap.get( BBOX );
        String[] timeParameter = normalizedParameterMap.get( TIME );
        if ( isNotSet( bboxParameter ) && isNotSet( timeParameter ) ) {
            String msg = "Request must contain a \"bbox\" or \"time\" parameter, ignoring the casing. None Given.";
            throw new IllegalArgumentException( msg );
        }
        if ( !isNotSet( bboxParameter ) && isNotSingle( bboxParameter ) ) {
            throwException( BBOX, bboxParameter );
        }
        if ( !isNotSet( timeParameter ) && isNotSingle( timeParameter ) ) {
            throwException( TIME, timeParameter );
        }
    }

    private void checkWidthAndHeightOrResXAndResYParameter( Map<String, String[]> normalizedParameterMap ) {
        String[] widthParameter = normalizedParameterMap.get( WIDTH );
        String[] heightParameter = normalizedParameterMap.get( HEIGHT );
        String[] resxParameter = normalizedParameterMap.get( RESX );
        String[] resyParameter = normalizedParameterMap.get( RESY );
        boolean isNotWidthAndHeight = isNotSet( widthParameter ) || isNotSet( heightParameter );
        boolean isNotResXAndResY = isNotSet( resxParameter ) || isNotSet( resyParameter );
        if ( isNotWidthAndHeight && isNotResXAndResY ) {
            String msg = "Request must contain a \"width\" and \"height\" or \"resx\" and \"resy\" "
                         + "parameter, ignoring the casing.";
            throw new IllegalArgumentException( msg );
        }
        if ( !isNotSet( widthParameter ) && isNotSingle( widthParameter ) ) {
            throwException( WIDTH, widthParameter );
        }
        if ( !isNotSet( heightParameter ) && isNotSingle( heightParameter ) ) {
            throwException( HEIGHT, heightParameter );
        }
        if ( !isNotSet( resxParameter ) && isNotSingle( resxParameter ) ) {
            throwException( RESX, resxParameter );
        }
        if ( !isNotSet( resyParameter ) && isNotSingle( resyParameter ) ) {
            throwException( RESY, resyParameter );
        }
    }

    private void checkFormatParameter( Map<String, String[]> normalizedParameterMap ) {
        checkSingleRequiredParameter( normalizedParameterMap, FORMAT );
    }

}