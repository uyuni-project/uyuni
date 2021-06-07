import * as React from "react";
import { Messages } from "components/messages";
import Network from "utils/network";
import { RecurringStatesDetails } from "./recurring-states-details";
import { RecurringStatesList } from "./recurring-states-list";
import { RecurringStatesEdit } from "./recurring-states-edit";
import { Utils as MessagesUtils } from "components/messages";
import SpaRenderer from "core/spa/spa-renderer";

/**
 * See:
 *  - java/code/src/com/suse/manager/webui/templates/groups/recurring-states.jade
 *  - java/code/src/com/suse/manager/webui/templates/minion/recurring-states.jade
 *  - java/code/src/com/suse/manager/webui/templates/org/recurring-states.jade
 *  - java/code/src/com/suse/manager/webui/templates/schedule/recurring-states.jade
 *  - java/code/src/com/suse/manager/webui/templates/yourorg/recurring-states.jade
 */
declare global {
  interface Window {
    groupId?: any;
    groupName?: any;
    orgId?: any;
    orgName?: any;
    minions?: any[];
    entityType?: any;
    timezone?: any;
    localTime?: any;
  }
}

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
    return "/MINION/" + window.minions?.[0].id;
  }
  return "";
}

type Props = {

};

type State = {
  messages: any[];
  schedules: any[];
  minionIds?: any[];
  action?: any;
  selected?: any;
};

class RecurringStates extends React.Component<Props, State> {
  constructor(props) {
    super(props);

    [
      "deleteSchedule",
      "handleForwardAction",
      "handleDetailsAction",
      "handleEditAction",
      "handleResponseError",
      "updateSchedule",
      "toggleActive",
    ].forEach(method => (this[method] = this[method].bind(this)));
    this.state = {
      messages: [],
      schedules: [],
      minionIds: (window.minions?.length ?? 0) > 0 && window.minions?.[0].id ? window.minions?.map(minion => minion.id) : undefined,
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
      this.setState({ action: action });
    }
    this.clearMessages();
  }

  isFilteredList = () => {
    return !!inferEntityParams();
  };

  getRecurringScheduleList = () => {
    const entityParams = inferEntityParams();
    const endpoint = "/rhn/manager/api/recurringactions" + entityParams;
    return Network.get(endpoint)
      .then(schedules => {
        this.setState({
          action: undefined,
          selected: undefined,
          schedules: schedules,
        });
      })
      .catch(this.handleResponseError);
  };

  getScheduleDetails(row, action) {
    this.setState({ selected: row, action: action });
  }

  handleDetailsAction(row) {
    this.getScheduleDetails(row, "details");
    window.history.pushState(null, "", "#/details/" + row.recurringActionId);
  }

  handleEditAction(row) {
    this.getScheduleDetails(row, "edit");
    window.history.pushState(null, "", "#/edit/" + row.recurringActionId);
  }

  toggleActive(schedule) {
    Object.assign(schedule, {
      active: !schedule.active,
    });
    this.updateSchedule(schedule);
  }

  updateSchedule(schedule) {
    return Network.post("/rhn/manager/api/recurringactions/save", JSON.stringify(schedule))
      .then(_ => {
        const successMsg = (
          <span>{t("Schedule successfully" + (this.state.action === "create" ? " created." : " updated."))}</span>
        );
        const msgs = this.state.messages.concat(MessagesUtils.info(successMsg));

        while (msgs.length > messagesCounterLimit) {
          msgs.shift();
        }

        this.setState({
          messages: msgs,
        });

        this.handleForwardAction();
      })
      .catch(this.handleResponseError);
  }

  deleteSchedule(item) {
    return Network.del("/rhn/manager/api/recurringactions/" + item.recurringActionId + "/delete")
      .then(_ => {
        this.setState({
          messages: MessagesUtils.info("Schedule '" + item.scheduleName + "' has been deleted."),
        });
        this.handleForwardAction();
      })
      .catch(data => {
        const taskoErrorMsg = MessagesUtils.error(t("Error when deleting the action. Check if Taskomatic is running"));
        let messages = data && data.status === 503 ? taskoErrorMsg : Network.responseErrorMessage(data);
        this.setState({
          messages: messages,
        });
      });
  }

  handleForwardAction = (action?: string) => {
    const loc = window.location;
    if (typeof action === "undefined" || action === "back") {
      this.getRecurringScheduleList().then(data => {
        window.history.pushState(null, "", loc.pathname + loc.search);
      });
    } else {
      this.setState({
        action: action,
      });
      window.history.pushState(null, "", loc.pathname + loc.search + "#/" + action);
    }
  };

  clearMessages() {
    this.setState({
      messages: [],
    });
  }

  handleResponseError = jqXHR => {
    this.setState({
      messages: Network.responseErrorMessage(jqXHR),
    });
  };

  render() {
    const messages = this.state.messages ? <Messages items={this.state.messages} /> : null;
    const notification = (
      <Messages
        items={[
          {
            severity: "warning",
            text: "The timezone displayed is the server timezone. The scheduled time will be the server time.",
          },
        ]}
      />
    );
    return (
      <div>
        {messages}
        {this.state.action === "details" && this.state.selected ? (
          <RecurringStatesDetails
            data={this.state.selected}
            onCancel={this.handleForwardAction}
            onEdit={this.handleEditAction}
            onDelete={this.deleteSchedule}
          />
        ) : (this.state.action === "edit" && this.state.selected) ||
          (this.state.action === "create" && this.isFilteredList()) ? (
          [
            notification,
            <RecurringStatesEdit
              schedule={this.state.selected}
              onEdit={this.updateSchedule}
              onActionChanged={this.handleForwardAction}
            />,
          ]
        ) : (
          <RecurringStatesList
            data={this.state.schedules}
            disableCreate={!this.isFilteredList()}
            onActionChanged={this.handleForwardAction}
            onToggleActive={this.toggleActive}
            onSelect={this.handleDetailsAction}
            onEdit={this.handleEditAction}
            onDelete={this.deleteSchedule}
          />
        )}
      </div>
    );
  }
}

export const renderer = () =>
  SpaRenderer.renderNavigationReact(<RecurringStates />, document.getElementById("recurring-states"));
