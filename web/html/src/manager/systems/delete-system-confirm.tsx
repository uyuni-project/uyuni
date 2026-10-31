/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import { Utils } from "utils/functions";

import { DeleteSystem } from "./delete-system";

// See java/core/src/main/resources/WEB-INF/pages/systems/sdc/delete_confirm.jsp
declare global {
  interface Window {
    getServerIdToDelete: () => any;
  }
}

export const renderer = (id: string) =>
  SpaRenderer.renderNavigationReact(
    <DeleteSystem
      serverId={window.getServerIdToDelete()}
      onDeleteSuccess={() => Utils.urlBounce("/rhn/manager/systems/list/all")}
      buttonText={t("Delete Profile")}
      buttonClass="btn-danger"
    />,
    document.getElementById(id)
  );
