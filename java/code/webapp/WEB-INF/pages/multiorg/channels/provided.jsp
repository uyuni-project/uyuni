<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<html>

<body>
<script src="/javascript/channel_tree.js?cb=${rhn:getConfig('web.buildtimestamp')}" type="text/javascript"></script>
<script type="text/javascript">
  var filtered = ${requestScope.isFiltered};
  function showFiltered() {
    if (filtered)
      showAllRows();
  }
</script>
<rhn:toolbar base="h1" icon="header-organisation">
  ${fn:escapeXml(trustorg)}
</rhn:toolbar>

<rhn:dialogmenu mindepth="0" maxdepth="3"
                    definition="/WEB-INF/nav/org_trust.xml"
                    renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />

<p>
        <bean:message key="orgtrust.jsp.channelprovide.summary" arg0="${fn:escapeXml(trustorg)}" />
</p>

<form method="post" name="rhn_list" action="/rhn/multiorg/channels/Provided.do">
  <rhn:csrf />
  <rhn:submitted />
  <%@ include file="/WEB-INF/pages/common/fragments/channel/channel_tree.jspf" %>
  <rhn:hidden name="oid" value="${param.oid}"/>
</form>

<script>
  onLoadStuff(3);
  showFiltered();
</script>

</body>
</html>
