import * as React from "react";
import { localizedMoment } from "utils";

// These aren't the actual proper types, just what I've inferred from code usage below
type Instance = JQuery & Date;
type StaticProperties = { dates: any };
type PickerType = ((config: object) => Instance) & ((method: "show" | "hide") => Instance) & StaticProperties;

type DatePickerType = PickerType & ((method: "setDate", value: Date) => void) & ((method: "getDate") => Date);
type TimePickerType = PickerType & ((method: "setTime", value: Date) => void) & ((method: "getTime") => Date);

declare global {
  interface JQuery {
    datepicker: DatePickerType;
    timepicker: TimePickerType;
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
  open?: boolean;
  onToggle: (state: boolean) => void;
  year: number;
  month: number;
  date: number;
  onDateChanged: (year: number, month: number, date: number) => void;
};

class DatePicker extends React.PureComponent<DatePickerProps> {
  _input: JQuery | null = null;

  constructor(props: DatePickerProps) {
    super(props);
    this.setVisible.bind(this);
  }

  componentDidMount() {
    this._input?.datepicker({});
    this.setVisible(this.props.open);
    this._input?.datepicker("setDate", this.toFauxBrowserDate(this.props));
    this._input?.on("changeDate", () => {
      // TODO: Check the types here
      const unsafeDate: Date | undefined = this._input?.datepicker("getDate");

      // Only if value has actually changed
      const year = unsafeDate?.getFullYear() ?? this.props.year;
      const month = unsafeDate?.getMonth() ?? this.props.month;
      const date = unsafeDate?.getDate() ?? this.props.date;
      if (!(this.props.year === year && this.props.month === month && this.props.date === date)) {
        // TODO: Check this
        //in case the date is unselected
        if (!isNaN(unsafeDate as any)) {
          this.props.onDateChanged(year, month, date);
        }
        this._input?.datepicker("setDate", this.toFauxBrowserDate(this.props));
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
    if (!(this.props.year === props.year && this.props.month === props.month && this.props.date === props.date)) {
      this._input?.datepicker("setDate", this.toFauxBrowserDate(props));
    }
    this.setVisible(props.open);
  }

  // The jQuery date picker always uses browser time (not user or server time), so we can only use it to get specific numeric values, not a coherent object
  toFauxBrowserDate(props: DatePickerProps) {
    const date = new Date();
    date.setFullYear(props.year);
    date.setMonth(props.month);
    date.setDate(props.date);
    return date;
  }

  setVisible(visible?: boolean) {
    if (visible) {
      this._input?.datepicker("show");
    } else {
      this._input?.datepicker("hide");
    }
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
        data-date-format="yyyy-mm-dd"
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
  open?: boolean;
  onToggle: (state: boolean) => void;
  hours: number;
  minutes: number;
  seconds: number;
  onTimeChanged: (hours: number, minutes: number, seconds: number) => void;
};

class TimePicker extends React.PureComponent<TimePickerProps> {
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
    this._input?.timepicker("setTime", this.toFauxBrowserDate(this.props));
    this._input?.on("changeTime", () => {
      const unsafeDate: Date | undefined = this._input?.timepicker("getTime");

      // Only if value has actually changed
      const hours = unsafeDate?.getHours() ?? this.props.hours;
      const minutes = unsafeDate?.getMinutes() ?? this.props.minutes;
      const seconds = unsafeDate?.getSeconds() ?? this.props.seconds;
      if (!(this.props.hours === hours && this.props.minutes === minutes && this.props.seconds === seconds)) {
        this.props.onTimeChanged(hours, minutes, seconds);
        this._input?.timepicker("setTime", this.toFauxBrowserDate(this.props));
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

  UNSAFE_componentWillReceiveProps(props: TimePickerProps) {
    if (
      !(
        this.props.hours === props.hours &&
        this.props.minutes === props.minutes &&
        this.props.seconds === props.seconds
      )
    ) {
      this._input?.timepicker("setTime", this.toFauxBrowserDate(props));
    }
    this.setVisible(props.open);
  }

  // The jQuery date picker always uses browser time (not user or server time), so we can only use it to get specific numeric values, not a coherent object
  toFauxBrowserDate(props: TimePickerProps) {
    const date = new Date();
    date.setHours(props.hours);
    date.setMinutes(props.minutes);
    date.setSeconds(props.seconds);
    date.setMilliseconds(0);
    return date;
  }

  setVisible(visible?: boolean) {
    if (visible) {
      this._input?.timepicker("show");
    } else {
      this._input?.timepicker("hide");
    }
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
  timeZone: string;
};

export class DateTimePicker extends React.Component<DateTimePickerProps, DateTimePickerState> {
  constructor(props: DateTimePickerProps) {
    super(props);
    this.state = {
      dateOpen: false,
      timeOpen: false,
      hideDate: props.hideDatePicker || false,
      hideTime: props.hideTimePicker || false,
      timeZone: localizedMoment.serverTimeZone,
    };
  }

  onToggleDate = open => {
    this.setState({
      dateOpen: open,
    });
  };

  onToggleTime = open => {
    this.setState({
      timeOpen: open,
    });
  };

  toggleDatepicker = () => {
    this.setState({
      dateOpen: !this.state.dateOpen,
    });
  };

  toggleTimepicker = () => {
    this.setState({
      timeOpen: !this.state.timeOpen,
    });
  };

  toggleTimeZone = () => {
    if (localizedMoment.serverTimeZone === localizedMoment.userTimeZone) {
      return;
    }
    this.setState({
      timeZone:
        this.state.timeZone === localizedMoment.serverTimeZone
          ? localizedMoment.userTimeZone
          : localizedMoment.serverTimeZone,
    });
  };

  onDateChanged = (year: number, month: number, day: number) => {
    const newValue = localizedMoment(this.props.value)
      // The user made the choice in the given timezone
      .tz(this.state.timeZone)
      .year(year)
      .month(month)
      .date(day);
    if (this.props.value.valueOf() !== newValue.valueOf()) {
      // Always propagate a standard UTC state
      this.props.onChange(localizedMoment(newValue));
    }
  };

  onTimeChanged = (hours: number, minutes: number, seconds: number) => {
    const newValue = localizedMoment(this.props.value)
      // The user made the choice in the given timezone
      .tz(this.state.timeZone)
      .hours(hours)
      .minutes(minutes)
      .seconds(seconds)
      .milliseconds(0);
    if (this.props.value.valueOf() !== newValue.valueOf()) {
      // Always propagate a standard UTC state
      this.props.onChange(localizedMoment(newValue));
    }
  };

  render() {
    // Make a copy so we don't modify the passed prop
    const zonedMoment = localizedMoment(this.props.value).tz(this.state.timeZone);
    const year = zonedMoment.year();
    const month = zonedMoment.month();
    const date = zonedMoment.date();
    const hours = zonedMoment.hours();
    const minutes = zonedMoment.minutes();
    const seconds = zonedMoment.seconds();
    return (
      <div className="input-group">
        {!this.state.hideDate && [
          <span className="input-group-addon" data-picker-type="date" onClick={this.toggleDatepicker} key="calendar">
            &nbsp;<i className="fa fa-calendar"></i>
          </span>,
          <DatePicker
            id={this.props.id ? this.props.id + "_date" : undefined}
            onDateChanged={this.onDateChanged}
            onToggle={this.onToggleDate}
            open={this.state.dateOpen}
            year={year}
            month={month}
            date={date}
            key="date-picker"
          />,
        ]}
        {!this.state.hideTime && [
          <span className="input-group-addon" data-picker-type="time" onClick={this.toggleTimepicker} key="clock">
            &nbsp;<i className="fa fa-clock-o"></i>
          </span>,
          <TimePicker
            id={this.props.id ? this.props.id + "_time" : undefined}
            onTimeChanged={this.onTimeChanged}
            onToggle={this.onToggleTime}
            open={this.state.timeOpen}
            hours={hours}
            minutes={minutes}
            seconds={seconds}
            key="time-picker"
          />,
          <span className="input-group-addon" key="tz" onClick={this.toggleTimeZone}>
            {this.state.timeZone}
          </span>,
        ]}
      </div>
    );
  }
}
