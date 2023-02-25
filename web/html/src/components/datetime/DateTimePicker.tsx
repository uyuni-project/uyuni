import "react-datepicker/dist/react-datepicker.css";

import { forwardRef, useRef } from "react";

import ReactDatePicker from "react-datepicker";

import { localizedMoment } from "utils";

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

const InputPassthrough = forwardRef<HTMLInputElement, React.HTMLProps<HTMLInputElement>>((props, ref) => {
  return <input ref={ref} {...props} />;
});

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

  const onChange = (newDateValue: Date) => {
    const newValue = localizedMoment(newDateValue).tz(timeZone);
    if (props.value.valueOf() !== newValue.valueOf()) {
      props.onChange(newValue);
    }
  };

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

  return (
    <div className="input-group">
      {hideDatePicker ? null : (
        <>
          <span key="calendar" className="input-group-addon" data-picker-type="date" onClick={() => openDatePicker()}>
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
            selected={props.value.toDate()}
            onChange={onChange}
            dateFormat={DATE_FORMAT}
            wrapperClassName="form-control date-time-picker-wrapper"
            popperModifiers={popperModifiers}
            customInput={
              <InputPassthrough
                id={datePickerId}
                // TODO: The styling logic here is hacky, would be nice to clean it up once everything works
                className="form-control"
                // This is used by Cucumber to interact with the component
                data-testid="date-picker"
              />
            }
          />
        </>
      )}
      {hideTimePicker ? null : (
        <>
          <span key="clock" className="input-group-addon" data-picker-type="time" onClick={openTimePicker}>
            &nbsp;<i className="fa fa-clock-o"></i>
          </span>
          <ReactDatePicker
            key="time-picker"
            portalId="time-picker-portal"
            ref={timePickerRef}
            selected={props.value.toDate()}
            onChange={onChange}
            showTimeSelect
            showTimeSelectOnly
            // We want the regular primary display to only show the time here, so using TIME_FORMAT is intentional
            dateFormat={TIME_FORMAT}
            timeFormat={TIME_FORMAT}
            wrapperClassName="form-control date-time-picker-wrapper"
            popperModifiers={popperModifiers}
            customInput={
              <InputPassthrough
                id={timePickerId}
                className="form-control"
                // This is used by Cucumber to interact with the component
                data-testid="time-picker"
              />
            }
          />
        </>
      )}
      <span className="input-group-addon" key="tz">
        {timeZone}
      </span>
    </div>
  );
};
