package com.it_goes.api.controller;

import com.it_goes.api.config.security.jwt.JwtUtils;
import com.it_goes.api.config.security.user.UserDetailsImpl;
import com.it_goes.api.jpa.model.User;
import com.it_goes.api.jpa.repo.UserRepository;
import com.it_goes.api.payload.request.LoginRequest;
import com.it_goes.api.payload.request.SignupRequest;
import com.it_goes.api.payload.response.JwtResponse;
import com.it_goes.api.payload.response.MessageResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        try {
            final Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));


            logger.info("User authenticated: {}", loginRequest.getUsername());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info(authentication.toString());
            final String jwt = jwtUtils.generateJwtToken(authentication);

            if (jwt == null || jwt.isEmpty()) {
                logger.error("JWT generation returned null or empty token");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Authentication token generation failed");
            }

            final UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            if (userDetails == null || userDetails.getUser() == null) {
                logger.error("UserDetails or User is null");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("User details not found");
            }

            final List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getUser().getId(),
                    userDetails.getUsername(),
                    userDetails.getUser().getEmail(),
                    roles));

        } catch (Exception e) {
            logger.error("Authentication error: ", e);
            throw e;
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                null);

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}