/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { HubDetailData } from "components/hub";
import { MessagesContainer } from "components/toastr";

import { HubDetails } from "./hub-details";

export const renderer = (id: string, hubDetailsData: HubDetailData) => {
  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <HubDetails hub={hubDetailsData} />
    </RolesProvider>,
    document.getElementById(id)
  );
};
