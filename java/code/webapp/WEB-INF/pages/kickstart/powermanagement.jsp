<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<html:xhtml />
<html>
<head>
  <meta http-equiv="Pragma" content="no-cache" />
</head>

<body>
  <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>
  <br />

  <h2>
    <img src="/img/icon_kickstart_session-medium.gif"
      alt="<bean:message key='system.common.kickstartAlt' />"
    >
    <bean:message key="kickstart.powermanagement.jsp.heading" />
  </h2>

  <c:if test="${fn:length(types) >= 1}">
    <html:form action="/systems/details/kickstart/PowerManagement.do?sid=${sid}">

      <rhn:csrf />
      <rhn:submitted />
      <div class="search-choices">
        <div class="search-choices-group">
          <table class="details">

          <tr>
            <th>
              <label for="powerType">
                <bean:message key="kickstart.powermanagement.jsp.powertype" />
                <rhn:required-field />
              </label>
            </th>
            <td>
              <c:choose>
                <c:when test="${fn:length(types) == 1}">
                  <bean:message key="kickstart.powermanagement.${types[0]}" />
                  <input type="hidden" name="powerType" value="${types[0]}">
                </c:when>
                <c:otherwise>
                  <select name="powerType">
                    <c:forEach items="${types}" var="type">
                      <option value="${type}" <c:if test="${type eq powerType}">selected</c:if>>
                        <bean:message key="kickstart.powermanagement.${type}" />
                      </option>
                    </c:forEach>
                  </select>
                </c:otherwise>
              </c:choose>
            </td>
          </tr>

            <tr>
              <th>
                <label for="powerAddress">
                  <bean:message key="kickstart.powermanagement.jsp.poweraddress" />
                  <rhn:required-field />
                </label>
              </th>
              <td>
                <html:text property="powerAddress" name="powerAddress"
                  value="${powerAddress}"
                />
              </td>
            </tr>
            <tr>
              <th>
                <label for="powerUsername">
                  <bean:message key="kickstart.powermanagement.jsp.powerusername" />
                  <rhn:required-field />
                </label>
              </th>
              <td>
                <html:text property="powerUsername" name="powerUsername"
                  value="${powerUsername}"
                />
              </td>
            </tr>
            <tr>
              <th>
                <label for="powerPassword">
                  <bean:message key="kickstart.powermanagement.jsp.powerpassword" />
                  <rhn:required-field />
                </label>
              </th>
              <td>
                <html:password property="powerPassword" name="powerPassword"
                  value="${powerPassword}"
                />
              </td>
            </tr>
            <tr>
              <th>
                <label for="powerId">
                  <bean:message key="kickstart.powermanagement.jsp.powerid" />
                </label>
              </th>
              <td>
                <html:text property="powerId" name="powerId" value="${powerId}" />
              </td>
            </tr>
            <tr>
              <th>
                <label for="powerStatus">
                  <bean:message key="kickstart.powermanagement.jsp.power_status" />
                </label>
              </th>
              <td>
                <c:choose>
                  <c:when test="${powerStatusOn eq true}">
                    <bean:message key="kickstart.powermanagement.jsp.on" />
                  </c:when>
                  <c:when test="${powerStatusOn eq false}">
                    <bean:message key="kickstart.powermanagement.jsp.off" />
                  </c:when>
                  <c:otherwise>
                    <bean:message key="kickstart.powermanagement.jsp.unknown" />
                  </c:otherwise>
                </c:choose>
              </td>
            </tr>
          </table>
        </div>
      </div>

      <hr>

      <button type="submit" name="powerAdditionalAction" value="none">
        <bean:message key="kickstart.powermanagement.jsp.save" />
      </button>
      <button type="submit" name="powerAdditionalAction" value="powerOn">
        <bean:message key="kickstart.powermanagement.jsp.savepoweron" />
      </button>
      <button type="submit" name="powerAdditionalAction" value="powerOff">
        <bean:message key="kickstart.powermanagement.jsp.savepoweroff" />
      </button>
      <button type="submit" name="powerAdditionalAction" value="reboot">
        <bean:message key="kickstart.powermanagement.jsp.savereboot" />
      </button>
      <button type="submit" name="powerAdditionalAction" value="getStatus">
        <bean:message key="kickstart.powermanagement.jsp.save_get_status" />
      </button>
    </html:form>
  </c:if>
</body>
</html>
