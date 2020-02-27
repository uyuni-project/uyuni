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

//todo rename scheduleId in json usages to recurringActionId

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
        // todo create different endpoints for each use case ("/rhn/manager/api/recurringactions/group/id")
        const endpoint = "/rhn/manager/api/recurringactions/minion/" + minions[0].id;
        return Network.get(endpoint, "application/json").promise
            .then(schedules => {
                schedules.data.map(schedule => schedule.minionIds = minions.map(minion => minion.id));
                schedules.data.map(schedule => schedule.minionNames = minions.map(minion => minion.name));
                this.setState({
                    action: undefined,
                    selected: undefined,
                    schedules: this.isFiltered() ? this.filterSchedules(schedules) : schedules.data
                });
            }).catch(this.handleResponseError);
    };

    getScheduleDetails(row, action) {
        this.setState({selected: row, action: action});
    }

    handleDetailsAction(row) {
        this.getScheduleDetails(row, "details");
        history.pushState(null, null, "#/details/" + row.recurringActionId);
    }

    handleEditAction(row) {
        this.getScheduleDetails(row, "edit");
        history.pushState(null, null, "#/edit/" + row.recurringActionId);
    }

    toggleActive(schedule) {
        Object.assign(schedule, {
            active: !(schedule.active)
        });
        this.updateSchedule(schedule);
    }

    skipNext(item) {
        /* Write implementation to skip next run */
        this.handleForwardAction();
    }

    createSchedule(schedule) {
        return Network.post(
            "/rhn/manager/api/recurringactions/save",
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
            "/rhn/manager/api/recurringactions/save",
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
        return Network.del("/rhn/manager/api/recurringactions/" + item.recurringActionId + "/delete")
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
