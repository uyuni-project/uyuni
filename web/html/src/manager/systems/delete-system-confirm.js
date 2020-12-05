/* eslint-disable */
'use strict';

import * as React from 'react';
import ReactDOM from 'react-dom';
import { Utils } from 'utils/functions';
import { DeleteSystem } from './delete-system';
import SpaRenderer from 'core/spa/spa-renderer';

export const renderer = (id) => SpaRenderer.renderNavigationReact(
  <DeleteSystem serverId={getServerIdToDelete()} onDeleteSuccess={() => Utils.urlBounce("/rhn/systems/Overview.do")}
   buttonText={t("Delete Profile")} buttonClass="btn-danger"/>,
    document.getElementById(id)
);
