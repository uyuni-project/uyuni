/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Messages = require("components/messages").Messages;
const Network = require("utils/network");
const {MaintenanceWindowsDetails} = require("./details/maintenance-windows-details");
const {MaintenanceWindowsList} = require("./list/maintenance-windows-list");
const {MaintenanceWindowsEdit} =  require("./edit/maintenance-windows-edit");
const MessagesUtils = require("components/messages").Utils;
const SpaRenderer  = require("core/spa/spa-renderer").default;
const MaintenanceWindowsApi = require("./maintenance-windows-api");

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

class MaintenanceWindows extends React.Component {

    constructor(props) {
        super(props);

        ["delete", "handleForwardAction", "handleDetailsAction", "handleEditAction", "handleResponseError",
            "update", "refreshCalendar"]
            .forEach(method => this[method] = this[method].bind(this));
        this.state = {
            type: type,
            messages: [],
            schedules: [],
        };
    }

    componentDidMount() {
        this.updateView(getHashAction(), getHashId());
        this.state.type === "schedule" && this.getCalendarNames();
        window.addEventListener("popstate", () => {
            this.updateView(getHashAction(), getHashId());
        });
    }

    updateView(action, id) {
        if (action === "details" && id) {
            this.getDetails(id, "details");
        } else if (id || !action) {
            this.handleForwardAction();
        } else {
            this.setState({action: action});
        }
        this.clearMessages();
    }

    listMaintenanceWindowItems = () => {
        /* Returns a list of maintenance schedules or calendars depending on the type provided */
        return MaintenanceWindowsApi.list(this.state.type)
            .then(schedules => {
                this.setState({
                    action: undefined,
                    selected: undefined,
                    schedules: schedules
                });
            }).catch(this.handleResponseError);
    };

    getCalendarNames = () => {
        return MaintenanceWindowsApi.calendarNames()
            .then(calendarNames => {
                /* Convert list of calendar names into ComboboxItem
                Add "<None>" as first element to allow unassigning of calendars */
                const names = Array.from(Array(calendarNames.length + 1).keys()).map(id => (id === 0)
                    ? ({id: 0, text: "<None>"})
                    : ({id: Number(id), text: calendarNames[id - 1]}));
                this.setState({
                    calendarNames: names
                });
            }).catch(this.handleResponseError);
    };

    getDetails(row, action) {
        /* Returns the details of given schedule or calendar depending on the type provided */
        return MaintenanceWindowsApi.details(row, this.state.type)
            .then(item => {
                this.setState({
                    selected: item,
                    action: action
                });
                history.pushState(null, null, "#/" + action + "/" +
                    (this.state.type === "schedule" ? item.scheduleId : item.calendarId));
            }).catch(this.handleResponseError);
    }

    handleDetailsAction(row) {
        this.getDetails(row, "details");
    }

    handleEditAction(row) {
        this.getDetails(row, "edit");
    }

    toggleActive(schedule) {
        Object.assign(schedule, {
            active: !(schedule.active)
        });
        this.updateSchedule(schedule);
    }

    update(item) {
        return Network.post(
            "/rhn/manager/api/maintenance/" + this.state.type + "/save",
            JSON.stringify(item),
            "application/json"
        ).promise.then((_) => {
            const successMsg = <span>{t(
                (this.state.type === "schedule" ? "Schedule " : "Calendar ") +
                "successfully" + (this.state.action === "create" ? " created." : " updated."))}</span>
            const msgs = this.state.messages.concat(MessagesUtils.info(successMsg));

            this.setState({
                messages: msgs.slice(0, messagesCounterLimit)
            });

            this.handleForwardAction();
        }).catch(this.handleResponseError);
    }

    delete(item) {
        return Network.del("/rhn/manager/api/maintenance/" + this.state.type + "/delete",
            JSON.stringify(item),
            "application/json")
            .promise.then((_) => {
                this.setState({
                    messages: MessagesUtils.info(
                        (this.state.type === "schedule" ? "Schedule " : "Calendar ") +
                        "\'" + (this.state.type === "schedule" ? item.scheduleName : item.calendarName) +
                        "\' has been deleted."
                    )
                });
                this.handleForwardAction();
            })
            .catch(data => {
                const errorMsg = MessagesUtils.error(
                    t("Error when deleting the " + this.state.type));
                let messages = (data && data.status === 400)
                    ? errorMsg
                    : Network.responseErrorMessage(jqXHR);
                this.setState({
                    messages: messages
                });
            });
    }

    refreshCalendar(item) {
        return Network.post(
            "/rhn/manager/api/maintenance/calendar/refresh",
            JSON.stringify(item),
            "application/json"
        ).promise.then((_) => {
            const msgs = this.state.messages.concat(MessagesUtils.info(
                t("Calendar successfully refreshed"))
            );
            this.setState({
                action: undefined,
                messages: msgs.slice(0, messagesCounterLimit)
            });

            this.getDetails(item.calendarId, "edit");
        }).catch(this.handleResponseError);
    };

    handleForwardAction = (action) => {
        const loc = window.location;
        if (action === undefined || action === "back") {
            this.listMaintenanceWindowItems().then(data => {
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
        return (
            <div>
                {messages}
                { this.state.action === 'details' ?
                    <MaintenanceWindowsDetails type={this.state.type}
                                               data={this.state.selected}
                                               onCancel={this.handleForwardAction}
                                               onEdit={this.handleEditAction}
                                               onDelete={this.delete}
                    />
                    : (this.state.action === 'edit' || this.state.action === 'create') && isAdmin ?
                        <MaintenanceWindowsEdit type={this.state.type}
                                                calendarNames={this.state.calendarNames}
                                                schedule={this.state.selected}
                                                onEdit={this.update}
                                                onActionChanged={this.handleForwardAction}
                                                onRefresh={this.refreshCalendar}
                        />
                        :
                        <MaintenanceWindowsList type={this.state.type}
                                                data={this.state.schedules}
                                                onActionChanged={this.handleForwardAction}
                                                onToggleActive={this.toggleActive}
                                                onSelect={this.handleDetailsAction}
                                                onEdit={this.handleEditAction}
                                                onDelete={this.delete}
                        />
                }
            </div>
        );
    }
}

export const renderer = () => SpaRenderer.renderNavigationReact(
    <MaintenanceWindows/>,
    document.getElementById('maintenance-windows')
);
