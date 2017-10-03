/**
 * Copyright (c) 2017 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.webui.controllers;

import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static spark.Spark.halt;

/**
 * Controller class providing backend code for the systems page.
 */
public class SystemsController {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(SystemsController.class);

    public static String delete(Request request, Response response, User user) {
        String sidStr = request.params("sid");
        String noclean = request.queryParams("noclean");
        long sid;
        try {
            sid = Long.parseLong(sidStr);
        }
        catch (NumberFormatException e) {
            halt(500, e.getMessage());
            return null;
        }
        Server server = ServerFactory.lookupById(sid);

        boolean sshPush = Stream.of(
                ServerFactory.findContactMethodByLabel(ContactMethodUtil.SSH_PUSH),
                ServerFactory.findContactMethodByLabel(ContactMethodUtil.SSH_PUSH_TUNNEL)
        ).anyMatch(cm -> server.getContactMethod().equals(cm));

        Optional<String> cleanupResult = Optional.empty();
        if (server.asMinionServer().isPresent() && sshPush) {
            cleanupResult = syncSSHCleanup(server.asMinionServer().get());
            if (cleanupResult.isPresent()) {
                return cleanupResult.get();
            }
        }

        if (server.hasEntitlement(EntitlementManager.MANAGEMENT)) {
            // But what if this system is in some other user's RhnSet???
            RhnSet set = RhnSetDecl.SYSTEMS.get(user);

            // Remove from SSM if required
            if (set.getElementValues().contains(sid)) {
                set.removeElement(sid);
                RhnSetManager.store(set);
            }
        }

        try {
            // Now we can remove the system
            SystemManager.deleteServer(user, sid);
//            createSuccessMessage(request, "message.serverdeleted.param",
//                    sid.toString());
        }
        catch (RuntimeException e) {
            if (e.getMessage().contains("cobbler")) {
//                createErrorMessage(request, "message.servernotdeleted_cobbler",
//                        sid.toString());
            }
            else {
//                createErrorMessage(request, "message.servernotdeleted", sid.toString());
                throw e;
            }
        }
        return json(response, "OK");
    }

    public static Optional<String> syncSSHCleanup(MinionServer minion) {
        CompletableFuture timeOut = SaltService.INSTANCE.failAfter(300); // 5 mins

        try {

            Map<String, CompletionStage<Result<Map<String, State.ApplyResult>>>> res =
                SaltService.INSTANCE.completableAsyncCall(
                        State.apply("ssh_cleanup"),
                        new MinionList(minion.getMinionId()),
                        timeOut
                        );
            CompletionStage<Result<Map<String, State.ApplyResult>>> future = res.get(minion.getMinionId());
            if (future == null) {
                // TODO err
            }

            return future.handle((applyResult, err) -> {
                if (applyResult != null) {
                    return applyResult.fold((saltErr) -> {
                        return saltErr.fold(
                                fn -> Optional.of("Function not available: " + fn.getFunctionName()),
                                fn -> Optional.of(""),
                                fn -> Optional.of(""),
                                fn -> Optional.of("")
                                );
                        },
                        (saltRes) -> {
                            if (!saltRes.get("").isResult()) {
                                return Optional.of("");
                            }
                            return Optional.<String>empty();
                        }
                    );
                } else {
                    return Optional.of("err");
                }
            }).toCompletableFuture().get();

        } catch (SaltException|InterruptedException|ExecutionException e) {
            LOG.error("Error applying state ssh_cleanup", e);
            return Optional.of(e.getMessage());
        }

    }


}
