import * as React from "react";
import { useState, useEffect, useRef } from "react";

import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import allLocales from "@fullcalendar/core/locales-all";

import { MessageType, Utils as MessagesUtils } from "components/messages";
import Network from "utils/network";

type WebCalendarProps = {
  id: number;
  type: string;
  messages: (messages: MessageType[]) => void;
  clearMessages: (messages: void) => void;
  responseError: (messages: MessageType[]) => void;
};

type Event = {
  start: string;
  end: string;
  title: string;
}

const WebCalendar = (props: WebCalendarProps) => {

  const [events, setEvents] = useState<Event[]>([]);
  const [currentDate, setCurrentDate] = useState<Date>();
  const calendarRef = useRef<any>();

  useEffect(() => {
    getEvents("initial");
    setCurrentDate(getApi().currentDataManager.data.currentDate);
    // Hack: Make Fullcalendar button icons show correctly
    jQuery(".fc-backButton-button").html('<div class="fa fa-angle-left"></div>');
    jQuery(".fc-nextButton-button").html('<div class="fa fa-angle-right"></div>');
    jQuery(".fc-skipBackButton-button").html('<div class="fa fa-angle-double-left"></div>')
      .prop('title', t("Skip to the last maintenance window"));
    jQuery(".fc-skipNextButton-button").html('<div class="fa fa-angle-double-right"></div>')
      .prop('title', t("Skip to the next maintenance window"));
  }, []);

  const getApi = () => {
    return calendarRef.current.getApi();
  };

  const getEvents = (operation) => {
    const date = getDate(operation);
    if (operation === "initial" || needsUpdate(date)) {
      const endpoint = `/rhn/manager/api/maintenance/events/${operation}/${props.type}/${date.valueOf()}/${props.id}`;
      return Network.get(endpoint)
        .then(events => {
          setEvents(events);
          navigateTo(operation);
          props.clearMessages();
          setCurrentDate(getApi().currentDataManager.data.currentDate);
        }).catch(props.responseError)
    }
    navigateTo(operation);
  };

  const onSkipBack = () => {
    // Get first date of currently displayed range
    const date = getDate("back");
    // Check if we need to update the events
    if (!needsUpdate(date) && skipToPrevEvent(date)) {
      return;
    }
    const endpoint = `/rhn/manager/api/maintenance/events/skipBack/${props.type}/${date.valueOf()}/${props.id}`;
    return Network.get(endpoint)
      .then(events => {
        if (events.length === 0) {
          props.messages(MessagesUtils.info(t("There are no more past maintenance windows")));
        } else {
          setEvents(events);
          // Skip to next event that is not in current month
          const filteredEvents = events.filter(event =>
            moment.parseZone(event.start).month() !== moment(currentDate).month());
          const lastEvent = moment.parseZone(filteredEvents[filteredEvents.length - 1].start);
          getApi().gotoDate(lastEvent.format("YYYY-MM-DD"));
          props.clearMessages();
          setCurrentDate(getApi().currentDataManager.data.currentDate);
        }
      }).catch(props.responseError)
  };

  const onSkipNext = () => {
    // Get last date of currently displayed range
    let date = getDate("next");
    // Check if we need to update the events
    if (!needsUpdate(date) && skipToNextEvent(date)) {
      return;
    }
    // If we are in day view and need to update we want to make sure to always use the first day of the next month
    if (getApi().currentDataManager.data.currentViewType === "timeGridDay" && date.date() !== 1) {
      date = date.add(1, 'month').startOf('month');
    }
    const endpoint = `/rhn/manager/api/maintenance/events/skipNext/${props.type}/${date.valueOf()}/${props.id}`;
    return Network.get(endpoint)
      .then(events => {
        if (events.length === 0) {
          props.messages(MessagesUtils.info(t("There are no more future maintenance windows")));
        } else {
          setEvents(events);
          // Skip to next event that is not in current month
          const firstEvent = moment.parseZone(events.filter(event =>
            moment.parseZone(event.start).month() !== moment(currentDate).month())[0].start
          );
          getApi().gotoDate(firstEvent.format("YYYY-MM-DD"));
          props.clearMessages();
          setCurrentDate(getApi().currentDataManager.data.currentDate);
        }
      }).catch(props.responseError)
  };

  const onClickMonth = () => {
    if (getApi().currentDataManager.data.currentViewType === "dayGridMonth") {
      return;
    }
    if (needsUpdate(moment(getApi().currentDataManager.data.currentDate))) {
      getEvents("current");
    }
    getApi().changeView("dayGridMonth", getApi().currentDataManager.data.currentDate);
  };

  const onClickDay = () => {
    getApi().changeView("timeGridDay", getApi().currentDataManager.data.currentDate);
  };

  // Switch to the day view of the clicked date
  const onDateClick = (info) => {
    setCurrentDate(getApi().currentDataManager.data.currentDate);
    getApi().changeView('timeGridDay', info.date);
    if (needsUpdate(moment(info.date))) {
      getEvents("current");
    }
  };

  // Get the date used to fetch events based on the operation
  const getDate = (operation) => {
    switch (operation) {
      case "next":
        return moment(getApi().currentDataManager.state.dateProfile.currentRange.end);
      case "back":
        // currentRange.start returns the first day of the displayed month, but we want to end up in
        // the previous month so we subtract one day in this case
        return moment(getApi().currentDataManager.state.dateProfile.currentRange.start).subtract(1, "day");
      case "current":
        return moment(getApi().currentDataManager.data.currentDate);
      default:
        return moment(getApi().currentDataManager.data.dateProfileGenerator.nowDate);
    }
  };

  const navigateTo = (operation) => {
    switch (operation) {
      case "next":
        getApi().next();
        break;
      case "back":
        getApi().prev();
        break;
      case "current":
        getApi().gotoDate(getApi().currentDataManager.data.currentDate);
        break;
      default:
        getApi().today();
    }
  }

  const needsUpdate = (date) => {
    return date.month() !== moment(currentDate).month() ||
      date.year() !== moment(currentDate).year();
  };

  const skipToNextEvent = (date) => {
    // We are only interested in events from the same month that are later or equal to 'date'
    const filteredEvents = events.filter(event => (
      moment.parseZone(event.start).month() === moment(currentDate).month() &&
      moment.parseZone(event.start).date() >= date.date())
    );
    // Only perform action if there is an event to go to
    if (filteredEvents.length > 0) {
      getApi().gotoDate(filteredEvents[0].start);
    }
    // Return true if there's an event false otherwise
    return filteredEvents.length > 0;
  }

  const skipToPrevEvent = (date) => {
    // We are only interested in events from the same month that are earlier or equal to 'date'
    const filteredEvents = events.filter(event => (
      moment.parseZone(event.start).month() === moment(currentDate).month() &&
      moment.parseZone(event.start).date() <= date.date())
    );
    // Only perform action if there is an event to go to
    if (filteredEvents.length > 0) {
      getApi().gotoDate(filteredEvents[filteredEvents.length - 1].start);
    }
    // Return true if there's an event false otherwise
    return filteredEvents.length > 0;
  }

  return (
    <div>
      <FullCalendar
        ref={calendarRef}
        timeZone={""} // Prevent FullCalendar from using the browsers set timezone
        locales={allLocales}
        // locale strings come with '_' from the backend but FullCalendar expects them with '-' so we exchange these
        locale={window.preferredLocale.replace("_", "-")}
        plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
        customButtons={{
          skipBackButton: {
            click: () => onSkipBack()
          },
          backButton: {
            click: () => getEvents("back")
          },
          todayButton: {
            text: t("Today"),
            click: () => getEvents("today")
          },
          nextButton: {
            click: () => getEvents("next")
          },
          skipNextButton: {
            click: () => onSkipNext()
          },
          monthButton: {
            text: t("Month"),
            click: () => onClickMonth()
          },
          dayButton: {
            text: t("Day"),
            click: () => onClickDay()
          }
        }}
        height={"800px"}
        headerToolbar={{
          left: "skipBackButton backButton,todayButton,nextButton skipNextButton",
          center: "title",
          right: "monthButton,dayButton"
        }}
        eventTimeFormat={{
          hour: "numeric",
          minute: "2-digit",
          meridiem: "short"
        }}
        initialView="dayGridMonth"
        initialEvents={events}
        dateClick={onDateClick}
        events={events}
        editable={false}
        eventsSet={() => setEvents(events)}
        eventDisplay={"block"}
        nowIndicator={true}
      />
    </div>
  );
};

export {
  WebCalendar
};
