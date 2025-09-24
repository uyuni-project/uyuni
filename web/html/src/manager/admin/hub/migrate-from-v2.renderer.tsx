import * as React from "react";

import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { MigrateSlavesForm, MigrationVersion } from "components/hub";
import { MessagesContainer } from "components/toastr";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <MigrateSlavesForm title={t("Migrate from ISSv2")} migrateFrom={MigrationVersion.v2} />
    </RolesProvider>,
    document.getElementById(id)
  );
};
