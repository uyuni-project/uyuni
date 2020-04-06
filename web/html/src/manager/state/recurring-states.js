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

function inferEntityParams() {
    if (window.entityType === "GROUP") {
        return "/GROUP/" + window.groupId;
    } else if (window.entityType === "ORG") {
        return "/ORG/" + window.orgId;
    } else if (window.entityType === "MINION") {
        return "/MINION/" + minions[0].id;
    }
    return "";
}

class RecurringStates extends React.Component {

    constructor(props) {
        super(props);

        ["deleteSchedule", "handleForwardAction", "handleDetailsAction", "handleEditAction", "handleResponseError", "updateSchedule", "toggleActive", "skipNext"]
            .forEach(method => this[method] = this[method].bind(this));
        this.state = {
            messages: [],
            schedules: [],
            minionIds: minions.length > 0 && minions[0].id ? minions.map(minion => minion.id) : undefined,
        };
    }

    componentDidMount() {
        this.updateView(getHashAction(), getHashId());
        window.addEventListener("popstate", () => {
            this.updateView(getHashAction(), getHashId());
        });
    }

    updateView(action, id) {
        if (id || !action) {
            this.handleForwardAction();
        } else {
            this.setState({action: action});
        }
        this.clearMessages();
    }

    isFilteredList = () => {
        return !!inferEntityParams();
    };

    getRecurringScheduleList = () => {
        const entityParams = inferEntityParams();
        const endpoint = "/rhn/manager/api/recurringactions" + entityParams;
        return Network.get(endpoint, "application/json").promise
            .then(schedules => {
                this.setState({
                    action: undefined,
                    selected: undefined,
                    schedules: schedules
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

    skipNext(schedule) {
        Object.assign(schedule, {
            skipNext: !(schedule.skipNext)
        });
        this.updateSchedule(schedule);
    }

    updateSchedule(schedule) {
        return Network.post(
            "/rhn/manager/api/recurringactions/save",
            JSON.stringify(schedule),
            "application/json"
        ).promise.then((_) => {
            const successMsg = <span>{t("Schedule successfully" + (this.state.action === "create" ? " created." : " updated."))}</span>
            const msgs = this.state.messages.concat(MessagesUtils.info(successMsg));

            while (msgs.length > messagesCounterLimit) {
                msgs.shift();
            }

            this.setState({
                messages: msgs
            });

            this.handleForwardAction();
        }).catch(this.handleResponseError);
    }

    deleteSchedule(item) {
        return Network.del("/rhn/manager/api/recurringactions/" + item.recurringActionId + "/delete")
            .promise.then((_) => {
                this.setState({
                    messages: MessagesUtils.info("Schedule \'" + item.scheduleName + "\' has been deleted.")
                });
                this.handleForwardAction();
            })
            .catch(data => {
                const taskoErrorMsg = MessagesUtils.error(
                    t("Error when deleting the action. Check if Taskomatic is running"));
                let messages = (data && data.status === 503)
                    ? taskoErrorMsg
                    : Network.responseErrorMessage(jqXHR);
                this.setState({
                    messages: messages
                });
            });
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

    handleResponseError = (jqXHR) => {
        this.setState({
            messages: Network.responseErrorMessage(jqXHR)
        });
    };

    render() {
        const messages = this.state.messages ? <Messages items={this.state.messages}/> : null;
        const notification = <Messages items={[{
            severity: "warning",
            text: "The timezone displayed is the server timezone. The scheduled time will be the server time."
        }]}/>;
        return (
            <div>
                {messages}
                { (this.state.action === 'details' && this.state.selected) ?
                    <RecurringStatesDetails data={this.state.selected}
                                            onCancel={this.handleForwardAction}
                                            onEdit={this.handleEditAction}
                                            onDelete={this.deleteSchedule}
                    />
                    : (this.state.action === 'edit' && this.state.selected) ||
                    (this.state.action === 'create' && this.isFilteredList()) ? [
                            notification,
                            <RecurringStatesEdit schedule={this.state.selected}
                                                 onEdit={this.updateSchedule}
                                                 onActionChanged={this.handleForwardAction}
                            /> ]
                        :
                        <RecurringStatesList data={this.state.schedules}
                                             disableCreate={!this.isFilteredList()}
                                             onActionChanged={this.handleForwardAction}
                                             onToggleActive={this.toggleActive}
                                             onSkipNext={this.skipNext}
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
