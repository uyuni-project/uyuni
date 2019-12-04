/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Messages = require("components/messages").Messages;
const Network = require("utils/network");
const { InnerPanel } = require('components/panels/InnerPanel');
const {RecurringStatesList} = require("./recurring-states-list");
const {RecurringStatesEdit} =  require("./recurring-states-edit");
const MessagesUtils = require("components/messages").Utils;
const SpaRenderer  = require("core/spa/spa-renderer").default;

const hashUrlRegex = /^#\/([^\/]*)(?:\/(.+))?$/;

function getHashId() {
    const match = window.location.hash.match(hashUrlRegex);
    return match ? match[2] : undefined;
}

function getHashAction() {
    const match = window.location.hash.match(hashUrlRegex);
    return match ? match[1] : undefined;
}

export type Schedule = {
    scheduleId: string,
    scheduleName: string,
    type: string,
    minionIds: string,
    minute: string,
    hour: string,
    dayOfMonth: string,
    dayOfWeek: string,
    frequency: string,
    isTest: string,
    createdAt: string,
};

type RecurringStatesStates = {
    schedule: Schedule
}

type RecurringStatesProps = {
    schedules: Array<Schedule>,
    selected: Schedule
}

class RecurringStates extends React.Component<RecurringStatesStates, RecurringStatesProps> {

    constructor(props: RecurringStatesProps) {
        super(props);

        ["deleteSchedule", "handleForwardAction", "handleEditAction", "handleResponseError"]
            .forEach(method => this[method] = this[method].bind(this));
        this.state = {
            messages: [],
            selected: props.selected,
            schedules: props.schedules
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
            /* TODO: implement edit and detail actions */
        } else if (!action) {
            this.getRecurringScheduleList();
        } else {
            this.setState({action: action, id: id});
        }
        this.clearMessages();
    }

    getRecurringScheduleList = () => {
        return Network.get("/rhn/manager/api/states/schedules", "application/json").promise
            .then(schedules => {
                this.setState({
                    action: undefined,
                    selected: undefined,
                    schedules: schedules.data
                });
            }).catch(this.handleResponseError);
    };

    handleDetailsAction(row) {

    }

    getScheduleDetails(id, action) {
        return Network.get("/rhn/manager/api/states/schedules/" + id, "application/json").promise
            .catch(this.handleResponseError);
    }

    handleEditAction(row) {
        this.getScheduleDetails(row.scheduleId).then(data => {
            this.setState({selected: data.data, action: "edit"});
            history.pushState(null, null, "#/edit/" + row.scheduleId);
        });
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
            messages: undefined
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
                { this.state.action === 'create' ?
                    <RecurringStatesEdit onMessageChanged={this.onMessageChanged}
                                         onActionChanged={this.handleForwardAction}
                    />
                    :
                    this.state.action === 'edit' ?
                        <RecurringStatesEdit onMessageChanged={this.onMessageChanged}
                                             onActionChanged={this.handleForwardAction}
                                             schedule={this.state.selected}
                        />
                        :
                        <RecurringStatesList onActionChanged={this.handleForwardAction}
                                             data={this.state.schedules}
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
