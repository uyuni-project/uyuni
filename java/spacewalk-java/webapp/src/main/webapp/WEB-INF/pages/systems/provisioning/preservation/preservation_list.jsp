<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html:html >
<body>
<rhn:toolbar base="h1" icon="header-kickstart"
    imgAlt="system.common.kickstartAlt"
    creationUrl="PreservationListCreate.do"
    creationType="filelist"
    helpUrl="/docs/${rhn:getDocsLocale(pageContext)}/reference/systems/autoinst-file-preservation.html">
  <bean:message key="preservation_list.jsp.toolbar"/>
</rhn:toolbar>

<div>
    <bean:message key="preservation_list.jsp.summary"
                  arg0="/docs/${rhn:getDocsLocale(pageContext)}/reference/systems/autoinst-file-preservation.html"/>
</div>
    <form method="post" name="rhn_list" action="PreservationListDeleteSubmit.do">
      <rhn:csrf />
      <rhn:list pageList="${requestScope.pageList}" noDataText="preservation_list.jsp.nokeys">

      <%@ include file="/WEB-INF/pages/common/fragments/systems/file_listdisplay.jspf" %>

      </rhn:list>
    </form>

</body>
</html:html>

