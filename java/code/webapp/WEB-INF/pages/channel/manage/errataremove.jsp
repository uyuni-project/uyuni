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
      <img src="/img/rhn-icon-errata.gif" alt="erratum" /> <bean:message key="header.jsp.errata"/>
    </h2>

  <bean:message key="channel.jsp.errata.remove.message"/>

  <rl:listset name="errata_list_set">
   <rhn:csrf />
   <input type="hidden" name="cid" value="${cid}">

		  <rl:list  dataset="errata_data"
					decorator="SelectableDecorator"
					emptykey="channel.jsp.errata.listempty"
					alphabarcolumn="advisory" >

				<rl:decorator name="ElaborationDecorator"/>
				<rl:decorator name="PageSizeDecorator"/>

				<rl:selectablecolumn value="${current.selectionKey}"
					selected="${current.selected}"/>





                 <rl:column sortable="true"
                                   bound="false"
                           headerkey="erratalist.jsp.type"
                           headerclass="thin-column"
                           sortattr="advisoryType">
                        <c:if test="${current.advisoryType == 'Product Enhancement Advisory'}">
				 <img src="/img/wrh-product.gif" alt="Product Enhancement Advisory" title="Product Enhancement Advisory" />
                        </c:if>
                       <c:if test="${current.advisoryType == 'Security Advisory'}">
				 <img src="/img/wrh-security.gif" alt="Security Advisory" title="Security Advisory" />
                        </c:if>
                       <c:if test="${current.advisoryType == 'Bug Fix Advisory'}">
				  <img src="/img/wrh-bug.gif" alt="Bug Fix Advisory" title="Bug Fix Advisory" />
                        </c:if>

                </rl:column>


                 <rl:column sortable="true"
                                   bound="false"
                           headerkey="erratalist.jsp.advisory"
                           sortattr="advisory"
                           >

                        <a href="/rhn/errata/manage/Edit.do?eid=${current.id}">${current.advisoryName}</a>
                </rl:column>


                 <rl:column sortable="true"
                                   bound="false"
                                   filterattr="advisorySynopsis"
                           headerkey="erratalist.jsp.synopsis"
                           sortattr="advisorySynopsis"
                          >
                        ${current.advisorySynopsis}
                </rl:column>

               <%--
                 <rl:column sortable="true"
                                   bound="false"
                           headerkey="lastModified"
                           sortattr="lastModifiedObject"
                          >
                        ${current.lastModified}
                </rl:column>

                --%>

                 <rl:column sortable="true"
                                   bound="false"
                           headerkey="erratalist.jsp.updated"
                           sortattr="updateDateObj"
                           defaultsort="desc"
                          >
                        ${current.updateDate}
                </rl:column>


			</rl:list>

			<p align="right">
			<input type="submit" name="dispatch"  value="<bean:message key='channel.jsp.errata.remove'/>"
	            <c:choose>
	                <c:when test="${empty errata_data}">disabled</c:when>
	            </c:choose>
            >
			</p>
     <rhn:submitted/>


</rl:listset>


</body>
</html>
