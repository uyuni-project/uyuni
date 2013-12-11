<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>


<html>

<body>
<rhn:toolbar base="h1" icon="header-system-groups"
               helpUrl="/rhn/help/reference/en-US/s1-sm-monitor.jsp#s2-sm-monitor-psuites">
    <bean:message key="probesuitesremoveconfirm.jsp.header1"/>
</rhn:toolbar>

    <h2>
      <bean:message key="probesuitesremoveconfirm.jsp.header2" />
    </h2>

<div>
  <p>
    <bean:message key="probesuitesremoveconfirm.jsp.summary"/>

    <form method="POST" name="rhn_list" action="/rhn/monitoring/config/ProbeSuitesRemoveConfirmSubmit.do">
       <rhn:csrf />
       <rhn:list pageList="${requestScope.pageList}"
            noDataText="probesuitesremoveconfirm.jsp.nothingselected">
       <rhn:listdisplay button="probesuitesremoveconfirm.jsp.deleteprobesuites">
          <rhn:column header="probesuites.jsp.name">
            <a href="ProbeSuiteEdit.do?suite_id=${current.suite_id}">${current.suite_name}</A>
          </rhn:column>
      </rhn:listdisplay>
      </rhn:list>
    </form>


  </p>
</div>

</body>
</html>
