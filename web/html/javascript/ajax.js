var onAjaxRequestError = function (res, status, error) {
  console.log("Error processing ajax request...")
  console.log(res.responseText, status, error)
}

var ajax_post = function(url, data, onSuccess, onError, contentType = false) {
  jQuery.ajax({
    type: 'POST',
    url: `/rhn/ajax/${url}`,
    processData: false,
    data,
    contentType: contentType,
    success: onSuccess,
    error: onError
  })
}

function ajax(url, data, onSuccess, contentType) {
  ajax_post(url, JSON.stringify(data), onSuccess, onAjaxRequestError, contentType)
}
