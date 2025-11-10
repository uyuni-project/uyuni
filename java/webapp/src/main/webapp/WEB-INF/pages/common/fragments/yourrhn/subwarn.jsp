<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<rhn:require acl="authorized_for(admin.config)">
  <c:if test="${requestScope.subscriptionwarning}">
    <div class="alert alert-warning">
      <bean:message key="notification.subscriptionwarning.detail" />
    </div>
  </c:if>
</rhn:require>