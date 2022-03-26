import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { MgrServer } from "./mgr-server-info";

type RendererProps = {
  serverId: string;
  name: string;
  version: string;
  reportDbName: string;
  reportDbHost: string;
  reportDbPort: number;
  reportDbUser: string;
  reportDbLastSynced?: string;
  isAdmin: boolean;
};

export const renderer = (
  id: string,
  docsLocale: string,
  {
    serverId,
    name,
    version,
    reportDbName,
    reportDbHost,
    reportDbPort,
    reportDbUser,
    reportDbLastSynced,
    isAdmin,
  }: RendererProps
) => {
  SpaRenderer.renderNavigationReact(
    <MgrServer
      serverId={serverId}
      name={name}
      version={version}
      reportDbName={reportDbName}
      reportDbHost={reportDbHost}
      reportDbPort={reportDbPort}
      reportDbUser={reportDbUser}
      reportDbLastSynced={reportDbLastSynced}
      isAdmin={isAdmin}
      docsLocale={docsLocale}
    />,
    document.getElementById(id)
  );
};
