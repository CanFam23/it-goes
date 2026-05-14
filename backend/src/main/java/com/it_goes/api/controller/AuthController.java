package com.it_goes.api.controller;

import com.it_goes.api.config.security.jwt.JwtUtils;
import com.it_goes.api.config.security.user.UserDetailsImpl;
import com.it_goes.api.dto.UserDto;
import com.it_goes.api.jpa.model.User;
import com.it_goes.api.payload.request.LoginRequest;
import com.it_goes.api.payload.request.SignupRequest;
import com.it_goes.api.payload.response.UserDataResponse;
import com.it_goes.api.payload.response.MessageResponse;
import com.it_goes.api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserService userService;

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

            final ResponseCookie cookie = ResponseCookie.from("token", jwt)
                    .httpOnly(true)
                    .secure(false)     // must be false on localhost
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(86400)
                    .build();

            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new UserDataResponse(
                            userDetails.getUser().getUsername(),
                            userDetails.getUser().getEmail(),
                            userDetails.getUser().getFirstName(),
                            userDetails.getUser().getLastName()
                    ));

        } catch (BadCredentialsException e) {
            logger.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Invalid username / password"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        if (!userService.validateAccessCode(signUpRequest.getAccessCode(), signUpRequest.getEmail())){
            logger.warn("registerUser: Invalid access code given");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Invalid access code"));
        }

        if (!signUpRequest.getPassword().equals(signUpRequest.getConfirmPassword())) {
            logger.warn("registerUser: Passwords do not match");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Passwords do not match"));
        }

        final User newUser = userService.createUser(new UserDto(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                signUpRequest.getPassword(),
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                null // TODO Need to handle saving images, prob in service
        )).orElse(null);

        if (newUser == null) {
            logger.warn("registerUser: Failed to create user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Failed to create user"));
        }

        logger.info("registerUser: Created new user with id #{}", newUser.getId());

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/protected")
    public ResponseEntity<?> protectedEndpoint(HttpServletRequest request) {
        final String token = request.getHeader("Cookie");
        logger.info("Token: {}", token);

        if (jwtUtils.validateJwtToken(token)) {
            final String username = jwtUtils.getUserNameFromJwtToken(token);

            final User foundUser = userService.getUserByUsername(username);

            return ResponseEntity.ok(new UserDataResponse(
                    foundUser.getUsername(),
                    foundUser.getEmail(),
                    foundUser.getFirstName(),
                    foundUser.getLastName()
            ));
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
