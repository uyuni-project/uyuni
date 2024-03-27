<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html:html>
    <body>
        <%@ include file="/WEB-INF/pages/common/fragments/kickstart/kickstart-toolbar.jspf" %>
        <rhn:dialogmenu mindepth="0"
                        maxdepth="1"
                        definition="/WEB-INF/nav/kickstart_details.xml"
                        renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />
        <h2><bean:message key="kickstartdetails.jsp.header2"/></h2>
        <c:choose>
            <c:when test="${empty requestScope.invalid}">
                <bean:message key="kickstartdetails.jsp.summary1"/>
                <html:form method="post"
                           styleClass="form-horizontal"
                           action="/kickstart/KickstartDetailsEdit.do">
                    <rhn:csrf />
                    <html:hidden property="ksid" value="${ksdata.id}"/>
                    <html:hidden property="submitted" value="true"/>
                        <div class="form-group">
                            <label class="col-lg-3 control-label">
                                <rhn:required-field key="kickstartdetails.jsp.label"/>:
                            </label>
                            <div class="col-lg-6">
                                <html:text property="label" maxlength="64" size="32" styleClass="form-control" />
                                <span class="help-block">
                                    <bean:message key="kickstartdetails.jsp.labelwarning" />
                                </span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-lg-3 control-label">
                                <bean:message key="kickstartdetails.jsp.install_type" />
                            </label>
                            <div class="col-lg-6">
                                <h3>
                                    <span class="label label-primary"><c:out value="${ksdata.kickstartDefaults.kstree.channel.name}"/></span>
                                    <a class="btn btn-default" href="/rhn/kickstart/KickstartSoftwareEdit.do?ksid=${ksdata.id}"><bean:message key="kickstartdetails.jsp.changeos"/></a>
                                </h3>
                            </div>
                        </div>
                        
                        <%@ include file="/WEB-INF/pages/common/fragments/kickstart/virtoptions.jspf" %>
                        
                        <div class="form-group">
                            <div class="col-lg-offset-3 offset-lg-3 col-lg-6">
                                <div class="checkbox">
                                    <label class="control-label">
                                        <html:checkbox property="active" />
                                        <bean:message key="kickstartdetails.jsp.active"/>
                                    </label>
                                </div>
                                <span class="help-block">
                                    <bean:message key="kickstartdetails.jsp.activeDescription"/>
                                </span>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-lg-offset-3 offset-lg-3 col-lg-6">
                                <div class="checkbox">
                                    <label class="control-label">
                                        <html:checkbox property="post_log" />
                                        <bean:message key="kickstartdetails.jsp.postlog"/>
                                    </label>
                                </div>
                                <span class="help-block">
                                    <bean:message key="kickstartdetails.jsp.postlog.msg"  />
                                </span>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-lg-offset-3 offset-lg-3 col-lg-6">
                                <div class="checkbox">
                                    <label class="control-label">
                                        <html:checkbox property="pre_log" />
                                        <bean:message key="kickstartdetails.jsp.prelog"/>
                                    </label>
                                    <span class="help-block">
                                        <bean:message key="kickstartdetails.jsp.prelog.msg"  />
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-lg-offset-3 offset-lg-3 col-lg-6">
                                <div class="checkbox">
                                    <label class="control-label">
                                        <html:checkbox property="ksCfg" />
                                        <bean:message key="kickstartdetails.jsp.kscfg"/>
                                    </label>
                                    <span class="help-block">
                                        <bean:message key="kickstartdetails.jsp.kscfg.msg"  />
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-lg-offset-3 offset-lg-3 col-lg-6">
                                <div class="checkbox">
                                    <label class="control-label">
                                        <html:checkbox property="org_default" />
                                        <bean:message key="kickstartdetails.jsp.org_default" />
                                    </label>
                                    <span class="help-block">
                                        <bean:message key="kickstartdetails.jsp.summary2" arg0="${ksurl}" />
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-lg-3 control-label">
                                <bean:message key="kickstartdetails.jsp.kernel_options"/>
                            </label>
                            <div class="col-lg-6">
                                <html:text property="kernel_options" maxlength="2048" size="32" styleClass="form-control"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-lg-3 control-label">
                                <bean:message key="kickstartdetails.jsp.post_kernel_options"/>
                            </label>
                            <div class="col-lg-6">
                                <html:text property="post_kernel_options" maxlength="2048" size="32" styleClass="form-control"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-lg-3 control-label">
                                <bean:message key="kickstartdetails.jsp.comments"/>
                            </label>
                            <div class="col-lg-6">
                                <html:textarea property="comments" cols="80" rows="6" styleClass="form-control"/>
                            </div>
                        </div>

                        <div class="form-group">
                             <div class="col-lg-offset-3 offset-lg-3 col-lg-6">
                                <html:submit styleClass="btn btn-success">
                                    <bean:message key="kickstartdetails.jsp.updatekickstart"/>
                                </html:submit>
                            </div>
                        </div>
                </html:form>
            </c:when>
            <c:otherwise>
                <p><bean:message key="kickstartdetails.invalid.jsp.summary"/>
                    <bean:message key="kickstartdetails.invalid.jsp.summary-option1"
                                  arg0="${ksdata.tree.label}"
                                  arg1="/rhn/kickstart/TreeEdit.do?kstid=${ksdata.tree.id}"/></p>
                <p><bean:message key="kickstartdetails.invalid.jsp.summary-option2"
                              arg0="/rhn/kickstart/KickstartSoftwareEdit.do?ksid=${ksdata.id}"/></p>
                </c:otherwise>
            </c:choose>
    </body>
</html:html>

