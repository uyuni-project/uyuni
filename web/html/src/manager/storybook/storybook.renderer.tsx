/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import { Storybook } from "./storybook";

export const renderer = (id: string) =>
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <Storybook />
    </>,
    document.getElementById(id)
  );
