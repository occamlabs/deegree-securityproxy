package org.deegree.securityproxy.responsefilter.logging;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Encapsulates detailed information about the clipping result (geometry as well as failures)
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class ResponseClippingReport implements ResponseFilterReport {

    private final Geometry returnedVisibleArea;

    private final String failure;

    private final boolean isFiltered;

    /**
     * Instantiates a new {@link ResponseClippingReport} with an error message, the geometry is set to <code>null</code>
     * , isFiltered to <code>false</code>
     * 
     * @param failure
     *            the error message, may not be <code>null</code>
     * @throws IllegalArgumentException
     *             if required parameter is null
     */
    public ResponseClippingReport( String failure ) {
        if ( failure == null )
            throw new IllegalArgumentException( "failure must not be null!" );
        this.failure = failure;
        this.isFiltered = false;
        this.returnedVisibleArea = null;
    }

    /**
     * Instantiates a new {@link ResponseClippingReport} with the resulting visible area and the information if clipping
     * was required
     * 
     * @param returnedVisibleArea
     *            never <code>null</code>
     * @param isFiltered
     *            if clipping was required or not
     * @throws IllegalArgumentException
     *             if required parameter is null
     */
    public ResponseClippingReport( Geometry returnedVisibleArea, boolean isFiltered ) {
        if ( returnedVisibleArea == null )
            throw new IllegalArgumentException( "returnedVisbleArea must not be null!" );
        this.failure = null;
        this.isFiltered = isFiltered;
        this.returnedVisibleArea = returnedVisibleArea;
    }

    /**
     * @return <code>true</code> if the returned image was clipped, <code>false</code> otherwise. If failure is not
     *         <code>null</code> <code>false</code> is returned
     */
    @Override
    public boolean isFiltered() {
        return isFiltered;
    }

    @Override
    public String getMessage() {
        return failure != null ? failure : "Image was " + ( isFiltered ? "" : "not " )
                                           + "clipped. Returned visible area: " + returnedVisibleArea;
    }

    @Override
    public String toString() {
        return getMessage();
    }

    /**
     * 
     * @return the failure message if an error occurred during clipping.
     */
    public String getFailure() {
        return failure;
    }

    /**
     * @return the returnedVisibleArea <code>null</code> if an error occurred (failure is not null)
     */
    public Geometry getReturnedVisibleArea() {
        return returnedVisibleArea;
    }

}