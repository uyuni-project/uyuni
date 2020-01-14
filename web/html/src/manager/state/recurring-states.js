/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Messages = require("components/messages").Messages;
const Network = require("utils/network");
const { InnerPanel } = require('components/panels/InnerPanel');
const {RecurringStatesDetails} = require("./recurring-states-details");
const {RecurringStatesList} = require("./recurring-states-list");
const {RecurringStatesEdit} =  require("./recurring-states-edit");
const MessagesUtils = require("components/messages").Utils;
const SpaRenderer  = require("core/spa/spa-renderer").default;
const Utils = require("utils/functions").Utils;

const messagesCounterLimit = 1;
const hashUrlRegex = /^#\/([^\/]*)(?:\/(.+))?$/;

function getHashId() {
    const match = window.location.hash.match(hashUrlRegex);
    return match ? match[2] : undefined;
}

function getHashAction() {
    const match = window.location.hash.match(hashUrlRegex);
    return match ? match[1] : undefined;
}

class RecurringStates extends React.Component {

    constructor(props) {
        super(props);

        ["deleteSchedule", "handleForwardAction", "handleDetailsAction", "handleEditAction", "handleResponseError",
        "onMessageChanged", "updateSchedule", "createSchedule", "toggleActive"]
            .forEach(method => this[method] = this[method].bind(this));
        this.state = {
            messages: [],
            minionIds: minions[0].id ? minions.map(minion => minion.id) : undefined,
        };
    }

    componentDidMount() {
        this.updateView(getHashAction(), getHashId());
        window.addEventListener("popstate", () => {
            this.updateView(getHashAction(), getHashId());
        });
    }

    updateView(action, id) {
        if ((action === "edit" || action === "details") && id) {
            this.getScheduleDetails(id, action);
        } else if (!action) {
            this.getRecurringScheduleList();
        } else {
            this.setState({action: action});
        }
        this.clearMessages();
    }

    isFiltered = () => {
        return !!this.state.minionIds;
    };

    toArray = (string: String) => {
        return string
            .slice(1, -1).split(",")
            .map(element => {
                const num = Number(element.trim());
                return isNaN(num) ? element.trim() : num;
            });
    };

    filterSchedules = (schedules) => {
        return schedules.data = schedules.data.filter(
            schedule => {return this.state.minionIds.every(
                minion => schedule.minionIds.includes(minion));
            }
        );
    };

    getRecurringScheduleList = () => {
        return Network.get("/rhn/manager/api/states/schedules", "application/json").promise
            .then(schedules => {
                schedules.data.map(schedule => schedule.minionIds = this.toArray(schedule.minionIds));
                schedules.data.map(schedule => schedule.minionNames = this.toArray(schedule.minionNames));
                this.setState({
                    action: undefined,
                    selected: undefined,
                    schedules: this.isFiltered() ? this.filterSchedules(schedules) : schedules.data
                });
            }).catch(this.handleResponseError);
    };

    getScheduleDetails(id, action) {
        return Network.get("/rhn/manager/api/states/schedules/" + id, "application/json").promise
            .then(schedule => {
                schedule.data.minionIds = this.toArray(schedule.data.minionIds);
                schedule.data.minionNames = this.toArray(schedule.data.minionNames);
                schedule.data.minions = schedule.data.minionIds.reduce((minions, minion, i) => {
                    minions[i] = {id: minion, name: schedule.data.minionNames[i]};
                    return minions;
                }, []);
                this.setState({selected: schedule.data, action: action});
            }).catch(this.handleResponseError);
    }

    handleDetailsAction(row) {
        this.getScheduleDetails(row.scheduleId, "details").then(() => {
            history.pushState(null, null, "#/details/" + row.scheduleId);
        });
    }

    handleEditAction(row) {
        this.getScheduleDetails(row.scheduleId, "edit").then(() => {
            history.pushState(null, null, "#/edit/" + row.scheduleId);
        });
    }

    toggleActive(schedule) {
        Object.assign(schedule, {
            cronTimes: {
                minute: schedule.minute,
                hour: schedule.hour,
                dayOfMonth: schedule.dayOfMonth,
                dayOfWeek: schedule.dayOfWeek
            },
            active: !(schedule.active === "true")
        });
        schedule.minions = schedule.minionIds.reduce((minions, minion, i) => {
            minions[i] = {id: minion, name: schedule.minionNames[i]};
            return minions;
        }, []);
        this.updateSchedule(schedule);
        console.log(schedule);
    }

    skipNext(item) {
        this.handleForwardAction();
    }

    createSchedule(schedule) {
        return Network.post(
            "/rhn/manager/api/states/schedules/save",
            JSON.stringify(schedule),
            "application/json"
        ).promise.then(() => {
            const msg = MessagesUtils.info(
                <span>{t("Schedule successfully created.")}</span>);

            const msgs = this.state.messages.concat(msg);

            while (msgs.length > messagesCounterLimit) {
                msgs.shift();
            }

            this.onMessageChanged(msgs);
            this.setState({
                messages: msgs
            });
            this.handleForwardAction();
        }).catch(this.handleResponseError);
    }

    updateSchedule(schedule) {
        return Network.post(
            "/rhn/manager/api/states/schedules/" + schedule.scheduleId + "/update",
            JSON.stringify(schedule),
            "application/json"
        ).promise.then(() => {
            const msg = MessagesUtils.info(
                <span>{t("Schedule '") + schedule.scheduleName + t("' successfully updated.")}</span>);

            const msgs = this.state.messages.concat(msg);

            while (msgs.length > messagesCounterLimit) {
                msgs.shift();
            }

            this.onMessageChanged(msgs);
            this.setState({
                messages: msgs
            });
            this.handleForwardAction();
        }).catch(this.handleResponseError);
    }

    deleteSchedule(item) {
        if (!item) return false;
        return Network.del("/rhn/manager/api/states/schedules/" + item.scheduleId + "/delete")
            .promise.then(data => {
                this.handleForwardAction();
                this.setState({
                    messages: MessagesUtils.info("Schedule \'" + item.scheduleName + "\' has been deleted.")
                });
            })
            .catch(this.handleResponseError);
    }

    handleForwardAction = (action) => {
        const loc = window.location;
        if (action === undefined || action === "back") {
            this.getRecurringScheduleList().then(data => {
                history.pushState(null, null, loc.pathname + loc.search);
            });
        } else {
            this.setState({
                action: action
            });
            history.pushState(null, null, loc.pathname + loc.search + "#/" + action);
        }
    };

    clearMessages() {
        this.setState({
            messages: []
        });
    }

    onMessageChanged = (message) => {
        this.setState({messages: message});
    };

    handleResponseError = (jqXHR) => {
        this.setState({
            messages: Network.responseErrorMessage(jqXHR)
        });
    };

    render() {
        const messages = this.state.messages ? <Messages items={this.state.messages}/> : null;

        return (
            <div>
                {messages}
                { (this.state.action === 'details' && this.state.selected) ?
                    <RecurringStatesDetails data={this.state.selected}
                                            onCancel={this.handleForwardAction}
                                            onEdit={this.handleEditAction}
                                            onDelete={this.deleteSchedule}
                    />
                    :
                    (this.state.action === 'create' && this.isFiltered()) ?
                        <RecurringStatesEdit onCreate={this.createSchedule}
                                             onActionChanged={this.handleForwardAction}
                        />
                        :
                        (this.state.action === 'edit' && this.state.selected) ?
                            <RecurringStatesEdit schedule={this.state.selected}
                                                 onEdit={this.updateSchedule}
                                                 onActionChanged={this.handleForwardAction}
                            />
                            :
                            <RecurringStatesList data={this.state.schedules}
                                                 disableCreate={!this.isFiltered()}
                                                 onActionChanged={this.handleForwardAction}
                                                 onToggleActive={this.toggleActive}
                                                 onSkip={this.skipNext}
                                                 onSelect={this.handleDetailsAction}
                                                 onEdit={this.handleEditAction}
                                                 onDelete={this.deleteSchedule}
                            />
                }
            </div>
        );
    }
}

export const renderer = () => SpaRenderer.renderNavigationReact(
    <RecurringStates/>,
    document.getElementById('recurring-states')
);
