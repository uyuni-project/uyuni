<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<ul>
    <c:forEach items="${subscriptions}" var="current">
        <li>${current.productClass}</li>
    </c:forEach>
</ul>
