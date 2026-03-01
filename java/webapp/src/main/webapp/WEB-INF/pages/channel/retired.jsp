<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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
<rhn:toolbar base="h1" icon="header-channel" imgAlt="channels.overview.toolbar.imgAlt"
             helpUrl="/docs/${rhn:getDocsLocale(pageContext)}/reference/software/software-channel-list-menu.html"
             creationUrl="/rhn/channels/manage/Edit.do"
             creationType="channel"
             creationAcl="authorized_for(software.manage.details)">
  <bean:message key="channels.retired.jsp.toolbar"/>
</rhn:toolbar>

<%@ include file="/WEB-INF/pages/common/fragments/channel/channel_tabs.jspf" %>

<p>
        <bean:message key="channels.retired.jsp.header1" />
</p>



<form method="post" name="rhn_list" action="/rhn/software/channels/Retired.do">
    <rhn:csrf />
    <rhn:submitted />
        <%@ include file="/WEB-INF/pages/common/fragments/channel/channel_tree_multiorg.jspf" %>
</form>

<script>
    onLoadStuff(3);
    showFiltered();
</script>

</body>
</html>
