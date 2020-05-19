/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Messages = require("components/messages").Messages;
const Network = require("utils/network");
const {MaintenanceSchedulesDetails} = require("./maintenance-schedules-details");
const {MaintenanceSchedulesList} = require("./maintenance-schedules-list");
const {MaintenanceSchedulesEdit} =  require("./maintenance-schedules-edit");
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

class MaintenanceSchedules extends React.Component {

    constructor(props) {
        super(props);

        ["deleteSchedule", "handleForwardAction", "handleDetailsAction", "handleEditAction", "handleResponseError", "updateSchedule"]
            .forEach(method => this[method] = this[method].bind(this));
        this.state = {
            messages: [],
            schedules: [],
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

    getMaintenanceSchedules = () => {
        const endpoint = "/rhn/manager/api/maintenance";
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

    getScheduleDetails(row, action) {
        this.setState({selected: row, action: action});
    }

    handleDetailsAction(row) {
        this.getScheduleDetails(row, "details");
        history.pushState(null, null, "#/details/" + row.scheduleId);
    }

    handleEditAction(row) {
        this.getScheduleDetails(row, "edit");
        this.getCalendarNames();
        history.pushState(null, null, "#/edit/" + row.scheduleId);
    }

    toggleActive(schedule) {
        Object.assign(schedule, {
            active: !(schedule.active)
        });
        this.updateSchedule(schedule);
    }

    updateSchedule(schedule) {
        return Network.post(
            "/rhn/manager/api/maintenance/save",
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
        return Network.del("/rhn/manager/api/maintenance/" + item.scheduleId + "/delete")
            .promise.then((_) => {
                this.setState({
                    messages: MessagesUtils.info("Schedule \'" + item.scheduleName + "\' has been deleted.")
                });
                this.handleForwardAction();
            })
            .catch(data => {
                const errorMsg = MessagesUtils.error(
                    t("Error when deleting the schedule"));
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
            if (action === "create") {
                this.getCalendarNames();
            }
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
                    <MaintenanceSchedulesDetails data={this.state.selected}
                                            onCancel={this.handleForwardAction}
                                            onEdit={this.handleEditAction}
                                            onDelete={this.deleteSchedule}
                    />
                    : (this.state.action === 'edit' && this.state.selected) ||
                    this.state.action === 'create' ?
                        <MaintenanceSchedulesEdit calendarNames={this.state.calendarNames}
                                                  schedule={this.state.selected}
                                                  onEdit={this.updateSchedule}
                                                  onActionChanged={this.handleForwardAction}
                        />
                        :
                        <MaintenanceSchedulesList data={this.state.schedules}
                                             onActionChanged={this.handleForwardAction}
                                             onToggleActive={this.toggleActive}
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
    <MaintenanceSchedules/>,
    document.getElementById('maintenance-schedules')
);
