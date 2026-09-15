/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
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
