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
  ...Appstreams,
  ...Audit,
  ...ContentManagement,
  ...Errors,
  ...Groups,
  ...Header,
  ...Highstate,
  ...Images,
  ...Login,
  ...MaintenanceWindows,
  ...Minion,
  ...Notifications,
  ...Organizations,
  ...Packages,
  ...Proxy,
  ...RecurringActions,
  ...Salt,
  ...ScheduleOptions,
  ...Shared,
  ...Systems,
  ...Storybook,
};

type PageName = keyof typeof pages;
type Page<T extends PageName> = typeof pages[T];
type PageRendererParams<T extends PageName> = Parameters<Awaited<ReturnType<Page<T>>>["renderer"]>;

// Drop first item of tuple type, e.g. [foo, bar, tea] -> [bar, tea]
type Tail<T extends any[]> = T extends [any, ...infer Rest] ? Rest : never;

const injectReactPage = async <T extends PageName>(pageName: T, ...params: Tail<PageRendererParams<T>>) => {
  if (!pages[pageName]) {
    throw new RangeError(
      `Found no page with name "${pageName}", did you add the renderer to \`pages\` in \`web/html/src/manager/index.ts\`?`
    );
  }
  SpaRenderer.addReactApp(pageName);

  const current = document.currentScript;
  if (!current) {
    throw new RangeError("Unable to identify `currentScript` in `injectReactPage`");
  }

  const injectTarget = current.parentElement;
  if (!injectTarget) {
    throw new RangeError("Unable to find `injectTarget` in `injectReactPage`");
  }

  // For debug purposes only
  injectTarget.setAttribute("data-page-name", pageName);

  const page = await pages[pageName]();

  /**
   * Typescript doesn't currently support partial type inference for generics, so we cannot infer both of the correct types here
   * See https://github.com/Microsoft/TypeScript/pull/23696
   */
  // @ts-expect-error
  page.renderer(injectTarget, ...(params ?? {}));
};

declare global {
  interface Window {
    injectReactPage: typeof injectReactPage;
  }
}

window.injectReactPage = injectReactPage;
