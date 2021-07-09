import * as React from "react";

// These aren't the actual proper types, just what I've inferred from code usage below
type Instance = JQuery & {
  getTime: () => any;
  getFullYear: () => any;
  getMonth: () => any;
  getDate: () => any;
  getHours: () => any;
  getMinutes: () => any;
  getSeconds: () => any;
};
type StaticProperties = { dates: any; };
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
  value: Date;
  onToggle: (state: boolean) => void;
  onDateChanged: (value: any) => any;
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
    this._input?.datepicker("setDate", this.props.value);
    this._input?.on("changeDate", () => {
      const datepickerValue = this._input?.datepicker("getDate");

      // Call the callback only if value has actually changed
      const propsTime = this.props.value.getTime();
      const pickerTime = datepickerValue?.getTime();
      if (propsTime !== pickerTime) {
        //in case the date is unselected
        if (!Number.isNaN(pickerTime)) {
          this.props.onDateChanged(datepickerValue);
        }
        this._input?.datepicker("setDate", this.props.value);
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

  UNSAFE_componentWillReceiveProps(props) {
    this._input?.datepicker("setDate", props.value);
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
  value: Date;
  open?: boolean;
  onToggle: (state: boolean) => void;
  onTimeChanged: (value: any) => any;
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
      const timepickerValue = this._input?.timepicker("getTime");

      // Call the callback only if value has actually changed
      if (this.props.value.getTime() !== timepickerValue?.getTime()) {
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

  setVisible(visible: boolean) {
    if (visible) this._input?.timepicker("show");
    else this._input?.timepicker("hide");
  }

  UNSAFE_componentWillReceiveProps(props) {
    if (props.value.getTime() !== this.props.value.getTime()) {
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
  value: Date;
  timezone?: string;
  onChange: (value: any) => any;
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

  onDateChanged(date) {
    const value = this.props.value;
    const merged = new Date(
      date.getFullYear(),
      date.getMonth(),
      date.getDate(),
      value.getHours(),
      value.getMinutes(),
      value.getSeconds(),
      value.getMilliseconds()
    );
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

  onTimeChanged(date) {
    const value = this.props.value;
    const merged = new Date(
      value.getFullYear(),
      value.getMonth(),
      value.getDate(),
      date.getHours(),
      date.getMinutes(),
      date.getSeconds(),
      date.getMilliseconds()
    );
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
            {this.props.timezone}
          </span>,
        ]}
      </div>
    );
  }
}
