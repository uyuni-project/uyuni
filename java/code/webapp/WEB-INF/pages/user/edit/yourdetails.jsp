<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>


<html>
    <head>
    </head>
    <body>
        <rhn:toolbar base="h1" icon="header-user"
                     helpUrl="/rhn/help/reference/en-US/s1-sm-your-rhn.jsp#s2-sm-your-rhn-account"
                     imgAlt="users.jsp.imgAlt">
            <bean:message key="details.jsp.account_details" />
        </rhn:toolbar>
        <div class="panel panel-default">
            <div class="panel-heading">
                <h4><bean:message key="details.jsp.personal_info" /></h4>
            </div>
            <div class="panel-body">
                <p><bean:message key="yourdetails.jsp.summary" /></p>
                <hr />
                <html:form action="/account/UserDetailsSubmit" styleClass="form-horizontal">
                    <rhn:csrf />
                    <%@ include file="/WEB-INF/pages/common/fragments/user/edit_user_table_rows.jspf"%>
                    <div class="form-group">
                        <label class="col-lg-3 control-label"><bean:message key="created.displayname"/></label>
                        <div class="col-lg-6">${created}</div>
                    </div>
                    <div class="form-group">
                        <label class="col-lg-3 control-label"><bean:message key="last_sign_in.displayname"/></label>
                        <div class="col-lg-6">${lastLoggedIn}</div>
                    </div>
                    <div class="form-group">
                        <div class="col-lg-offset-3 col-lg-6">
                            <button type="submit" value="<bean:message key='message.Update'/>"
                                <c:choose>
                                   <c:when test="${empty mailableAddress}">
                                       disabled class="btn"
                                   </c:when>
                                   <c:otherwise>
                                       class="btn btn-success"
                                    </c:otherwise>
                                </c:choose>
                            >
                                <bean:message key="message.Update"/>
                            </button>
                        </div>
                    </div>
                    <html:hidden property="uid"/>
                </html:form>
            </div>
        </div>
    </body>
</html>
