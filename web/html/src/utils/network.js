"use strict";

const Functions = require("../utils/functions.js");

function request(url, type, headers, data, contentType) {
   const a = $.ajax({
         url: url,
         data: data,
         type: type,
         contentType: contentType,
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

function post(url, data, contentType) {
    return request(url, "POST", { "X-CSRF-Token": csrfToken }, data, contentType);
}

function del(url, data, contentType) {
    return request(url, "DELETE", { "X-CSRF-Token": csrfToken }, data, contentType);
}

function put(url, data, contentType) {
    return request(url, "PUT", { "X-CSRF-Token": csrfToken }, data, contentType);
}

function get(url, contentType) {
    return request(url, "GET", {}, {}, contentType);
}

module.exports = {
    get: get,
    post: post,
    put: put,
    del: del
}
