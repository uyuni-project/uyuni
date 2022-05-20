/**
 * Copyright (c) 2021 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.webui.controllers.admin.mappers;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;

import com.suse.manager.webui.controllers.admin.beans.PaygFullResponse;
import com.suse.manager.webui.controllers.admin.beans.PaygProperties;
import com.suse.manager.webui.controllers.admin.beans.PaygResumeResponse;
import com.suse.manager.webui.utils.ViewHelper;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class PaygResponseMappers {
    private static LocalizationService ls = LocalizationService.getInstance();

    private PaygResponseMappers() { }

    /**
     * map a list of ssh connection data objets fro database to UI objects containing only the sumary data
     * @param paygSshDataDB list of payg ssh connection data from the databse
     * @return a list of PaygResumeResponse
     */
    public static List<PaygResumeResponse> mapPaygPropertiesResumeFromDB(List<PaygSshData> paygSshDataDB) {
        return paygSshDataDB.stream()
                .map(payg -> {
                    PaygResumeResponse paygResponse = new PaygResumeResponse();
                    paygResponse.setId(payg.getId().toString());
                    paygResponse.setHost(payg.getHost());
                    paygResponse.setDescription(payg.getDescription());
                    paygResponse.setStatus(payg.getStatus());
                    paygResponse.setStatusMessage(getStatusMessage(payg));
                    paygResponse.setLastChange(ViewHelper.formatDateTimeToISO(payg.getModified()));
                    return paygResponse;
                })
                .collect(Collectors.toList());
    }

    /**
     * map all properties of a payg ss connection data from database objects to UI objects
     * @param paygSshData payg ssh connection data from database
     * @return PaygFullResponse to be returned in the UI
     */
    public static PaygFullResponse mapPaygPropertiesFullFromDB(PaygSshData paygSshData) {
        PaygFullResponse paygResponse = new PaygFullResponse();
        paygResponse.setId(paygSshData.getId().toString());
        paygResponse.setStatus(paygSshData.getStatus());
        paygResponse.setStatusMessage(getStatusMessage(paygSshData));

        paygResponse.setLastChange(ViewHelper.formatDateTimeToISO(paygSshData.getModified()));
        paygResponse.setProperties(
                new PaygProperties(paygSshData.getDescription(),
                        paygSshData.getHost(),
                        paygSshData.getPort() != null ? String.valueOf(paygSshData.getPort()) : null,
                        paygSshData.getUsername(),
                        "",
                        "",
                        "",
                        paygSshData.getBastionHost(),
                        paygSshData.getBastionPort() != null ? String.valueOf(paygSshData.getBastionPort()) : null,
                        paygSshData.getBastionUsername(),
                        "",
                        "",
                        "", false, false
                        ));
        return paygResponse;
    }

    private static String getStatusMessage(PaygSshData paygSshData) {
        switch (paygSshData.getStatus()) {
            case S:
                return ls.getMessage("taskomatic.payg.success");
            case E:
                if (StringUtils.isEmpty(paygSshData.getErrorMessage())) {
                    return ls.getMessage("taskomatic.payg.error_unknown");
                }
                else {
                    return ls.getMessage("taskomatic.payg.error_instance", paygSshData.getErrorMessage());
                }
            case P:
                return ls.getMessage("taskomatic.payg.pending");
            default:
                return "";
        }
    }

}
