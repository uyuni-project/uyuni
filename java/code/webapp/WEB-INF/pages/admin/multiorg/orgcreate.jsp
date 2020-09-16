<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<c:if test="${empty param.action_path}">
    <c:set var="action_path" value="/admin/multiorg/OrgCreate" scope="page" />
</c:if>
<c:if test="${!empty param.action_path}">
    <c:set var="action_path" value="${param.action_path}" scope="page"/>
</c:if>

<html>
    <body>
        <rhn:toolbar base="h1" icon="header-organisation">
            <bean:message key="orgcreate.jsp.title"/>
        </rhn:toolbar>

        <html:form action="${action_path}" styleClass="form-horizontal">
            <rhn:csrf />
            <html:hidden property="submitted" value="true"/>
            <h4><bean:message key="orgdetails.jsp.header"/></h4>
            <div class="form-group">
                <label for="orgName" class="col-lg-3 control-label">
                    <rhn:required-field key="org.name.jsp"/>:
                </label>
                <div class="col-lg-6">
                    <html:text property="orgName" maxlength="128"
                               styleClass="form-control"
                               size="40" styleId="orgName" />
                    <span class="help-block">
                        <strong>
                            <bean:message key="tip" />
                        </strong>
                            <bean:message key="org.name.length.tip" />
                    </span>
                </div>
            </div>
            <c:if test="${param.account_type == 'create_sat'}">
                <h4><bean:message key="usercreate.createFirstLogin"/></h4>
                <p><bean:message key="usercreate.satSummary"/></p>
            </c:if>
            <c:if test="${empty param.account_type}">
                <h4><bean:message key="orgcreate.jsp.adminheader"/></h4>
                <p><bean:message key="orgcreate.header2"/></p>
            </c:if>
            <div class="form-group">
                <label class="col-lg-3 control-label" for="login">
                    <rhn:required-field key="desiredlogin"/>:
                </label>
                <div class="col-lg-6">
                    <html:text property="login" size="15"
                               styleClass="form-control"
                               maxlength="45" styleId="loginname" />
                    <span class="help-block">
                        <strong><bean:message key="tip" /></strong>
                        <bean:message key="org.login.tip" arg0="${rhn:getConfig('java.min_user_len')}" /><br/>
                        <c:if test="${empty param.account_type}">
                            <bean:message key="org.login.examples" />
                        </c:if>
                    </span>
                </div>
            </div>

            <div class="form-group">
                <label class="col-lg-3 control-label" for="desiredpass">
                    <bean:message key="desiredpass" />
                    <span name="password-asterisk" class="required-form-field">*</span>:
                </label>
                <div class="col-lg-6">
                    <div id="desiredpassword-input-group" class="input-group">
                        <html:password property="desiredpassword"
                                       size="15"
                                       styleClass="form-control"
                                       maxlength="32"
                                       styleId="desiredpass" />
                        <span class="input-group-addon">
                            <i class="fa fa-times-circle text-danger fa-1-5x" id="desiredtick"></i>
                        </span>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <label for="confirmpass" class="col-lg-3 control-label">
                    <bean:message key="confirmpass" />
                    <span name="password-asterisk" class="required-form-field">*</span>:
                </label>
                <div class="col-lg-6">
                    <div class="input-group">
                        <html:password property="desiredpasswordConfirm" size="15"
                                       styleClass="form-control"
                                       onkeyup="updateTickIcon()"
                                       maxlength="32" styleId="confirmpass"/>
                        <span class="input-group-addon">
                            <i class="fa fa-times-circle text-danger fa-1-5x" id="confirmtick"></i>
                        </span>
                    </div>
                </div>
            </div>

            <script type="text/javascript" src="/javascript/pwstrength-bootstrap-1.0.2.js"></script>
            <script type="text/javascript" src="/javascript/spacewalk-pwstrength-handler.js?cb=${rhn:getConfig('web.buildtimestamp')}"></script>
            <script type="text/javascript">
function toggleAsterisk() {
  jQuery("[name='password-asterisk']").toggle()
}
            </script>
            <div class="form-group">
              <label class="col-lg-3 control-label"><bean:message key="help.credentials.jsp.passwordstrength"/>:</label>
                <div class="col-lg-6" id="pwstrenghtfield">
                  <!-- progress-bar will attach to this container -->
                </div>
            </div>

            <c:if test="${empty param.account_type}">
                <div class="form-group">
                    <label for="pam" class="col-lg-3 control-label">
                        <bean:message key="usercreate.jsp.pam"/>
                    </label>
                    <div class="col-lg-6">
                        <c:choose>
                            <c:when test="${displaypamcheckbox == 'true'}">
                                <label for="pam">
                                    <html:checkbox property="usepam" onclick="toggleAsterisk()" styleId="pam"/>
                                    <bean:message key="usercreate.jsp.pam.instructions"/>
                                </label>
                                <span class="help-block">
                                    <bean:message key="usercreate.jsp.pam.instructions.note"/>
                                </span>
                            </c:when>
                                <c:otherwise>
                                    <bean:message key="usercreate.jsp.pam.reference"/>
                                </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:if>

            <div class="form-group">
                <label for="email" class="col-lg-3 control-label">
                    <rhn:required-field key="email"/>:
                </label>
                <div class="col-lg-6">
                    <html:text property="email" size="45"
                               styleClass="form-control"
                               maxlength="128" styleId="email" />
                </div>
            </div>

            <div class="form-group">
                <label class="col-lg-3 control-label" for="firstNames">
                    <rhn:required-field key="firstNames"/>:
                </label>
                <div class="col-lg-2">
                    <html:select property="prefix" styleClass="form-control">
                        <html:options collection="availablePrefixes"
                                      property="value"
                                      labelProperty="label" />
                    </html:select>
                </div>
                <div class="col-lg-4">
                    <html:text property="firstNames" size="45"
                               styleClass="form-control"
                               maxlength="128" styleId="firstNames" />
                </div>
            </div>

            <div class="form-group">
                <label class="col-lg-3 control-label" for="lastName">
                    <rhn:required-field key="lastName"/>:
                </label>
                <div class="col-lg-6">
                    <html:text property="lastName" size="45"
                               styleClass="form-control"
                               maxlength="128" styleId="lastName"/>
                </div>
            </div>

            <div class="form-group">
                <div class="col-lg-offset-3 col-lg-6">
                    <span class="help-block">
                        <span class="required-form-field">*</span> - <bean:message key="usercreate.requiredField" />
                    </span>
                </div>
            </div>

            <div class="form-group">
                <div class="col-lg-offset-3 col-lg-6">
                    <html:submit styleClass="btn btn-success">
                        <bean:message key="orgcreate.jsp.submit"/>
                    </html:submit>
                </div>
            </div>
        </html:form>
        <%-- This makes sure that the asterisks toggle correctly. Before, they could get off
             if the user checked the usepam checkbox, submitted the form, and had errors. Then
             the form would start with the box checked but the asterisks visible.
        --%>
        <script language="javascript">
            var items = document.getElementsByName('password-asterisk');
            if (('undefined' !== typeof document.orgCreateForm.usepam) && (document.orgCreateForm.usepam.checked == true)) {
                for (var i = 0; i < items.length; i++) {
                    items[i].style.display = "none";
                }
            }
            else {
                for (var i = 0; i < items.length; i++) {
                    items[i].style.display = "";
                }
            }
        </script>

    </body>
</html>
