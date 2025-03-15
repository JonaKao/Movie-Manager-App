package de.uhd.ifi.se.moviemanager.model.movie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.uhd.ifi.se.moviemanager.model.Movie;
import de.uhd.ifi.se.moviemanager.model.MovieRelease;
import de.uhd.ifi.se.moviemanager.util.DateUtils;

/**
 * Tests basic methods such as equals, getters and setters of {@link
 * de.uhd.ifi.se.moviemanager.model.Movie} class.
 */
class MovieTest {
    private Movie movie;
    // Konstante für Zeitberechnungen (24h * 60min * 60sec * 1000ms)
    private static final long DAYS_IN_MILLIS = 24 * 60 * 60 * 1000L;

    @BeforeEach
    void setUp() {
        movie = new Movie("Test Movie");
    }

    @Test
    void testTitleAndDescription() {
        Movie movie = new Movie("Guardians of the Galaxy 3");
        movie.setDescription(
                "a 2014 American superhero film based on the Marvel " +
                        "Comics superhero team of the same name.");

        assertEquals("Guardians of the Galaxy 3", movie.getTitle());
        assertFalse(movie.getDescription().isEmpty());
    }

    @Test
    void testRatingAndRuntime() {
        Movie movie = new Movie();
        movie.setRating(4.5);
        movie.setRuntime(120);

        assertEquals(4.5, movie.getRating());
        assertEquals(120, movie.getRuntime());
    }

    @Test
    void testWatchDate() {
        Movie movie = new Movie();
        Date today = DateUtils.nowAtMidnight();
        movie.setWatchDate(today);

        assertEquals(today, movie.getWatchDate());
    }

    @Test
    void testLanguages() {
        Movie movie = new Movie();
        List<String> languages = new ArrayList<>();
        languages.add("German");
        languages.add("English");
        movie.setLanguages(languages);

        assertEquals(languages, movie.getLanguages());
    }

    @Test
    void testReleases() {
        Movie movie = new Movie();
        List<MovieRelease> releases = new ArrayList<>();
        releases.add(new MovieRelease("USA", new Date()));
        releases.add(new MovieRelease("Australia", new Date(
                DateUtils.nowAtMidnight()
                        .getTime() + 31 * 24 * 60 * 60 * 1000L)));
        movie.setReleases(releases);

        assertEquals(releases, movie.getReleases());
    }

    @Test
    void testProductionLocations() {
        Movie movie = new Movie();
        List<String> productionLocations = new ArrayList<>();
        productionLocations.add("USA");
        productionLocations.add("Australia");
        movie.setProductionLocations(productionLocations);

        assertEquals(productionLocations, movie.getProductionLocations());
    }

    // New tests

    /**
     * Tests the initial state of a newly created movie regarding its lending properties.
     * Ensures that new movies are not lent, have no return date and are not overdue.
     */
    @Test
    void testInitialLendingState() {
        assertFalse(movie.isLent(), "New movie should not be lent");
        assertNull(movie.getReturnDate(), "New movie should have no return date");
        assertFalse(movie.isOverdue(), "New movie should not be overdue");
    }

    /**
     * Tests setting and changing the lending status of a movie.
     * Verifies that the lending status can be correctly set and retrieved.
     * Related to requirement A1.
     */
    @Test
    void testLendingStatus() {
        assertFalse(movie.isLent(), "Default state should be not lent");

        movie.setLent(true);
        assertTrue(movie.isLent(), "Movie should be marked as lent");

        movie.setLent(false);
        assertFalse(movie.isLent(), "Movie should be marked as not lent");
    }

    /**
     * Tests setting and retrieving the return date for a movie.
     * Uses a future date to ensure valid setting of return dates.
     * Related to requirements A1a and A1b.
     */
    @Test
    void testReturnDate() {
        assertNull(movie.getReturnDate(), "Initial return date should be null");

        // Setze Rückgabedatum 7 Tage in der Zukunft um sicherzustellen,
        // dass es ein gültiges Datum ist
        Date futureDate = new Date(DateUtils.nowAtMidnight().getTime() + (7 * DAYS_IN_MILLIS));
        movie.setReturnDate(futureDate);
        assertEquals(futureDate, movie.getReturnDate(), "Return date should be set correctly");
    }

    /**
     * Tests the overdue status calculation of a movie under different conditions:
     * - No return date set
     * - Return date in the past (overdue)
     * - Return date in the future (not overdue)
     * Related to requirement A5.
     */
    @Test
    void testOverdueStatus() {
        movie.setLent(true);
        assertFalse(movie.isOverdue(), "Newly lent movie without return date should not be overdue");

        // Test mit überfälligem Rückgabedatum (7 Tage in der Vergangenheit)
        Date pastDate = new Date(DateUtils.nowAtMidnight().getTime() - (7 * DAYS_IN_MILLIS));
        movie.setReturnDate(pastDate);
        assertTrue(movie.isOverdue(), "Movie with past return date should be overdue");

        // Test mit Rückgabedatum in der Zukunft
        Date futureDate = new Date(DateUtils.nowAtMidnight().getTime() + (7 * DAYS_IN_MILLIS));
        movie.setReturnDate(futureDate);
        assertFalse(movie.isOverdue(), "Movie with future return date should not be overdue");
    }

    /**
     * Tests the complete process of returning a movie:
     * - Setting the movie as lent with a return date
     * - Returning the movie
     * - Verifying that the lending status and return date are reset
     * Related to requirement A2.
     */
    @Test
    void testReturnMovie() {
        Date futureDate = new Date(DateUtils.nowAtMidnight().getTime() + (7 * DAYS_IN_MILLIS));

        // Film ausleihen
        movie.setLent(true);
        movie.setReturnDate(futureDate);
        assertTrue(movie.isLent(), "Movie should be marked as lent");
        assertEquals(futureDate, movie.getReturnDate(), "Return date should be set");

        // Film zurückgeben und Status überprüfen
        movie.setLent(false);
        assertFalse(movie.isLent(), "Movie should be marked as not lent after return");
        assertNull(movie.getReturnDate(), "Return date should be null after return");
    }

    /**
     * Tests changing the due date of a lent movie.
     * Verifies that the return date can be extended to a later date.
     * Related to requirement A3.
     */
    @Test
    void testChangeDueDate() {
        movie.setLent(true);

        // Initialer Ausleihzeitraum: 7 Tage
        Date initialDate = new Date(DateUtils.nowAtMidnight().getTime() + (7 * DAYS_IN_MILLIS));
        movie.setReturnDate(initialDate);
        assertEquals(initialDate, movie.getReturnDate(), "Initial return date should be set");

        // Verlängern um weitere 7 Tage
        Date extendedDate = new Date(DateUtils.nowAtMidnight().getTime() + (14 * DAYS_IN_MILLIS));
        movie.setReturnDate(extendedDate);
        assertEquals(extendedDate, movie.getReturnDate(), "Return date should be extended");
    }

    /**
     * Tests handling of null return dates.
     * Verifies that a movie with a null return date is not considered overdue.
     */
    @Test
    void testNullReturnDate() {
        movie.setLent(true);
        movie.setReturnDate(null);

        assertNull(movie.getReturnDate(), "Return date should be null");
        assertFalse(movie.isOverdue(), "Movie with null return date should not be overdue");
    }

    /**
     * Tests that a movie that is not lent cannot be overdue,
     * regardless of its return date.
     * Related to requirement A5.
     */
    @Test
    void testOverdueStatusWhenNotLent() {
        Date pastDate = new Date(DateUtils.nowAtMidnight().getTime() - (7 * DAYS_IN_MILLIS));
        movie.setReturnDate(pastDate);
        assertFalse(movie.isOverdue(), "Not lent movie should not be overdue regardless of return date");
    }
}
