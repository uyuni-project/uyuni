/* eslint-disable */
// @flow
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

const {DateTimePicker} = require("./datetimepicker");
const {Combobox} = require("./combobox");
import type {ComboboxItem} from "./combobox";
const Functions = require("../utils/functions");

type RecurringEventPickerProps = {
    timezone: string,
    scheduleName: string,
    type: string,
    cronTimes: Hash<string, string>,
    customCron: string,
    onScheduleNameChanged: (scheduleName: string) => void,
    onTypeChanged: (type: string) => void,
    onCronTimesChanged: (cronTimes: Hash<string, string>) => void,
    onCustomCronChanged: (customCron: string) => void
};

type RecurringEventPickerState = {
    scheduleName: string,
    type: "disabled" | "daily" | "weekly" | "monthly" | "cron",
    time: Date,
    weekDay: ComboboxItem,
    monthDay: ComboboxItem,
};

class RecurringEventPicker extends React.Component<RecurringEventPickerProps, RecurringEventPickerState> {

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
            type: props.type || "disabled",
            cronTimes: props.cronTimes || {minute: "", hour: "", dayOfMonth: "", dayOfWeek: ""},
            customCron: props.customCron || "",
            weekDay: this.weekDays[0],
            monthDay: this.monthDays[0],
        };

        if (this.props.cronTimes) {
            this.setTimeAndDays(this.state.time);
        }
    }

    setTimeAndDays = (date: Date) => {
        const time = new Date(
            date.getFullYear(),
            date.getMonth(),
            date.getDate(),
            Number(this.state.cronTimes.hour) || date.getHours(),
            Number(this.state.cronTimes.minute) || date.getMinutes(),
            date.getSeconds(),
            date.getMilliseconds()
        );
        Object.assign(
            this.state, {
                time: time,
                weekDay: this.weekDays[(Number(this.state.cronTimes.dayOfWeek) || 1) - 1],
                monthDay: this.monthDays[(Number(this.state.cronTimes.dayOfMonth) || 1) - 1],
                readOnly: "readonly"
            });
    };

    setScheduleName = (scheduleName) => {
        scheduleName = scheduleName.target.value;
        this.setState({
            scheduleName: scheduleName
        });
        this.props.onScheduleNameChanged(scheduleName);
    };

    setCustomCron = (customCron: string) => {
        if (customCron) {
            this.setState({
                customCron: customCron
            });
        }
        this.props.onCustomCronChanged(customCron);
    };

    selectType = (type: string) => {
        this.setState({
            type: type
        });
        this.props.onTypeChanged(type);
        this.setCustomCron("");
    };

    onSelectDisabled = () => {
        this.props.onCronTimesChanged({minute: "", hour: "", dayOfMonth: "", dayOfWeek: ""});
        this.selectType("disabled");
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
       this.props.onCustomCronChanged(this.state.customCron);
       this.selectCustom();
    };

    onCustomCronChanged = (customCron) => {
        this.setCustomCron(customCron.target.value);
        this.selectCustom();
    };

    render() {
        return (
        <div className="form-horizontal">
            <div className="form-group">
                <label className="col-sm-3 control-label">Schedule Name:</label>
                <div className="col-sm-6">
                    <input name="schedule-name" className="form-control" type="text" value={this.state.scheduleName} onChange={this.setScheduleName} readOnly={this.state.readOnly}/>
                </div>
            </div>
            <div className="panel panel-default">
                <div className="panel-heading">
                    <h3>{t("Select a Schedule")}</h3>
                </div>
                <div className="form-horizontal">
                    <div className="form-group">
                        <div className="col-sm-3 control-label">
                            <input type="radio" name="date_disabled" value="true" checked={this.state.type === "disabled"} id="schedule-disabled" onChange={this.onSelectDisabled}/>
                            <label htmlFor="schedule-disabled">{t("Disable Schedule")}</label>
                        </div>
                    </div>
                    <div className="form-group">
                        <div className="col-sm-3 control-label">
                            <input type="radio" name="date_daily" value="false" checked={this.state.type === "daily"} id="schedule-daily" onChange={this.onSelectDaily}/>
                            <label htmlFor="schedule-daily">{t("Daily:")}</label>
                        </div>
                        <div className="col-sm-6">
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
                        <div className="col-sm-6">
                            <input className="form-control" type="text" name="cron" value={this.state.customCron} id="custom-cron" onChange={this.onCustomCronChanged}/>
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
