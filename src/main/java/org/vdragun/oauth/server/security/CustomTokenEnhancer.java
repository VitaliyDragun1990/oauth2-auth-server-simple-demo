package org.vdragun.oauth.server.security;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.time.ZoneId;
import java.util.Map;

public class CustomTokenEnhancer implements TokenEnhancer {

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        DefaultOAuth2AccessToken enhancedToken = new DefaultOAuth2AccessToken(accessToken);

        Map<String, Object> info = Map.of(
                "generatedInZone", ZoneId.systemDefault().toString()
        );

        enhancedToken.setAdditionalInformation(info);

        return enhancedToken;
    }
}
