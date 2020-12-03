package org.vdragun.oauth.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.InMemoryClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;
import org.vdragun.oauth.server.security.CustomTokenEnhancer;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${jks.password}")
    private String privateKeyPassword;

    @Value("${jks.privateKey}")
    private String privateKey;

    @Value("${jks.alias}")
    private String privateKeyAlias;

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();

        List<TokenEnhancer> tokenEnhancers = List.of(new CustomTokenEnhancer(), jwtAccessTokenConverter());
        tokenEnhancerChain.setTokenEnhancers(tokenEnhancers);

        endpoints
                .authenticationManager(authenticationManager)
                .tokenStore(tokenStore())
                .tokenEnhancer(tokenEnhancerChain);
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();

        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(
                new ClassPathResource(privateKey),
                privateKeyPassword.toCharArray()
        );

        converter.setKeyPair(keyStoreKeyFactory.getKeyPair(privateKeyAlias));

        return converter;
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
        security.tokenKeyAccess("isAuthenticated()");
    }
}
