/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Messages = require("components/messages").Messages;
const Network = require("utils/network");
const {MaintenanceWindowsDetails} = require("./maintenance-windows-details");
const {MaintenanceWindowsList} = require("./maintenance-windows-list");
const {MaintenanceWindowsEdit} =  require("./maintenance-windows-edit");
const MessagesUtils = require("components/messages").Utils;
const SpaRenderer  = require("core/spa/spa-renderer").default;

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

        ["delete", "handleForwardAction", "handleDetailsAction", "handleEditAction", "handleResponseError", "update"]
            .forEach(method => this[method] = this[method].bind(this));
        this.state = {
            messages: [],
            schedules: [],
        };
    }

    componentDidMount() {
        this.updateView(getHashAction(), getHashId());
        type === "schedule" && this.getCalendarNames();
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

    getMaintenanceSchedules = () => {
        const endpoint = "/rhn/manager/api/maintenance/" + type + "/list";
        return Network.get(endpoint, "application/json").promise
            .then(schedules => {
                this.setState({
                    action: undefined,
                    selected: undefined,
                    schedules: schedules
                });
            }).catch(this.handleResponseError);
    };

    getCalendarNames = () => {
      const endpoint = "/rhn/manager/api/maintenance/calendar";
      return Network.get(endpoint, "application/json").promise
          .then(calendarNames => {
              this.setState({
                  /* TODO: Is there a prettier way to turn Array into ComboboxItem? */
                  calendarNames: Array.from(Array(calendarNames.length).keys()).map(id => ({id: Number(id), text: calendarNames[id]}))
              });
          }).catch(this.handleResponseError);
    };

    getDetails(row, action) {
        const endpoint = "/rhn/manager/api/maintenance/" + type + "/" +
            (type === "schedule" ? row.scheduleName : row.calendarName) + "/details";
        return Network.get(endpoint, "application/json").promise
            .then(item => {
                this.setState({
                    selected: item,
                    action: action
                });
                history.pushState(null, null, "#/" + action + "/" +
                    (type === "schedule" ? item.scheduleId : item.calendarId));
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
            "/rhn/manager/api/maintenance/" + type + "/save",
            JSON.stringify(item),
            "application/json"
        ).promise.then((_) => {
            const successMsg = <span>{t(
                (type === "schedule" ? "Schedule " : "Calendar ") +
                "successfully" + (this.state.action === "create" ? " created." : " updated."))}</span>
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

    delete(item) {
        /* TODO: schedule name in url fine? Or id better? */
        return Network.del("/rhn/manager/api/maintenance/" + type + "/delete",
            JSON.stringify(item),
            "application/json")
            .promise.then((_) => {
                this.setState({
                    messages: MessagesUtils.info(
                        (type === "schedule" ? "Schedule " : "Calendar ") +
                        "\'" + (type === "schedule" ? item.scheduleName : item.calendarName) +
                        "\' has been deleted."
                    )
                });
                this.handleForwardAction();
            })
            .catch(data => {
                const errorMsg = MessagesUtils.error(
                    t("Error when deleting the " + type));
                let messages = (data && data.status === 400)
                    ? errorMsg
                    : Network.responseErrorMessage(jqXHR);
                this.setState({
                    messages: messages
                });
            });
    }

    handleForwardAction = (action) => {
        const loc = window.location;
        if (action === undefined || action === "back") {
            this.getMaintenanceSchedules().then(data => {
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
                { (this.state.action === 'details' && this.state.selected) ?
                    <MaintenanceWindowsDetails data={this.state.selected}
                                            onCancel={this.handleForwardAction}
                                            onEdit={this.handleEditAction}
                                            onDelete={this.delete}
                    />
                    : (this.state.action === 'edit' && this.state.selected) ||
                    this.state.action === 'create' ?
                        <MaintenanceWindowsEdit calendarNames={this.state.calendarNames}
                                                  schedule={this.state.selected}
                                                  onEdit={this.update}
                                                  onActionChanged={this.handleForwardAction}
                        />
                        :
                        <MaintenanceWindowsList data={this.state.schedules}
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
