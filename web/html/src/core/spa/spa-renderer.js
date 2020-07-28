//@flow
// globals onDocumentReadyInitOldJS
import type {Element as ReactElement} from 'react';
import React from 'react';
import ReactDOM from 'react-dom';

declare var onDocumentReadyInitOldJS: Function;

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.spa = window.pageRenderers.spa || {};

// React trees that are always available on the page: Menu, Breadcrumbs, etc
window.pageRenderers.spa.globalRenderersToUpdate = window.pageRenderers.spa.globalRenderersToUpdate || [];
// Name of all the react apps in the current route
window.pageRenderers.spa.reactAppsName  = window.pageRenderers.spa.reactAppsName || [];
// Renderers of all the react apps in the current route
window.pageRenderers.spa.reactRenderers = window.pageRenderers.spa.reactRenderers || [];
// Previous renderers of all the react apps in the current route
window.pageRenderers.spa.previousReactRenderers = window.pageRenderers.spa.previousReactRenderers || [];

function addReactApp(appName: string) {
  window.pageRenderers.spa.reactAppsName.push(appName);
}

function hasReactApp() {
  return window.pageRenderers.spa.reactAppsName.length > 0;
}

function renderGlobalReact(element: ReactElement<any>, container: ?Element) {
  if (container == null) {
    throw new Error('The DOM element is not present.');
  }

  function registerGlobalRender(instance) {
    window.pageRenderers.spa.globalRenderersToUpdate.push(instance);
  }
  const elementWithRef = React.cloneElement(element, {ref: registerGlobalRender});
  ReactDOM.render(elementWithRef, container);
}

function renderNavigationReact(element: ReactElement<any>, container: ?Element) {
  if (container == null) {
    throw new Error('The DOM element is not present.');
  }

  window.pageRenderers.spa.reactRenderers.push({
    element,
    container,
    clean: () => {
      ReactDOM.unmountComponentAtNode(container)
    }
  });
  ReactDOM.render(element, container, () => {
    onDocumentReadyInitOldJS();
  });
}

function beforeNavigation() {
  window.pageRenderers.spa.previousReactRenderers = window.pageRenderers.spa.reactRenderers;
  window.pageRenderers.spa.reactAppsName = [];
  window.pageRenderers.spa.reactRenderers = [];
}

function afterNavigationTransition() {
  window.pageRenderers.spa.previousReactRenderers.forEach(
    navigationRenderer => {
      try {
        navigationRenderer.clean()
      } catch (error) {
        console.error(error);
      }
    }
  );
  window.pageRenderers.spa.previousReactRenderers = [];
}

function onSpaEndNavigation() {
  window.pageRenderers.spa.globalRenderersToUpdate.forEach(
    comp => comp.onSPAEndNavigation && comp.onSPAEndNavigation()
  );
}

export default {
  addReactApp,
  hasReactApp,
  renderGlobalReact,
  renderNavigationReact,
  beforeNavigation,
  afterNavigationTransition,
  onSpaEndNavigation
}
