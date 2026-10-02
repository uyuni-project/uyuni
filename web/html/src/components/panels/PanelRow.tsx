/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import type { ReactNode } from "react";

type Props = {
  className?: string;
  children: ReactNode;
};

function PanelRow(props: Props) {
  return <div className={`row ${props.className || ""}`}>{props.children}</div>;
}

export { PanelRow };
