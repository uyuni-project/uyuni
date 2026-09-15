/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import { useState } from "react";

import { Check } from "components/input";

export default () => {
  const [checked, setChecked] = useState(true);

  return <Check name="foo" checked={checked} onChange={setChecked} />;
};
