package com.redhat.rhn.frontend.action.systems;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.action.rhnpackage.BaseSystemPackagesAction;
import com.redhat.rhn.manager.system.SystemManager;

public class NonCompliantPackageListAction extends BaseSystemPackagesAction {
	@Override
	protected DataResult getDataResult(Server server) {
		return SystemManager.listProfileForeignPackages(server.getId());
	}
}
