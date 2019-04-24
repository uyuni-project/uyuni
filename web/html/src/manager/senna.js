// import App, {HtmlScreen} from "senna";

// KEEP STATE OF PRE-FETCH AND CACHED PAGES

var appInstance = new senna.App();
appInstance.setLinkSelector("a.js-spa");
appInstance.setFormSelector("");
appInstance.addSurfaces('page-body');
// app.addSurfaces('breadcrumb');
appInstance.addRoutes([{
    path: /.*/,
    handler: function (route, a, b) {
        console.log('Middleware route handler for ' + route.getPath());
        const screen = new senna.HtmlScreen();
        screen.setCacheable(false);
        return screen;
    }
}]);
