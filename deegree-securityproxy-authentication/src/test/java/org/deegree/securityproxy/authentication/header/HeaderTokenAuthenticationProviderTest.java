package org.deegree.securityproxy.authentication.header;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.deegree.securityproxy.authentication.repository.UserDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author <a href="goltz@lat-lon.de">Lyn Goltz</a>
 * @author <a href="erben@lat-lon.de">Alexander Erben</a>
 * @author last edited by: $Author: erben $
 * 
 * @version $Revision: $, $Date: $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:org/deegree/securityproxy/authentication/HeaderTokenAuthenticationProviderTestContext.xml" })
public class HeaderTokenAuthenticationProviderTest {

    private static final String VALID_TOKEN = "VALID";

    private static final String VALID_USERNAME = "USER";

    private static final String VALID_PASSWORD = "PASSWD";

    private static final String INVALID_TOKEN = "INVALID";

    @Autowired
    private AuthenticationProvider provider;

    @Autowired
    private UserDao source;

    @SuppressWarnings("unchecked")
    @Before
    public void setupDatasource() {
        Mockito.reset( source );
        when( source.retrieveUserById( INVALID_TOKEN ) ).thenReturn( null );
        UserDetails validUser = new User( VALID_USERNAME, VALID_PASSWORD, Collections.<GrantedAuthority> emptyList() );
        when( source.retrieveUserById( VALID_TOKEN ) ).thenReturn( validUser );
        when( source.retrieveUserById( null ) ).thenThrow( IllegalArgumentException.class );
    }

    @Test
    public void testAuthenticateValidToken() {
        Authentication authenticate = provider.authenticate( createHeaderAuthenticationTokenWithValidHeader() );
        assertThat( authenticate.isAuthenticated(), is( Boolean.TRUE ) );
        assertThat( (String) authenticate.getCredentials(), is( VALID_TOKEN ) );
        UserDetails details = (UserDetails) authenticate.getPrincipal();
        assertThat( details.getUsername(), is( VALID_USERNAME ) );
        assertThat( details.getPassword(), is( VALID_PASSWORD ) );
    }

    @Test
    public void testAuthenticateInvalidToken() {
        provider.authenticate( createHeaderAuthenticationTokenWithInvalidHeader() );
    }

    @Test
    public void testAuthenticateShouldThrowExceptionOnNullParameter() {
        Authentication authenticate = provider.authenticate( null );
        assertThat( authenticate instanceof AnonymousAuthenticationToken, is( true ) );
    }

    private Authentication createHeaderAuthenticationTokenWithInvalidHeader() {
        Authentication mock = mock( Authentication.class );
        when( mock.getPrincipal() ).thenReturn( INVALID_TOKEN );
        return mock;

    }

    private Authentication createHeaderAuthenticationTokenWithValidHeader() {
        Authentication mock = mock( Authentication.class );
        when( mock.getPrincipal() ).thenReturn( VALID_TOKEN );
        return mock;
    }

}