/*
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.domain.action.salt.inspect;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFormatter;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ImageInspectAction
 */
public class ImageInspectAction extends Action {

    private ImageInspectActionDetails details;

    /**
     * Return the details.
     * @return details
     */
    public ImageInspectActionDetails getDetails() {
        return details;
    }

    /**
     * Set the details.
     * @param detailsIn details
     */
    public void setDetails(ImageInspectActionDetails detailsIn) {
        if (detailsIn != null) {
            detailsIn.setParentAction(this);
        }
        this.details = detailsIn;
    }

    @Override
    public ActionFormatter getFormatter() {
        if (formatter == null) {
            formatter = new ImageInspectActionFormatter(this);
        }
        return formatter;
    }

    /**
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @param action action which has all the revisions
     * @return minion summaries grouped by local call
     */
    public static Map<LocalCall<?>, List<MinionSummary>> imageInspectAction(
            List<MinionSummary> minionSummaries, ImageInspectAction action) {

        ImageInspectActionDetails details = action.getDetails();
        if (details == null) {
            return Collections.emptyMap();
        }
        return ImageStoreFactory.lookupById(details.getImageStoreId())
                .map(store -> imageInspectAction(minionSummaries, details, store))
                .orElseGet(Collections::emptyMap);
    }

    private static Map<LocalCall<?>, List<MinionSummary>> imageInspectAction(
            List<MinionSummary> minions, ImageInspectActionDetails details, ImageStore store) {
        Map<String, Object> pillar = new HashMap<>();
        Map<LocalCall<?>, List<MinionSummary>> result = new HashMap<>();
        if (ImageStoreFactory.TYPE_OS_IMAGE.equals(store.getStoreType())) {
            pillar.put("build_id", "build" + details.getBuildActionId());
            LocalCall<Map<String, State.ApplyResult>> apply = State.apply(
                    Collections.singletonList("images.kiwi-image-inspect"),
                    Optional.of(pillar));
            result.put(apply, minions);
            return result;
        }
        else {
            List<ImageStore> imageStores = new LinkedList<>();
            imageStores.add(store);
            Map<String, Object> dockerRegistries = ImageStore.dockerRegPillar(imageStores);
            pillar.put("docker-registries", dockerRegistries);
            pillar.put("imagename", store.getUri() + "/" + details.getName() + ":" + details.getVersion());
            pillar.put("build_id", "build" + details.getBuildActionId());
            LocalCall<Map<String, State.ApplyResult>> apply = State.apply(
                    Collections.singletonList("images.profileupdate"),
                    Optional.of(pillar));
            result.put(apply, minions);
            return result;
        }
    }

}
