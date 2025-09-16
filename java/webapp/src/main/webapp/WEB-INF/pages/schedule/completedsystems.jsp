<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
<body>

<%@ include file="/WEB-INF/pages/common/fragments/scheduledactions/action-header.jspf" %>

<h2><bean:message key="completedsystems.jsp.completedsystems"/></h2>

 <rl:listset name="completedSystemsSet">
      <rhn:csrf />
      <rl:list emptykey="completedsystems.jsp.nogroups"
               dataset="pageList"
               name="completedSystemsList">
          <rl:decorator name="ElaborationDecorator"/>
          <rl:decorator name="SelectableDecorator"/>
          <rl:decorator name="PageSizeDecorator"/>
          <rl:decorator name="AddToSsmDecorator"/>
          <rl:selectablecolumn value="${current.selectionKey}"
                               width="75"
                               checkboxText="<span class='span-ssm-marg'>SSM</spank>"
                               selected="${current.selected}"
                               disabled="${not current.selectable}"
          />
          <rl:column sortable="true"
                     bound="false"
                     headerkey="actions.jsp.system"
                     sortattr="name"
                     defaultsort="asc">
              <c:out value="<a class='js-spa' href='/rhn/systems/details/history/Event.do?sid=${current.id}&aid=${action.id}'\>"  escapeXml="false" />
              <c:out value="${current.name}" escapeXml="true" />
          </rl:column>
          <rl:column sortable="false"
                     bound="false"
                     headerkey="completedsystems.jsp.completed">
              <c:out value="${current.displayDate}"/>
          </rl:column>
          <rl:column sortable="false"
                     bound="false"
                     headerkey="actions.jsp.basechannel">
              <c:out value="${current.channelLabels}"/>
          </rl:column>
          <rhn:hidden name="aid" value="${action.id}"/>
          <rhn:hidden name="formvars" value="aid" />
      </rl:list>
  </rl:listset>

</body>
</html>
