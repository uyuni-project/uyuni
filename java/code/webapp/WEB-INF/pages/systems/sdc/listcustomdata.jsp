<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html:html >
  <body>
    <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>

    <rhn:toolbar base="h2" icon="header-info"
      creationUrl="/rhn/systems/details/CreateCustomData.do?sid=${system.id}"
      creationType="customdata">
      <bean:message key="sdc.details.customdata.header"/>
    </rhn:toolbar>

    <div class="page-summary">
      <p><bean:message key="sdc.details.customdata.summary"/></p>
    </div>

    <rl:listset name="keySet">
      <rhn:csrf />
      <rhn:submitted />

      <rl:list dataset="pageList"
          name="keyList"
          emptykey="sdc.details.customdata.nosystems"
          alphabarcolumn="label">

        <rl:column sortable="true"
            bound="false"
            headerkey="system.jsp.customkey.keylabel"
            sortattr="label"
            defaultsort="asc">
          <a href="/rhn/systems/customdata/UpdateCustomKey.do?cikid=${current.cikid}">
            <c:out value="${current.label}" />
          </a>
        </rl:column>

        <rl:column sortable="false"
            bound="false"
            headerkey="system.jsp.customkey.description">
          <c:out value="${current.description}" />
        </rl:column>

        <rl:column sortable="false"
            bound="false"
            headerkey="system.jsp.customkey.value">
          <a href="/rhn/systems/details/UpdateCustomData.do?sid=${system.id}&cikid=${current.cikid}">
            <c:out value="${current.value}" />
          </a>
        </rl:column>
      </rl:list>

      <rl:csv dataset="pageList"
          name="keyList"
          exportColumns="label, description, value" />
    </rl:listset>
  </body>
</html:html>
