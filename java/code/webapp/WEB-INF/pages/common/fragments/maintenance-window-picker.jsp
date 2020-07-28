<%@ page pageEncoding="iso-8859-1" contentType="text/html;charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<%-- This widget assumes the "maintenanceWindows" is populated --%>

<div class="form-group">
    <div class="col-sm-6">
        <select name="maintenance_window" id="maintenance-window-select" class="form-control">
            <c:forEach var="window" items="${maintenanceWindows}">
                <option value="${window.right}">
                    <p><c:out value="${window.left} - ${window.middle}" /></p>
                </option>
            </c:forEach>
        <select>
    </div>
</div>
