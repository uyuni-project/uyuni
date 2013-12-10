<%@ page
    pageEncoding="iso-8859-1"
    contentType="text/html;charset=utf-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
    <%--
      @param widget name of the date picker bean which should be in the request
                    scope. The bean should be DatePickerBean
    --%>
    <div class="row">
        <div class="input-group col-md-1">
        <c:set value="${requestScope[param.widget]}" var="picker"/>
        <c:if test="${! picker.dayBeforeMonth}">
            <span class="input-group-btn">
                <select name='${picker.name}_month' size="1" class="form-control">
                    <c:forEach var="monthLabel" items="${picker.dateFormatSymbols.months}" varStatus="s">
                        <c:if test="${monthLabel ne ''}">
                            <option <c:if test="${s.index eq picker.month}">selected="selected"</c:if> value='${s.index}'>${monthLabel}</option>
                        </c:if>
                    </c:forEach>
                </select>
            </span>
            <span class="input-group-btn">
                <select name='${picker.name}_day' size="1" class="form-control">
                    <c:forEach var="dayLabel" begin="1" end="31">
                        <option <c:if test="${dayLabel eq picker.day}">selected="selected"</c:if> value='${dayLabel}'>${dayLabel}</option>
                    </c:forEach>
                </select>
            </span>
        </c:if>
        <c:if test="${picker.dayBeforeMonth}">
            <span class="input-group-btn">
                <select name='${picker.name}_day' size="1" class="form-control">
                    <c:forEach var="dayLabel" begin="1" end="31">
                        <option <c:if test="${dayLabel eq picker.day}">selected="selected"</c:if> value='${dayLabel}'>${dayLabel}</option>
                    </c:forEach>
                </select>
            </span>
            <span class="input-group-btn">
                <select name='${picker.name}_month' size="1" class="form-control">
                    <c:forEach var="monthLabel" items="${picker.dateFormatSymbols.months}" varStatus="s">
                        <option <c:if test="${s.index eq picker.month}">selected</c:if> value='${s.index}'>${monthLabel}</option>
                    </c:forEach>
                </select>
            </span>
        </c:if>
        <span class="input-group-btn">
            <select name='${picker.name}_year' size="1" class="form-control">
                <c:forEach var="yearLabel" items="${picker.yearRange}">
                    <option <c:if test="${yearLabel eq picker.year}">selected="selected"</c:if> value='${yearLabel}'>${yearLabel}</option>
                </c:forEach>
            </select>
        </span>
        <c:if test="${! picker.disableTime}">
            <span class="input-group-addon"> - </span>
            <span class="input-group-btn">
                <select name='${picker.name}_hour' size="1" class="form-control">
                    <c:forEach var="hourLabel" items="${picker.hourRange}">
                        <option <c:if test="${hourLabel eq picker.hour}">selected="selected"</c:if> value='${hourLabel}'>${hourLabel}</option>
                    </c:forEach>
                </select>
            </span>
            <span class="input-group-btn">
                <select name='${picker.name}_minute' size="1" class="form-control">
                    <c:forEach var="minLabel" begin="0" end="9">
                        <option <c:if test="${minLabel eq picker.minute}">selected="selected"</c:if> value='${minLabel}'>
                            0${minLabel}
                        </option>
                    </c:forEach>
                    <c:forEach var="minLabel" begin="10" end="59">
                        <option <c:if test="${minLabel eq picker.minute}">selected="selected"</c:if> value='${minLabel}'>
                            ${minLabel}
                        </option>
                    </c:forEach>
                </select>
            </span>
            <c:if test="${picker.latin}">
                <span class="input-group-btn">
                    <select name='${picker.name}_am_pm' size="1" class="form-control">
                        <c:forEach var="ampmLabel" items="${picker.dateFormatSymbols.amPmStrings}" varStatus="s">
                            <option <c:if test="${s.index eq picker.amPm}">selected="selected"</c:if> value='${s.index}'>${ampmLabel}</option>
                        </c:forEach>
                    </select>
                </span>
            </c:if>
            <span class="input-group-addon">
                <fmt:formatDate value="${picker.date}" pattern="z" timeZone="${picker.calendar.timeZone}"/>
            </span>
        </c:if>
        <c:if test="${picker.disableTime}">
            <input type="hidden" name='${picker.name}_hour' value="12" />
            <input type="hidden" name='${picker.name}_minute' value="0" />
            <input type="hidden" name='${picker.name}_am_pm' value="0" />
        </c:if>
        </div>
    </div>
