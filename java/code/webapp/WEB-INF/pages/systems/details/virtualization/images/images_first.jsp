<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>

<html:xhtml/>
<html>

<head>
  <script type="text/javascript" src="/rhn/dwr/interface/ImagesRenderer.js"></script>
  <script type="text/javascript" src="/rhn/dwr/engine.js"></script>
  <script type="text/javascript" src="/javascript/scriptaculous.js"></script>
  <script type="text/javascript" src="/javascript/render.js"></script>
  <script type="text/javascript" src="/javascript/images.js"></script>
</head>

<body>
  <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>

  <div id="images-content">
    <rhn:toolbar base="h1" img="/img/spinner.gif"></rhn:toolbar>
    <script type="text/javascript">
      ImagesRenderer.renderAsync(makeAjaxCallback("images-content", false));
    </script>
  </div>
</body>
</html>
