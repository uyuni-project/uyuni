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

// HACK: infer entity type and id based on the globals set
function inferEntityParams() {
    if (window.groupId !== undefined) {
        return "/GROUP/" + window.groupId;
    } else if (window.orgId !== undefined) {
        return "/ORG/" + window.orgId;
    } else if (minions.length > 0) {
        return "/MINION/" + minions[0].id;
    }
    return "";
}

class RecurringStates extends React.Component {

    constructor(props) {
        super(props);

        ["deleteSchedule", "handleForwardAction", "handleDetailsAction", "handleEditAction", "handleResponseError",
        "onMessageChanged", "updateSchedule", "toggleActive"]
            .forEach(method => this[method] = this[method].bind(this));
        this.state = {
            messages: [],
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

    getRecurringScheduleList = () => {
        // todo create different endpoints for each use case ("/rhn/manager/api/recurringactions/group/id")
        const entityParams = inferEntityParams();
        const endpoint = "/rhn/manager/api/recurringactions" + entityParams;
        return Network.get(endpoint, "application/json").promise
            .then(schedules => {
                this.setState({
                    action: undefined,
                    selected: undefined,
                    schedules: schedules.data
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

    updateSchedule(schedule) {
        return Network.post(
            "/rhn/manager/api/recurringactions/save",
            JSON.stringify(schedule),
            "application/json"
        ).promise.then((data) => {
            // HACK: propagate the errors from messages to the UI
            let newMsgs = [];
            if (data.messages === undefined || data.messages.length === 0) { // no errors from the server
                newMsgs = MessagesUtils.info(<span>{t("Schedule successully created.")}</span>);
            } else {
                const decorator = data.success ? MessagesUtils.info : MessagesUtils.error;
                newMsgs = decorator.apply(null, data.messages);
            }

            const msgs = this.state.messages.concat(newMsgs);

            while (msgs.length > messagesCounterLimit) {
                msgs.shift();
            }

            this.onMessageChanged(msgs);
            this.setState({
                messages: msgs
            });

            if (data.success) {
                this.handleForwardAction();
            } else {
                this.handleResponseError();
            }

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
        const notification = <Messages items={[{
            severity: "warning",
            text: "The timezone displayed is the server timezone. Picked execution time is server execution time"
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
                    (this.state.action === 'create' && this.isFiltered()) ? [
                            notification,
                            <RecurringStatesEdit schedule={this.state.selected}
                                                 onEdit={this.updateSchedule}
                                                 onActionChanged={this.handleForwardAction}
                            /> ]
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
