/* eslint-disable */
// @flow
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

const {DateTimePicker} = require("./datetimepicker");
const {Combobox} = require("./combobox");
import type {ComboboxItem} from "./combobox";
const { Form } = require('components/input/Form');
const { Text } = require('components/input/Text');
const Functions = require("../utils/functions");

type RecurringEventPickerProps = {
    timezone: string,
    scheduleName: string,
    type: string,
    cron: string,
    cronTimes: Hash<string, string>,
    onScheduleNameChanged: (scheduleName: string) => void,
    onTypeChanged: (type: string) => void,
    onCronTimesChanged: (cronTimes: Hash<string, string>) => void,
    onCronChanged: (cron: string) => void
};

type RecurringEventPickerState = {
    scheduleName: string,
    type: "hourly" | "daily" | "weekly" | "monthly" | "cron",
    time: Date,
    minutes: ComboboxItem,
    weekDay: ComboboxItem,
    monthDay: ComboboxItem,
};

class RecurringEventPicker extends React.Component<RecurringEventPickerProps, RecurringEventPickerState> {

    minutes = Array.from(Array(60).keys()).map(id => ({id: Number(id), text: (id).toString()}));

    weekDays = [{id: Number(1), text: 'Sunday'},
        {id: Number(2), text: 'Monday'},
        {id: Number(3), text: 'Tuesday'},
        {id: Number(4), text: 'Wednesday'},
        {id: Number(5), text: 'Thursday'},
        {id: Number(6), text: 'Friday'},
        {id: Number(7), text: 'Saturday'}];

    monthDays = Array.from(Array(28).keys()).map(id => ({id: Number(id + 1), text: (id + 1).toString()}));

    constructor(props: RecurringEventPickerProps) {
        super(props);

        this.state = {
            time: Functions.Utils.dateWithTimezone(localTime),
            scheduleName: props.scheduleName || "",
            type: props.type || "weekly",
            cronTimes: props.cronTimes || {minute: "", hour: "", dayOfMonth: "", dayOfWeek: ""},
            cron: props.cron || "",
            minutes: this.minutes[0],
            weekDay: this.weekDays[0],
            monthDay: this.monthDays[0]
        };

        this.props.cronTimes ? this.setTimeAndDays(this.state.time) : this.initialize()
    }

    initialize = () => {
        this.onSelectWeekly();
    };

    setTimeAndDays = (date: Date) => {
        const hours = Number(this.state.cronTimes.hour);
        const minutes = Number(this.state.cronTimes.minute);
        const time = new Date(
            date.getFullYear(),
            date.getMonth(),
            date.getDate(),
            isNaN(hours) ? date.getHours() : hours,
            isNaN(minutes) ? date.getMinutes() : minutes,
            date.getSeconds(),
            date.getMilliseconds()
        );
        Object.assign(
            this.state, {
                time: time,
                cron: this.state.cron,
                minutes: this.minutes[(Number(this.state.cronTimes.minute) || 0)],
                weekDay: this.weekDays[(Number(this.state.cronTimes.dayOfWeek) || 1) - 1],
                monthDay: this.monthDays[(Number(this.state.cronTimes.dayOfMonth) || 1) - 1],
                readOnly: "readonly"
            });
    };

    setScheduleName = (model) => {
        const scheduleName = model.scheduleName;
        this.setState({
            scheduleName: scheduleName
        });
        this.props.onScheduleNameChanged(scheduleName);
    };

    selectType = (type: string) => {
        this.setState({
            type: type
        });
        this.props.onTypeChanged(type);
        this.props.onCronChanged("");
    };

    onSelectHourly = () => {
        this.props.onCronTimesChanged({
            minute: this.state.minutes.text,
            hour: "",
            dayOfMonth: "",
            dayOfWeek: ""
        });
        this.selectType("hourly");
    };

    onSelectMinutes = (event) => {
        this.onMinutesChanged({
            id: isNaN(event.target.valueAsNumber) ? "" : event.target.valueAsNumber,
            text: event.target.value
        });
    };

    onMinutesChanged = (selectedItem: ComboboxItem) => {
        let newMinutes: ComboboxItem;

        newMinutes = {
            id: selectedItem.id,
            text: selectedItem.text
        };

        this.setState({
            minutes: newMinutes
        });
        this.props.onCronTimesChanged({
            minute: selectedItem.id,
            hour: "",
            dayOfMonth: "",
            dayOfWeek: "",
        });
        this.selectType("hourly");
    };

    onSelectDaily = () => {
        this.props.onCronTimesChanged({
            minute: this.state.time.getMinutes(),
            hour: this.state.time.getHours(),
            dayOfMonth: "",
            dayOfWeek: ""
        });
        this.selectType("daily");
    };

    onDailyTimeChanged = (date: Date) => {
        this.setState({
            time: date
        });
        this.props.onCronTimesChanged({
            minute: date.getMinutes(),
            hour: date.getHours(),
            dayOfMonth: "",
            dayOfWeek: ""
        });
        this.selectType("daily");
    };

    onSelectWeekly = () => {
        this.props.onCronTimesChanged({
            minute: this.state.time.getMinutes(),
            hour: this.state.time.getHours(),
            dayOfMonth: "",
            dayOfWeek: this.state.weekDay.id
        });
        this.selectType("weekly");
    };

    onFocusWeekDay = () => {
        this.onWeekDayChanged(this.state.weekDay);
    };

    onSelectWeekDay = (selectedItem: ComboboxItem) => {
        this.onWeekDayChanged({
            id: selectedItem.id,
            text: selectedItem.text
        });
    };

    onWeekDayChanged = (selectedItem: ComboboxItem) => {
        let newWeekDay: ComboboxItem;

        newWeekDay = {
            id: selectedItem.id,
            text: selectedItem.text
        };

        this.setState({
            weekDay: newWeekDay
        });
        this.props.onCronTimesChanged({
            minute: this.state.time.getMinutes(),
            hour: this.state.time.getHours(),
            dayOfMonth: "",
            dayOfWeek: selectedItem.id
        });
        this.selectType("weekly");
    };

    onWeeklyTimeChanged = (date: Date) => {
        this.setState({
            time: date
        });
        this.props.onCronTimesChanged({
            minute: date.getMinutes(),
            hour: date.getHours(),
            dayOfMonth: "",
            dayOfWeek: this.state.weekDay.id
        });
        this.selectType("weekly");
    };

    onSelectMonthly = () => {
        this.props.onCronTimesChanged({
            minute: this.state.time.getMinutes(),
            hour: this.state.time.getHours(),
            dayOfMonth: this.state.monthDay.id,
            dayOfWeek: ""
        });
        this.selectType("monthly");
    };

    onFocusMonthDay = () => {
        this.onMonthDayChanged(this.state.monthDay);
    };

    onSelectMonthDay = (selectedItem: ComboboxItem) => {
        this.onMonthDayChanged({
            id: selectedItem.id,
            text: selectedItem.text
        });
    };

    onMonthDayChanged = (selectedItem: ComboboxItem) => {
        let newMonthDay: ComboboxItem;

        newMonthDay = {
            id: selectedItem.id,
            text: selectedItem.text
        };

        this.setState({
            monthDay: newMonthDay,
        });
        this.props.onCronTimesChanged({
            minute: this.state.time.getMinutes(),
            hour: this.state.time.getHours(),
            dayOfMonth: selectedItem.id,
            dayOfWeek: ""
        });
        this.selectType("monthly");
    };

    onMonthlyTimeChanged = (date: Date) => {
        this.setState({
            time: date,
        });
        this.props.onCronTimesChanged({
            minute: date.getMinutes(),
            hour: date.getHours(),
            dayOfMonth: this.state.monthDay.id,
            dayOfWeek: ""
        });
        this.selectType("monthly");
    };

    selectCustom = () => {
        this.setState({
            type: "cron"
        });
        this.props.onTypeChanged("cron");
        this.props.onCronTimesChanged({minute: "", hour: "", dayOfMonth: "", dayOfWeek: ""});
    };

    onSelectCustom = () => {
        this.props.onCronChanged(this.state.cron);
        this.selectCustom();
    };

    onCronChanged = (cron) => {
        this.setState({
            cron: cron.target.value
        });
        this.props.onCronChanged(cron.target.value);
        this.selectCustom();
    };

    render() {
        return (
            <div className="form-horizontal">
                <Form onChange={this.setScheduleName} model={{scheduleName: this.state.scheduleName}}>
                    <Text name="scheduleName" required type="text" label={t("Schedule Name")} labelClass="col-sm-3" divClass="col-sm-6" />
                </Form>
                <div className="panel panel-default">
                    <div className="panel-heading">
                        <h3>{t("Select a Schedule")}</h3>
                    </div>
                    <div className="panel-body">
                        <div className="form-horizontal">
                            <div className="form-group">
                                <div className="col-sm-3 control-label">
                                    <input type="radio" name="minutes" value="false" checked={this.state.type === "hourly"} id="schedule-hourly" onChange={this.onSelectHourly}/>
                                    <label htmlFor="schedule-hourly">{t("Hourly:")}</label>
                                </div>
                                <div className="col-sm-3">
                                    <input className="form-control" name="minutes" type="number" value={this.state.minutes.id} min="0" max="59" onChange={this.onSelectMinutes} />
                                </div>
                            </div>
                            <div className="form-group">
                                <div className="col-sm-3 control-label">
                                    <input type="radio" name="date_daily" value="false" checked={this.state.type === "daily"} id="schedule-daily" onChange={this.onSelectDaily}/>
                                    <label htmlFor="schedule-daily">{t("Daily:")}</label>
                                </div>
                                <div className="col-sm-3">
                                    <DateTimePicker onChange={this.onDailyTimeChanged} value={this.state.time} timezone={this.props.timezone} hideDatePicker={true}/>
                                </div>
                            </div>
                            <div className="form-group">
                                <div className="col-sm-3 control-label">
                                    <input type="radio" name="date_weekly" value="false" checked={this.state.type === "weekly"} id="schedule-weekly" onChange={this.onSelectWeekly}/>
                                    <label htmlFor="schedule-weekly">{t("Weekly:")}</label>
                                </div>
                                <div className="col-sm-3">
                                    <Combobox id="weekly-day-picker" name="date_weekly" selectedId={this.state.weekDay.id}
                                              data={this.weekDays}
                                              onSelect={this.onSelectWeekDay}
                                              onFocus={this.onFocusWeekDay}
                                    />
                                </div>
                                <div className="col-sm-3">
                                    <DateTimePicker onChange={this.onWeeklyTimeChanged} value={this.state.time} timezone={this.props.timezone} hideDatePicker={true}/>
                                </div>
                            </div>
                            <div className="form-group">
                                <div className="col-sm-3 control-label">
                                    <input type="radio" name="date_monthly" value="false" checked={this.state.type === "monthly"} id="schedule-monthly" onChange={this.onSelectMonthly}/>
                                    <label htmlFor="schedule-monthly">{t("Monthly:")}</label>
                                </div>
                                <div className="col-sm-3">
                                    <Combobox id="monthly-day-picker" name="date_monthly" selectedId={this.state.monthDay.id}
                                              data={this.monthDays}
                                              onSelect={this.onSelectMonthDay}
                                              onFocus={this.onFocusMonthDay}
                                    />
                                </div>
                                <div className="col-sm-3">
                                    <DateTimePicker onChange={this.onMonthlyTimeChanged} value={this.state.time} timezone={this.props.timezone} hideDatePicker={true}/>
                                </div>
                            </div>
                            <div className="form-group">
                                <div className="col-sm-3 control-label">
                                    <input type="radio" name="date_cron" value="false" checked={this.state.type === "cron"} id="schedule-cron" onChange={this.onSelectCustom}/>
                                    <label htmlFor="schedule-cron">{t("Custom Quartz format:")}</label>
                                </div>
                                <div className="col-sm-3">
                                    <input className="form-control" type="text" name="cron" value={this.state.cron} placeholder={t("e.g. \"0 15 2 ? * 7\"")} id="custom-cron" onChange={this.onCronChanged}/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

module.exports = {
    RecurringEventPicker: RecurringEventPicker
};
