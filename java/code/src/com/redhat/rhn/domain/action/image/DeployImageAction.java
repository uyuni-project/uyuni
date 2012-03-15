package com.redhat.rhn.domain.action.image;

import com.redhat.rhn.domain.action.Action;

public class DeployImageAction extends Action {

	private static final long serialVersionUID = 1438261396065921002L;
	private DeployImageActionDetails details;

	public DeployImageActionDetails getDetails() {
		return details;
	}

	public void setDetails(DeployImageActionDetails details) {
		this.details = details;
	}
}
