/* eslint-disable */
'use strict';

import * as React from 'react';
import {useState, useEffect} from "react";
import {Messages} from "components/messages";

import {MaintenanceWindowsDetails} from "./details/maintenance-windows-details";
import {MaintenanceWindowsList} from "./list/maintenance-windows-list";
import {MaintenanceWindowsEdit} from "./edit/maintenance-windows-edit";

import { Utils as MessagesUtils } from 'components/messages';
import SpaRenderer from 'core/spa/spa-renderer';
import Network from 'utils/network';
import MaintenanceWindowsApi from './api/maintenance-windows-api';

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

const MaintenanceWindows = () => {
    const [messages, setMessages] = useState([]);
    const [items, setItems] = useState([]);
    const [action, setAction] = useState();
    const [selected, setSelected] = useState();
    const [calendarNames,setCalendarNames] = useState();

    useEffect(() => {
        updateView(getHashAction(), getHashId());
        type === "schedule" && getCalendarNames();
        window.addEventListener("popstate", () => {
            updateView(getHashAction(), getHashId());
        });
    }, []);

    const updateView = (newAction, id) => {
        if (newAction === "details" && id) {
            getDetails(id, "details");
        } else if (id || !newAction) {
            handleForwardAction();
        } else {
            setAction(newAction);
        }
        clearMessages();
    }

    const listMaintenanceWindowItems = () => {
        /* Returns a list of maintenance schedules or calendars depending on the type provided */
        return MaintenanceWindowsApi.list(type)
            .then(newItems => {
                setAction();
                setSelected();
                setItems(newItems);
            }).catch(handleResponseError);
    };

    const getCalendarNames = () => {
        return MaintenanceWindowsApi.calendarNames()
            .then(newCalendarNames => {
                /* Convert list of calendar names into ComboboxItem
                Add "<None>" as first element to allow unassigning of calendars */
                const names = Array.from(Array(newCalendarNames.length + 1).keys()).map(id => (id === 0)
                    ? ({id: 0, text: "<None>"})
                    : ({id: Number(id), text: newCalendarNames[id - 1]}));
                setCalendarNames(names);
            }).catch(handleResponseError);
    };

    const getDetails = (row, newAction) => {
        /* Returns the details of given schedule or calendar depending on the type provided */
        return MaintenanceWindowsApi.details(row, type)
            .then(newItem => {
                setSelected(newItem);
                setAction(newAction);
                history.pushState(null, null, "#/" + newAction + "/" + newItem.id);
            }).catch(handleResponseError);
    }

    const handleDetailsAction = (row) => {
        getDetails(row, "details");
    }

    const handleEditAction = (row) => {
        getDetails(row, "edit");
    }

    const update = (itemIn) => {
        return Network.post(
            "/rhn/manager/api/maintenance/" + type + "/save",
            JSON.stringify(itemIn),
            "application/json"
        ).promise.then((_) => {
            const successMsg = <span>{t(
                (type === "schedule" ? "Schedule " : "Calendar ") +
                "successfully" + (action === "create" ? " created." : " updated."))}</span>
            const msgs = messages.concat(MessagesUtils.info(successMsg));

            setMessages(msgs.slice(-messagesCounterLimit));

            handleForwardAction();
        }).catch(handleResponseError);
    }

    const deleteItem = (itemIn) => {
        return Network.del("/rhn/manager/api/maintenance/" + type + "/delete",
            JSON.stringify(itemIn),
            "application/json")
            .promise.then((_) => {
                setMessages(
                    MessagesUtils.info(
                        (type === "schedule" ? "Schedule " : "Calendar ") + "\'" +
                        itemIn.name + "\' has been deleted."
                    )
                );
                handleForwardAction();
            })
            .catch(data => {
                const errorMsg = MessagesUtils.error(
                    t("Error when deleting the " + type));
                const msgs = (data && data.status === 400)
                    ? errorMsg
                    : Network.responseErrorMessage(jqXHR);
                setMessages(msgs);
            });
    }

    const refreshCalendar = (itemIn) => {
        return Network.post(
            "/rhn/manager/api/maintenance/calendar/refresh",
            JSON.stringify(itemIn),
            "application/json"
        ).promise.then((_) => {
            const msgs = messages.concat(MessagesUtils.info(
                t("Calendar successfully refreshed"))
            );
            setAction();
            setMessages(msgs.slice(-messagesCounterLimit));

            getDetails(itemIn.id, "edit");
        }).catch(handleResponseError);
    };

    const handleForwardAction = (newAction) => {
        const loc = window.location;
        if (newAction === undefined || newAction === "back") {
            listMaintenanceWindowItems().then(data => {
                history.pushState(null, null, loc.pathname + loc.search);
            });
        } else {
            setAction(newAction);
            history.pushState(null, null, loc.pathname + loc.search + "#/" + newAction);
        }
    };

    const clearMessages = () => {
        setMessages([]);
    }

    const handleResponseError = (jqXHR) => {
        setMessages(Network.responseErrorMessage(jqXHR));
    };

    return (
        <div>
            <Messages items={messages} />
            { action === 'details' ?
                <MaintenanceWindowsDetails type={type}
                                           data={selected}
                                           onCancel={handleForwardAction}
                                           onEdit={handleEditAction}
                                           onMessage={setMessages}
                                           onDelete={deleteItem}
                />
                : (action === 'edit' || action === 'create') && isAdmin ?
                    <MaintenanceWindowsEdit type={type}
                                            calendarNames={calendarNames}
                                            selected={selected}
                                            messages={i => setMessages(i)}
                                            onEdit={update}
                                            onActionChanged={handleForwardAction}
                                            onRefresh={refreshCalendar}
                    />
                    :
                    <MaintenanceWindowsList type={type}
                                            data={items}
                                            onActionChanged={handleForwardAction}
                                            onSelect={handleDetailsAction}
                                            onEdit={handleEditAction}
                                            onDelete={deleteItem}
                    />
            }
        </div>
    );
}

export const renderer = () => SpaRenderer.renderNavigationReact(
    <MaintenanceWindows/>,
    document.getElementById('maintenance-windows')
);
