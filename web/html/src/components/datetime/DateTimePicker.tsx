import "react-datepicker/dist/react-datepicker.css";

import { forwardRef, useRef } from "react";

import ReactDatePicker from "react-datepicker";

import { localizedMoment, parseTimeString } from "utils";

// Turn this on to view internal state under the picker in the UI
const SHOW_DEBUG_VALUES = false;

type InputPassthroughProps = {
  "data-id": string | undefined;
};

const InputPassthrough = forwardRef<HTMLInputElement, React.HTMLProps<HTMLInputElement> & InputPassthroughProps>(
  (props, ref) => {
    // react-datepicker internally resets the id prop so we use a named prop to bypass the issue
    const { "data-id": dataId, ...rest } = props;
    return <input ref={ref} {...rest} id={dataId} />;
  }
);

type Props = {
  id?: string;
  legacyId?: string;
  value: moment.Moment;
  onChange: (value: moment.Moment) => void;
  hideDatePicker?: boolean;
  hideTimePicker?: boolean;
  // By default date times are shown in the user's configured time zone. Setting this property will default to the server time zone instead.
  serverTimeZone?: boolean;
};

export const DateTimePicker = (props: Props) => {
  // See https://github.com/date-fns/date-fns/blob/main/docs/unicodeTokens.md
  const DATE_FORMAT = "yyyy-MM-dd";
  const TIME_FORMAT = "HH:mm";

  const datePickerRef = useRef<ReactDatePicker | null>(null);
  const timePickerRef = useRef<ReactDatePicker | null>(null);

  const hideDatePicker = props.hideDatePicker ?? false;
  const hideTimePicker = props.hideTimePicker ?? false;
  const timeZone = props.serverTimeZone ? localizedMoment.serverTimeZone : localizedMoment.userTimeZone;

  // Legacy id offers compatibility with the DateTimePickerTag.java format
  const datePickerId = props.legacyId
    ? `${props.legacyId}_datepicker_widget_input`
    : props.id
    ? props.id + "_date"
    : undefined;
  const timePickerId = props.legacyId
    ? `${props.legacyId}_timepicker_widget_input`
    : props.id
    ? props.id + "_time"
    : undefined;

  const openDatePicker = () => {
    datePickerRef.current?.setOpen(true);
  };

  const openTimePicker = () => {
    timePickerRef.current?.setOpen(true);
  };

  const onChange = (date: Date | null) => {
    // Currently we don't support propagating null values, we might want to do this in the future
    if (date === null) {
      return;
    }
    // The date we get here is now in the browsers local timezone but with values that should be reinterpreted
    // as the users configured timezone.
    const newValue =
      // We wrap everything in localizedMoment again to have a consistent internal value in utc.
      localizedMoment(
        // We first clone the date again to not modify the original. This has the unintended side effect of converting to
        // UTC and adjusting the values.
        localizedMoment(date)
          // To get back to the values we want we just convert back to the browsers local timezone as it was before.
          .local()
          // Then we set the timezone of the date to the users configured timezone without adjusting the values.
          .tz(timeZone, true)
      );
    if (props.value.valueOf() !== newValue.valueOf()) {
      props.onChange(newValue);
    }
  };

  // We use localizedMoment to clone the date so we don't modify the original
  const browserTimezoneValue = localizedMoment(props.value)
    // We convert the date to the user's or server's configured timezone because this is what we want to show the user
    .tz(timeZone)
    // The react-datepicker component only shows the browsers local timezone and will convert any date to that
    // before showing so since we already got the date with the right values we now pretend the date we have is in
    // the browsers local timezone but without changing its values. This will prevent the react component from
    // converting it again.
    .local(true);

  // Fix https://github.com/Hacker0x01/react-datepicker/issues/3176#issuecomment-1262100937
  const popperModifiers = [
    {
      name: "arrow",
      options: {
        padding: ({ popper, reference, placement }) => ({
          right: Math.min(popper.width, reference.width) - 24,
        }),
      },
    },
  ];

  const previousMonth = t("Previous month");
  const nextMonth = t("Next month");
  const previousYear = t("Previous year");
  const nextYear = t("Next year");

  return (
    <>
      <div className="input-group">
        {hideDatePicker ? null : (
          <>
            <span
              key="calendar"
              className="input-group-addon input-group-text"
              data-picker-type="date"
              onClick={() => openDatePicker()}
            >
              &nbsp;<i className="fa fa-calendar"></i>
            </span>
            <ReactDatePicker
              key="date-picker"
              /**
               * Here and below, since an element with this id doesn't exist it will be created for the portal in the document
               * body. Please don't remove this as it otherwise breaks z-index stacking.
               */
              portalId="date-picker-portal"
              ref={datePickerRef}
              selected={browserTimezoneValue.toDate()}
              onChange={onChange}
              dateFormat={DATE_FORMAT}
              wrapperClassName="form-control date-time-picker-wrapper"
              popperModifiers={popperModifiers}
              // This is used by Cucumber to check whether the picker is open
              popperClassName="date-time-picker-popup"
              customInput={
                <InputPassthrough
                  data-id={datePickerId}
                  // TODO: The styling logic here is hacky, would be nice to clean it up once everything works
                  className="form-control no-right-border"
                  // This is used by Cucumber to interact with the component
                  data-testid="date-picker"
                  maxLength={10}
                />
              }
              previousMonthAriaLabel={previousMonth}
              previousMonthButtonLabel={previousMonth}
              nextMonthAriaLabel={nextMonth}
              nextMonthButtonLabel={nextMonth}
              previousYearAriaLabel={previousYear}
              previousYearButtonLabel={previousYear}
              nextYearAriaLabel={nextYear}
              nextYearButtonLabel={nextYear}
            />
          </>
        )}
        {hideTimePicker ? null : (
          <>
            <span
              key="clock"
              className="input-group-addon input-group-text no-right-border"
              data-picker-type="time"
              onClick={openTimePicker}
            >
              &nbsp;<i className="fa fa-clock-o"></i>
            </span>
            <ReactDatePicker
              key="time-picker"
              portalId="time-picker-portal"
              ref={timePickerRef}
              selected={browserTimezoneValue.toDate()}
              onChange={(date, event) => {
                // If this fires without an event, it means the user picked a time from the dropdown
                // This handler is only used the dropdown selection, onChangeRaw() handles regular user input
                if (date === null || event) {
                  return;
                }
                /**
                 * NB! Only take the hours and minutes from this change event since react-datepicker updates the date
                 * value when it should only update the time value (bsc#1202991, bsc#1215820)
                 */
                const mergedDate = browserTimezoneValue.toDate();
                mergedDate.setHours(date.getHours(), date.getMinutes());
                onChange(mergedDate);
              }}
              onChangeRaw={(event) => {
                // In case the user pastes a value, clean it up and cut it to max length
                const rawValue = event.target.value.replaceAll(/[^\d:]/g, "");
                const cutValue = rawValue.includes(":") ? rawValue.substring(0, 5) : rawValue.substring(0, 4);
                if (cutValue !== event.target.value) {
                  event.target.value = cutValue;
                }

                const parsed = parseTimeString(cutValue);
                if (!parsed) {
                  return;
                }
                const mergedDate = browserTimezoneValue.toDate();
                mergedDate.setHours(parsed.hours, parsed.minutes);
                onChange(mergedDate);
              }}
              showTimeSelect
              showTimeSelectOnly
              // We want the regular primary display to only show the time here, so using TIME_FORMAT is intentional
              dateFormat={TIME_FORMAT}
              timeFormat={TIME_FORMAT}
              wrapperClassName="form-control date-time-picker-wrapper"
              popperModifiers={popperModifiers}
              // This is used by Cucumber to check whether the picker is open
              popperClassName="date-time-picker-popup"
              customInput={
                <InputPassthrough
                  data-id={timePickerId}
                  className="form-control"
                  // This is used by Cucumber to interact with the component
                  data-testid="time-picker"
                />
              }
            />
          </>
        )}
        <span className="input-group-addon input-group-text" key="tz">
          {timeZone}
        </span>
      </div>
      {process.env.NODE_ENV !== "production" && SHOW_DEBUG_VALUES ? (
        <pre>
          user:{"   "}
          {props.value.toUserDateTimeString()} ({localizedMoment.userTimeZone})<br />
          server: {props.value.toServerDateTimeString()} ({localizedMoment.serverTimeZone})<br />
          iso:{"    "}
          {props.value.toISOString()}
        </pre>
      ) : null}
    </>
  );
};
