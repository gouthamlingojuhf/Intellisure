package com.intellisure.quotepolicyservice.testsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builds genuinely signed HS256 JWTs so the integration tests exercise the
 * real {@code NimbusReactiveJwtDecoder} and
 * {@code JwtAuthenticationConverter} wiring end to end.
 */
public final class JwtTestTokens {

    private static final String SECRET =
            "VVGF9BeSRx3K2epQp4/DRnq881+8YixWG3KHI4qTl9I=";

    private static final Base64.Encoder ENCODER =
            Base64.getUrlEncoder().withoutPadding();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JwtTestTokens() {
    }

    public static String tokenFor(
            UUID subject,
            List<String> roles,
            UUID customerId
    ) {
        long now = Instant.now().getEpochSecond();

        ObjectNode header = MAPPER.createObjectNode();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        ObjectNode payload = MAPPER.createObjectNode();
        payload.put("sub", subject.toString());
        payload.put("iat", now);
        payload.put("exp", now + 3600);

        ArrayNode rolesNode = MAPPER.createArrayNode();
        roles.forEach(rolesNode::add);
        payload.set("roles", rolesNode);

        if (customerId != null) {
            payload.put("customerId", customerId.toString());
        }

        return sign(header, payload);
    }

    public static String policyholderToken(UUID customerId) {
        return tokenFor(
                customerId,
                List.of("POLICYHOLDER"),
                customerId
        );
    }

    public static String underwriterToken(UUID underwriterId) {
        return tokenFor(
                underwriterId,
                List.of("UNDERWRITER"),
                null
        );
    }

    public static String adminToken(UUID adminId) {
        return tokenFor(adminId, List.of("ADMIN"), null);
    }

    public static String claimsAdjusterToken(UUID userId) {
        return tokenFor(userId, List.of("CLAIMS_ADJUSTER"), null);
    }

    private static String sign(
            ObjectNode header,
            ObjectNode payload
    ) {
        try {
            String unsigned = ENCODER.encodeToString(
                    MAPPER.writeValueAsBytes(header)
            ) + "." + ENCODER.encodeToString(
                    MAPPER.writeValueAsBytes(payload)
            );

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(
                    new SecretKeySpec(
                            Base64.getDecoder().decode(SECRET),
                            "HmacSHA256"
                    )
            );

            String signature = ENCODER.encodeToString(
                    mac.doFinal(
                            unsigned.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    )
            );

            return unsigned + "." + signature;
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to build test JWT",
                    exception
            );
        }
    }

    public static Map<String, String> bearer(String token) {
        return Map.of("Authorization", "Bearer " + token);
    }
}
