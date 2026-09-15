/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import { AceEditor } from "components/ace-editor";

import exampleContent from "./index.example.tsx?raw";

export default () => {
  return <AceEditor mode="jsx" content={exampleContent} />;
};
