"use strict";


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
   return Promise.resolve(a);
}

function post(url, data, contentType) {
    return request(url, "POST", { "X-CSRF-Token": csrfToken }, data, contentType);
}

function get(url) {
    return request(url, "GET");
}

module.exports = {
    get: get,
    post: post
}
