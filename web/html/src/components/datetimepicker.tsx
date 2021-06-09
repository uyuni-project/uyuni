import * as React from "react";
import { localizedMoment } from "utils";

// These aren't the actual proper types, just what I've inferred from code usage below
type Instance = JQuery & Date;
type StaticProperties = { dates: any };
type Picker = ((...args: any[]) => Instance) & StaticProperties;

declare global {
  interface JQuery {
    datepicker: Picker;
    timepicker: Picker;
  }
}

jQuery.fn.datepicker.dates["en_US"] = {
  days: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
  daysShort: ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
  daysMin: ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
  months: [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
  ],
  monthsShort: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
};

type DatePickerProps = {
  id?: string;
  open: boolean;
  value: moment.Moment;
  onToggle: (state: boolean) => void;
  onDateChanged: (value: moment.Moment) => void;
};

class DatePicker extends React.Component<DatePickerProps> {
  _input: JQuery | null = null;

  constructor(props: DatePickerProps) {
    super(props);
    this.setVisible.bind(this);
  }

  setVisible(visible: boolean) {
    if (visible) {
      this._input?.datepicker("show");
    } else {
      this._input?.datepicker("hide");
    }
  }

  componentDidMount() {
    this._input?.datepicker({});
    this.setVisible(this.props.open);
    this._input?.datepicker("setDate", this.props.value.toDate());
    this._input?.on("changeDate", () => {
      // TODO: Check the types here
      const unsafeDate: Date | undefined = this._input?.datepicker("getDate");
      const datepickerValue = unsafeDate ? localizedMoment(unsafeDate) : null;

      // Call the callback only if value has actually changed
      if (this.props.value.valueOf() !== datepickerValue?.valueOf()) {
        // TODO: Check this
        //in case the date is unselected
        if (datepickerValue) {
          this.props.onDateChanged(datepickerValue);
        }
        this._input?.datepicker("setDate", this.props.value.toDate());
      }
    });
    this._input?.on("show", () => {
      if (!this.props.open) {
        this.setVisible(false);
        this.props.onToggle(true);
      }
    });
    this._input?.on("hide", () => {
      if (this.props.open) {
        this.setVisible(true);
        this.props.onToggle(false);
      }
    });
  }

  UNSAFE_componentWillReceiveProps(props: DatePickerProps) {
    this._input?.datepicker("setDate", props.value.toDate());
    this.setVisible(props.open);
  }

  render() {
    return (
      <input
        type="text"
        id={this.props.id}
        data-date-today-highlight="true"
        data-date-orientation="top auto"
        data-date-autoclose="true"
        data-date-language="en_US"
        data-date-format="dd.mm.yy"
        data-date-week-start="0"
        className="form-control"
        size={15}
        ref={c => (this._input = jQuery(c!))}
      />
    );
  }
}

type TimePickerProps = {
  id?: string;
  value: moment.Moment;
  open?: boolean;
  onToggle: (state: boolean) => void;
  onTimeChanged: (value: moment.Moment) => void;
};

class TimePicker extends React.Component<TimePickerProps> {
  _input: JQuery | null = null;

  componentDidMount() {
    this._input?.timepicker({
      roundingFunction: (seconds, options) => seconds,
    });
    this._input?.on("change", () => {
      // Do nothing
    });
    this._input?.on("timeFormatError", () => {
      // Do nothing
    });
    this._input?.timepicker("setTime", this.props.value);
    this._input?.on("changeTime", () => {
      const timepickerValue = localizedMoment(this._input?.timepicker("getTime"));
      console.log(this.props.value.valueOf(), timepickerValue.valueOf());

      // Call the callback only if value has actually changed
      if (this.props.value.valueOf() !== timepickerValue.valueOf()) {
        this.props.onTimeChanged(timepickerValue);
        this._input?.timepicker("setTime", this.props.value);
      }
    });
    this._input?.on("showTimepicker", () => {
      if (!this.props.open) {
        this.setVisible(false);
        this.props.onToggle(true);
      }
    });
    this._input?.on("hideTimepicker", () => {
      if (this.props.open) {
        this.setVisible(true);
        this.props.onToggle(false);
      }
    });
  }

  setVisible(visible?: boolean) {
    if (visible) {
      this._input?.timepicker("show");
    } else {
      this._input?.timepicker("hide");
    }
  }

  UNSAFE_componentWillReceiveProps(props: TimePickerProps) {
    if (props.value.valueOf() !== this.props.value.valueOf()) {
      this._input?.timepicker("setTime", props.value);
    }
    this.setVisible(props.open);
  }

  render() {
    return (
      <input
        type="text"
        id={this.props.id}
        data-time-format="H:i"
        className="form-control"
        size={10}
        ref={c => (this._input = jQuery(c!))}
      />
    );
  }
}

type DateTimePickerProps = {
  id?: string;
  value: moment.Moment;
  onChange: (value: moment.Moment) => void;
  hideDatePicker?: boolean;
  hideTimePicker?: boolean;
};

type DateTimePickerState = {
  dateOpen: boolean;
  timeOpen: boolean;
  hideDate: boolean;
  hideTime: boolean;
};

export class DateTimePicker extends React.Component<DateTimePickerProps, DateTimePickerState> {
  constructor(props: DateTimePickerProps) {
    super(props);
    this.state = {
      dateOpen: false,
      timeOpen: false,
      hideDate: props.hideDatePicker || false,
      hideTime: props.hideTimePicker || false,
    };
  }

  onDateChanged(value: moment.Moment) {
    const originalValue = this.props.value;
    // TODO: Remove double creation
    const newValue = localizedMoment(value);
    const merged = localizedMoment(originalValue)
      .year(newValue.year())
      .month(newValue.month())
      .date(newValue.date());
    console.log(merged.toISOString(), merged.toUserDateString());
    this.props.onChange(merged);
  }

  onToggleDate(open) {
    this.setState({
      dateOpen: open,
    });
  }

  onToggleTime(open) {
    this.setState({
      timeOpen: open,
    });
  }

  toggleDatepicker() {
    this.setState({
      dateOpen: !this.state.dateOpen,
    });
  }

  toggleTimepicker() {
    this.setState({
      timeOpen: !this.state.timeOpen,
    });
  }

  onTimeChanged(value: moment.Moment) {
    const originalValue = this.props.value;
    // TODO: Remove double creation
    const newValue = localizedMoment(value);
    const merged = localizedMoment(originalValue)
      .hours(newValue.hours())
      .minutes(newValue.minutes())
      .seconds(newValue.seconds())
      .milliseconds(newValue.milliseconds());
    console.log(merged.toISOString(), merged.toUserDateString());
    this.props.onChange(merged);
  }

  render() {
    return (
      <div className="input-group">
        {!this.state.hideDate && [
          <span
            className="input-group-addon"
            data-picker-type="date"
            onClick={this.toggleDatepicker.bind(this)}
            key="calendar"
          >
            &nbsp;<i className="fa fa-calendar"></i>
          </span>,
          <DatePicker
            id={this.props.id ? this.props.id + "_date" : undefined}
            onDateChanged={date => this.onDateChanged(date)}
            onToggle={this.onToggleDate.bind(this)}
            open={this.state.dateOpen}
            value={this.props.value}
            key="date-picker"
          />,
        ]}
        {!this.state.hideTime && [
          <span
            className="input-group-addon"
            data-picker-type="time"
            onClick={this.toggleTimepicker.bind(this)}
            key="clock"
          >
            &nbsp;<i className="fa fa-clock-o"></i>
          </span>,
          <TimePicker
            id={this.props.id ? this.props.id + "_time" : undefined}
            onTimeChanged={date => this.onTimeChanged(date)}
            onToggle={this.onToggleTime.bind(this)}
            open={this.state.timeOpen}
            value={this.props.value}
            key="time-picker"
          />,
          <span className="input-group-addon" key="tz">
            {this.props.value.toServerTimeZoneString()}
          </span>,
        ]}
      </div>
    );
  }
}
