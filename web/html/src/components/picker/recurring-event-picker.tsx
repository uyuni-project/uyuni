import * as React from "react";

import { DateTimePicker } from "components/datetime";
import { Combobox } from "../combobox";
import { ComboboxItem } from "../combobox";
import { Form } from "components/input/Form";
import { Text } from "components/input/Text";
import styles from "./recurring-event-picker.css";
import { localizedMoment } from "utils";

type RecurringType = "hourly" | "daily" | "weekly" | "monthly" | "cron";
type CronTimes = {
  // TODO: These should be `string` or `number`, but currently they're used mixed up
  minute: number | string;
  hour: number | string;
  dayOfMonth: string;
  dayOfWeek: string;
};

type RecurringEventPickerProps = {
  timezone: string;
  scheduleName: string;
  type: RecurringType;
  cron: string;
  cronTimes: CronTimes;
  onScheduleNameChanged: (scheduleName: string) => void;
  onTypeChanged: (type: string) => void;
  onCronTimesChanged: (cronTimes: CronTimes) => void;
  onCronChanged: (cron: string) => void;
};

type RecurringEventPickerState = {
  scheduleName: string;
  type: RecurringType;
  time: moment.Moment;
  minutes: ComboboxItem;
  weekDay: ComboboxItem;
  monthDay: ComboboxItem;
  cron: string;
  cronTimes: CronTimes;
};

class RecurringEventPicker extends React.Component<RecurringEventPickerProps, RecurringEventPickerState> {
  minutes = Array.from(Array(60).keys()).map((id) => ({ id: Number(id), text: id.toString() }));

  weekDays = [
    { id: Number(1), text: t("Sunday") },
    { id: Number(2), text: t("Monday") },
    { id: Number(3), text: t("Tuesday") },
    { id: Number(4), text: t("Wednesday") },
    { id: Number(5), text: t("Thursday") },
    { id: Number(6), text: t("Friday") },
    { id: Number(7), text: t("Saturday") },
  ];

  monthDays = Array.from(Array(28).keys()).map((id) => ({ id: Number(id + 1), text: (id + 1).toString() }));

  constructor(props: RecurringEventPickerProps) {
    super(props);

    this.state = {
      time: localizedMoment(),
      scheduleName: props.scheduleName || "",
      type: props.type || "weekly",
      cronTimes: props.cronTimes || { minute: "", hour: "", dayOfMonth: "", dayOfWeek: "" },
      cron: props.cron || "",
      minutes: this.minutes[0],
      weekDay: this.weekDays[0],
      monthDay: this.monthDays[0],
    };

    this.props.cronTimes ? this.setInitialTimeAndDays(this.state.time) : this.initialize();
  }

  /**
   * In this view, for legacy reasons, the user selects time values (without any date values) that are used as inputs to
   * Quartz cron configuration without regarding time zones. To accommodate for this, we manually offset the values as
   * needed. This is a very specific use case and should **NOT** be used elsewhere where dates and times are concerned.
   */
  private fromLegacyServerTime(value: moment.Moment, hour: number, minute: number): moment.Moment {
    const serverTime = localizedMoment(value).tz(localizedMoment.serverTimeZone);
    if (!isNaN(hour)) {
      serverTime.hours(hour);
    }
    if (!isNaN(minute)) {
      serverTime.minutes(minute);
    }
    return localizedMoment(serverTime);
  }

  private toLegacyServerTime(value: moment.Moment): { hour: number; minute: number } {
    const serverTime = localizedMoment(value).tz(localizedMoment.serverTimeZone);
    return {
      hour: serverTime.hours(),
      minute: serverTime.minutes(),
    };
  }

  initialize = () => {
    // TODO: This logic needs to be lifted up since this internally calls `setState()` which is not valid while mounting
    this.onSelectWeekly();
  };

  setInitialTimeAndDays = (value: moment.Moment) => {
    const hour = Number(this.state.cronTimes.hour);
    const minute = Number(this.state.cronTimes.minute);
    const time = this.fromLegacyServerTime(value, hour, minute);
    Object.assign(this.state, {
      time: time,
      cron: this.state.cron,
      minutes: this.minutes[Number(this.state.cronTimes.minute) || 0],
      weekDay: this.weekDays[(Number(this.state.cronTimes.dayOfWeek) || 1) - 1],
      monthDay: this.monthDays[(Number(this.state.cronTimes.dayOfMonth) || 1) - 1],
      readOnly: "readonly",
    });
  };

  setScheduleName = (model) => {
    const scheduleName = model.scheduleName;
    this.setState({
      scheduleName: scheduleName,
    });
    this.props.onScheduleNameChanged(scheduleName);
  };

  selectType = (type: RecurringType) => {
    this.setState({
      type: type,
    });
    this.props.onTypeChanged(type);
    this.props.onCronChanged("");
  };

  onSelectHourly = () => {
    this.props.onCronTimesChanged({
      minute: this.state.minutes.text,
      hour: "",
      dayOfMonth: "",
      dayOfWeek: "",
    });
    this.selectType("hourly");
  };

  onSelectMinutes = (event) => {
    this.onMinutesChanged({
      id: isNaN(event.target.valueAsNumber) ? "" : event.target.valueAsNumber,
      text: event.target.value,
    });
  };

  onMinutesChanged = (selectedItem: ComboboxItem) => {
    let newMinutes: ComboboxItem;

    newMinutes = {
      id: selectedItem.id,
      text: selectedItem.text,
    };

    this.setState({
      minutes: newMinutes,
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
      ...this.toLegacyServerTime(this.state.time),
      dayOfMonth: "",
      dayOfWeek: "",
    });
    this.selectType("daily");
  };

  onDailyTimeChanged = (value: moment.Moment) => {
    this.setState({
      time: value,
    });
    this.props.onCronTimesChanged({
      ...this.toLegacyServerTime(value),
      dayOfMonth: "",
      dayOfWeek: "",
    });
    this.selectType("daily");
  };

  onSelectWeekly = () => {
    this.props.onCronTimesChanged({
      ...this.toLegacyServerTime(this.state.time),
      dayOfMonth: "",
      dayOfWeek: this.state.weekDay.id,
    });
    this.selectType("weekly");
  };

  onFocusWeekDay = () => {
    this.onWeekDayChanged(this.state.weekDay);
  };

  onSelectWeekDay = (selectedItem: ComboboxItem) => {
    this.onWeekDayChanged({
      id: selectedItem.id,
      text: selectedItem.text,
    });
  };

  onWeekDayChanged = (selectedItem: ComboboxItem) => {
    let newWeekDay: ComboboxItem;

    newWeekDay = {
      id: selectedItem.id,
      text: selectedItem.text,
    };

    this.setState({
      weekDay: newWeekDay,
    });
    this.props.onCronTimesChanged({
      ...this.toLegacyServerTime(this.state.time),
      dayOfMonth: "",
      dayOfWeek: selectedItem.id,
    });
    this.selectType("weekly");
  };

  onWeeklyTimeChanged = (value: moment.Moment) => {
    this.setState({
      time: value,
    });
    this.props.onCronTimesChanged({
      ...this.toLegacyServerTime(value),
      dayOfMonth: "",
      dayOfWeek: this.state.weekDay.id,
    });
    this.selectType("weekly");
  };

  onSelectMonthly = () => {
    this.props.onCronTimesChanged({
      ...this.toLegacyServerTime(this.state.time),
      dayOfMonth: this.state.monthDay.id,
      dayOfWeek: "",
    });
    this.selectType("monthly");
  };

  onFocusMonthDay = () => {
    this.onMonthDayChanged(this.state.monthDay);
  };

  onSelectMonthDay = (selectedItem: ComboboxItem) => {
    this.onMonthDayChanged({
      id: selectedItem.id,
      text: selectedItem.text,
    });
  };

  onMonthDayChanged = (selectedItem: ComboboxItem) => {
    let newMonthDay: ComboboxItem;

    newMonthDay = {
      id: selectedItem.id,
      text: selectedItem.text,
    };

    this.setState({
      monthDay: newMonthDay,
    });
    this.props.onCronTimesChanged({
      ...this.toLegacyServerTime(this.state.time),
      dayOfMonth: selectedItem.id,
      dayOfWeek: "",
    });
    this.selectType("monthly");
  };

  onMonthlyTimeChanged = (value: moment.Moment) => {
    this.setState({
      time: value,
    });
    this.props.onCronTimesChanged({
      ...this.toLegacyServerTime(value),
      dayOfMonth: this.state.monthDay.id,
      dayOfWeek: "",
    });
    this.selectType("monthly");
  };

  selectCustom = () => {
    this.setState({
      type: "cron",
    });
    this.props.onTypeChanged("cron");
    this.props.onCronTimesChanged({ minute: "", hour: "", dayOfMonth: "", dayOfWeek: "" });
  };

  onSelectCustom = () => {
    this.props.onCronChanged(this.state.cron);
    this.selectCustom();
  };

  onCronChanged = (cron) => {
    this.setState({
      cron: cron.target.value,
    });
    this.props.onCronChanged(cron.target.value);
    this.selectCustom();
  };

  render() {
    return (
      <div className="form-horizontal">
        <Form onChange={this.setScheduleName} model={{ scheduleName: this.state.scheduleName }}>
          <Text
            name="scheduleName"
            required
            type="text"
            label={t("Schedule Name")}
            labelClass="col-sm-3"
            divClass="col-sm-6"
          />
        </Form>
        <div className="panel panel-default">
          <div className="panel-heading">
            <h3>{t("Select a Schedule")}</h3>
          </div>
          <div className="panel-body">
            <div className="form-horizontal">
              <div className={`form-group ${styles.center}`}>
                <div className="col-sm-3 control-label">
                  <input
                    type="radio"
                    name="minutes"
                    value="false"
                    checked={this.state.type === "hourly"}
                    id="schedule-hourly"
                    onChange={this.onSelectHourly}
                  />
                  <label className={styles.radio} htmlFor="schedule-hourly">
                    {t("Hourly:")}
                  </label>
                </div>
                <div className="col-sm-3">
                  <input
                    className="form-control"
                    name="minutes"
                    type="number"
                    value={this.state.minutes.id}
                    min="0"
                    max="59"
                    onChange={this.onSelectMinutes}
                  />
                </div>
                <div className={`col-sm-1 ${styles.helpIcon}`}>
                  <i
                    className="fa fa-question-circle spacewalk-help-link"
                    title={t("The action will be executed every hour at the specified minute")}
                  />
                </div>
              </div>
              <div className={`form-group ${styles.center}`}>
                <div className="col-sm-3 control-label">
                  <input
                    type="radio"
                    name="date_daily"
                    value="false"
                    checked={this.state.type === "daily"}
                    id="schedule-daily"
                    onChange={this.onSelectDaily}
                  />
                  <label className={styles.radio} htmlFor="schedule-daily">
                    {t("Daily:")}
                  </label>
                </div>
                <div className="col-sm-3">
                  <DateTimePicker
                    onChange={this.onDailyTimeChanged}
                    value={this.state.time}
                    hideDatePicker
                    serverTimeZone
                    id="time-daily"
                  />
                </div>
              </div>
              <div className={`form-group ${styles.center}`}>
                <div className="col-sm-3 control-label">
                  <input
                    type="radio"
                    name="date_weekly"
                    value="false"
                    checked={this.state.type === "weekly"}
                    id="schedule-weekly"
                    onChange={this.onSelectWeekly}
                  />
                  <label className={styles.radio} htmlFor="schedule-weekly">
                    {t("Weekly:")}
                  </label>
                </div>
                <div className="col-sm-3">
                  <Combobox
                    id="weekly-day-picker"
                    name="date_weekly"
                    selectedId={this.state.weekDay.id}
                    data={this.weekDays}
                    onSelect={this.onSelectWeekDay}
                    onFocus={this.onFocusWeekDay}
                  />
                </div>
                <div className="col-sm-3">
                  <DateTimePicker
                    onChange={this.onWeeklyTimeChanged}
                    value={this.state.time}
                    hideDatePicker
                    serverTimeZone
                    id="time-weekly"
                  />
                </div>
              </div>
              <div className={`form-group ${styles.center}`}>
                <div className="col-sm-3 control-label">
                  <input
                    type="radio"
                    name="date_monthly"
                    value="false"
                    checked={this.state.type === "monthly"}
                    id="schedule-monthly"
                    onChange={this.onSelectMonthly}
                  />
                  <label className={styles.radio} htmlFor="schedule-monthly">
                    {t("Monthly:")}
                  </label>
                </div>
                <div className="col-sm-3">
                  <Combobox
                    id="monthly-day-picker"
                    name="date_monthly"
                    selectedId={this.state.monthDay.id}
                    data={this.monthDays}
                    onSelect={this.onSelectMonthDay}
                    onFocus={this.onFocusMonthDay}
                  />
                </div>
                <div className="col-sm-3">
                  <DateTimePicker
                    onChange={this.onMonthlyTimeChanged}
                    value={this.state.time}
                    hideDatePicker
                    serverTimeZone
                    id="time-monthly"
                  />
                </div>
                <div className={`col-sm-1 ${styles.helpIcon}`}>
                  <i
                    className="fa fa-question-circle spacewalk-help-link"
                    title={t("Days are limited to 28 to have a recurring schedule available for all the months")}
                  />
                </div>
              </div>
              <div className={`form-group ${styles.center}`}>
                <div className="col-sm-3 control-label">
                  <input
                    type="radio"
                    name="date_cron"
                    value="false"
                    checked={this.state.type === "cron"}
                    id="schedule-cron"
                    onChange={this.onSelectCustom}
                  />
                  <label className={styles.radio} htmlFor="schedule-cron">
                    {t("Custom Quartz format:")}
                  </label>
                </div>
                <div className="col-sm-3">
                  <div className="input-group">
                    <input
                      className="form-control"
                      type="text"
                      name="cron"
                      value={this.state.cron}
                      placeholder={t('e.g. "0 15 2 ? * 7"')}
                      id="custom-cron"
                      onChange={this.onCronChanged}
                    />
                    {/* This field is always in the server time zone, but just be explicit to the user */}
                    <span className="input-group-addon">{localizedMoment.serverTimeZone}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export { RecurringEventPicker };
