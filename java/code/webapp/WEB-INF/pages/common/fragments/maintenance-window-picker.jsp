<%@ page pageEncoding="iso-8859-1" contentType="text/html;charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<%-- This widget assumes the "maintenanceWindows" is populated --%>

<div class="form-group">
    <div class="col-sm-6">
        <select name="maintenance_window" id="maintenance-window-select" class="form-control">
            <c:choose>
                <c:when test="${empty maintenanceWindows}">
                    <option disabled value="">
                        <bean:message key="schedule.jsp.no_maintenance_windows" />
                    </option>
                </c:when>

                <c:otherwise>
                    <c:forEach var="window" items="${maintenanceWindows}">
                        <option value="${window.right}">
                            <p><c:out value="${window.left} - ${window.middle}" /></p>
                        </option>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        <select>
    </div>
</div>
