package com.it_goes.api.jpa.model;

import com.it_goes.api.util.enums.Country;
import com.it_goes.api.util.enums.State;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TripParticipationTests {
    private final User author = new User("author", "author@example.com", "hash", "The", "Author", null);
    private final User participant = new User("friend", "friend@example.com", "hash", "A", "Friend", null);
    private final Trip trip = new Trip("Trip", author,
            new Location("Mountain", State.MT, Country.US), LocalDate.of(2025, 1, 1));

    @Test
    void authorCannotBeAddedAsParticipant() {
        assertThat(trip.addUser(author)).isFalse();
        assertThat(author.addTrip(trip)).isFalse();
        assertThat(trip.getUsers()).isEmpty();
        assertThat(author.getTrips()).isEmpty();
        assertThat(trip.getAuthor()).isSameAs(author);
    }

    @Test
    void duplicateParticipantDoesNotChangeEitherAssociation() {
        assertThat(participant.addTrip(trip)).isTrue();
        assertThat(participant.addTrip(trip)).isFalse();
        assertThat(trip.addUser(participant)).isFalse();
        assertThat(trip.getAuthor()).isSameAs(author);
        assertThat(trip.getUsers()).containsExactly(participant);
        assertThat(participant.getTrips()).containsExactly(trip);
    }

    @Test
    void sameUsernameCannotBeAddedTwice() {
        User duplicate = new User("friend", "other@example.com", "hash", "Another", "Friend", null);

        assertThat(trip.addUser(participant)).isTrue();
        assertThat(trip.addUser(duplicate)).isFalse();
        assertThat(trip.getUsers()).containsExactly(participant);
    }
}
