/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import { useCallback, useState } from "react";

export function useInputValue(initialValue: string) {
  const [value, setValue] = useState(initialValue);
  const onChange = useCallback((event) => setValue(event.currentTarget.value), []);

  return {
    value,
    onChange,
  };
}
