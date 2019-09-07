<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<html:html >

<head>
<meta http-equiv="Pragma" content="no-cache" />
</head>

<body>
<%@ include file="/WEB-INF/pages/common/fragments/kickstart/kickstart-toolbar.jspf" %>
<rhn:dialogmenu mindepth="0" maxdepth="1"
                definition="/WEB-INF/nav/kickstart_details.xml"
                renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />
<html:form method="post"
           styleClass="form-horizontal"
           action="/kickstart/SystemDetailsEdit.do">
    <rhn:csrf />
    <html:hidden property="ksid" />
    <html:hidden property="submitted" />

    <h2><bean:message key="kickstart.systemdetails.jsp.header1"/></h2>

    <c:if test="${not ksdata.legacyKickstart}">
        <h3><bean:message key="kickstart.systemdetails.jsp.header2"/></h3>
        <div class="form-group">
            <label class="col-lg-3 control-label">
                <bean:message key="kickstart.selinux.jsp.label" />:
            </label>
            <div class="col-lg-6">
                <div class="radio">
                    <label>
                        <html:radio property="selinuxMode" value="enforcing" />
                        <bean:message key="kickstart.selinux.enforce.policy.jsp.label" />
                    </label>
                </div>
            </div>
        </div>
        <div class="form-group">
            <div class="col-lg-offset-3 col-lg-6">
                <div class="radio">
                    <label>
                        <html:radio property="selinuxMode" value="permissive" />
                        <bean:message key="kickstart.selinux.warn.policy.jsp.label" />
                    </label>
                </div>
            </div>
        </div>
        <div class="form-group">
            <div class="col-lg-offset-3 col-lg-6">
                <div class="radio">
                    <label>
                        <html:radio property="selinuxMode" value="disabled" />
                        <bean:message key="kickstart.selinux.disable.policy.jsp.label" />
                    </label>
                </div>
            </div>
        </div>
    </c:if>

    <h3><bean:message key="kickstart.systemdetails.jsp.header3"/></h3>
        <div class="form-group">
            <div class="col-lg-offset-3 col-lg-6">
                <div class="checkbox">
                    <label class="control-label">
                        <html:checkbox property="configManagement" />
                        <bean:message key="kickstart.config.mgmt.jsp.label" />
                    </label>
                </div>
                <span class="help-block">
                    <bean:message key="kickstart.config.mgmt.tip.jsp.label" />
                </span>
            </div>
        </div>
        <div class="form-group">
            <div class="col-lg-offset-3 col-lg-6">
                <div class="checkbox">
                    <label class="control-label">
                        <bean:message key="kickstart.remote.cmd.jsp.label" />
                        <html:checkbox property="remoteCommands" />
                    </label>
                </div>
                <span class="help-block">
                    <bean:message key="kickstart.remote.cmd.tip.jsp.label" />
                </span>
            </div>
        </div>
        <div class="form-group">
            <div class="col-lg-3 control-label">
                <bean:message key="kickstart.registration.type.jsp.label" />
            </div>
            <div>
                <p>
                    <bean:message key="kickstart.registration.type.jsp.message" />:
                </p>
            </div>
        </div>
        <div class="form-group">
            <div class="col-lg-offset-3 col-lg-6">
                <div class="radio">
                    <label>
                        <html:radio property="registrationType" value="reactivation" />
                        <bean:message key="kickstart.registration.type.reactivation.jsp.label" />
                    </label>
                </div>
            </div>
        </div>
        <div class="form-group">
            <div class="col-lg-offset-3 col-lg-6">
                <div class="radio">
                    <label>
                        <html:radio property="registrationType" value="deletion" />
                        <bean:message key="kickstart.registration.type.deletion.jsp.label" />
                    </label>
                </div>
            </div>
        </div>
        <div class="form-group">
            <div class="col-lg-offset-3 col-lg-6">
                <div class="radio">
                    <label>
                        <html:radio property="registrationType" value="none" />
                        <bean:message key="kickstart.registration.type.none.jsp.label" />
                    </label>
                </div>
            </div>
        </div>

    <h3><bean:message key="kickstart.systemdetails.jsp.header4"/></h3>
    <div class="form-group">
            <label class="col-lg-3 control-label">
                <bean:message key="kickstart.root.password.jsp.label" />:
            </label>
            <div class="col-lg-6">
                <html:password styleClass="form-control"
                               property="rootPassword" maxlength="32" size="32" redisplay="false"/>
            </div>
    </div>
    <div class="form-group">
            <label class="col-lg-3 control-label">
                <bean:message key="kickstart.root.password.verify.jsp.label" />:
            </label>
            <div class="col-lg-6">
                <html:password styleClass="form-control"
                               property="rootPasswordConfirm"
                               maxlength="32" size="32" redisplay="false"/>
            </div>
    </div>
    <div class="form-group">
        <div class="col-lg-offset-3 col-lg-6">
            <input type="submit" class="btn btn-success"
                   value="<bean:message key='kickstart.systemdetails.edit.submit.jsp.label'/>" />
        </div>
    </div>
</html:form>
</body>
</html:html>
