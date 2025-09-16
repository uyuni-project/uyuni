<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
<body>

  <%@ include file="/WEB-INF/pages/common/fragments/scheduledactions/action-header.jspf" %>

  <h2><bean:message key="failedsystems.jsp.failedsystems"/></h2>

  <p><bean:message key="failedsystems.jsp.summary"/></p>

  <rl:listset name="failedSystemsSet">
      <rhn:csrf />
      <c:if test="${canEdit and not empty pageList}">
          <div class="spacewalk-section-toolbar">
              <div class="action-button-wrapper">
                  <a class="js-spa" href="/rhn/schedule/FailedSystems.do?aid=${action.id}&reschedule=true">
                      <div class="btn btn-default">
                          <bean:message key="failedsystems.jsp.rescheduleactions"/>
                      </div>
                  </a>
              </div>
          </div>
      </c:if>
      <rl:list emptykey="failedsystems.jsp.nosystems"
               dataset="pageList"
               name="failedSystemsList">
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
              <c:out value="${current.serverName}" escapeXml="true" />
          </rl:column>
          <rl:column sortable="false"
                     bound="false"
                     headerkey="failedsystems.jsp.failed">
              <rhn:formatDate value="${current.date}"/>
          </rl:column>
          <rl:column sortable="false"
                     bound="false"
                     headerkey="failedsystems.jsp.failed">
              <c:out value="${current.message}"/>
          </rl:column>
          <rhn:hidden name="aid" value="${action.id}"/>
          <rhn:hidden name="formvars" value="aid" />
      </rl:list>
  </rl:listset>

</body>
</html>
