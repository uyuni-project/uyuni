import * as React from "react";
// TODO: This should eventually be localizedMoment instead
/* eslint-disable local-rules/no-raw-date */
import moment from "moment";

import { click, render, screen, server, waitFor } from "utils/test-utils";
import { WebCalendar } from "manager/maintenance/calendar/web-calendar";

const initialDate = moment.utc("2021-07-09", "YYYY-MM-DD");

function getApiPath(operation: string, date: moment.Moment) {
  return `/rhn/manager/api/maintenance/events/${operation}/schedule/0/${date.valueOf()}/0`;
}

describe("Web calendar", () => {
  test("Load initial events", async () => {
    const data = [
      {
        title: "July Maintenance Window",
        start: "2021-07-15T08:00 CEST",
        end: "2021-07-15T10:00 CEST",
      },
    ];

    server.mockGetJson(getApiPath("initial", initialDate), data);

    render(
      <WebCalendar
        id={0}
        type={"schedule"}
        eventNames={[]}
        messages={() => {}}
        clearMessages={() => {}}
        responseError={() => {}}
        date={initialDate}
      />
    );

    await waitFor(() => {
      screen.getByText("July 2021");
      screen.getByText("8:00am CEST July Maintenance Window");
    });
  });

  test("Next button", async () => {
    const data = [
      {
        title: "August Maintenance Window",
        start: "2021-08-15T09:00 CEST",
        end: "2021-08-15T12:00 CEST",
      },
    ];

    const date = moment.utc("2021-08-01", "YYYY-MM-DD");

    server.mockGetJson(getApiPath("initial", initialDate), []);
    server.mockGetJson(getApiPath("next", date), data);

    render(
      <WebCalendar
        id={0}
        type={"schedule"}
        eventNames={[]}
        messages={() => {}}
        clearMessages={() => {}}
        responseError={() => {}}
        date={initialDate}
      />
    );

    await waitFor(() => {
      screen.getByText("July 2021");
    });

    const nextButton = screen.getByTestId("next") as HTMLButtonElement;
    click(nextButton);

    await waitFor(() => {
      screen.getByText("August 2021");
      screen.getByText("9:00am CEST August Maintenance Window");
    });
  });

  test("Back button", async () => {
    const data = [
      {
        title: "June Maintenance Window",
        start: "2021-06-15T09:00 CEST",
        end: "2021-06-15T12:00 CEST",
      },
    ];

    const date = moment.utc("2021-06-30", "YYYY-MM-DD");

    server.mockGetJson(getApiPath("initial", initialDate), []);
    server.mockGetJson(getApiPath("back", date), data);

    render(
      <WebCalendar
        id={0}
        type={"schedule"}
        eventNames={[]}
        messages={() => {}}
        clearMessages={() => {}}
        responseError={() => {}}
        date={initialDate}
      />
    );

    await waitFor(() => {
      screen.getByText("July 2021");
    });

    const nextButton = screen.getByTestId("back") as HTMLButtonElement;
    click(nextButton);

    await waitFor(() => {
      screen.getByText("June 2021");
      screen.getByText("9:00am CEST June Maintenance Window");
    });
  });

  test("Skip next button", async () => {
    const data = [
      {
        title: "October Maintenance Window",
        start: "2021-10-15T09:00 CEST",
        end: "2021-10-15T12:00 CEST",
      },
    ];

    const date = moment.utc("2021-08-01", "YYYY-MM-DD");

    server.mockGetJson(getApiPath("initial", initialDate), []);
    server.mockGetJson(getApiPath("skipNext", date), data);

    render(
      <WebCalendar
        id={0}
        type={"schedule"}
        eventNames={[]}
        messages={() => {}}
        clearMessages={() => {}}
        responseError={() => {}}
        date={initialDate}
      />
    );

    await waitFor(() => {
      screen.getByText("July 2021");
    });

    const nextButton = screen.getByTestId("skip-next") as HTMLButtonElement;
    click(nextButton);

    await waitFor(() => {
      screen.getByText("October 2021");
      screen.getByText("9:00am CEST October Maintenance Window");
    });
  });

  test("Skip back button", async () => {
    const data = [
      {
        title: "April Maintenance Window",
        start: "2021-04-15T09:00 CEST",
        end: "2021-04-15T12:00 CEST",
      },
    ];

    const date = moment.utc("2021-06-30", "YYYY-MM-DD");

    server.mockGetJson(getApiPath("initial", initialDate), []);
    server.mockGetJson(getApiPath("skipBack", date), data);

    render(
      <WebCalendar
        id={0}
        type={"schedule"}
        eventNames={[]}
        messages={() => {}}
        clearMessages={() => {}}
        responseError={() => {}}
        date={initialDate}
      />
    );

    await waitFor(() => {
      screen.getByText("July 2021");
    });

    const nextButton = screen.getByTestId("skip-back") as HTMLButtonElement;
    click(nextButton);

    await waitFor(() => {
      screen.getByText("April 2021");
      screen.getByText("9:00am CEST April Maintenance Window");
    });
  });

  test("Today button", async () => {
    const data = [
      {
        title: "Today's Maintenance Window",
        start: "2021-07-09T09:00 CEST",
        end: "2021-07-09T12:00 CEST",
      },
    ];

    const date = moment.utc("2021-08-01", "YYYY-MM-DD");

    server.mockGetJson(getApiPath("initial", initialDate), []);
    server.mockGetJson(getApiPath("next", date), []);
    server.mockGetJson(getApiPath("today", initialDate), data);

    render(
      <WebCalendar
        id={0}
        type={"schedule"}
        eventNames={[]}
        messages={() => {}}
        clearMessages={() => {}}
        responseError={() => {}}
        date={initialDate}
      />
    );

    await waitFor(() => {
      screen.getByText("July 2021");
    });

    const nextButton = screen.getByTestId("next") as HTMLButtonElement;
    click(nextButton);

    await waitFor(() => {
      screen.getByText("August 2021");
    });

    const todayButton = screen.getByRole("button", { name: "Today" }) as HTMLButtonElement;
    click(todayButton);

    await waitFor(() => {
      screen.getByText("July 2021");
      screen.getByText("9:00am CEST Today's Maintenance Window");
    });

    const dayButton = screen.getByRole("button", { name: "Day" }) as HTMLButtonElement;
    click(dayButton);

    await waitFor(() => {
      screen.getByText("July 9, 2021");
    });
  });

  test("Day and Month buttons", async () => {
    const data = [
      {
        title: "Today's Maintenance Window",
        start: "2021-07-09T09:00 CEST",
        end: "2021-07-09T12:00 CEST",
      },
    ];

    server.mockGetJson(getApiPath("initial", initialDate), data);

    render(
      <WebCalendar
        id={0}
        type={"schedule"}
        eventNames={[]}
        messages={() => {}}
        clearMessages={() => {}}
        responseError={() => {}}
        date={initialDate}
      />
    );

    await waitFor(() => {
      screen.getByText("July 2021");
      screen.getByText("9:00am CEST Today's Maintenance Window");
    });

    const dayButton = screen.getByRole("button", { name: "Day" }) as HTMLButtonElement;
    click(dayButton);

    await waitFor(() => {
      screen.getByText("July 9, 2021");
    });

    const monthButton = screen.getByRole("button", { name: "Month" }) as HTMLButtonElement;
    click(monthButton);

    await waitFor(() => {
      screen.getByText("July 2021", { exact: false });
      screen.getByText("9:00am CEST Today's Maintenance Window");
    });
  });
});
