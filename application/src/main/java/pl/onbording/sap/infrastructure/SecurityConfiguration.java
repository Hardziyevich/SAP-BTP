package pl.onbording.sap.infrastructure;

import com.sap.cloud.security.spring.config.IdentityServicesPropertySourceFactory;
import com.sap.cloud.security.spring.token.authentication.AuthenticationToken;
import com.sap.cloud.security.token.TokenClaims;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity(debug = true) // TODO "debug" may include sensitive information. Do not use in a production system!
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@PropertySource(factory = IdentityServicesPropertySourceFactory.class, ignoreResourceNotFound = true, value = {""})
@RequiredArgsConstructor
public class SecurityConfiguration {

    public final Converter<Jwt, AbstractAuthenticationToken> authConverter; // Required only when Xsuaa is used

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // @formatter:off
        return http
            .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(registry -> registry
                    .requestMatchers("/callback/v1.0/tenants/").hasAuthority("Callback")
                    .requestMatchers("/hello").hasAuthority("Display")
                    .requestMatchers(HttpMethod.GET, "users").hasAuthority("Display")
                    .requestMatchers("/**").authenticated()
                    .anyRequest().denyAll())

            .oauth2ResourceServer(oauth2Config -> oauth2Config
                .jwt(jwtConfig -> jwtConfig
                    .jwtAuthenticationConverter(new MyCustomHybridTokenAuthenticationConverter())))  // Adjust the converter to represent your use case
                                                                                                     // Use MyCustomHybridTokenAuthenticationConverter when IAS and XSUAA is used
                                                                                                     // Use MyCustomIasTokenAuthenticationConverter when only IAS is used
            .build();
        // @formatter: on
    }

    //Workaround for hybrid use case until Cloud Authorization Service is globally available.
    class MyCustomHybridTokenAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken>
    {
        @Override
        public AbstractAuthenticationToken convert(Jwt jwt)
        {
            if( jwt.hasClaim(TokenClaims.XSUAA.EXTERNAL_ATTRIBUTE) ) {
                return authConverter.convert(jwt);
            }
            return new AuthenticationToken(jwt, deriveAuthoritiesFromGroup(jwt));
        }

        private Collection<GrantedAuthority> deriveAuthoritiesFromGroup(Jwt jwt)
        {
            Collection<GrantedAuthority> groupAuthorities = new ArrayList<>();
            if( jwt.hasClaim(TokenClaims.GROUPS) ) {
                List<String> groups = jwt.getClaimAsStringList(TokenClaims.GROUPS);
                for( String group : groups ) {
                    groupAuthorities.add(new SimpleGrantedAuthority(group.replace("IASAUTHZ_", "")));
                }
            }
            return groupAuthorities;
        }
    }
}

