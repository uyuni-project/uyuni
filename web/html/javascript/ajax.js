var onAjaxRequestError = function (request, status, error) {
  console.log("Error processing ajax request...")
  console.log(request.responseText, status, error)
}

var ajax_post = function(url, data, onSuccess, onError, contentType = false) {
  console.log("Starting ajax request...")
  
  var onAjaxRequestSuccess = function(response) {
    console.log("Handling ajax response...")
    onSuccess(response)
  }

  jQuery.ajax({
    type: 'POST',
    url: `/rhn/ajax/${url}`,
    processData: false,
    data,
    contentType: contentType,
    success: onAjaxRequestSuccess,
    error: onError
  })
}

function ajax(url, data, onSuccess, contentType) {
  ajax_post(url, JSON.stringify(data), onSuccess, onAjaxRequestError, contentType)
}
