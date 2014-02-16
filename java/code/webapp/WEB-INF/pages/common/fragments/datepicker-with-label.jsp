<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<div class="form-group">
  <label class="col-md-3 control-label"><bean:message key="${param.label_text}" /></label>
  <div class="col-md-4">
    <jsp:include page="/WEB-INF/pages/common/fragments/date-picker.jsp">
      <jsp:param name="widget" value="${param.widget}" />
    </jsp:include>
  </div>
</div>
