/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { MessagesContainer, showSuccessToastr } from "./toastr";

export default () => {
  return (
    <>
      <MessagesContainer />
      <button onClick={() => showSuccessToastr("Great success")}>showSuccessToastr</button>
    </>
  );
};
