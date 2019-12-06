/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Messages = require("components/messages").Messages;
const MessagesUtils = require("components/messages").Utils;
const {RecurringEventPicker} = require("components/recurring-event-picker");
const {DisplayHighstate} = require("./display-highstate");
const Button = require("components/buttons").Button;
const AsyncButton = require("components/buttons").AsyncButton;
const {Toggler} = require("components/toggler");
const Network = require("utils/network");
const { InnerPanel } = require("components/panels/InnerPanel");
const Functions = require("utils/functions");

const messagesCounterLimit = 3;

function msg(severityIn, textIn) {
    return {severity: severityIn, text: textIn};
}

class RecurringStatesEdit extends React.Component {
    constructor(props) {
        super(props);

        ["onCreate", "onUpdate"]
            .forEach(method => this[method] = this[method].bind(this));

        this.state = {
            minions : minions,
            messages: []
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
                scheduleName: schedule.scheduleName,
                minions: schedule.minionIds.reduce((minions, minion, i) => {
                    minions[i] = {id: minion, name: schedule.minionNames[i]};
                    return minions;}, []),
                type: schedule.type,
                targetType: schedule.targetType,
                cronTimes: {
                    minute: schedule.minute,
                    hour: schedule.hour,
                    dayOfWeek: schedule.dayOfWeek,
                    dayOfMonth: schedule.dayOfMonth
                },
                customCron: schedule.frequency
            });
    };

    getTargetType = () => {
        const search = window.location.search;
        if (search.match("\\?sid")) {
            Object.assign(this.state, {targetType: "Minion"});
        } else if (search.match("\\?sgid")) {
            Object.assign(this.state, {targetType: "Group"});
        } else if (window.location.pathname.match("/ssm") && !search) {
            Object.assign(this.state, {targetType: "System Set Manager"});
        } else {
            Object.assign(this.state, {targetType: "Organization"});
        }
    };

    isEdit = () => {
        return !!this.props.schedule;
    };

    onCreate = () => {
        if (this.isEdit()) {
            return false;
        }
        return Network.post(
            "/rhn/manager/api/states/schedules/save",
            JSON.stringify({
                minionIds: this.state.minions.map(minion => minion.id),
                minionNames: this.state.minions.map(minion => minion.name),
                scheduleName: this.state.scheduleName,
                type: this.state.type,
                targetType: this.state.targetType,
                cronTimes: this.state.cronTimes,
                cron: this.state.customCron,
                test: this.state.test
            }),
            "application/json"
        ).promise.then(() => {
            const msg = MessagesUtils.info(
                <span>{t("Schedule successfully created.")}</span>);

            const msgs = this.state.messages.concat(msg);

            while (msgs.length > messagesCounterLimit) {
                msgs.shift();
            }

            this.props.onMessageChanged(msgs);
            this.setState({
                messages: msgs
            });
            this.props.onActionChanged();
        }).catch(this.handleResponseError);
    };

    onUpdate = () => {
        if (!this.isEdit()) {
            return false;
        }
        return Network.post(
            "/rhn/manager/api/states/schedules/" + this.props.schedule.scheduleId + "/update",
            JSON.stringify({
                minionIds: this.state.minions.map(minion => minion.id),
                minionNames: this.state.minions.map(minion => minion.name),
                scheduleName: this.state.scheduleName,
                type: this.state.type,
                targetType: this.state.targetType,
                cronTimes: this.state.cronTimes,
                cron: this.state.customCron,
                test: this.state.test
            }),
            "application/json"
        ).promise.then(() => {
            const msg = MessagesUtils.info(
                <span>{t("Schedule successfully updated.")}</span>);

            const msgs = this.state.messages.concat(msg);

            while (msgs.length > messagesCounterLimit) {
                msgs.shift();
            }

            this.props.onMessageChanged(msgs);
            this.setState({
                messages: msgs
            });
            this.props.onActionChanged();
        }).catch(this.handleResponseError);
    };

    handleResponseError = (jqXHR) => {
        let message = Network.responseErrorMessage(jqXHR);
        this.props.onMessageChanged(message);
        this.setState({
            messages: message
        });
    };

    onScheduleNameChanged = (scheduleName) => {
        this.setState({scheduleName: scheduleName});
    };

    onTypeChanged = (type) => {
        this.setState({type: type})
    };

    onCronTimesChanged = (cronTimes) => {
        this.setState({cronTimes: cronTimes})
    };

    onCustomCronChanged = (customCron) => {
        this.setState({customCron: customCron})
    };

    toggleTestState = () => {
        this.setState({test: !this.state.test})
    };

    resetFields = () => {
        /* TODO: Write implementation */
    };

    render() {
        const buttons = [
                <div className="btn-group pull-right">
                    <Toggler text={t('Test mode')} value={this.state.test} className="btn" handler={this.toggleTestState.bind(this)} />
                    <AsyncButton action={this.isEdit() ? this.onUpdate : this.onCreate} defaultType="btn-success" text={(this.isEdit() ? t("Update ") : t("Create ")) + t("Schedule")} disabled={minions.length === 0} />
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
                                          type={this.state.type}
                                          cronTimes={this.state.cronTimes}
                                          customCron={this.state.customCron}
                                          onScheduleNameChanged={this.onScheduleNameChanged}
                                          onTypeChanged={this.onTypeChanged}
                                          onCronTimesChanged={this.onCronTimesChanged}
                                          onCustomCronChanged={this.onCustomCronChanged} />
                    <DisplayHighstate minions={this.state.minions}/>
                </InnerPanel>
            </div>
        );
    }
}

module.exports = {
    RecurringStatesEdit: RecurringStatesEdit
};
