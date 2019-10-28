import toastr from "toastr";
import  "toastr/build/toastr.css";
import  "./toastr.css";


toastr.options = {
  "debug": false,
  "newestOnTop": false,
  "progressBar": false,
  "positionClass": "toast-top-full-width",
  "preventDuplicates": false,
  "onclick": null,
  "showDuration": "1200",
  "hideDuration": "500",
  "timeOut": "6000",
  "extendedTimeOut": "4000",
  "showMethod": "fadeIn",
  "hideMethod": "fadeOut"
}

function setOptionsToPersist(toastrIn) {
  toastrIn.options.timeOut = 0;
  toastrIn.options.extendedTimeOut = 0;
}

export function showSuccessToastr(message, optionalParams = {autoHide: true}) {
  if (!optionalParams.autoHide) {
    setOptionsToPersist(toastr);
  }
  toastr.success(message)
}

export function showErrorToastr(message, optionalParams = {autoHide: true}) {
  if (!optionalParams.autoHide) {
    setOptionsToPersist(toastr);
  }
  toastr.error(message)
}

export function showInfoToastr(message, optionalParams = {autoHide: true}) {
  if (!optionalParams.autoHide) {
    setOptionsToPersist(toastr);
  }
  toastr.info(message)
}
