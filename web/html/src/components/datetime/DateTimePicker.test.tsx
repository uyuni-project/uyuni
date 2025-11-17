import { useState } from "react";

import { localizedMoment } from "utils";
import { render, screen, type } from "utils/test-utils";

import { DateTimePicker } from "./DateTimePicker";

describe("DateTimePicker", () => {
  const getInputs = () => {
    const datePicker = screen.getByTestId<HTMLInputElement>("date-picker");
    const timePicker = screen.getByTestId<HTMLInputElement>("time-picker");
    return { datePicker, timePicker };
  };

  test("prop value is rendered in the user's timezone", () => {
    const validISOString = "2020-02-01T04:00:00.000Z";
    const Setup = () => {
      const [value, setValue] = useState(localizedMoment(validISOString));
      return <DateTimePicker value={value} onChange={setValue} />;
    };
    render(<Setup />);

    const { datePicker, timePicker } = getInputs();
    // These values need to be offset by the user's timezone
    expect(datePicker.value).toEqual("2020-01-31");
    expect(timePicker.value).toEqual("20:00");
  });

  test("picking a date and time uses the user's timezone", (done) => {
    const validISOString = "2020-02-01T04:00:00.000Z";
    let changeEventCount = 0;

    const Setup = () => {
      const [value, setValue] = useState(localizedMoment(validISOString));
      return (
        <DateTimePicker
          value={value}
          onChange={(newValue) => {
            changeEventCount += 1;
            setValue(newValue);

            if (changeEventCount === 2) {
              // Note that this value should be in a different day due to the timezone offset
              expect(newValue.toISOString()).toEqual("2020-01-17T07:30:00.000Z");
              done();
            }
          }}
        />
      );
    };
    render(<Setup />);

    const { datePicker, timePicker } = getInputs();
    datePicker.click();
    screen.getByText("16").click();
    timePicker.click();
    screen.getByText("23:30").click();

    expect(datePicker.value).toEqual("2020-01-16");
    expect(timePicker.value).toEqual("23:30");
  });

  test("clearing or manually editing the time input doesn't change the date (bsc#1210253, bsc#1215820)", (done) => {
    const validISOString = "2020-01-30T15:00:00.000Z";
    let changeEventCount = 0;

    const Setup = () => {
      const [value, setValue] = useState(localizedMoment(validISOString));
      return (
        <DateTimePicker
          value={value}
          onChange={(newValue) => {
            changeEventCount += 1;
            setValue(newValue);

            if (changeEventCount === 2) {
              expect(newValue.toUserDateTimeString()).toEqual("2020-01-16 00:00");
              done();
            }
          }}
        />
      );
    };
    render(<Setup />);

    const { datePicker, timePicker } = getInputs();
    datePicker.click();
    screen.getByText("16").click();
    type(timePicker, "0", false);
  });

  test("picking a time from the dropdown works", (done) => {
    const validISOString = "2020-02-01T04:00:00.000Z";

    const Setup = () => {
      const [value, setValue] = useState(localizedMoment(validISOString));
      return (
        <DateTimePicker
          value={value}
          onChange={(newValue) => {
            setValue(newValue);

            expect(newValue.toUserDateTimeString()).toEqual("2020-01-31 14:30");
            done();
          }}
        />
      );
    };
    render(<Setup />);

    const { timePicker } = getInputs();
    timePicker.click();
    screen.getByText("14:30").click();
  });

  test("manually entering a time value works", (done) => {
    const validISOString = "2020-02-01T04:00:00.000Z";

    const Setup = () => {
      const [value, setValue] = useState(localizedMoment(validISOString));
      return (
        <DateTimePicker
          value={value}
          onChange={(newValue) => {
            setValue(newValue);

            expect(newValue.toUserDateTimeString()).toEqual("2020-01-31 23:45");
            done();
          }}
        />
      );
    };
    render(<Setup />);

    const { timePicker } = getInputs();
    type(timePicker, "23:45");
  });
});
