package de.uhd.ifi.se.moviemanager.ui.dialog;

import android.os.Parcel;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.Date;
import java.util.function.Consumer;

import de.uhd.ifi.se.moviemanager.util.DateUtils;

/**
 * Dialog to select a date, e.g. the movie watch date.
 * <p>
 * Use {@link #create(Date initial, Date minDate, Date maxDate)} or {@link
 * #createFromTodayAndOnward()} to create the {@link DateSelectionDialog}.
 */
public class DateSelectionDialog {
    private MaterialDatePicker<Long> datePicker;

    public static DateSelectionDialog createFromTodayAndOnward() {
        return create(null, DateUtils.nowAtMidnight(), null);
    }

    /**
     * Creates a new DateSelectionDialog and returns it.
     *
     * @param selectedDate The date initially selected. If null, the current date is selected.
     * @param minDate      The minimum Date which is selectable.
     *                     If null, the DateSelectionDialog isn't limited to the Past.
     * @param maxDate      The maximum Date which is selectable.
     *                     If null, the DateSelectionDialog isn't limited to the Future.
     * @return the DateSelectionDialog
     */
    public static DateSelectionDialog create(@Nullable Date selectedDate,
                                             @Nullable Date minDate, @Nullable Date maxDate) {

        long initialDateInMilliseconds;
        if (selectedDate == null)
            initialDateInMilliseconds = MaterialDatePicker.todayInUtcMilliseconds();
        else
            initialDateInMilliseconds = selectedDate.getTime();

        CalendarConstraints calendarConstraints = new CalendarConstraints.Builder().setValidator(
                new IntervalDateValidator(minDate, maxDate)).build();

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setCalendarConstraints(calendarConstraints)
                .setSelection(initialDateInMilliseconds).build();

        DateSelectionDialog selectionDialog = new DateSelectionDialog();
        selectionDialog.datePicker = datePicker;

        return selectionDialog;
    }

    public void setPositiveButtonListener(@NonNull Consumer<Date> listener) {
        datePicker.addOnPositiveButtonClickListener(selection ->
                listener.accept(new Date(selection)));
    }

    public void setNegativeButtonListener(@NonNull Consumer<View> listener) {
        datePicker.addOnNegativeButtonClickListener(listener::accept);
    }

    public void show(FragmentManager fm, String tag) {
        datePicker.show(fm, tag);
    }

    /**
     * This class is used to enable days in a custom interval in the {@link DateSelectionDialog} and
     * disable the rest.
     */
    private static class IntervalDateValidator implements CalendarConstraints.DateValidator {
        final long minBound;
        final long maxBound;

        //is needed cause a DateValidator is a Parcelable
        public static final Creator<IntervalDateValidator> CREATOR = new Creator<IntervalDateValidator>() {
            @Override
            public IntervalDateValidator createFromParcel(Parcel source) {
                long read1 = source.readLong();
                long read2 = source.readLong();

                return new IntervalDateValidator(new Date(read1), new Date(read2));
            }

            @Override
            public IntervalDateValidator[] newArray(int size) {
                return new IntervalDateValidator[size];
            }
        };

        /**
         * Creates a new IntervalDateValidator, which validates the dates in the given interval as
         * enabled.
         *
         * @param leftBound  The left bound of the interval. If null, the interval isn't bounded to
         *                   the left.
         * @param rightBound The right bound of the interval. If null, the interval isn't bounded to
         *                   the right.
         */
        public IntervalDateValidator(@Nullable Date leftBound, @Nullable Date rightBound) {
            minBound = leftBound != null ? leftBound.getTime() : Long.MIN_VALUE;
            maxBound = rightBound != null ? rightBound.getTime() : Long.MAX_VALUE;
        }

        @Override
        public boolean isValid(long date) {
            return minBound <= date && date <= maxBound;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeLong(minBound);
            dest.writeLong(maxBound);
        }
    }
}