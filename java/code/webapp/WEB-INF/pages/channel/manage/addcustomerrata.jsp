<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
<head></head>
<body>

    <%@ include file="/WEB-INF/pages/common/fragments/channel/manage/manage_channel_header.jspf" %>

    <rl:listset name="errataSet">
        <rhn:csrf />
        <rhn:hidden name="cid" value="${cid}" />
        <rhn:submitted/>

        <p><bean:message key="channel.manage.errata.custommsg"/></p>

        <div class="form-horizontal">
            <div class="form-group">
                <label class="col-md-2 col-lg-1 control-label">
                    <strong>Package Association:</strong>
                </label>
                <div class="col-md-8 col-lg-5">
                    <input type="checkbox" name="assoc_checked" ${assoc_checked ? 'checked' : ''}/>
                    <bean:message key="channel.manage.errata.packageassocmsg" />
                </div>
            </div>

            <c:if test="${selected_channel != null}">
                <rhn:hidden name="selected_channel_old"  value="${selected_channel}" />
            </c:if>

            <c:if test="${channel_list != null}">
                <div class="form-group">
                    <label class="col-md-2 col-lg-1 control-label">Channel:</label>
                    <div class="col-md-8 col-lg-5">
                        <select name="selected_channel">
                            <option value="">All Custom Channels</option>
                            <c:set var="ingroup" value="false"/>
                            <c:forEach var="option" items="${channel_list}">
                                <c:choose>
                                    <c:when test="${option.baseChannel}">
                                        <c:if test="${ingroup}">
                                            <c:set var="ingroup" value="false"/>
                                            </optgroup>
                                        </c:if>
                                        <option value="${option.id}" <c:if test="${option.selected eq true}">selected = "selected"</c:if>>${option.name}   </option>
                                    </c:when>
                                    <c:otherwise>
                                        <c:if test="${!ingroup}">
                                            <c:set var="ingroup" value="true"/>
                                            <optgroup>
                                        </c:if>
                                        <option value="${option.id}" <c:if test="${option.selected eq true}">selected = "selected"</c:if>>${option.name}</option>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                            <c:if test="${ingroup}">
                                </optgroup>
                            </c:if>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                  <div class="col-md-offset-2 offset-md-2 col-md-8 col-lg-offset-1 offset-lg-1 col-lg-5">
                      <input class="btn btn-default" type="submit" name="dispatch"  value="<bean:message key='frontend.actions.channels.manager.add.viewErrata'/>">
                  </div>
                </div>
            </c:if>
        </div>

        <div class="spacewalk-section-toolbar">
            <div class="action-button-wrapper">
                <input class="btn btn-success" type="submit" name="dispatch"  value="<bean:message key='frontend.actions.channels.manager.add.submit'/>" ${empty pageList ? 'disabled' : ''}/>
            </div>
        </div>

        <c:if test="${pageList != null}">
            <%@ include file="/WEB-INF/pages/common/fragments/errata/selectableerratalist.jspf" %>
        </c:if>
    </rl:listset>

</body>
</html>

