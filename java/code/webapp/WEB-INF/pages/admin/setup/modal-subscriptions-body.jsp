<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:if test="${subscriptions == null}">
    <span>No subscriptions available.</span>
</c:if>
<ul>
    <c:forEach items="${subscriptions}" var="current">
        <li>${current.productClass}</li>
    </c:forEach>
</ul>
