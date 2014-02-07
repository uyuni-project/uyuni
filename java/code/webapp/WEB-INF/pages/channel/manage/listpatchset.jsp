<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>


<html>
<head>
</head>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/channel/manage/manage_channel_header.jspf" %>




    <div class="page-summary">

      <p><bean:message key="channel.manage.patchset.desc"/></p>
    </div>

    <h2>
      <rhn:icon type="header-errata-set" />
      <bean:message key="channel.manage.patchset.title"/>

    </h2>


 <rl:listset name="patchListSet">
 <rhn:csrf />
 <input type="hidden" name="cid" value="${cid}" />

<rl:list dataset="pageList"
         width="100%"
         name="patchsetlist"
         styleclass="list"
         emptykey="channel.manage.patchset.none"
         decorator="SelectableDecorator">
	 <rl:selectablecolumn value="${current.selectionKey}"
						selected="${current.selected}"/>

	<rl:column bound="false"
	           sortable="false"
	           headerkey="channel.manage.patchset.clustername"
	           attr="name">
		<a href="/rhn/software/packages/Details.do?pid=${current.id}"><c:out value="${current.nvrea}" /></a>
	</rl:column>

	<rl:column bound="false"
	           sortable="false"
	           headerkey="channel.manage.patchset.releasedate"
	           attr="name">
		<c:out value="${current.setDate}" />
	</rl:column>



      </rl:list>
      <p align="right">
			<input type="submit" name="dispatch"  value="<bean:message key='channel.manage.patchset.delete'/>" >
	    </p>
<rhn:submitted/>

 </rl:listset>


</body>
</html>



