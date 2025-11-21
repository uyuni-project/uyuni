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
                <div class="text-left col-10 d-flex">
                    <span id="verify-${current.id}" class="d-flex"></span>
                    <span id="primary-${current.id}">
                        <c:if test="${current.primary}">
                            <rhn:icon type="setup-wizard-creds-primary" title="mirror-credentials.jsp.primary" />
                        </c:if>
                        <c:if test="${not current.primary}">
                            <button type="button" class="btn btn-tertiary" onClick="makePrimaryCredentials('${current.id}')">
                                <rhn:icon type="setup-wizard-creds-make-primary" title="mirror-credentials.jsp.make-primary" />
                            </button>
                        </c:if>
                    </span>
                    <button
                        type="button"
                        onClick="initSubscriptions('${current.id}')"
                        data-bs-toggle="modal"
                        data-bs-target="#modal-list-subscriptions"
                        class="btn btn-tertiary"
                    >
                        <rhn:icon type="setup-wizard-creds-subscriptions" title="mirror-credentials.jsp.subscriptions" />
                    </button>
                    <button type="button"
                        data-id="<c:out value='${current.id}' />"
                        data-user="<c:out value='${current.user}' />"
                        onClick="initEdit(this);"
                        data-bs-toggle="modal"
                        data-bs-target="#modal-edit-credentials"
                        class="btn btn-tertiary"
                    >
                        <rhn:icon type="setup-wizard-creds-edit" title="mirror-credentials.jsp.edit" />
                    </button>
                </div>
                <div class="text-right col-2">
                    <span id="delete-${current.id}">
                        <button type="button"
                            data-id="<c:out value='${current.id}' />"
                            data-user="<c:out value='${current.user}' />"
                            onClick="initDelete(this);"
                            data-bs-toggle="modal"
                            data-bs-target="#modal-delete-credentials"
                            class="btn btn-tertiary"
                        >
                            <rhn:icon type="item-del" title="mirror-credentials.jsp.delete" />
                        </button>
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
