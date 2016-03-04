'use strict';

const React = require("react");

class DatePicker extends React.Component {

    constructor(props) {
        super();
    }

    componentDidMount() {
        this._input.datepicker();
        this._input.datepicker('setDate', this.props.value);
        this._input.on('changeDate', () => {
            const datepickerValue = this._input.datepicker('getDate');

            console.log("datepickerValue: " + datepickerValue.getTime());
            console.log("vs. value: " + this.props.value.getTime());

            // Call the callback only if value has actually changed
            if (this.props.value.getTime() !== datepickerValue.getTime()) {
                this.props.onDateChanged(datepickerValue);
            }
        });
    }

    componentWillReceiveProps() {
        console.log("props received: " + this.props.value);
        // this._input.datepicker('setDate', this.props.value);
    }

    render() {
        return (
            <input type="text" data-date-today-highlight="true" data-date-orientation="top auto"
                    data-date-autoclose="true" data-date-language="en_US" data-date-format="m/d/yy"
                    data-date-week-start="0" className="form-control" size="15"
                    ref={(c) => this._input = $(c)} />
        );
    }
}

class TimePicker extends React.Component {

    constructor(props) {
        super();
    }

    componentDidMount() {
        this._input.timepicker({});
        this._input.timepicker('setTime', this.props.value);
        this._input.on('changeTime', () => {
            const timepickerValue = this._input.timepicker('getTime');

            console.log("timepickerValue: " + timepickerValue.getTime());
            console.log("vs. value: " + this.props.value.getTime());

            // Call the callback only if value has actually changed
            if (this.props.value.getTime() !== timepickerValue.getTime()) {
                this.props.onTimeChanged(timepickerValue);
            }
        });
    }

    componentWillReceiveProps() {
        console.log("props received: " + this.props.value);
        // this._input.timepicker('setTime', this.props.value);
    }

    render() {
        return (
            <input type="text" data-time-format="g:i a" className="form-control"
                    size="10" ref={(c) => this._input = $(c)} />
        );
    }
}

class DateTimePicker extends React.Component {

    constructor(props) {
        super();
    }

    componentDidMount() {
        // TODO: show datepicker when its addon is clicked
        this._addonDate.on('click', () => {
            console.log("TODO: show datepicker");
        });

        // TODO: show timepicker when its addon is clicked
        this._addonTime.on('click', () => {
            console.log("TODO: show timepicker");
        });
    }

    onDateChanged(date) {
        console.log("--> onDateChanged(): " + date);
        const value = this.props.value;
        const merged = new Date(date.getFullYear(), date.getMonth(), date.getDate(),
                value.getHours(), value.getMinutes(), value.getSeconds(), value.getMilliseconds());
        this.props.onChange(merged);
    }

    onTimeChanged(date) {
        console.log("--> onTimeChanged(): " + date);
        const value = this.props.value;
        const merged = new Date(value.getFullYear(), value.getMonth(), value.getDate(),
                date.getHours(), date.getMinutes(), date.getSeconds(), date.getMilliseconds());
        this.props.onChange(merged);
    }

    render() {
        return (
            <div className="input-group">
                <span className="input-group-addon" data-picker-type="date" ref={(c) => this._addonDate = $(c)}>
                    &nbsp;<i className="fa fa-calendar"></i>
                </span>
                <DatePicker onDateChanged={date => this.onDateChanged(date)} value={this.props.value} />
                <span className="input-group-addon" data-picker-type="time" ref={(c) => this._addonTime = $(c)}>
                    &nbsp;<i className="fa fa-clock-o"></i>
                </span>
                <TimePicker onTimeChanged={date => this.onTimeChanged(date)} value={this.props.value} />
                <span className="input-group-addon">
                    {this.props.timezone}
                </span>
            </div>
        );
    }
}

module.exports = {
    DateTimePicker : DateTimePicker
}
