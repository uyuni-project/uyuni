import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { Messages } from "components/messages/messages";
import { Utils as MessagesUtils } from "components/messages/messages";

import { localizedMoment } from "utils";
import Network from "utils/network";

import { RecurringActionsDetails } from "./recurring-actions-details";
import { RecurringActionsEdit } from "./recurring-actions-edit";
import { RecurringActionsList } from "./recurring-actions-list";
import { inferEntityParams } from "./recurring-actions-utils";

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

const hashUrlRegex = /^#\/([^/]*)(?:\/(.+))?$/;

function getHashId() {
  const match = window.location.hash.match(hashUrlRegex);
  return match ? match[2] : undefined;
}

function getHashAction() {
  const match = window.location.hash.match(hashUrlRegex);
  return match ? match[1] : undefined;
}

type Props = {};

type State = {
  messages: any[];
  minionIds?: any[];
  action?: any;
  selected?: any;
};

class RecurringActions extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      messages: [],
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
    this.setMessages([]);
  }

  isFilteredList = () => {
    return !!inferEntityParams();
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

  handleForwardAction = (action?: string) => {
    const loc = window.location;
    if ((typeof action === "undefined" || action === "back") && this.isFilteredList()) {
      this.setState({
        action: undefined,
        selected: undefined,
      });
      window.history.pushState(null, "", loc.pathname + loc.search);
    } else {
      this.setState({
        action: action,
      });
      window.history.pushState(null, "", loc.pathname + loc.search + "#/" + action);
    }
  };

  setMessages = (messages) => {
    this.setState({
      messages: messages,
    });
  };

  handleResponseError = (jqXHR) => {
    this.setState({
      messages: Network.responseErrorMessage(jqXHR),
    });
  };

  handleDeleteError = (jqXHR) => {
    const taskoErrorMsg = MessagesUtils.error(t("Error when deleting the action. Check if Taskomatic is running"));
    let messages = jqXHR && jqXHR.status === 503 ? taskoErrorMsg : Network.responseErrorMessage(jqXHR);
    this.setMessages(messages);
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
                "The below times are displayed in the server time zone {timeZone}. The scheduled time will be the server time.",
                { timeZone: localizedMoment.serverTimeZone }
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
            onDeleteError={this.handleDeleteError}
            onEdit={this.handleEditAction}
            onError={this.handleResponseError}
            onSetMessages={this.setMessages}
          />
        ) : (this.state.action === "edit" && this.state.selected) ||
          (this.state.action === "create" && this.isFilteredList()) ? (
          <>
            {notification}
            <RecurringActionsEdit
              key="edit"
              schedule={this.state.selected}
              onActionChanged={this.handleForwardAction}
              onError={this.handleResponseError}
              onSetMessages={this.setMessages}
            />
          </>
        ) : (
          <RecurringActionsList
            isFilteredList={this.isFilteredList()}
            onActionChanged={this.handleForwardAction}
            onDeleteError={this.handleDeleteError}
            onEdit={this.handleEditAction}
            onError={this.handleResponseError}
            onSelect={this.handleDetailsAction}
            onSetMessages={this.setMessages}
          />
        )}
      </div>
    );
  }
}

export const renderer = () =>
  SpaRenderer.renderNavigationReact(<RecurringActions />, document.getElementById("recurring-actions"));
