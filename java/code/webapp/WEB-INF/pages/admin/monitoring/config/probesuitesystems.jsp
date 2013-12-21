<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>


<html>
<body>
<rhn:toolbar base="h1" icon="header-system-groups"
  	           creationUrl="ProbeSuiteSystemsEdit.do?suite_id=${probeSuite.id}"
               creationType="probesuitesystem"
	           helpUrl="/rhn/help/reference/en-US/s1-sm-monitor.jsp#s2-sm-monitor-psuites">
    <bean:message key="probesuitesystems.jsp.header1" arg0="${probeSuite.suiteName}" />
  </rhn:toolbar>


<rhn:dialogmenu mindepth="0" maxdepth="1" definition="/WEB-INF/nav/probesuite_detail_edit.xml"
    renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />

<h2><bean:message key="probesuitesystems.jsp.header2"/></h2>

<div>
  <p>
    <bean:message key="probesuitesystems.jsp.summary"/>

    <form method="POST" name="rhn_list" action="/rhn/monitoring/config/ProbeSuiteSystemsSubmit.do">
    <rhn:csrf />
    <rhn:list pageList="${requestScope.pageList}" noDataText="probesuitesystems.jsp.nosystems"
        legend="probes-list">
      <rhn:listdisplay   set="${requestScope.set}" exportColumns="id,name,status"
        hiddenvars="${requestScope.newset}">
        <rhn:set value="${current.id}" />
        <rhn:column header="probesuitesystems.jsp.state">
            <c:if test="${current.status == 'UNKNOWN'}">
              <rhn:icon type="monitoring-unknown" title="monitoring.status.unknown" />
            </c:if>
            <c:if test="${current.status == 'OK'}">
              <rhn:icon type="monitoring-ok" title="monitoring.status.ok" />
            </c:if>
            <c:if test="${current.status == 'WARNING'}">
              <rhn:icon type="monitoring-warn" title="monitoring.status.warn" />
            </c:if>
            <c:if test="${current.status == 'PENDING'}">
              <rhn:icon type="monitoring-pending" title="monitoring.status.pending" />
            </c:if>
            <c:if test="${current.status == 'CRITICAL'}">
              <rhn:icon type="monitoring-crit" title="monitoring.status.critical" />
            </c:if>
        </rhn:column>
        <rhn:column header="probesuitesystems.jsp.system">
            <a href="/rhn/systems/details/probes/ProbesList.do?sid=${current.id}">${current.name}</a>
        </rhn:column>
      </rhn:listdisplay>
    </rhn:list>
    <html:hidden property="suite_id" value="${probeSuite.id}"/>
    <c:if test="${not empty pageList}">
    <div class="text-right">
        <hr>
      <html:submit property="dispatch">
        <bean:message key="probesuitesystems.jsp.detachsystem"/>
      </html:submit>
      <html:submit property="dispatch">
        <bean:message key="probesuitesystems.jsp.removesystem"/>
      </html:submit>
    </div>
    </c:if>
    </form>
  </p>
</div>


</body>
</html>

