import {Utils} from "../utils/functions";
import {Utils as MessagesUtils} from "../components/messages";

import {Cancelable} from "../utils/functions";
import {MessageType} from "components/messages";

declare var csrfToken: string;

export type JsonResult<T> = {
    success: boolean;
    messages: Array<String>;
    data: T;
};

/**
 * There are some cases where network requests with empty data are accidentally made as follows:
 *  Network.post("url", "application/json", ...);
 * This is a bug and should instead be:
 *  Network.post("url", undefined, "application/json", ...);
 * 
 * This type disallows assigning common MIME types to the data type so we can avoid the bug without
 * changing the network layer logic (which would require a lot of testing and create regressions).
 * 
 * See: https://stackoverflow.com/a/51445345/1470607
 */
type CommonMimeTypes = "application/json" | "application/xml" | "application/x-www-form-urlencoded";
type DataType<T> = T & (T extends CommonMimeTypes ? never : T);

function request(
    url: string,
    type: "GET" | "POST" | "DELETE" | "PUT",
    headers: Record<string, string>,
    data: any,
    contentType: string,
    processData: boolean = true
): Cancelable {
    const a = jQuery.ajax({
        url: url,
        data: data,
        type: type,
        contentType: `${contentType}; charset=UTF-8`,
        processData: processData,
        beforeSend: xhr => {
            if (headers !== undefined) {
                Object.keys(headers).forEach(header => {
                    xhr.setRequestHeader(header, headers[header]);
                });
            }
        },
    });
    return Utils.cancelable(Promise.resolve(a), () => a.abort());
}

function post<T>(url: string, data?: DataType<T>, contentType: string = "application/json", processData: boolean = true): Cancelable {
    return request(url, "POST", {"X-CSRF-Token": csrfToken}, data, contentType, processData);
}

function del<T>(url: string, data?: DataType<T>, contentType: string = "application/json", processData: boolean = true): Cancelable {
    return request(url, "DELETE", {"X-CSRF-Token": csrfToken}, data, contentType, processData);
}

function put<T>(url: string, data?: DataType<T>, contentType: string = "application/json", processData: boolean = true): Cancelable {
    return request(url, "PUT", {"X-CSRF-Token": csrfToken}, data, contentType, processData);
}

function get(url: string, contentType: string = "application/json"): Cancelable {
    return request(url, "GET", {}, {}, contentType);
}

function errorMessageByStatus(status: number): Array<string> {
    if (status === 401) {
        return [t("Session expired, please reload the page.")];
    } else if (status === 403) {
        return [t("Authorization error, please reload the page or try to logout/login again.")];
    } else if (status === 404) {
        return [t("Resource not found.")];
    } else if (status >= 500) {
        return [t("Server error, please check log files.")];
    } else {
        return [t("HTTP Error code " + status)];
    }
}

export type MapFuncType = (status: string, message: string) => string | null | undefined;

function responseErrorMessage(
    jqXHR: Error | JQueryXHR,
    messageMapFunc: MapFuncType | null | undefined = null
): Array<MessageType> {
    if (jqXHR instanceof Error) {
        console.log("Error: " + jqXHR.toString());
        throw jqXHR;
    } else {
        console.log("Error: " + jqXHR.status + " " + jqXHR.statusText + ", response text: " + jqXHR.responseText);
    }

    if (
        jqXHR.responseJSON &&
        jqXHR.responseJSON.messages &&
        Array.isArray(jqXHR.responseJSON.messages) &&
        jqXHR.responseJSON.messages.length > 0
    ) {
        let msgs: Array<string>;
        if (messageMapFunc) {
            msgs = jqXHR.responseJSON.messages.map(msg => {
                let m = messageMapFunc(jqXHR.status.toString(), msg);
                return m ? m : msg;
            });
        } else {
            msgs = jqXHR.responseJSON.messages;
        }

        return MessagesUtils.error(msgs);
    } else {
        let msg: string | string[] = errorMessageByStatus(jqXHR.status);
        if (msg.length === 0) {
            msg = "Server error, please check log files.";
        }
        return MessagesUtils.error(msg);
    }
}

export default {
    get,
    post,
    put,
    del,
    errorMessageByStatus,
    responseErrorMessage,
};
