package ch.m1m.infra;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

// from: https://stackoverflow.com/questions/74621459/using-a-custom-identity-provider-in-quarkus

@ApplicationScoped
public class LdapIdentityProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

    private static final Logger log = LoggerFactory.getLogger(ConfigItemService.class);

    private static final Map<String, String> CREDENTIALS = Map.of("admin", "admin");

    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest request,
                                              AuthenticationRequestContext authenticationRequestContext) {

        if (new String(request.getPassword().getPassword()).equals(CREDENTIALS.get(request.getUsername()))) {
            return Uni.createFrom().item(QuarkusSecurityIdentity.builder()
                    .setPrincipal(new QuarkusPrincipal(request.getUsername()))
                    .addCredential(request.getPassword())
                    .setAnonymous(false)
                    .addRole("admin")
                    .build());
        }
        log.error("failed to authenticate user");
        throw new AuthenticationFailedException("password invalid or user not found");
    }
}