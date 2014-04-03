<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<c:forEach var="product" items="${productsList}">
    <tr>
        <td><c:out value="${product.baseProductIdent}"></c:out> - <c:out value="${product.name}"></c:out></td>
        <td><c:out value="${product.arch}"></c:out></td>
        <td>
          <c:choose>
              <c:when test="${not product.synchronizing}">
                  <i class="fa fa-refresh btn-synchronize text-success"></i>
              </c:when>
              <c:otherwise>
                  <i class="fa fa-check text-success"></i>
              </c:otherwise>
          </c:choose>
        </td>
    </tr>
</c:forEach>
