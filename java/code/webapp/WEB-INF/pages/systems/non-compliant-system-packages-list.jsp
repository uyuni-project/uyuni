<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<html:xhtml/>
<html>

<body>

<div class="toolbar-h1">
	<c:choose>
		<c:when test="${system.virtualGuest}">
			<img src="/img/virt-guest.png" alt="system" />
		</c:when>
		<c:when test="${system.virtualHost}">
			<img src="/img/virt-host.png" alt="system" />
		</c:when>
		<c:otherwise>
			<img src="/img/rhn-icon-system.gif" alt="system" />
		</c:otherwise>
    </c:choose>

	<c:if test="${empty system}">
		<decorator:getProperty property="meta.name" />
	</c:if>
	<c:if test="${not empty system}">
		${fn:escapeXml(system.name)}
	</c:if>

	<a href="/rhn/help/reference/en-US/s1-sm-systems.jsp#s3-sm-system-details" target="_new" class="help-title">
		<img src="/img/rhn-icon-help.gif" alt="<bean:message key="toolbar.jsp.helpicon.alt"/>" />
	</a>
</div>

<h2>
	<img src="/img/rhn-icon-packages-foreign.gif" alt="<bean:message key='errata.common.deletepackageAlt' />" />
	<bean:message key="packagelist.jsp.foreignpackages" />
</h2>
<div class="page-summary">
	<p><bean:message key="packagelist.jsp.foreignpackagessummary" /></p>
</div>

<c:set var="pageList" value="${requestScope.all}" />

<rl:listset name="packageListSet">
    <rhn:csrf />
    <rhn:submitted />

	<rl:list dataset="pageList"
	         width="100%"
             styleclass="list"
    	     name="packageList"
        	 emptykey="packagelist.jsp.nopackages"
         	 alphabarcolumn="nvre">
 			 
 	    <rl:decorator name="PageSizeDecorator"/>

        <rl:column headerkey="packagelist.jsp.packagename" bound="false" sortattr="nvre" sortable="true" filterattr="nvre">${current.nvre}</rl:column>
        <rl:column headerkey="packagelist.jsp.packagearch" bound="false">
          <c:choose>
            <c:when test ="${not empty current.arch}">${current.arch}</c:when>
            <c:otherwise><bean:message key="packagelist.jsp.notspecified"/></c:otherwise>
          </c:choose>
        </rl:column>
        <rl:column headerkey="packagelist.jsp.installtime" bound="false" styleclass="last-column" sortattr="installTimeObj" sortable="true">
          <c:choose>
            <c:when test ="${not empty current.installtime}">${current.installtime}</c:when>
            <c:otherwise><bean:message key="packagelist.jsp.notspecified"/></c:otherwise>
          </c:choose>
        </rl:column>
    </rl:list>

</rl:listset>
</body>
</html>
