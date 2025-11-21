import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { MigrateSlavesForm, MigrationEntry, MigrationVersion } from "components/hub";
import { MessagesContainer } from "components/toastr";

export const renderer = (id: string, migrationEntries: MigrationEntry[]) => {
  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <MigrateSlavesForm
        title={t("Migrate from ISSv1 Slaves")}
        migrationEntries={migrationEntries}
        migrateFrom={MigrationVersion.v1}
      />
    </RolesProvider>,
    document.getElementById(id)
  );
};
