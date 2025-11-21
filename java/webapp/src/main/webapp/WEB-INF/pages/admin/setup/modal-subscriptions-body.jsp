<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>

<c:if test="${subscriptions == null}">
    <span>No subscriptions available.</span>
</c:if>
<ul class="subscriptions-list">
    <c:forEach items="${subscriptions}" var="current">
        <li>
            <span>${current.name}</span>
            <span class="text-muted">
                <rhn:formatDate value="${current.startDate}" type="date" />-
                <rhn:formatDate value="${current.endDate}" type="date" />
            </span>
        </li>
    </c:forEach>
</ul>
