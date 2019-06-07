<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html:html >
<head>
</head>
<body>
<script language="javascript" type="text/javascript">
    //<!--
    function reloadForm(ctl) {
        var submittedFlag = document.getElementById("editFormSubmitted");
        submittedFlag.value = "false";
        var changedField = document.getElementById("fieldChanged");
        changedField.value = ctl.id;
        var form = document.getElementsByName("kickstartSoftwareForm")[0];
        form.submit();
    }

    function toggleKSTree(what) {
        var form = document.getElementsByName("kickstartSoftwareForm")[0];
        if(what.checked) {
            form.tree.disabled=1;
        } else {
            form.tree.disabled=0;
        }
    }

    function clickNewestRHTree() {
        var form = document.getElementsByName("kickstartSoftwareForm")[0];
        if(form.useNewestRHTree.checked) {
            form.useNewestTree.checked = false;
        }
    }

    function clickNewestTree() {
        var form = document.getElementsByName("kickstartSoftwareForm")[0];
        if(form.useNewestTree.checked) {
            form.useNewestRHTree.checked = false;
        }
    }
    //-->
</script>
<%@ include file="/WEB-INF/pages/common/fragments/kickstart/kickstart-toolbar.jspf" %>

<rhn:dialogmenu mindepth="0" maxdepth="1"
    definition="/WEB-INF/nav/kickstart_details.xml"
    renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />

<h2><bean:message key="softwareedit.jsp.header2"/></h2>
<p><bean:message key="softwareedit.jsp.summary1"/></p>

    <html:form method="post"
               styleClass="form-horizontal"
               action="/kickstart/KickstartSoftwareEdit.do">
        <rhn:csrf />
            <div class="form-group">
                <label class="col-lg-3 control-label">
                    <rhn:required-field key="softwareedit.jsp.basechannel"/>:
                </label>
                <div class="col-lg-6">
                    <html:select styleClass="form-control" property="channel" onchange="reloadForm(this);" styleId="channel">
                        <html:options collection="channels" property="value" labelProperty="label" />
                    </html:select>
                    <span class="help-block">
                        <bean:message key="softwareedit.jsp.tip" />
                    </span>
                </div>
            </div>
            <div class="form-group">
                <label class="col-lg-3 control-label">
                    <bean:message key="softwareedit.jsp.child_channels"/>:
                </label>
                <div class="col-lg-6">
                    <c:choose>
                        <c:when test="${empty nochildchannels}">
                            <c:forEach items="${avail_child_channels}" var="child">
                                <input name="child_channels"
                                       value="${child.id}" 
                                       type="checkbox"
                                       id="${child.id}"
                                       <c:if test="${not empty stored_child_channels[child.id]}">checked=1</c:if> />
                                       <label for="${child.id}">${child.label}</label><br/>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-warning">
                                <bean:message key="softwareedit.jsp.nochildchannels" />
                            </div>
                        </c:otherwise>
                    </c:choose>
                    <span class="help-block">
                        <bean:message key="softwareedit.jsp.warning" arg0="${ksdata.id}"/>
                    </span>
                </div>
            </div>
            <div class="form-group">
                <label class="col-lg-3 control-label">
                    <rhn:required-field key="softwareedit.jsp.avail_trees"/>:
                </label>
                <div class="col-lg-6">
                    <c:choose>
                        <c:when test="${notrees == null}">
                            <c:if test="${usingNewest == true or usingNewestRH == true}">
                                <html:select styleClass="form-control"
                                             property="tree"
                                             onchange="reloadForm(this);"
                                             styleId="kstree"
                                             disabled="true">
                                    <html:options collection="trees"
                                                  property="id"
                                                  labelProperty="label" />
                                </html:select>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-warning"><bean:message key="kickstart.edit.software.notrees.jsp" /></div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <c:choose>
                <c:when test="${notrees == null}">
                    <c:if test="${usingNewest == true or usingNewestRH == true}">
                        <div class="form-group">
                            <div class="col-lg-offset-3 col-lg-6">
                                <html:select styleClass="form-control"
                                             property="tree"
                                             onchange="reloadForm(this);"
                                             styleId="kstree"
                                             disabled="true">
                                    <html:options collection="trees"
                                                  property="id"
                                                  labelProperty="label" />
                                </html:select>
                            </div>
                        </div>
                    </c:if>
                    <c:if test="${not (usingNewest == true or usingNewestRH == true)}">
                        <div class="form-group">
                            <div class="col-lg-offset-3 col-lg-6">
                                <html:select styleClass="form-control"
                                             property="tree"
                                             onchange="reloadForm(this);"
                                             styleId="kstree">
                                    <html:options collection="trees"
                                                  property="id"
                                                  labelProperty="label" />
                                </html:select>
                            </div>
                        </div>
                    </c:if>
                    <c:if test="${redHatTreesAvailable != null}">
                        <div class="form-group">
                            <div class="col-lg-offset-3 col-lg-6">
                                <div class="checkbox">
                                    <label>
                                        <input type="checkbox" name="useNewestRHTree" value="0"
                                               onclick="toggleKSTree(this); clickNewestRHTree()"
                                               <c:if test="${usingNewestRH == true}">checked=1</c:if> />
                                        <bean:message key="kickstart.jsp.create.wizard.kstree.always_new.label"/>
                                    </label>
                                </div>
                                    <div class="help-block">
                                        <bean:message key="kickstart.jsp.create.wizard.kstree.always_new_RH"/>                                
                                    </div>
                            </div>
                        </div>
                    </c:if>
                    <div class="form-group">
                        <div class="col-lg-offset-3 col-lg-6">
                            <div class="checkbox">
                                <label>
                                    <input type="checkbox"
                                           name="useNewestTree"
                                           value="0"
                                           onclick="toggleKSTree(this); clickNewestTree()"
                                           <c:if test="${usingNewest == true}">checked=1</c:if> />
                                    <bean:message key="kickstart.jsp.create.wizard.kstree.always_new.label"/>
                                </label>
                            </div>
                            <div class="help-block">
                                <bean:message key="kickstart.jsp.create.wizard.kstree.always_new"/>
                            </div>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="alert alert-warning"><bean:message key="kickstart.edit.software.notrees.jsp" /></div>
                </c:otherwise>
            </c:choose>

            <div class="form-group">
                <label class="col-lg-3 control-label">
                    <bean:message key="softwareedit.jsp.url" />:
                </label>
                <div class="col-lg-6">
                    <c:choose>
                        <c:when test="${nourl == null}">
                            <code><bean:write name="kickstartSoftwareForm" property="url" /></code>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-warning"><bean:message key="kickstart.edit.software.nofiles.jsp" /></div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <c:if test = "${not empty kickstartSoftwareForm.map.possibleRepos}">
                <div class="form-group">
                    <label class="col-lg-3 control-label">
                        <bean:message key="softwareedit.jsp.repos" />:
                    </label>
                    <div class="col-lg-6">
                        <c:forEach items="${kickstartSoftwareForm.map.possibleRepos}" var="item">
                            <div class="row">
                                <html:multibox property="selectedRepos" disabled="${item.disabled}"
                                               value = "${item.value}" styleId="type_${item.value}"/>
                                <label for="type_${item.value}"><c:out value="${item.label}"/></label>
                            </div>
                        </c:forEach>
                        <span class="help-block">
                            <rhn:tooltip key="softwareedit.jsp.repos-tooltip"/>
                        </span>
                    </div>
                </div>
            </c:if>
            <div class="form-group">
                <div class="col-lg-offset-3 col-lg-6">
                    <html:submit styleClass="btn btn-success">
                        <bean:message key="kickstartdetails.jsp.updatekickstart"/>
                    </html:submit>
                </div>
            </div>
            <html:hidden property="url" value="${kickstartSoftwareForm.map.url}"/>
            <html:hidden property="ksid" value="${ksdata.id}"/>
            <html:hidden property="submitted" value="true" styleId="editFormSubmitted"/>
            <html:hidden property="fieldChanged" value="" styleId="fieldChanged" />
    </html:form>
</body>
</html:html>
