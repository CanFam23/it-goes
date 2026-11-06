package com.it_goes.api.controller;

import com.it_goes.api.jpa.model.User;
import com.it_goes.api.jpa.repo.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
public class TripParticipantController {
    private final UserRepository users;

    public TripParticipantController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("users/options")
    public List<ParticipantOption> options(Authentication authentication) {
        return users.findAll().stream().filter(user -> !user.getUsername().equals(authentication.getName()))
                .map(ParticipantOption::of).sorted(Comparator.comparing(ParticipantOption::name)).toList();
    }

    public record ParticipantOption(Long id, String name) {
        static ParticipantOption of(User user) {
            return new ParticipantOption(user.getId(), user.getFirstName() + " " + user.getLastName());
        }
    }
}
