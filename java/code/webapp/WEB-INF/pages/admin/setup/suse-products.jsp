<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
  <head>
    <script type="text/javascript" src="/rhn/dwr/interface/ProductsRenderer.js"></script>
    <script type="text/javascript" src="/rhn/dwr/engine.js"></script>
    <script type="text/javascript" src="/javascript/render.js"></script>
  </head>
  <body>
    <rhn:toolbar base="h1" icon="header-preferences">
      Setup Wizard
    </rhn:toolbar>
    <rhn:dialogmenu mindepth="0" maxdepth="1" definition="/WEB-INF/nav/setup_wizard.xml"
                    renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />
    <p>
      Select SUSE Products below to trigger the synchronization of software channels.
    </p>
    <div id="products-content">
      <div style="padding: 1em;">
        <img src="/img/spinner.gif" style="vertical-align: middle;" />
        <span style="padding-left: 0.3em;">Loading ...</span>
      </div>
      <script type="text/javascript">
        ProductsRenderer.renderAsync(makeAjaxCallback("products-content", false));
      </script>
    </div>
  </body>
</html>
