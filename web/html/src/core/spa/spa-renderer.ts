// This binds the global translation logic
import "core/intl";

import ReactDOM from "react-dom";

import { DEPRECATED_unsafeEquals } from "utils/legacy";

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.spa = window.pageRenderers.spa || {};

// React trees that are always available on the page: Menu, Breadcrumbs, etc
window.pageRenderers.spa.globalRenderersToUpdate = window.pageRenderers.spa.globalRenderersToUpdate || [];
// Name of all the react apps in the current route
window.pageRenderers.spa.reactAppsName = window.pageRenderers.spa.reactAppsName || [];
// Renderers of all the react apps in the current route
window.pageRenderers.spa.reactRenderers = window.pageRenderers.spa.reactRenderers || [];
// Previous renderers of all the react apps in the current route
window.pageRenderers.spa.previousReactRenderers = window.pageRenderers.spa.previousReactRenderers || [];

type GlobalRenderer = {
  onSPAEndNavigation?: () => void;
};

function addReactApp(appName: string) {
  window.pageRenderers?.spa?.reactAppsName?.push(appName);
}

function hasReactApp() {
  return (window.pageRenderers?.spa?.reactAppsName?.length || 0) > 0;
}

function renderGlobalReact(element: JSX.Element, container: Element | null | undefined) {
  if (DEPRECATED_unsafeEquals(container, null)) {
    throw new Error("The DOM element is not present.");
  }

  // React 16 returns the component instance for class roots and null for function roots.
  // The generic JSX.Element type selects the void overload, so preserve the runtime contract explicitly.
  const instance = ReactDOM.render(element, container) as unknown as GlobalRenderer | null;
  if (instance) {
    window.pageRenderers?.spa?.globalRenderersToUpdate?.push(instance);
  }
}

function renderNavigationReact(element: JSX.Element, container: Element | null | undefined) {
  if (DEPRECATED_unsafeEquals(container, null)) {
    throw new Error("The DOM element is not present.");
  }

  window.pageRenderers?.spa?.reactRenderers?.push({
    element,
    container,
    clean: () => {
      ReactDOM.unmountComponentAtNode(container);
    },
  });
  ReactDOM.render(element, container, () => {
    onDocumentReadyInitOldJS();
  });
}

function beforeNavigation() {
  if (window.pageRenderers?.spa) {
    window.pageRenderers.spa.previousReactRenderers = window.pageRenderers.spa.reactRenderers;
    window.pageRenderers.spa.reactAppsName = [];
    window.pageRenderers.spa.reactRenderers = [];
  }
}

function afterNavigationTransition() {
  window.pageRenderers?.spa?.previousReactRenderers?.forEach((navigationRenderer) => {
    try {
      (navigationRenderer as any).clean();
    } catch (error) {
      Loggerhead.error(error);
    }
  });
  if (window.pageRenderers?.spa) {
    window.pageRenderers.spa.previousReactRenderers = [];
  }
}

function onSpaEndNavigation() {
  window.pageRenderers?.spa?.globalRenderersToUpdate?.forEach((comp) => comp.onSPAEndNavigation?.());
}

export default {
  addReactApp,
  hasReactApp,
  renderGlobalReact,
  renderNavigationReact,
  beforeNavigation,
  afterNavigationTransition,
  onSpaEndNavigation,
};
