import App, {HtmlScreen} from "senna";
import "senna/build/senna.css"
import "./spa-engine.css"
import SpaRenderer from "core/spa/spa-renderer";

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.spa = window.pageRenderers.spa || {};

window.pageRenderers.spa.init = function init() {
  const appInstance = new App();
  appInstance.setLinkSelector("a.js-spa");
  appInstance.setFormSelector("");
  appInstance.addSurfaces('left-menu-data');
  appInstance.addSurfaces('ssm-box');
  appInstance.addSurfaces('page-body');
  appInstance.addRoutes([{
    path: /.*/,
    handler: function (route, a, b) {
      const screen = new HtmlScreen();
      screen.setCacheable(false);
      return screen;
    }
  }]);

  appInstance.on('endNavigate', function() {
    SpaRenderer.onSpaEndNavigation();
  });

  return appInstance;
}
