package org.opengroup.osdu.auth;

import org.gluu.oxauth.client.*;
import org.gluu.oxauth.model.common.AuthenticationMethod;
import org.gluu.oxauth.model.common.GrantType;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthFilter implements Filter {

    private static final String WELL_KNOWN_CONNECT_PATH = "/.well-known/openid-configuration";

    private static final String CALLBACK_URL = "/auth/callback";

    private String authorizeParameters;
    private String redirectUri;
    private String discoveryUrl;
    private String clientId;
    private String clientSecret;

    @Override
    public void init(FilterConfig filterConfig) {
        authorizeParameters = "scope=openid email&response_type=code";
        redirectUri = "http://localhost:8080" + CALLBACK_URL;
        discoveryUrl = System.getenv("OSDU_AUTH_BASE_URL") + WELL_KNOWN_CONNECT_PATH;
        clientId = System.getenv("OSDU_CLIENT_ID");
        clientSecret = System.getenv("OSDU_CLIENT_SECRET");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        Object accessToken = request.getSession(true).getAttribute("access_token");

        if (accessToken == null) {
            OpenIdConfigurationResponse discoveryResponse = fetchDiscovery();

            if (isRedirectRequest(request)) {
                fetchToken(discoveryResponse, request);
            } else {
                redirectToLogin(discoveryResponse, response);
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean isRedirectRequest(HttpServletRequest request) {
        return request.getRequestURI().endsWith(CALLBACK_URL) &&
                request.getParameter("code") != null;
    }

    private OpenIdConfigurationResponse fetchDiscovery() {
        OpenIdConfigurationClient discoveryClient = new OpenIdConfigurationClient(discoveryUrl);

        try {
            return discoveryClient.execOpenIdConfiguration();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param request request
     * @return whether login is still required
     */
    private void fetchToken(OpenIdConfigurationResponse discoveryResponse, HttpServletRequest request) {
        String code = request.getParameter("code");
        TokenRequest tokenRequest = new TokenRequest(GrantType.AUTHORIZATION_CODE);
        tokenRequest.setCode(code);
        tokenRequest.setRedirectUri(redirectUri);
        tokenRequest.setAuthUsername(clientId);
        tokenRequest.setAuthPassword(clientSecret);
        tokenRequest.setAuthenticationMethod(AuthenticationMethod.CLIENT_SECRET_BASIC);

        TokenClient tokenClient = new TokenClient(discoveryResponse.getTokenEndpoint());
        tokenClient.setRequest(tokenRequest);

        TokenResponse tokenResponse = tokenClient.exec();
        request.getSession(true).setAttribute("access_token", tokenResponse.getAccessToken());
        request.getSession(true).setAttribute("id_token", tokenResponse.getIdToken());
    }

    private void redirectToLogin(OpenIdConfigurationResponse discoveryResponse, HttpServletResponse response) throws IOException {
        String redirectTo = discoveryResponse.getAuthorizationEndpoint() +
                "?redirect_uri=" + redirectUri + "&client_id=" + clientId + "&" + authorizeParameters;
        response.sendRedirect(redirectTo);
    }
}
