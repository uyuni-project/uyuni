/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import type { ComponentType } from "react";

/** Log link destinations without leaving the preview or opening an application page. */
export const withLocalNavigation =
  (logDestination: (href: string | null) => void, selector = "a") =>
  (Story: ComponentType) => (
    <div
      onClickCapture={(event) => {
        const link = event.target instanceof Element ? event.target.closest(selector) : null;
        if (link) {
          event.preventDefault();
          logDestination(link.getAttribute("href"));
        }
      }}
    >
      <Story />
    </div>
  );
