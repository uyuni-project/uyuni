/*
How to add a new route:
All the routes exported on the files '<*folder_name*>/index.js' will be automatically registered.
Check the file content-management/index.js for an example
*/

import "./polyfills";
import "react-hot-loader";

import SpaRenderer from "core/spa/spa-renderer";
import "core/spa/spa-engine";
import "./legacy";

import Admin from "./admin";
import Audit from "./audit";
import ContentManagement from "./content-management";
import Errors from "./errors";
import Groups from "./groups";
import Highstate from "./state";
import Images from "./images";
import Login from "./login";
import MaintenanceWindows from "./maintenance";
import Minion from "./minion";
import Notifications from "./notifications";
import Organizations from "./organizations";
import Salt from "./salt";
import Shared from "./shared";
import Systems from "./systems";
import Virtualization from "./virtualization";
import Visualization from "./visualization";
import Clusters from "./clusters";

const pages = {
  ...Admin,
  ...Audit,
  ...ContentManagement,
  ...Errors,
  ...Groups,
  ...Highstate,
  ...Images,
  ...Login,
  ...MaintenanceWindows,
  ...Minion,
  ...Notifications,
  ...Organizations,
  ...Salt,
  ...Shared,
  ...Systems,
  ...Virtualization,
  ...Visualization,
  ...Clusters,
};

window.spaImportReactPage = function spaImportReactPage(pageName) {
  SpaRenderer.addReactApp(pageName);
  return pages[pageName]();
};
