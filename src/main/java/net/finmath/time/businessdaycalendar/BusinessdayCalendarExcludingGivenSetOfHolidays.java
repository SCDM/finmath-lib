package net.finmath.time.businessdaycalendar;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * A class for a business day calendar, where every day is a business day, except
 * weekends days provided by a <code>Set</code>.
 *
 * @author Christian Fries
 */
public abstract class BusinessdayCalendarExcludingGivenSetOfHolidays extends BusinessdayCalendarExcludingGivenHolidays {

	/**
	 * 
	 */
	private static final long serialVersionUID = -485496533316101770L;
	private final Set<LocalDate> holidays;
	
	public BusinessdayCalendarExcludingGivenSetOfHolidays(String name, BusinessdayCalendarInterface baseCalendar, boolean isExcludeWeekends, Set<LocalDate> holidays) {
		super(name, baseCalendar, isExcludeWeekends);
		this.holidays = holidays;
	}

	public BusinessdayCalendarExcludingGivenSetOfHolidays(String name, boolean isExcludeWeekends, Set<LocalDate> holidays) {
		this(name, null, isExcludeWeekends, holidays);
	}

	/**
	 * @return A set of (additional) holidays.
	 */
	public Set<LocalDate> getHolidays() { return holidays; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BusinessdayCalendarExcludingGivenSetOfHolidays other = (BusinessdayCalendarExcludingGivenSetOfHolidays) obj;
        if (!Objects.equals(this.holidays, other.holidays)) {
            return false;
        }
        return true;
    }
}