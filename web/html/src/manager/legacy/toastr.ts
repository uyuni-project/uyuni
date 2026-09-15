/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import { showErrorToastr } from "components/toastr";

declare global {
  interface Window {
    showErrorToastr: typeof showErrorToastr;
  }
}
window.showErrorToastr = showErrorToastr;
