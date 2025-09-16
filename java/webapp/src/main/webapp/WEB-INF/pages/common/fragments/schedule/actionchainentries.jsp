<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>

<ul>
    <c:forEach items="${entries}" var="entry">
        <li class="entry" data-entry-id="${entry.id}">
            <c:out value="${entry.server.name}" />

            <c:set var="maintenanceSchedule" value="${entry.server.maintenanceSchedule}" />
            <c:if test="${maintenanceSchedule != null}">
                <c:out value="(" />
                    <bean:message key="actionchain.jsp.maintenanceschedule" />
                    <a href="/rhn/manager/schedule/maintenance/schedules#/details/${maintenanceSchedule.id}">
                        <c:out value="${maintenanceSchedule.name}" />
                    </a>
                <c:out value=")" />
            </c:if>

            <a class="delete-entry" href="#">
                <i class="fa fa-trash-o"></i><bean:message key="actionchain.jsp.deletesystem" />
            </a>
        </li>
    </c:forEach>
</ul>
