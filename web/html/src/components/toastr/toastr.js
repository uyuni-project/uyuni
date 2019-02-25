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
  "timeOut": "2700",
  "extendedTimeOut": "1000",
  "showMethod": "fadeIn",
  "hideMethod": "fadeOut"
}


export function showSuccessToastr(message) {
  toastr.success(message)
}

export function showErrorToastr(message) {
  toastr.error(message)
}

export function showInfoToastr(message) {
  toastr.info(message)
}
