/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { useEffect, useRef } from "react";

/**
 * Get the value of a variable from the previous render of a functional component, initially returns undefined.
 * Can be used for variables, props, state, or anything really.
 */
export function usePrevious<T>(value: T) {
  const ref = useRef<T>();
  useEffect(() => {
    ref.current = value;
  });
  return ref.current;
}
