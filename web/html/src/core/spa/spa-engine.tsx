import "senna/build/senna.css";
import "./spa-engine.css";

import * as React from "react";

import App, { HtmlScreen } from "senna";

import SpaRenderer from "core/spa/spa-renderer";

import { showErrorToastr } from "components/toastr";

import { onEndNavigate } from "./theme-loader";

function isLoginPage(pathName) {
  const allLoginPossiblePaths = ["/", "/rhn/manager/login"];
  return allLoginPossiblePaths.some((loginPath) => loginPath === pathName);
}

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.spaengine = window.pageRenderers.spaengine || {};

// Navigation hook for standalone renderers to detect navigation
const onSpaEndNavigationCallbacks: Function[] = [onEndNavigate];
window.pageRenderers.spaengine.onSpaEndNavigation = function onSpaEndNavigation(callback: Function) {
  if (onSpaEndNavigationCallbacks.indexOf(callback) === -1) {
    onSpaEndNavigationCallbacks.push(callback);
  }
};

window.pageRenderers.spaengine.init = function init(timeout?: number) {
  if (typeof timeout !== "number") {
    throw new TypeError(`Invalid SPA engine timeout configuration, expected number, got ${typeof timeout}`);
  }
  // We need this until the login page refactor using a different layout template is completed
  if (!isLoginPage(window.location.pathname)) {
    const appInstance = new App();
    // appInstance.setLinkSelector("a.js-spa");
    appInstance.setFormSelector("form.js-spa");

    appInstance.addSurfaces(["left-menu-data", "ssm-box", "page-body"]);
    appInstance.addRoutes([
      {
        path: /.*/,
        handler: function (route, a, b) {
          const screen = new HtmlScreen();

          screen.setTimeout(timeout * 1000);
          screen.setCacheable(false);
          //workaround for posts until https://github.com/liferay/senna.js/pull/311/files is merged
          screen.setHttpHeaders({
            ...screen.getHttpHeaders(),
            ...{ "Content-type": "application/x-www-form-urlencoded" },
          });
          screen.getFormData = function (form, submitedButton) {
            let body = jQuery(form).serialize();
            if (submitedButton && submitedButton.name) {
              body += "&" + encodeURI(submitedButton.name) + "=" + encodeURI(submitedButton.value);
            }
            return body;
          };
          screen.beforeActivate = function () {
            // Preparing already for the new DelayedAsyncTransitionSurface(page-body) surface
            SpaRenderer.beforeNavigation();
            SpaRenderer.afterNavigationTransition();
          };

          return screen;
        },
      },
    ]);
    if (window.pageRenderers?.spaengine) {
      window.pageRenderers.spaengine.appInstance = appInstance;
    }

    appInstance.on("beforeNavigate", function (navigation) {
      // Integration with bootstrap 3. We need to make sure all the existing modals get fully removed
      // but we have to do it after the navigation ends unless all form inputs contained in the modal will be dropped
      // before the form serialization happens and they will not submitted because they will not exist anymore
      jQuery(".modal").addClass("removeWhenNavigationEnds");
      jQuery(".modal-backdrop").addClass("removeWhenNavigationEnds");
      jQuery("body").removeClass("modal-open");

      let urlParser = document.createElement("a");
      urlParser.href = navigation.path;
      if (isLoginPage(urlParser.pathname)) {
        window.location = navigation.path;
      }
    });

    appInstance.on("endNavigate", function (navigation) {
      // Drop everything that was marked to be removed
      jQuery(".modal.removeWhenNavigationEnds").remove();
      jQuery(".modal-backdrop.removeWhenNavigationEnds").remove();

      // If an error happens we make a full refresh to make sure the original request is shown instead of a SPA replacement
      if (navigation.error) {
        if (navigation.error.statusCode === 401 || navigation.error.invalidStatus || navigation.error.requestError) {
          window.location = navigation.path;
        } else if (navigation.error.timeout) {
          // Stop loading bar
          jQuery(document.documentElement).removeClass("senna-loading");
          // Inform user that page must be reloaded
          const message = (
            <>
              Request has timed out, please
              <button className="btn-link" onClick={() => (window.location = navigation.path)}>
                reload the page
              </button>
            </>
          );
          showErrorToastr(message, { autoHide: false, containerId: "global" });
        }
      }

      Loggerhead.info("Loading `" + window.location + "`");
      SpaRenderer.onSpaEndNavigation();
      onDocumentReadyInitOldJS();
      onSpaEndNavigationCallbacks.forEach((callback) => callback());
    });

    return appInstance;
  }
};

window.pageRenderers.spaengine.navigate = function navigate(url: string) {
  if (window.pageRenderers?.spaengine?.appInstance) {
    window.pageRenderers.spaengine.appInstance.navigate(url);
  } else {
    window.location.href = url;
  }
};
