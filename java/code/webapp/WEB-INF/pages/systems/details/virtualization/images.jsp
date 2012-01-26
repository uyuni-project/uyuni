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
</head>

<body>
  <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>
  <br/>

	<rhn:toolbar base="h1" img="/img/susestudio.png"></rhn:toolbar>

	<div class="page-summary">
      <p>Please choose one of the available SUSE Studio images below for deployment to this virtual host.</p>
	</div>

  <div style="clear: both;">
    <div id="images-content" class="full-width-wrapper">
      <div style="text-align:center"><img src="/img/please-wait.gif" alt="Please Wait" /></div>
      <script type="text/javascript">
        ImagesRenderer.renderAsync(makeAjaxCallback("images-content", false));
      </script>
    </div>
  </div>

</body>
</html>
