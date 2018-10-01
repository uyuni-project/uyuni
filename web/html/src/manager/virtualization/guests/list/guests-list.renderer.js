// @flow
const GuestsList = require("./guests-list");
const ReactDOM = require("react-dom");
const React = require("react");
window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.guests = window.pageRenderers.guests || {};
window.pageRenderers.guests.list = window.pageRenderers.guests.list || {};
window.pageRenderers.guests.list.renderer = (id, {server_id, salt_entitled, is_admin}) => ReactDOM.render(
  <GuestsList
    refreshInterval={5 * 1000}
    server_id={server_id}
    salt_entitled={salt_entitled}
    is_admin={is_admin}/>,
  document.getElementById(id)
);
