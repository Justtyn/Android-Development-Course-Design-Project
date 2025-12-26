package com.justyn.meow.checkin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import com.justyn.meow.R;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class CheckInCalendarActivity extends AppCompatActivity
{
    private CalendarView calendarView;
    private TextView tvMonthTitle;
    private TextView tvSelectedDate;
    private TextView tvSelectedStatus;
    private TextView tvMonthSummary;
    private MaterialButton btnCheckIn;
    private LocalDate selectedDate;
    private final Set<LocalDate> checkedDates = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check_in_calendar);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        calendarView = findViewById(R.id.calendarView);
        tvMonthTitle = findViewById(R.id.tvMonthTitle);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedStatus = findViewById(R.id.tvSelectedStatus);
        tvMonthSummary = findViewById(R.id.tvMonthSummary);
        btnCheckIn = findViewById(R.id.btnCheckIn);

        selectedDate = LocalDate.now();
        loadCheckedDates();
        setupCalendar();
        updateSelectedStatus(selectedDate);
        updateMonthSummary();
        updateCheckInButtonState();

        btnCheckIn.setOnClickListener(v -> handleCheckIn());
    }

    private void handleCheckIn()
    {
        CheckInStore.CheckInResult result = CheckInStore.checkInToday(this);
        if (result.isAlreadyChecked())
        {
            Toast.makeText(this, getString(R.string.checkin_toast_already), Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, getString(R.string.checkin_toast_done), Toast.LENGTH_SHORT).show();
        }
        LocalDate oldSelected = selectedDate;
        loadCheckedDates();
        selectedDate = LocalDate.now();
        calendarView.notifyDateChanged(selectedDate);
        if (oldSelected != null && !oldSelected.equals(selectedDate))
        {
            calendarView.notifyDateChanged(oldSelected);
        }
        calendarView.scrollToMonth(YearMonth.from(selectedDate));
        updateMonthSummary();
        updateCheckInButtonState();
        updateSelectedStatus(selectedDate);
    }

    private void updateSelectedStatus(LocalDate date)
    {
        if (date == null)
        {
            return;
        }
        String dateText = CheckInStore.formatDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        tvSelectedDate.setText(dateText);
        boolean checked = checkedDates.contains(date);
        tvSelectedStatus.setText(checked
                ? getString(R.string.checkin_status_done)
                : getString(R.string.checkin_status_missed));
        int colorRes = checked ? R.color.meow_primary : R.color.meow_on_surface_muted;
        tvSelectedStatus.setTextColor(getColor(colorRes));
    }

    private void updateMonthSummary()
    {
        java.util.Calendar today = java.util.Calendar.getInstance();
        int year = today.get(java.util.Calendar.YEAR);
        int month = today.get(java.util.Calendar.MONTH) + 1;
        int dayOfMonth = today.get(java.util.Calendar.DAY_OF_MONTH);

        int checkedCount = 0;
        Set<String> dates = CheckInStore.getCheckInDates(this);
        for (String date : dates)
        {
            int[] parts = parseDateParts(date);
            if (parts == null)
            {
                continue;
            }
            if (parts[0] == year && parts[1] == month && parts[2] <= dayOfMonth)
            {
                checkedCount += 1;
            }
        }
        int missedCount = Math.max(0, dayOfMonth - checkedCount);
        tvMonthSummary.setText(getString(R.string.checkin_month_summary, checkedCount, missedCount));
    }

    private void updateCheckInButtonState()
    {
        boolean checkedToday = CheckInStore.isTodayCheckedIn(this);
        btnCheckIn.setEnabled(!checkedToday);
        btnCheckIn.setText(checkedToday
                ? getString(R.string.checkin_today_done)
                : getString(R.string.checkin_today_button));
    }

    private void setupCalendar()
    {
        calendarView.setDayViewResource(R.layout.item_checkin_day);
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>()
        {
            @Override
            public DayViewContainer create(View view)
            {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(DayViewContainer container, CalendarDay day)
            {
                container.day = day;
                container.tvDayText.setText(String.valueOf(day.getDate().getDayOfMonth()));

                boolean isMonthDate = day.getPosition() == DayPosition.MonthDate;
                if (isMonthDate)
                {
                    container.tvDayText.setTextColor(getColor(R.color.meow_on_surface));
                    container.viewCheckInDot.setVisibility(
                            checkedDates.contains(day.getDate()) ? View.VISIBLE : View.INVISIBLE
                    );
                }
                else
                {
                    container.tvDayText.setTextColor(getColor(R.color.meow_on_surface_muted));
                    container.viewCheckInDot.setVisibility(View.INVISIBLE);
                }

                if (isMonthDate && selectedDate != null && day.getDate().equals(selectedDate))
                {
                    container.tvDayText.setBackgroundResource(R.drawable.bg_calendar_selected_day);
                }
                else
                {
                    container.tvDayText.setBackground(null);
                }
            }
        });

        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(12);
        YearMonth endMonth = currentMonth.plusMonths(12);
        calendarView.setup(startMonth, endMonth, DayOfWeek.SUNDAY);
        calendarView.scrollToMonth(currentMonth);
        updateMonthTitle(currentMonth);

        calendarView.setMonthScrollListener(new Function1<CalendarMonth, Unit>()
        {
            @Override
            public Unit invoke(CalendarMonth month)
            {
                updateMonthTitle(month.getYearMonth());
                return Unit.INSTANCE;
            }
        });
    }

    private void updateMonthTitle(YearMonth month)
    {
        tvMonthTitle.setText(getString(
                R.string.checkin_month_title,
                month.getYear(),
                month.getMonthValue()
        ));
    }

    private void loadCheckedDates()
    {
        checkedDates.clear();
        Set<String> dates = CheckInStore.getCheckInDates(this);
        for (String date : dates)
        {
            LocalDate parsed = parseLocalDate(date);
            if (parsed != null)
            {
                checkedDates.add(parsed);
            }
        }
    }

    private static LocalDate parseLocalDate(String date)
    {
        int[] parts = parseDateParts(date);
        if (parts == null)
        {
            return null;
        }
        return LocalDate.of(parts[0], parts[1], parts[2]);
    }

    private static int[] parseDateParts(String date)
    {
        if (date == null || date.length() != 10)
        {
            return null;
        }
        try
        {
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(5, 7));
            int day = Integer.parseInt(date.substring(8, 10));
            return new int[]{year, month, day};
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private class DayViewContainer extends ViewContainer
    {
        private final TextView tvDayText;
        private final View viewCheckInDot;
        private CalendarDay day;

        private DayViewContainer(View view)
        {
            super(view);
            tvDayText = view.findViewById(R.id.tvDayText);
            viewCheckInDot = view.findViewById(R.id.viewCheckInDot);
            view.setOnClickListener(v -> {
                if (day == null || day.getPosition() != DayPosition.MonthDate)
                {
                    return;
                }
                LocalDate oldSelected = selectedDate;
                selectedDate = day.getDate();
                updateSelectedStatus(selectedDate);
                if (oldSelected != null)
                {
                    calendarView.notifyDateChanged(oldSelected);
                }
                calendarView.notifyDateChanged(selectedDate);
            });
        }
    }
}
