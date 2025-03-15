package de.uhd.ifi.se.moviemanager.ui.master.comparator;

import static java.lang.Math.abs;

import java.util.Date;

import de.uhd.ifi.se.moviemanager.model.Movie;
import de.uhd.ifi.se.moviemanager.util.DateUtils;

/**
 * Compares two {@link Movie}s by their return date.
 * Only considers movies that are currently lent out.
 *
 * @see Movie#getReturnDate()
 * @see Movie#isLent()
 */
public class ReturnDateComparator implements CategorizedComparator<Movie> {

    @Override
    public int compare(Movie movie1, Movie movie2) {
        // Nicht ausgeliehene Filme ans Ende der Liste
        if (!movie1.isLent() && !movie2.isLent()) return 0;
        if (!movie1.isLent()) return 1;
        if (!movie2.isLent()) return -1;

        // Beide Filme sind ausgeliehen, nach Rückgabedatum sortieren
        Date returnDate1 = movie1.getReturnDate();
        Date returnDate2 = movie2.getReturnDate();

        if (returnDate1 == null && returnDate2 == null) return 0;
        if (returnDate1 == null) return 1;
        if (returnDate2 == null) return -1;

        return returnDate1.compareTo(returnDate2);
    }

    @Override
    public String getCategoryNameFor(Movie movie) {
        if (!movie.isLent()) {
            return "Available";
        }

        Date returnDate = movie.getReturnDate();
        if (returnDate == null) {
            return "No Return Date Set";
        }

        Date now = DateUtils.nowAtMidnight();
        if (movie.isOverdue()) {
            long overdueDays = DateUtils.differenceInDays(
                    DateUtils.normDateTimeToMidnight(returnDate), now);
            return getOverdueCategoryName(overdueDays);
        } else {
            long daysUntilReturn = DateUtils.differenceInDays(now,
                    DateUtils.normDateTimeToMidnight(returnDate));
            return getDueCategoryName(daysUntilReturn);
        }
    }

    private static String getOverdueCategoryName(long days) {
        if (days == 1) {
            return "1 Day Overdue";
        } else if (days <= 7) {
            return "This Week Overdue";
        } else if (days <= 30) {
            return "This Month Overdue";
        } else {
            return "Long Overdue";
        }
    }

    private static String getDueCategoryName(long days) {
        if (days == 0) {
            return "Due Today";
        } else if (days == 1) {
            return "Due Tomorrow";
        } else if (days <= 7) {
            return "Due This Week";
        } else if (days <= 30) {
            return "Due This Month";
        } else {
            return "Due Later";
        }
    }

    @Override
    public String getSubText(Movie movie) {
        if (!movie.isLent()) {
            return "Not lent";
        }

        Date returnDate = movie.getReturnDate();
        if (returnDate == null) {
            return "No return date set";
        }

        String status = DateUtils.dateToText(returnDate);
        if (movie.isOverdue()) {
            status += " (OVERDUE)";
        }
        return status;
    }
}