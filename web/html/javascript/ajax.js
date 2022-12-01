/**
 * This file was created as a temporary solution to make it possible to remove the DWR library quickly.
 * Please, don't use the functions available here in any other scenario.
 */

const onAjaxRequestError = function (res, status, error) {
  console.log("Error processing ajax request...")
  console.log(res.responseText, status, error)
}

const ajax_post = function(url, data, onSuccess, onAjaxRequestError, contentType = false) {
  jQuery.ajax({
    type: 'POST',
    url: `/rhn/ajax/${url}`,
    processData: false,
    data,
    contentType: contentType,
    success: onSuccess,
    error: onAjaxRequestError
  })
}

function ajax(url, data, onSuccess, contentType, onError) {
  ajax_post(url, JSON.stringify(data), onSuccess, onError, contentType)
}
