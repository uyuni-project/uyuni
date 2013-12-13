<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>


<html>
<head>
    <meta name="page-decorator" content="none" />
</head>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/channel/manage/manage_channel_header.jspf" %>
     <h2>
      <rhn:icon type="header-errata" title="erratum" /> <bean:message key="header.jsp.errata"/>
    </h2>

  <bean:message key="channel.jsp.errata.remove.confirmmessage"/>

  <rl:listset name="errata_list_set">
          <rhn:csrf />

		  <rl:list
					emptykey="channel.jsp.errata.listempty"
					alphabarcolumn="advisory" >

				<rl:decorator name="ElaborationDecorator"/>
				<rl:decorator name="PageSizeDecorator"/>


                 <rl:column sortable="true"
                                   bound="false"
                           headerkey="erratalist.jsp.advisory"
                           sortattr="advisory">

                        <a href="/rhn/errata/manage/Edit.do?eid=${current.id}">${current.advisory}</a>
                </rl:column>


                 <rl:column sortable="false"
                                   bound="false"
                                   filterattr="advisorySynopsis"
                           headerkey="erratalist.jsp.synopsis" >
                        ${current.advisorySynopsis}
                </rl:column>


                <%--
                 <rl:column sortable="true"
                                   bound="false"
                           headerkey="lastModified"
                           sortattr="lastModifiedObject">
                        ${current.lastModified}
                </rl:column>
                --%>

                 <rl:column sortable="true"
                                   bound="false"
                           headerkey="erratalist.jsp.updated"
                           sortattr="updateDateObj"
                           defaultsort="desc" >
                        ${current.updateDate}
                </rl:column>


			</rl:list>

			<p align="right">
			<input type="submit" name="dispatch"  value="<bean:message key='channel.jsp.errata.confirmremove'/>">
			</p>
     <rhn:submitted/>
     <input type="hidden" name="cid" value="${cid}">

</rl:listset>


</body>
</html>
