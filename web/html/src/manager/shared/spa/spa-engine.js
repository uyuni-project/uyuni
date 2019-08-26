/* global onDocumentReadyInitOldJS, Loggerhead */
import App, {HtmlScreen} from "senna";
import "senna/build/senna.css"
import "./spa-engine.css"
import SpaRenderer from "core/spa/spa-renderer";

function isLoginPage (pathName) {
  const allLoginPossiblePaths = ["/", "/rhn/manager/login"];
  return allLoginPossiblePaths.some(loginPath => loginPath === pathName);
}

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.spa = window.pageRenderers.spa || {};

window.pageRenderers.spa.init = function init() {
  // We need this until the login page refactor using a different layout template is completed
  if(!isLoginPage(window.location.pathname)) {
    const appInstance = new App();
    // appInstance.setLinkSelector("a.js-spa");
    appInstance.setFormSelector("form.js-spa");
    appInstance.addSurfaces(["left-menu-data", "ssm-box", "page-body"])
    appInstance.addRoutes([{
      path: /.*/,
      handler: function (route, a, b) {
        const screen = new HtmlScreen();
        screen.setCacheable(false);
        //TODO: remove after https://github.com/liferay/senna.js/pull/311/files
        screen.setHttpHeaders({
          ...screen.getHttpHeaders(),
          ...{"Content-type": "application/x-www-form-urlencoded"}
        });
        screen.getFormData = function(form, submitedButton) {
          let body = $(form).serialize();
          if (submitedButton && submitedButton.name) {
            body += '&' + encodeURI(submitedButton.name) + '=' + encodeURI(submitedButton.value)
          }
          return body;
        }
        screen.deactivate = function() {
          SpaRenderer.cleanOldReactTrees();
        }
        return screen;
      }
    }]);
    window.pageRenderers.spa.appInstance = appInstance;

    appInstance.on('beforeNavigate', function(navigation) {
      // Integration with bootstrap 3. We need to make sure all the existing modals get fully removed
      $('.modal').remove();
      $('.modal-backdrop').remove();
      $('body').removeClass( "modal-open" );
    })

    appInstance.on('endNavigate', function(navigation) {
      // workaround to redirect to the login page when there is no session:
      // More info: https://github.com/liferay/senna.js/issues/302
      const urlPath = appInstance.browserPathBeforeNavigate;
      let urlParser = document.createElement('a');
      urlParser.href = urlPath;
      if(isLoginPage(urlParser.pathname)) {
        document.getElementsByClassName("spacewalk-main-column-layout")[0].innerHTML = `
        <div class="container-fluid">
            <div class="alert alert-danger">
                No session. Redirecting to login page...
            </div>
        </div>
      `;
        window.location = urlPath;
      }

      // If an error happens we make a full refresh to make sure the original request is shown instead of a SPA replacement
      if (
        navigation.error && (navigation.error.invalidStatus || navigation.error.timeout || navigation.error.requestError)
      ) {
        window.location = navigation.path;
      }

      Loggerhead.info('[' + new Date().toUTCString() + '] - Loading `' + window.location + '`');
      SpaRenderer.onSpaEndNavigation();
      onDocumentReadyInitOldJS();
    });

    return appInstance;
  }
}

window.pageRenderers.spa.navigate = function navigate(url) {
  if(window.pageRenderers.spa.appInstance) {
    window.pageRenderers.spa.appInstance.navigate(url);
  } else {
    window.location = url;
  }
}
