/**
 * Copyright (c) 2020 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.maintenance;

import static java.util.Collections.emptySet;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import com.redhat.rhn.common.localization.LocalizationService;

import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.HasPropertyRule;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.Summary;

/**
 * Computation related to the ICalendar objects
 */
public class IcalUtils {

    private static Logger log = Logger.getLogger(IcalUtils.class);

    /**
     * Given MaintenanceSchedule calculate upcoming maintenance windows
     *
     * The windows are returned as a list of triples consisting of:
     * - window start date as a human-readable string
     * - window end date as a human-readable string
     * - start date as number of milliseconds since the epoch
     *
     * The formatting is done by {@link LocalizationService}.
     *
     * The upper limit of returned maintenance windows is currently hardcoded to 10.
     *
     * @param schedule the given MaintenanceSchedule
     * @return the optional upcoming maintenance windows
     */
    public Optional<List<MaintenanceWindowData>> calculateUpcomingMaintenanceWindows(MaintenanceSchedule schedule) {
        Optional<String> multiScheduleName = getScheduleNameForMulti(schedule);

        Stream<Pair<Instant, Instant>> periodStream = schedule.getCalendarOpt()
                .flatMap(c -> parseCalendar(c))
                .map(c -> calculateUpcomingPeriods(c, multiScheduleName, Instant.now(), 10))
                .orElseGet(Stream::empty);

        List<MaintenanceWindowData> result = periodStream
                .map(p -> new MaintenanceWindowData(p.getLeft(), p.getRight()))
                .collect(toList());
        return of(result);
    }

    /**
     * Convenience method: return schedule name if the schedule type is MULTI, return empty otherwise
     * @param schedule the schedule
     * @return optional of schedule name
     */
    private static Optional<String> getScheduleNameForMulti(MaintenanceSchedule schedule) {
        if (schedule.getScheduleType() == MaintenanceSchedule.ScheduleType.MULTI) {
            return of(schedule.getName());
        }
        return empty();
    }

    /**
     * THIS IS ONLY PUBLIC FOR TESTING.
     *
     * Calculate upcoming maintenance windows starting from given date based on calendar and optional filter name
     * (in case we're dealing with MULTI calendar and want to filter only events we're interested in).
     *
     * The algorithm only checks maintenance windows within roughly a year and a month since the startDate.
     *
     * @param calendar the {@link Calendar}
     * @param eventName for MULTI calendars: only deal with events with this name, filter out the rest
     * @param startDate the start date
     * @param limit upper limit of maintenance windows to return
     * @return the list of upcoming maintenance windows
     */
    public Stream<Pair<Instant, Instant>> calculateUpcomingPeriods(Calendar calendar, Optional<String> eventName,
            Instant startDate, int limit) {
        ComponentList<CalendarComponent> allEvents = calendar.getComponents(Component.VEVENT);

        Collection<CalendarComponent> filteredEvents = eventName.map(name -> {
            Predicate<CalendarComponent> summary = c -> c.getProperty("SUMMARY").equals(name);
            Predicate<CalendarComponent>[] ps = new Predicate[]{summary};
            Filter<CalendarComponent> filter = new Filter<>(ps, Filter.MATCH_ALL);
            return filter.filter(allEvents);
        }).orElse(allEvents);

        // we will look a year and month to the future
        Period period = new Period(new DateTime(startDate.toEpochMilli()), Duration.ofDays(365 + 31));

        List<PeriodList> periodLists = filteredEvents.stream()
                .map(c -> c.calculateRecurrenceSet(period))
                .filter(l -> !l.isEmpty())
                .collect(toList());

        Stream<Pair<Instant, Instant>> sortedLimited = periodLists.stream()
                .map(pl -> pl.stream())
                .reduce(Stream.empty(), Stream::concat)
                .sorted()
                .limit(limit)
                .map(p -> Pair.of(p.getStart().toInstant(), p.getRangeEnd().toInstant()));

        return sortedLimited;
    }

    /**
     * Get all schedules of given calendar at given date.
     * Filter results by summary, if the summary parameter is passed.
     *
     * @param date the date
     * @param calendar the calendar
     * @param summary event summary
     * @return the collection of calendars components matching given date (and optionally summary)
     */
    public Collection<CalendarComponent> getCalendarEventsAtDate(Date date, Optional<Calendar> calendar,
            Optional<String> summary) {
        if (calendar.isEmpty()) {
            return emptySet();
        }

        Period p = new Period(new DateTime(date), java.time.Duration.ofSeconds(1));
        ArrayList<Predicate<Component>> rules = new ArrayList<>();
        rules.add(new PeriodRule<>(p));

        summary.ifPresent(s -> {
            Summary filterSummary = new Summary(s);
            HasPropertyRule<Component> propertyRule = new HasPropertyRule<>(filterSummary);
            rules.add(propertyRule);
        });

        @SuppressWarnings("unchecked")
        Predicate<CalendarComponent>[] comArr = new Predicate[rules.size()];
        comArr = rules.toArray(comArr);

        Filter<CalendarComponent> filter = new Filter<>(comArr, Filter.MATCH_ALL);

        return filter.filter(calendar.get().getComponents(Component.VEVENT));
    }

    /**
     * Read calendar using given reader and parse it
     *
     * @param calendarIn the {@link MaintenanceCalendar}
     * @return the parsed calendar or empty, if there was a problem parsing the calendar
     */
    public Optional<Calendar> parseCalendar(MaintenanceCalendar calendarIn) {
        return parseCalendar(new StringReader(calendarIn.getIcal()));
    }

    /**
     * Read calendar using given reader and parse it
     *
     * @param calendarReader the reader
     * @return the parsed calendar or empty, if there was a problem parsing the calendar
     */
    public Optional<Calendar> parseCalendar(Reader calendarReader) {
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = null;
        try {
            calendar = builder.build(calendarReader);
        }
        catch (IOException | ParserException e) {
            log.error("Unable to build the calendar from reader: " + calendarReader, e);
        }
        return ofNullable(calendar);
    }
}
