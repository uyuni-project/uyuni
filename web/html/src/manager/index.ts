/*
How to add a new route:
All the routes exported on the files '<*folder_name*>/index.ts' will be automatically registered.
Check the file content-management/index.js for an example
*/
import "./polyfills";
import "react-hot-loader";
import "core/spa/spa-engine";
import "core/log";
import "core/debugUtils";
import "./legacy";

import SpaRenderer from "core/spa/spa-renderer";

import Admin from "./admin";
import Appstreams from "./appstreams";
import Audit from "./audit";
import ContentManagement from "./content-management";
import Errors from "./errors";
import Groups from "./groups";
import Header from "./header";
import Images from "./images";
import Login from "./login";
import MaintenanceWindows from "./maintenance";
import Minion from "./minion";
import Notifications from "./notifications";
import Organizations from "./organizations";
import Packages from "./packages";
import Proxy from "./proxy";
import RecurringActions from "./recurring";
import Salt from "./salt";
import ScheduleOptions from "./schedule-options";
import Shared from "./shared";
import Highstate from "./state";
import Systems from "./systems";
import Virtualization from "./virtualization";
import Visualization from "./visualization";

const pages = {
  ...Admin,
  ...Audit,
  ...ContentManagement,
  ...Errors,
  ...Groups,
  ...Header,
  ...Highstate,
  ...RecurringActions,
  ...Images,
  ...Login,
  ...MaintenanceWindows,
  ...Minion,
  ...Notifications,
  ...Organizations,
  ...Packages,
  ...Proxy,
  ...Salt,
  ...ScheduleOptions,
  ...Shared,
  ...Systems,
  ...Virtualization,
  ...Visualization,
  ...Appstreams,
};

window.spaImportReactPage = function spaImportReactPage(pageName) {
  SpaRenderer.addReactApp(pageName);
  return pages[pageName]();
};
