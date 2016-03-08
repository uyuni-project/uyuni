"use strict";


function request(url, type, headers, data, contentType, doneCallback, failCallback) {
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

   if (doneCallback) {
       a.done(doneCallback);
   }
   if (failCallback) {
       a.fail(failCallback);
   }

   return Promise.resolve(a);
}

function post(url, data, contentType, doneCallback, failCallback) {
    return request(url, "POST", { "X-CSRF-Token": csrfToken }, data, contentType, doneCallback, failCallback);
}

function del(url, data, contentType, doneCallback, failCallback) {
    return request(url, "DELETE", { "X-CSRF-Token": csrfToken }, data, contentType, doneCallback, failCallback);
}

function put(url, data, contentType, doneCallback, failCallback) {
    return request(url, "PUT", { "X-CSRF-Token": csrfToken }, data, contentType, doneCallback, failCallback);
}

function get(url) {
    return request(url, "GET");
}

module.exports = {
    get: get,
    post: post,
    put: put,
    del: del
}
