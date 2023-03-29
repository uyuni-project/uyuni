import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { Messages } from "components/messages";
import { Utils as MessagesUtils } from "components/messages";

import { localizedMoment } from "utils";
import Network from "utils/network";

import { RecurringActionsDetails } from "./recurring-actions-details";
import { RecurringActionsEdit } from "./recurring-actions-edit";
import { RecurringActionsList } from "./recurring-actions-list";

/**
 * See:
 *  - java/code/src/com/suse/manager/webui/templates/groups/recurring-actions.jade
 *  - java/code/src/com/suse/manager/webui/templates/minion/recurring-actions.jade
 *  - java/code/src/com/suse/manager/webui/templates/org/recurring-actions.jade
 *  - java/code/src/com/suse/manager/webui/templates/schedule/recurring-actions.jade
 *  - java/code/src/com/suse/manager/webui/templates/yourorg/recurring-actions.jade
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
const hashUrlRegex = /^#\/([^/]*)(?:\/(.+))?$/;

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

type Props = {};

type State = {
  messages: any[];
  schedules: any[];
  minionIds?: any[];
  action?: any;
  selected?: any;
};

class RecurringActions extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      messages: [],
      schedules: [],
      minionIds:
        (window.minions?.length ?? 0) > 0 && window.minions?.[0].id
          ? window.minions?.map((minion) => minion.id)
          : undefined,
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
      .then((schedules) => {
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

  handleDetailsAction = (row) => {
    this.getScheduleDetails(row, "details");
    window.history.pushState(null, "", "#/details/" + row.recurringActionId);
  };

  handleEditAction = (row) => {
    this.getScheduleDetails(row, "edit");
    window.history.pushState(null, "", "#/edit/" + row.recurringActionId);
  };

  toggleActive = (schedule) => {
    Object.assign(schedule, {
      active: !schedule.active,
    });
    this.updateSchedule(schedule);
  };

  updateSchedule = (schedule) => {
    return Network.post("/rhn/manager/api/recurringactions/save", schedule)
      .then((_) => {
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
  };

  deleteScheduleUpdateTable = (item, tableRef) => {
    return Network.del("/rhn/manager/api/recurringactions/" + item.recurringActionId + "/delete")
      .then((_) => {
        this.setState({
          messages: MessagesUtils.info("Schedule '" + item.scheduleName + "' has been deleted."),
        });
        this.handleForwardAction();
        if (tableRef) {
          tableRef.current?.refresh();
        }
      })
      .catch((data) => {
        const taskoErrorMsg = MessagesUtils.error(t("Error when deleting the action. Check if Taskomatic is running"));
        let messages = data && data.status === 503 ? taskoErrorMsg : Network.responseErrorMessage(data);
        this.setState({
          messages: messages,
        });
      });
  };

  deleteSchedule = (item) => {
    return this.deleteScheduleUpdateTable(item, null);
  };

  handleForwardAction = (action?: string) => {
    const loc = window.location;
    if ((typeof action === "undefined" || action === "back") && this.isFilteredList()) {
      this.getRecurringScheduleList().then((data) => {
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

  handleResponseError = (jqXHR) => {
    this.setState({
      messages: Network.responseErrorMessage(jqXHR),
    });
  };

  render() {
    const messages = this.state.messages ? <Messages key="state-messages" items={this.state.messages} /> : null;
    const notification =
      localizedMoment.userTimeZone !== localizedMoment.serverTimeZone ? (
        <Messages
          key="notification-messages"
          items={[
            {
              severity: "warning",
              text: t(
                "The below times are displayed in the server time zone {0}. The scheduled time will be the server time.",
                localizedMoment.serverTimeZone
              ),
            },
          ]}
        />
      ) : null;
    return (
      <div>
        {messages}
        {this.state.action === "details" && this.state.selected ? (
          <RecurringActionsDetails
            data={this.state.selected}
            onCancel={this.handleForwardAction}
            onEdit={this.handleEditAction}
            onDelete={this.deleteSchedule}
          />
        ) : (this.state.action === "edit" && this.state.selected) ||
          (this.state.action === "create" && this.isFilteredList()) ? (
          <>
            {notification}
            <RecurringActionsEdit
              key="edit"
              schedule={this.state.selected}
              onEdit={this.updateSchedule}
              onActionChanged={this.handleForwardAction}
            />
          </>
        ) : (
          <RecurringActionsList
            data={this.state.schedules}
            isFilteredList={this.isFilteredList()}
            onActionChanged={this.handleForwardAction}
            onToggleActive={this.toggleActive}
            onSelect={this.handleDetailsAction}
            onEdit={this.handleEditAction}
            onDelete={this.deleteScheduleUpdateTable}
          />
        )}
      </div>
    );
  }
}

export const renderer = () =>
  SpaRenderer.renderNavigationReact(<RecurringActions />, document.getElementById("recurring-actions"));
