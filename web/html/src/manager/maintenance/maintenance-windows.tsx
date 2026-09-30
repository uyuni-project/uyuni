import { useEffect, useRef, useState } from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { Messages, Utils as MessagesUtils } from "components/messages/messages";

import Network from "utils/network";

import MaintenanceWindowsApi from "./api/maintenance-windows-api";
import { MaintenanceWindowsDetails } from "./details/maintenance-windows-details";
import { MaintenanceWindowsEdit } from "./edit/maintenance-windows-edit";
import { MaintenanceWindowsList } from "./list/maintenance-windows-list";

// See java/core/src/main/resources/com/suse/manager/webui/templates/schedule/maintenance-windows.jade
declare global {
  interface Window {
    timezone?: any;
    localTime?: any;
    type?: any;
    isAdmin?: any;
    preferredLocale?: any;
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

const MaintenanceWindows = () => {
  const isMounted = useRef(true);
  const [messages, setMessages] = useState<any[]>([]);
  const [items, setItems] = useState<any[]>([]);
  const [action, setAction] = useState<string | undefined>();
  const [selected, setSelected] = useState<any | undefined>();
  const [calendarNames, setCalendarNames] = useState<any>();

  useEffect(() => {
    updateView(getHashAction(), getHashId());
    if (window.type === "schedule") {
      getCalendarNames();
    }

    const onPopState = () => {
      updateView(getHashAction(), getHashId());
    };
    window.addEventListener("popstate", onPopState);

    return () => {
      isMounted.current = false;
      window.removeEventListener("popstate", onPopState);
    };
  }, []);

  const updateView = (newAction, id) => {
    if (!isMounted.current) {
      return;
    }

    if (newAction === "details" && id) {
      getDetails(id, "details");
    } else if (id || !newAction) {
      handleForwardAction();
    } else {
      setAction(newAction);
    }
    clearMessages();
  };

  const listMaintenanceWindowItems = () => {
    /* Returns a list of maintenance schedules or calendars depending on the type provided */
    return MaintenanceWindowsApi.list(window.type)
      .then((newItems) => {
        if (!isMounted.current) {
          return;
        }

        setAction(undefined);
        setSelected(undefined);
        setItems(newItems);
      })
      .catch(handleResponseError);
  };

  const getCalendarNames = () => {
    return MaintenanceWindowsApi.calendarNames()
      .then((newCalendarNames) => {
        if (!isMounted.current) {
          return;
        }

        /* Convert list of calendar names into ComboboxItem
      Add "<None>" as first element to allow unassigning of calendars */
        const names = Array.from(new Array(newCalendarNames.length + 1).keys()).map((id) =>
          id === 0 ? { id: 0, text: "<None>" } : { id: Number(id), text: newCalendarNames[id - 1] }
        );
        setCalendarNames(names);
      })
      .catch(handleResponseError);
  };

  const getDetails = (row, newAction) => {
    /* Returns the details of given schedule or calendar depending on the type provided */
    return MaintenanceWindowsApi.details(row, window.type)
      .then((newItem) => {
        if (!isMounted.current) {
          return;
        }

        setSelected(newItem);
        setAction(newAction);
        window.history.pushState(null, "", "#/" + newAction + "/" + newItem.id);
      })
      .catch(handleResponseError);
  };

  const handleDetailsAction = (row) => {
    getDetails(row, "details");
  };

  const handleEditAction = (row) => {
    getDetails(row, "edit");
  };

  const update = (itemIn) => {
    return Network.post("/rhn/manager/api/maintenance/" + window.type + "/save", itemIn)
      .then(() => {
        if (!isMounted.current) {
          return;
        }

        const successMsg = (
          <span>
            {t(
              (window.type === "schedule" ? "Schedule " : "Calendar ") +
                "successfully" +
                (action === "create" ? " created." : " updated.")
            )}
          </span>
        );
        const msgs = messages.concat(MessagesUtils.info(successMsg));

        setMessages(msgs.slice(-messagesCounterLimit));

        handleForwardAction();
      })
      .catch(handleResponseError);
  };

  const deleteItem = (itemIn) => {
    return Network.del("/rhn/manager/api/maintenance/" + window.type + "/delete", itemIn)
      .then(() => {
        if (!isMounted.current) {
          return;
        }

        setMessages(
          MessagesUtils.info(
            (window.type === "schedule" ? "Schedule " : "Calendar ") + "'" + itemIn.name + "' has been deleted."
          )
        );
        handleForwardAction();
      })
      .catch((data) => {
        if (!isMounted.current) {
          return;
        }

        const errorMsg = MessagesUtils.error(t("Error when deleting the " + window.type));
        const msgs = data && data.status === 400 ? errorMsg : Network.responseErrorMessage(data);
        setMessages(msgs);
      });
  };

  const refreshCalendar = (itemIn) => {
    return Network.post("/rhn/manager/api/maintenance/calendar/refresh", itemIn)
      .then(() => {
        if (!isMounted.current) {
          return;
        }

        const msgs = messages.concat(MessagesUtils.info(t("Calendar successfully refreshed")));
        setAction(undefined);
        setMessages(msgs.slice(-messagesCounterLimit));

        getDetails(itemIn.id, "edit");
      })
      .catch(handleResponseError);
  };

  const handleForwardAction = (newAction?: string) => {
    if (!isMounted.current) {
      return;
    }

    const loc = window.location;
    if (newAction === undefined || newAction === "back") {
      listMaintenanceWindowItems().then(() => {
        if (!isMounted.current) {
          return;
        }

        window.history.pushState(null, "", loc.pathname + loc.search);
      });
    } else {
      setAction(newAction);
      window.history.pushState(null, "", loc.pathname + loc.search + "#/" + newAction);
    }
  };

  const clearMessages = () => {
    if (!isMounted.current) {
      return;
    }

    setMessages([]);
  };

  const handleResponseError = (jqXHR) => {
    if (!isMounted.current) {
      return;
    }

    setMessages(Network.responseErrorMessage(jqXHR));
  };

  return (
    <div>
      <Messages items={messages} />
      {action === "details" ? (
        <MaintenanceWindowsDetails
          type={window.type}
          data={selected}
          onCancel={handleForwardAction}
          onEdit={handleEditAction}
          onMessage={setMessages}
          onDelete={deleteItem}
          responseError={handleResponseError}
          clearMessages={clearMessages}
        />
      ) : (action === "edit" || action === "create") && window.isAdmin ? (
        <MaintenanceWindowsEdit
          type={window.type}
          calendarNames={calendarNames}
          selected={selected}
          messages={(i) => setMessages(i)}
          onEdit={update}
          onActionChanged={handleForwardAction}
          onRefresh={refreshCalendar}
        />
      ) : (
        <MaintenanceWindowsList
          type={window.type}
          data={items}
          onActionChanged={handleForwardAction}
          onSelect={handleDetailsAction}
          onEdit={handleEditAction}
          onDelete={deleteItem}
        />
      )}
    </div>
  );
};

export const renderer = () =>
  SpaRenderer.renderNavigationReact(<MaintenanceWindows />, document.getElementById("maintenance-windows"));
