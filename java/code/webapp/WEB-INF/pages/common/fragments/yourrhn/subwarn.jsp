<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<c:if test="${requestScope.subscriptionwarning}">
  <div class="panel panel-warning">
    <div class="panel-heading">
      <h3 class="panel-title">
        <bean:message key="notification.subscriptionwarning.summary" />
      </h3>
  </div>
  <div class="panel panel-body">
    <bean:message key="notification.subscriptionwarning.detail" />
  </div>
  <div class="panel-footer"></div>
</c:if>