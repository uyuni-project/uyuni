/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
// Mock the ACE Editor to avoid it causing issues due to the missing proper library import
jest.mock("components/ace-editor", () => {
  return {
    __esModule: true,
    AceEditor: () => {
      return <div>My AceEditor mockup</div>;
    },
  };
});
