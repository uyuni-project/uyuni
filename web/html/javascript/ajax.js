/**
 * This file was created as a temporary solution to make it possible to remove the DWR library quickly.
 * Please, don't use the functions available here in any other scenario.
 */

const onAjaxRequestError = function (error) {
  console.log("Error processing ajax request...", error)
}

const ajax_post = function(url, data, onSuccess, onError = onAjaxRequestError, contentType = false) {
  network.post(`/rhn/ajax/${url}`, data, contentType, false)
    .then(onSuccess)
    .catch(onError)
}

function ajax(url, data, onSuccess, contentType, onError) {
  ajax_post(url, JSON.stringify(data), onSuccess, onError, contentType)
}
