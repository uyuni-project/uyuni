/* eslint-disable */
"use strict";

const Functions = require("../utils/functions.js");
const MessagesUtils = require("../components/messages.js").Utils;

export type JsonResult<T> = {
  success: boolean,
  messages: List<String>,
  data: T
}

function request(url, type, headers, data, contentType, processData = true) {
   const a = $.ajax({
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

function post(url, data, contentType, processData = true) {
    return request(url, "POST", { "X-CSRF-Token": csrfToken }, data, contentType, processData);
}

function del(url, data, contentType, processData = true) {
    return request(url, "DELETE", { "X-CSRF-Token": csrfToken }, data, contentType, processData);
}

function put(url, data, contentType, processData = true) {
    return request(url, "PUT", { "X-CSRF-Token": csrfToken }, data, contentType, processData);
}

function get(url, contentType) {
    return request(url, "GET", {}, {}, contentType);
}

function errorMessageByStatus(status) {
    if (status === 401) {
        return [t("Session expired, please reload the page.")];
    } else if (status === 403) {
        return [t("Authorization error, please reload the page or try to logout/login again.")];
    } else if (status >= 500) {
        return [t("Server error, please check log files.")];
    } else {
        return [t("Server error, please check log files.")];
    }
}

function responseErrorMessage(jqXHR, messageMapFunc = null) {
   if (jqXHR instanceof Error) {
     console.log("Error: " + jqXHR);
     throw jqXHR;
   } else {
     console.log("Error: " + jqXHR.status + " " + jqXHR.statusText + ", response text: " + jqXHR.responseText);
   }

   if (jqXHR.responseJSON && jqXHR.responseJSON.messages &&
        Array.isArray(jqXHR.responseJSON.messages) &&
        jqXHR.responseJSON.messages.length > 0) {
      let msgs;
      if (messageMapFunc) {
        msgs = jqXHR.responseJSON.messages.map(msg => {
            let m = messageMapFunc(jqXHR.status, msg);
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

module.exports = {
    get: get,
    post: post,
    put: put,
    del: del,
    errorMessageByStatus: errorMessageByStatus,
    responseErrorMessage: responseErrorMessage
}
