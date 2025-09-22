<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<!-- Hide any modals -->
<script>hideModal();</script>

<c:forEach items="${credentials}" var="current">
    <div class="panel panel-default">
        <div class="panel-body">
            <h3><c:out value="${current.user}" /></h3>
        </div>
        <div class="panel-footer">
            <div class="row">
                <div class="text-left col-10 col-xs-10">
                    <span id="verify-${current.id}"></span>
                    <span id="primary-${current.id}">
                        <c:if test="${current.primary}">
                            <rhn:icon type="setup-wizard-creds-primary" title="mirror-credentials.jsp.primary" />
                        </c:if>
                        <c:if test="${not current.primary}">
                            <a href="javascript:void(0);" onCLick="makePrimaryCredentials('${current.id}')">
                                <rhn:icon type="setup-wizard-creds-make-primary" title="mirror-credentials.jsp.make-primary" />
                            </a>
                        </c:if>
                    </span>
                    <a href="javascript:void(0);" onCLick="initSubscriptions('${current.id}')" data-bs-toggle="modal" data-bs-target="#modal-list-subscriptions">
                        <rhn:icon type="setup-wizard-creds-subscriptions" title="mirror-credentials.jsp.subscriptions" />
                    </a>
                    <a href="javascript:void(0);"
                        data-id="<c:out value='${current.id}' />"
                        data-user="<c:out value='${current.user}' />"
                        onClick="initEdit(this);"
                        data-bs-toggle="modal"
                        data-bs-target="#modal-edit-credentials">
                        <rhn:icon type="setup-wizard-creds-edit" title="mirror-credentials.jsp.edit" />
                    </a>
                </div>
                <div class="text-right col-2 col-xs-2">
                    <span id="delete-${current.id}">
                        <a href="javascript:void(0);"
                            data-id="<c:out value='${current.id}' />"
                            data-user="<c:out value='${current.user}' />"
                            onClick="initDelete(this);"
                            data-bs-toggle="modal"
                            data-bs-target="#modal-delete-credentials">
                            <rhn:icon type="item-del" title="mirror-credentials.jsp.delete" />
                        </a>
                    </span>
                </div>
            </div>
        </div>
    </div>
    <script>verifyCredentials('${current.id}', false);</script>
</c:forEach>

<div class="panel panel-inactive" data-bs-toggle="modal" data-bs-target="#modal-edit-credentials">
    <div class="panel-body">
        <i class="fa fa-plus-circle"></i>
        <p>Add a new credential</p>
    </div>
</div>
