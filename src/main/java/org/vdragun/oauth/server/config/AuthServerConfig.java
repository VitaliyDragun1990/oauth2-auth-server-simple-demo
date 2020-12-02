package org.vdragun.oauth.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.InMemoryClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private DataSource dataSource;

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                .authenticationManager(authenticationManager)
                .tokenStore(tokenStore());
    }

    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        InMemoryClientDetailsService clientDetailsService = new InMemoryClientDetailsService();

        BaseClientDetails mobileAppClient = new BaseClientDetails();
        mobileAppClient.setClientId("mobile");
        mobileAppClient.setClientSecret("secret");
        mobileAppClient.setScope(List.of("read"));
        mobileAppClient.setAuthorizedGrantTypes(List.of("password", "refresh_token"));

        BaseClientDetails webAppClient = new BaseClientDetails();
        webAppClient.setClientId("web");
        webAppClient.setClientSecret("secret");
        webAppClient.setScope(List.of("read"));
        webAppClient.setAuthorizedGrantTypes(List.of("authorization_code", "refresh_token"));
        webAppClient.setRegisteredRedirectUri(Set.of("http://localhost:9090/home"));

        BaseClientDetails backendApp = new BaseClientDetails();
        backendApp.setClientId("backend");
        backendApp.setClientSecret("secret");
        backendApp.setScope(List.of("info"));
        backendApp.setAuthorizedGrantTypes(List.of("client_credentials"));

        BaseClientDetails resourceServerClient = new BaseClientDetails();
        resourceServerClient.setClientSecret("resourceserver");
        resourceServerClient.setClientSecret("secret");

        clientDetailsService.setClientDetailsStore(Map.of(
                "mobile", mobileAppClient,
                "web", webAppClient,
                "backend", backendApp,
                "resourceserver", resourceServerClient
        ));

        clients.withClientDetails(clientDetailsService);
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.checkTokenAccess("isAuthenticated()");
    }
}
