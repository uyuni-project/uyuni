/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

const {Utils} = require("utils/functions");
const {DeleteSystem} = require("./delete-system");

ReactDOM.render(
  <DeleteSystem serverId={getServerIdToDelete()} onDeleteSuccess={() => Utils.urlBounce("/rhn/systems/Overview.do")}
   buttonText={t("Delete Profile")} buttonClass="btn-danger"/>,
    document.getElementById("delete_system_button")
);
