// TODO: This should eventually be localizedMoment instead
/* eslint-disable local-rules/no-raw-date */
import moment from "moment";

import { WebCalendar } from "manager/maintenance/calendar/web-calendar";

import { click, render, screen, server } from "utils/test-utils";

const initialDate = moment.utc("2021-07-09", "YYYY-MM-DD");

function getApiPath(operation: string, date: moment.Moment) {
  return `/rhn/manager/api/maintenance/events/${operation}/schedule/0/${date.valueOf()}/0`;
}

function renderWebCalendar(date = initialDate) {
  return render(
    <WebCalendar
      id={0}
      type={"schedule"}
      eventNames={[]}
      messages={() => {}}
      clearMessages={() => {}}
      responseError={() => {}}
      date={date}
    />
  );
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

    renderWebCalendar();

    expect(await screen.findByText("July 2021")).toBeDefined();
    expect(await screen.findByText("8:00am CEST July Maintenance Window")).toBeDefined();
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

    renderWebCalendar();

    expect(await screen.findByText("July 2021")).toBeDefined();

    const nextButton = screen.getByTestId("next") as HTMLButtonElement;
    await click(nextButton);

    expect(await screen.findByText("August 2021")).toBeDefined();
    expect(await screen.findByText("9:00am CEST August Maintenance Window")).toBeDefined();
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

    renderWebCalendar();

    expect(await screen.findByText("July 2021")).toBeDefined();

    const nextButton = screen.getByTestId("back") as HTMLButtonElement;
    await click(nextButton);

    expect(await screen.findByText("June 2021")).toBeDefined();
    expect(await screen.findByText("9:00am CEST June Maintenance Window")).toBeDefined();
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

    renderWebCalendar();

    expect(await screen.findByText("July 2021")).toBeDefined();

    const nextButton = screen.getByTestId("skip-next") as HTMLButtonElement;
    await click(nextButton);

    expect(await screen.findByText("October 2021")).toBeDefined();
    expect(await screen.findByText("9:00am CEST October Maintenance Window")).toBeDefined();
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

    renderWebCalendar();

    expect(await screen.findByText("July 2021")).toBeDefined();

    const nextButton = screen.getByTestId("skip-back") as HTMLButtonElement;
    await click(nextButton);

    expect(await screen.findByText("April 2021")).toBeDefined();
    expect(await screen.findByText("9:00am CEST April Maintenance Window")).toBeDefined();
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

    renderWebCalendar();

    expect(await screen.findByText("July 2021")).toBeDefined();

    const nextButton = screen.getByTestId("next") as HTMLButtonElement;
    await click(nextButton);

    expect(await screen.findByText("August 2021")).toBeDefined();

    const todayButton = screen.getByRole("button", { name: "Today" }) as HTMLButtonElement;
    await click(todayButton);

    expect(await screen.findByText("July 2021")).toBeDefined();
    expect(await screen.findByText("9:00am CEST Today's Maintenance Window")).toBeDefined();

    const dayButton = screen.getByRole("button", { name: "Day" }) as HTMLButtonElement;
    await click(dayButton);

    expect(await screen.findByText("July 9, 2021")).toBeDefined();
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

    renderWebCalendar();

    expect(await screen.findByText("July 2021")).toBeDefined();
    expect(await screen.findByText("9:00am CEST Today's Maintenance Window")).toBeDefined();

    const dayButton = screen.getByRole("button", { name: "Day" }) as HTMLButtonElement;
    await click(dayButton);

    expect(await screen.findByText("July 9, 2021")).toBeDefined();

    const monthButton = screen.getByRole("button", { name: "Month" }) as HTMLButtonElement;
    await click(monthButton);

    expect(await screen.findByText("July 2021", { exact: false })).toBeDefined();
    expect(await screen.findByText("9:00am CEST Today's Maintenance Window")).toBeDefined();
  });
});
