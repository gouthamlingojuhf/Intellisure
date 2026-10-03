package com.intellisure.quotepolicyservice.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JwtAuthenticationConverter")
class JwtAuthenticationConverterTest {

    private final JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

    private Jwt jwtWith(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user-subject");

        claims.forEach(builder::claim);

        return builder.build();
    }

    private List<String> authoritiesOf(
            Map<String, Object> claims
    ) {
        AbstractAuthenticationToken token = converter
                .convert(jwtWith(claims))
                .block();

        assertNotNull(token);

        return token.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .toList();
    }

    @Nested
    @DisplayName("role claim handling")
    class Roles {

        @Test
        @DisplayName("converts a roles collection and prefixes each role")
        void rolesCollection() {
            assertEquals(
                    List.of(
                            "ROLE_UNDERWRITER",
                            "ROLE_ADMIN"
                    ),
                    authoritiesOf(
                            Map.of(
                                    "roles",
                                    List.of("underwriter", "admin")
                            )
                    )
            );
        }

        @Test
        @DisplayName("converts a comma separated roles string")
        void rolesString() {
            assertEquals(
                    List.of("ROLE_UNDERWRITER", "ROLE_ADMIN"),
                    authoritiesOf(
                            Map.of(
                                    "roles",
                                    " underwriter , admin "
                            )
                    )
            );
        }

        @Test
        @DisplayName("drops blank entries from a comma separated roles string")
        void rolesStringWithBlanks() {
            assertEquals(
                    List.of("ROLE_ADMIN"),
                    authoritiesOf(
                            Map.of("roles", "admin, ,  ")
                    )
            );
        }

        @Test
        @DisplayName("does not double prefix an already prefixed role")
        void keepsExistingPrefix() {
            assertEquals(
                    List.of("ROLE_ADMIN", "ROLE_UNDERWRITER"),
                    authoritiesOf(
                            Map.of(
                                    "roles",
                                    List.of("ROLE_ADMIN", "UNDERWRITER")
                            )
                    )
            );
        }

        @Test
        @DisplayName("normalises role casing")
        void normalisesCasing() {
            assertEquals(
                    List.of("ROLE_ADMIN"),
                    authoritiesOf(Map.of("roles", List.of("AdMiN")))
            );
        }

        @Test
        @DisplayName("falls back to the singular role claim")
        void singularRoleClaim() {
            assertEquals(
                    List.of("ROLE_CLAIMS_ADJUSTER"),
                    authoritiesOf(Map.of("role", "claims_adjuster"))
            );
        }

        @Test
        @DisplayName("prefers the roles claim over the singular role claim")
        void rolesClaimWins() {
            assertEquals(
                    List.of("ROLE_ADMIN"),
                    authoritiesOf(
                            Map.of(
                                    "roles",
                                    List.of("admin"),
                                    "role",
                                    "underwriter"
                            )
                    )
        );
        }

        @Test
        @DisplayName("returns no authorities when no role claim is present")
        void noRoleClaim() {
            assertTrue(
                    authoritiesOf(Map.of()).isEmpty()
            );
        }

        @Test
        @DisplayName("ignores a blank singular role claim")
        void blankSingularRoleClaim() {
            assertTrue(
                    authoritiesOf(Map.of("role", "   ")).isEmpty()
            );
        }

        @Test
        @DisplayName("ignores a roles claim of an unsupported type")
        void unsupportedRolesType() {
            assertTrue(
                    authoritiesOf(Map.of("roles", 42)).isEmpty()
            );
        }
    }

    @Nested
    @DisplayName("token creation")
    class TokenCreation {

        @Test
        @DisplayName("uses the JWT subject as the principal name")
        void usesSubjectAsName() {
            AbstractAuthenticationToken token = converter
                    .convert(jwtWith(Map.of()))
                    .block();

            assertNotNull(token);
            assertEquals("user-subject", token.getName());
        }

        @Test
        @DisplayName("carries the original JWT")
        void carriesJwt() {
            Jwt jwt = jwtWith(Map.of("role", "admin"));

            AbstractAuthenticationToken token = converter.convert(jwt)
                    .block();

            assertNotNull(token);
            assertInstanceOf(JwtAuthenticationToken.class, token);
            assertEquals(
                    jwt,
                    ((JwtAuthenticationToken) token).getToken()
            );
            assertTrue(token.isAuthenticated());
        }

        @Test
        @DisplayName("a JWT without a subject cannot be constructed at all")
        void nullSubject() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> Jwt.withTokenValue("token")
                            .header("alg", "none")
                            .build()
            );
        }
    }
}
