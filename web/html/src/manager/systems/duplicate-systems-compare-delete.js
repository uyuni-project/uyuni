/* eslint-disable */
'use strict';

import * as React from 'react';
import ReactDOM from 'react-dom';
import { Utils } from '../../utils/functions';
import { DeleteSystem } from './delete-system';
import SpaRenderer from 'core/spa/spa-renderer';

function postForm(serverId) {
  const form = jQuery("table[class='list compare-list']").closest("form");
  jQuery('<input>').attr({
      type: "hidden",
      id: "removedServerId",
      name: "removedServerId",
      value: serverId
  }).appendTo(form);
  form.submit();
}

export const renderer = () => {
  getServerIdsToDelete().forEach(serverId => {
    SpaRenderer.renderNavigationReact(
      <DeleteSystem serverId={serverId} onDeleteSuccess={() => postForm(serverId)}
                    buttonText={t("Confirm Deletion")} buttonClass="btn-danger"
      />,
      document.getElementById("div_confirm" + serverId)
    );
  });
}
