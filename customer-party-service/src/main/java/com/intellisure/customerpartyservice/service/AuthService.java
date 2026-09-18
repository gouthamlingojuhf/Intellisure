package com.intellisure.customerpartyservice.service;

import com.intellisure.customerpartyservice.dto.LoginRequest;
import com.intellisure.customerpartyservice.dto.LoginResponse;
import com.intellisure.customerpartyservice.entity.UserAccount;
import com.intellisure.customerpartyservice.exception.InvalidCredentialsException;
import com.intellisure.customerpartyservice.repository.UserAccountRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserAccountRepo userAccountRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Mono<LoginResponse> login(LoginRequest request) {
        return userAccountRepo.findByEmail(request.email())
                .switchIfEmpty(
                        Mono.error(
                                new InvalidCredentialsException(
                                        "Invalid email or password"
                                ))).flatMap(user -> authenticate(user, request.password()));
    }

    private Mono<LoginResponse> authenticate(UserAccount user, String password) {
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return Mono.error( new InvalidCredentialsException(
                            "Invalid email or password"
                    )
            );
        }
        if (!"ACTIVE".equals(user.getAccountStatus())) {
            return Mono.error( new InvalidCredentialsException(
                            "Invalid email or password"
                    )
            );
        }
        String token = jwtService.generateToken(user.getUserId(), user.getRole());
        return Mono.just(new LoginResponse(token, "Bearer", 3600000));
    }
}
