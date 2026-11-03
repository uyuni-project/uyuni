/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

type CounterBadgeProps = {
  count?: string;
  status?: "default" | "highlight";
};

export const CounterBadge = ({ count, status = "default" }: CounterBadgeProps) => {
  // Determine the class name based on the status prop
  const badgeClassName = `badge counter-badge badge--${status}`;

  return <span className={badgeClassName}>{count}</span>;
};
