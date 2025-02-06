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
import Storybook from "./storybook";
import Systems from "./systems";
import ActivationKeys from "./systems/activation-key";

const pages = {
  ...ActivationKeys,
  ...Admin,
  // ...Appstreams,
  // ...Audit,
  // ...ContentManagement,
  // ...Errors,
  // ...Groups,
  // ...Header,
  // ...Highstate,
  // ...Images,
  // ...Login,
  // ...MaintenanceWindows,
  // ...Minion,
  // ...Notifications,
  // ...Organizations,
  // ...Packages,
  // ...Proxy,
  // ...RecurringActions,
  // ...Salt,
  // ...ScheduleOptions,
  // ...Shared,
  // ...Systems,
  // ...Storybook,
};

type Pages = typeof pages;
type PageName = keyof Pages;
type Renderer<T extends PageName> = Awaited<ReturnType<Pages[T]>>["renderer"];

type PageTuple = {
  [K in PageName]: [K, Pages[K]];
}[PageName];

type X = Pages[keyof Pages];

const getPageRenderer = async <T extends PageName>(pageName: T): Promise<Renderer<T>> => {
  const module = await pages[pageName]();
  return module.renderer;
};

let idCounter = 0;
const injectReactPage = async function injectReactPage<T extends PageTuple>(pageName: T[0], params: Parameters<T[1]>) {
  const current = document.currentScript;
  if (!current) {
    throw new RangeError("Unable to identify `currentScript` in `injectReactPage`");
  }

  const targetName = pageName.replace(/[^\w]/g, "_") + idCounter;
  idCounter += 1;

  const div = document.createElement("div");
  div.setAttribute("id", targetName);
  current.after(div);

  const pagePromise: T[1] = pages[pageName];
  const bar: Awaited<ReturnType<T[1]>> = await pagePromise();
  const a = bar.renderer();
  // (function (f: Awaited<ReturnType<T[1]>>) {
  //   f.renderer(targetName, params);
  // });
  // const pagePromise = getPageRenderer(pageName);

  // pagePromise.then(function (renderer) {
  //   renderer(targetName, params);
  // });
};
