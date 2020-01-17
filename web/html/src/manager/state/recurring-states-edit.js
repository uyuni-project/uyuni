/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const {RecurringEventPicker} = require("components/recurring-event-picker");
const {DisplayHighstate} = require("./display-highstate");
const Button = require("components/buttons").Button;
const AsyncButton = require("components/buttons").AsyncButton;
const {Toggler} = require("components/toggler");
const { InnerPanel } = require("components/panels/InnerPanel");

class RecurringStatesEdit extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            minions: minions
        };

        if(this.isEdit()) {
            this.setSchedule(this.props.schedule);
        } else {
            this.getTargetType();
        }
    }

    setSchedule = (schedule) => {
        Object.assign(
            this.state,
            {
                scheduleId: schedule.scheduleId,
                scheduleName: schedule.scheduleName,
                type: schedule.type,
                minions: schedule.minions,
                active: schedule.active === "true",
                targetId: schedule.targetId,
                targetType: schedule.targetType,
                groupName: schedule.groupName,
                cronTimes: {
                    minute: schedule.minute,
                    hour: schedule.hour,
                    dayOfWeek: schedule.dayOfWeek,
                    dayOfMonth: schedule.dayOfMonth
                },
                cron: schedule.cron,
                test: schedule.test === "true"
            }
        );
    };

    getTargetType = () => {
        const search = window.location.search;
        if (search.match("\\?sid")) {
            Object.assign(this.state, {
                targetType: "Minion",
                targetId: minions[0].id
            });
        } else if (search.match("\\?sgid")) {
            Object.assign(this.state, {
                targetType: "Group",
                targetId: groupId
            });
        } else if (window.location.pathname.match("/ssm") && !search) {
            Object.assign(this.state, {targetType: "System Set Manager"});
        } else {
            Object.assign(this.state, {
                targetType: "Organization",
                targetId: orgId
            });
        }
    };

    isEdit = () => {
        return !!this.props.schedule;
    };

    onCreate = () => {
        if (this.isEdit()) {
            return false;
        }
        this.props.onCreate({
            targetId: this.state.targetId,
            minionNames: this.state.minions.map(minion => minion.name),
            scheduleName: this.state.scheduleName,
            active: true,
            type: this.state.type,
            targetType: this.state.targetType,
            cronTimes: this.state.cronTimes,
            cron: this.state.cron,
            test: this.state.test
        });
    };

    onEdit = () => {
        if (!this.isEdit()) {
            return false;
        }
        this.props.onEdit({
            targetId: this.state.targetId,
            minionNames: this.state.minions.map(minion => minion.name),
            scheduleId: this.state.scheduleId,
            scheduleName: this.state.scheduleName,
            active: this.state.active,
            type: this.state.type,
            targetType: this.state.targetType,
            cronTimes: this.state.cronTimes,
            cron: this.state.cron,
            test: this.state.test
        });
    };

    onScheduleNameChanged = (scheduleName) => {
        this.setState({scheduleName: scheduleName});
    };

    onToggleActive = (active) => {
        this.setState({active: active});
    };

    onTypeChanged = (type) => {
        this.setState({type: type});
    };

    onCronTimesChanged = (cronTimes) => {
        this.setState({cronTimes: cronTimes});
    };

    onCustomCronChanged = (cron) => {
        this.setState({cron: cron});
    };

    toggleTestState = () => {
        this.setState({test: !this.state.test});
    };

    resetFields = () => {
        /* TODO: Write implementation */
    };

    render() {
        const buttons = [
                <div className="btn-group pull-right">
                    <Toggler text={t('Test mode')} value={this.state.test} className="btn" handler={this.toggleTestState.bind(this)} />
                    <AsyncButton action={this.isEdit() ? this.onEdit : this.onCreate} defaultType="btn-success" text={(this.isEdit() ? t("Update ") : t("Create ")) + t("Schedule")} disabled={minions.length === 0} />
                </div>
                ];
        const buttonsLeft = [
                <div className="btn-group pull-left">
                    <Button id="back-btn" className="btn-default" icon="fa-chevron-left" text={t("Back")}  handler={() => {this.props.onActionChanged("back")}}/>
                    <Button id="reset-btn" className="btn-default" icon="fa-eraser" text={t("Reset fields")} handler={this.resetFields}/>
                </div>
                ];

        return (
            <div>
                <InnerPanel title={t("Schedule Recurring Highstate")} icon="spacewalk-icon-salt" buttonsLeft={buttonsLeft} buttons={buttons} >
                    <RecurringEventPicker timezone={timezone}
                                          scheduleName={this.state.scheduleName}
                                          active={this.state.active}
                                          type={this.state.type}
                                          cron={this.state.cron}
                                          cronTimes={this.state.cronTimes}
                                          onScheduleNameChanged={this.onScheduleNameChanged}
                                          onToggleActive={this.onToggleActive}
                                          onTypeChanged={this.onTypeChanged}
                                          onCronTimesChanged={this.onCronTimesChanged}
                                          onCronChanged={this.onCustomCronChanged} />
                    <DisplayHighstate minions={this.state.minions}/>
                </InnerPanel>
            </div>
        );
    }
}

module.exports = {
    RecurringStatesEdit: RecurringStatesEdit
};
