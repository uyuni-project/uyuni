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
            minions: minions,
            active: true
        };

        if(this.isEdit()) {
            this.setSchedule(this.props.schedule);
        } else {
            this.getTargetType();
        }
    }

    setSchedule = (schedule) => {
        Object.assign(this.state, schedule);
    };

    getTargetType = () => {
        console.log(window.entityType);
        if (window.entityType === "GROUP") {
            Object.assign(this.state, {
                targetType: entityType,
                targetId: groupId
            });
        } else if (window.entityType === "ORG") {
            Object.assign(this.state, {
                targetType: entityType,
                targetId: orgId
            });
        }
        else if (window.entityType === "MINION") {
            Object.assign(this.state, {
                targetType: entityType,
                targetId: minions[0].id
            });
        }
    };

    isEdit = () => {
        return this.props.schedule ? true : false;
    };

    onEdit = () => {
        this.props.onEdit({
            targetId: this.state.targetId,
            recurringActionId: this.state.recurringActionId,
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

    render() {
        const buttons = [
                <div className="btn-group pull-right">
                    <Toggler text={t('Test mode')} value={this.state.test} className="btn" handler={this.toggleTestState.bind(this)} />
                    <AsyncButton action={this.onEdit} defaultType="btn-success" text={(this.isEdit() ? t("Update ") : t("Create ")) + t("Schedule")} />
                </div>
                ];
        const buttonsLeft = [
                <div className="btn-group pull-left">
                    <Button id="back-btn" className="btn-default" icon="fa-chevron-left" text={t("Back")}  handler={() => {this.props.onActionChanged("back")}}/>
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
