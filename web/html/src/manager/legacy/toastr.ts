/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { showErrorToastr } from "components/toastr";

declare global {
  interface Window {
    showErrorToastr: typeof showErrorToastr;
  }
}
window.showErrorToastr = showErrorToastr;
