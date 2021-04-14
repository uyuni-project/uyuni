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
  }, []);

  const getApi = () => {
    return calendarRef.current.getApi();
  };

  const getEvents = (operation) => {
    const date = getDate(operation);
    if (operation === "initial" || needsUpdate(date)) {
      const endpoint = `/rhn/manager/api/maintenance/events/${operation}/${props.type}/${date}/${props.id}`;
      return Network.get(endpoint, "application/json").promise
        .then(events => {
          setEvents(events.data);
          navigateTo(operation);
          props.clearMessages();
          setCurrentDate(getApi().currentDataManager.data.currentDate);
        }).catch(props.responseError)
    }
    navigateTo(operation);
  };

  const onClickMonth = () => {
    if (getApi().currentDataManager.data.currentViewType === "dayGridMonth") {
      return;
    }
    if (needsUpdate(getApi().currentDataManager.data.currentDate)) {
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
    if (needsUpdate(info.date)) {
      getEvents("current");
    }
  };

  // Get the date used to fetch events based on the operation
  const getDate = (operation) => {
    switch (operation) {
      case "next":
        return Date.parse(getApi().currentDataManager.state.dateProfile.currentRange.end);
      case "back":
        // currentRange.start returns the first day of the displayed month, but we want to end up in
        // the previous month so we subtract one day in this case
        return Date.parse(getApi().currentDataManager.state.dateProfile.currentRange.start) - 24 * 60 * 60 * 1000;
      case "current":
        return Date.parse(getApi().currentDataManager.data.currentDate);
      default:
        return Date.parse(getApi().currentDataManager.data.dateProfileGenerator.nowDate);
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
    return (moment(date).month() !== moment(currentDate).month() ||
      moment(date).year() !== moment(currentDate).year());
  };

  return (
    <div>
      <FullCalendar
        ref={calendarRef}
        timeZone={""} // Prevent FullCalendar from using the browsers set timezone
        locales={allLocales}
        locale={window.preferredLocale.replace("_", "-")}
        plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
        customButtons={{
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
          left: "backButton,todayButton,nextButton",
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
