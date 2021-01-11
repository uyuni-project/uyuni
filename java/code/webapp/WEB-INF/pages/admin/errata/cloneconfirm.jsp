<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<html:html >
<body>
<h2><bean:message key="erratalist.jsp.cloneerrata" /></h2>

<div class="page-summary">
    <p><bean:message key="cloneconfirm.jsp.pagesummary" /></p>
</div>

<form method="post" name="rhn_list" action="/rhn/errata/manage/CloneConfirmSubmit.do">
<rhn:csrf />
<rhn:list pageList="${requestScope.errataList}" noDataText="erratalist.jsp.noerrata">
  <rhn:listdisplay>
    <rhn:column header="erratalist.jsp.type">
    <c:if test="${current.securityAdvisory}">
    <c:choose>
    <c:when test="${current.severityid=='0'}">
        <rhn:icon type="errata-security-critical"
                  title="erratalist.jsp.securityadvisory"/>
    </c:when>
    <c:when test="${current.severityid=='1'}">
        <rhn:icon type="errata-security-important"
                  title="erratalist.jsp.securityadvisory"/>
    </c:when>
    <c:when test="${current.severityid=='2'}">
        <rhn:icon type="errata-security-moderate"
                  title="erratalist.jsp.securityadvisory"/>
    </c:when>
    <c:when test="${current.severityid=='3'}">
        <rhn:icon type="errata-security-low"
                  title="erratalist.jsp.securityadvisory"/>
    </c:when>
    <c:otherwise>
        <rhn:icon type="errata-security"
                  title="erratalist.jsp.securityadvisory"/>
    </c:otherwise>
    </c:choose>
    </c:if>
        <c:if test="${current.bugFix}">
            <rhn:icon type="errata-bugfix" />
        </c:if>
        <c:if test="${current.productEnhancement}">
            <rhn:icon type="errata-enhance" />
        </c:if>
        <c:if test="${current.rebootSuggested}">
            <rhn:icon type="errata-reboot" title="errata-legend.jsp.reboot" />
        </c:if>
        <c:if test="${current.restartSuggested}">
            <rhn:icon type="errata-restart" title="errata.jsp.restart-tooltip" />
        </c:if>
    </rhn:column>
    <rhn:column header="erratalist.jsp.advisory">
      <a href="/rhn/errata/details/Details.do?eid=${current.id}">${current.advisoryName}</a>
    </rhn:column>
    <rhn:column header="erratalist.jsp.synopsis">
      ${current.advisorySynopsis}
    </rhn:column>
  </rhn:listdisplay>
</rhn:list>

<rhn:list pageList="${requestScope.pageList}" noDataText="errata.publish.nochannels">
  <rhn:listdisplay set="${requestScope.set}" hiddenvars="${requestScope.newset}">
    <rhn:set value="${current.id}" />
    <rhn:column header="errata.publish.channelname" url="/rhn/channels/ChannelDetail.do?cid=${current.id}">
        <c:out value="${current.name}"/>
    </rhn:column>

    <rhn:column header="errata.publish.relevantpackages">
        <c:if test="${current.relevantPackages > 0}">
            <a href="/rhn/errata/manage/ErrataChannelIntersection.do?cid=<c:out value="${current.id}"/>&eid=<c:out value="${param.eid}"/>">
        </c:if>
        <c:out value="${current.relevantPackages}"/>
        <c:if test="${current.relevantPackages > 0}">
            </a>
        </c:if>
    </rhn:column>
  </rhn:listdisplay>
</rhn:list>
<hr />
<rhn:hidden name="returnvisit" value="${param.returnvisit}"/>

<div class="text-right">
  <html:submit styleClass="btn btn-primary" property="dispatch">
    <bean:message key="deleteconfirm.jsp.confirm"/>
  </html:submit>
</div>

</body>
</html:html>
