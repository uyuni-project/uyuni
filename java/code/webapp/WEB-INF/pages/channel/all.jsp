<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>

<body>
<script src="/javascript/channel_tree.js?cb=${rhn:getConfig('web.buildtimestamp')}" type="text/javascript"></script>
<script type="text/javascript">
    var filtered = ${requestScope.isFiltered};
    function showFiltered() {
        if (filtered)
            ShowAll();
    }
</script>
<rhn:toolbar base="h1" icon="header-channel"
             creationUrl="/rhn/channels/manage/Edit.do"
             creationType="channel"
             creationAcl="user_role(channel_admin)"
             helpUrl="/docs/${rhn:getDocsLocale(pageContext)}/reference/software/software-channel-list-menu.html">
  <bean:message key="channels.all.jsp.toolbar"/>
</rhn:toolbar>

<%@ include file="/WEB-INF/pages/common/fragments/channel/channel_tabs.jspf" %>

<p>
        <bean:message key="channels.all.jsp.header1" />
</p>

<form method="post" role="form" name="rhn_list" action="/rhn/software/channels/All.do">
  <rhn:csrf />
  <rhn:submitted />
  <%@ include file="/WEB-INF/pages/common/fragments/channel/channel_tree_multiorg.jspf" %>
</form>

<script>
    onLoadStuff(5);
    showFiltered();
</script>

</body>
</html>
