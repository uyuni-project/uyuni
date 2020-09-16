// @flow
/* eslint-disable */
"use strict";

import Functions from '../utils/functions';
import {Messages, Utils as MessagesUtils} from '../components/messages';

import type {Cancelable} from "../utils/functions";
import type {MessageType} from 'components/messages';

declare var csrfToken: string;

export type JsonResult<T> = {
  success: boolean,
  messages: Array<String>,
  data: T
}

function request(url: string, type:  "GET" | "POST" | "DELETE" | "PUT", headers, data: any, contentType: string, processData: boolean = true) : Cancelable {
   const a = jQuery.ajax({
         url: url,
         data: data,
         type: type,
         contentType: contentType,
         processData: processData,
         beforeSend: (xhr) => {
            if(headers !== undefined) {
                Object.keys(headers).forEach(header => {
                    xhr.setRequestHeader(header, headers[header]);
                });
            }
         }
   });
   return Functions.Utils.cancelable(Promise.resolve(a), () => a.abort());
}

function post(url: string, data: any, contentType: string, processData: boolean = true): Cancelable {
    return request(url, "POST", { "X-CSRF-Token": csrfToken }, data, contentType, processData);
}

function del(url: string, data: any, contentType: string, processData: boolean = true): Cancelable {
    return request(url, "DELETE", { "X-CSRF-Token": csrfToken }, data, contentType, processData);
}

function put(url: string, data: any, contentType: string, processData: boolean = true): Cancelable {
    return request(url, "PUT", { "X-CSRF-Token": csrfToken }, data, contentType, processData);
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

type MapFuncType = (string, string) => ?string;
type jqXHR = {status: number, statusText: string, responseText: string, responseJSON: any};

function responseErrorMessage(jqXHR: Error | jqXHR , messageMapFunc: ?MapFuncType = null): Array<MessageType> {
   if (jqXHR instanceof Error) {
     console.log("Error: " + jqXHR.toString());
     throw jqXHR;
   } else {
     console.log("Error: " + jqXHR.status + " " + jqXHR.statusText + ", response text: " + jqXHR.responseText);
   }

   if (jqXHR.responseJSON && jqXHR.responseJSON.messages &&
        Array.isArray(jqXHR.responseJSON.messages) &&
        jqXHR.responseJSON.messages.length > 0) {
      let msgs : Array<string>;
      if (messageMapFunc) {
        msgs = jqXHR.responseJSON.messages.map(msg => {
            let m = messageMapFunc(jqXHR.status.toString(), msg);
            return m ? m : msg;
        })
      } else {
        msgs = jqXHR.responseJSON.messages;
      }

      return MessagesUtils.error(msgs);
   } else {
      let msg = errorMessageByStatus(jqXHR.status);
      if (msg.length === 0) {
        msg = "Server error, please check log files.";
      }
      return MessagesUtils.error(msg);
   }
}

export {
    get,
    post,
    put,
    del,
    errorMessageByStatus,
    responseErrorMessage
};
