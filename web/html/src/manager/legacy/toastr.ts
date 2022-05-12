import { showErrorToastr } from "components/toastr";

declare global {
  interface Window {
    showErrorToastr: typeof showErrorToastr;
  }
}
window.showErrorToastr = showErrorToastr;
