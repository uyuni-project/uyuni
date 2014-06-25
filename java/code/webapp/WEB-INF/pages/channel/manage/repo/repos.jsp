<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>

<html>
<body>
<rhn:toolbar base="h1" icon="header-info"
               creationUrl="RepoCreate.do"
               creationType="repos"
               imgAlt="info.alt.img">
  <bean:message key="repos.jsp.header"/>
</rhn:toolbar>
<div class="page-summary">
<p><bean:message key="repos.jsp.summary"/></p>
<c:if test="${not empty requestScope['default']}">
	<rhn:note key = "repos.jsp.note.default"/>
</c:if>
</div>


 <rl:listset name="keySet">
  <rhn:csrf />
  <rhn:submitted />
  <rl:list dataset="pageList"
         width="100%"
         name="keysList"
         styleclass="list"
         emptykey="repos.jsp.norepos"
         alphabarcolumn="label">

        <rl:decorator name="PageSizeDecorator"/>

        <!-- Description name column -->
        <rl:column bound="false"
                   sortable="true"
                   headerkey="repos.jsp.label"
                   sortattr= "label"
                   filterattr="label">
			<c:out value="<a href=\"/rhn/channels/manage/repos/RepoEdit.do?id=${current.id}\">${current.label}</a>" escapeXml="false" />
        </rl:column>
        <rl:column bound="false"
	           sortable="false"
	           headerkey="repo.jsp.channels"
	           attr="channels">
		  <a href="/rhn/channels/manage/repos/AssociatedChannels.do?id=${current.id}"><c:out value="${current.channels}" /></a>
	    </rl:column>
      </rl:list>
     </rl:listset>

</body>
</html>
