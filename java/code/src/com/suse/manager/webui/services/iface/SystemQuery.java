/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.webui.services.iface;

import com.redhat.rhn.common.NoWheelResultsException;
import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.client.ClientCertificate;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.webui.services.impl.runner.MgrK8sRunner;
import com.suse.manager.webui.utils.salt.custom.MgrActionChains;
import com.suse.manager.webui.utils.salt.custom.PkgProfileUpdateSlsResult;
import com.suse.salt.netapi.calls.RunnerCall;
import com.suse.salt.netapi.calls.modules.Config;
import com.suse.salt.netapi.calls.modules.Event;
import com.suse.salt.netapi.calls.modules.Grains;
import com.suse.salt.netapi.calls.modules.Match;
import com.suse.salt.netapi.calls.modules.SaltUtil;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.Zypper;
import com.suse.salt.netapi.calls.runner.Jobs;
import com.suse.salt.netapi.calls.wheel.Key;
import com.suse.salt.netapi.datatypes.target.Glob;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.errors.GenericError;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.CmdResult;
import com.suse.salt.netapi.results.Result;

import com.google.gson.reflect.TypeToken;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Interface containing methods for directly interacting and getting information from a system.
 * Note: This interface should be split up further at some point.
 */
public class SystemQuery {

    private static final Logger LOG = LogManager.getLogger(SystemQuery.class);

    /**
     * Enum of all the available status for Salt keys.
     */
    public enum KeyStatus {
        ACCEPTED, DENIED, UNACCEPTED, REJECTED
    }

    private static final int DELAY_TIME_SECONDS = 5;
    private SaltApi saltApi;

    /**
     * Constructor
     * @param saltApiIn
     */
    public SystemQuery(SaltApi saltApiIn)  {
        this.saltApi = saltApiIn;
    }

    /**
     * Return show highstate result.
     * @param minionId of the target minion.
     * @return show highstate result.
     * @throws SaltException if anything goes wrong.
     */
    public Map<String, Result<Object>> getShowHighstate(String minionId) throws SaltException {
        return saltApi.callSync(com.suse.salt.netapi.calls.modules.State.showHighstate(), new MinionList(minionId));
    }

    /**
     * Send notification about a system id to be generated.
     * @param minion target minion.
     * @throws InstantiationException if signature generation fails
     * @throws SaltException if anything goes wrong.
     */
    public void notifySystemIdGenerated(MinionServer minion) throws InstantiationException, SaltException {
        ClientCertificate cert = SystemManager.createClientCertificate(minion);
        Map<String, Object> data = new HashMap<>();
        data.put("data", cert.toString());
        saltApi.callAsync(
                Event.fire(data, "suse/systemid/generated"),
                new MinionList(minion.getMinionId())
        );
    }

    /**
     * Query product information.
     * @param minionId of the target minion.
     * @return product information
     */
    public Optional<List<Zypper.ProductInfo>> getProducts(String minionId) {
        return saltApi.callSync(Zypper.listProducts(false), minionId);
    }


    /**
     * Get redhat product information
     * @param minionId id of the target minion
     * @return redhat product information
     */
    public Optional<RedhatProductInfo> getRedhatProductInfo(String minionId) {
        return saltApi.callSync(State.apply(Collections.singletonList("packages.redhatproductinfo"),
                Optional.empty()), minionId)
                .map(result -> {
                    if (result.isEmpty()) {
                        return new RedhatProductInfo();
                    }
                    Optional<String> oracleReleaseContent = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_ORACLE_RELEASE)
                            .getChanges(CmdResult.class).getStdout());
                    Optional<String> centosReleaseContent = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_CENTOS_RELEASE)
                            .getChanges(CmdResult.class).getStdout());
                    Optional<String> rhelReleaseContent = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_REDHAT_RELEASE)
                            .getChanges(CmdResult.class).getStdout());
                    Optional<String> alibabaReleaseContent = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_ALIBABA_RELEASE)
                            .getChanges(CmdResult.class).getStdout());
                    Optional<String> almaReleaseContent = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_ALMA_RELEASE)
                            .getChanges(CmdResult.class).getStdout());
                    Optional<String> amazonReleaseContent = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_AMAZON_RELEASE)
                            .getChanges(CmdResult.class).getStdout());
                    Optional<String> rockyReleaseContent = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_ROCKY_RELEASE)
                            .getChanges(CmdResult.class).getStdout());
                    Optional<String> whatProvidesRes = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_WHATPROVIDES_SLES_RELEASE)
                            .getChanges(CmdResult.class).getStdout());

                    return new RedhatProductInfo(centosReleaseContent, rhelReleaseContent,
                            oracleReleaseContent, alibabaReleaseContent, almaReleaseContent,
                            amazonReleaseContent, rockyReleaseContent, whatProvidesRes);
                });
    }
    /**
     * Get information about all containers running in a Kubernetes cluster.
     * @param kubeconfig path to the kubeconfig file
     * @param context kubeconfig context to use
     * @return a list of containers
     */
    public Optional<List<MgrK8sRunner.Container>> getAllContainers(String kubeconfig, String context) {
        RunnerCall<MgrK8sRunner.ContainersList> call =
                MgrK8sRunner.getAllContainers(kubeconfig, context);
        return saltApi.callSync(call,
                err -> err.fold(
                    e -> {
                        LOG.error(String.format("Function [%s] not available for runner call %s.",
                                e.getFunctionName(), saltApi.callToString(call)));
                        throw new NoSuchElementException();
                    },
                    e -> {
                        LOG.error(String.format("Module [%s] not supported for runner call %s",
                                e.getModuleName(), saltApi.callToString(call)));
                        throw new NoSuchElementException();
                    },
                    e -> {
                        LOG.error(String.format("Error parsing json response from runner call %s: %s",
                                saltApi.callToString(call), e.getJson()));
                        throw new NoSuchElementException();
                    },
                    e -> {
                        LOG.error(String.format(SaltApi.Messages.GENERIC_RUNNER_ERROR.toString(),
                                saltApi.callToString(call), e.getMessage()));
                        throw new NoSuchElementException();
                    },
                    e -> {
                        LOG.error(String.format(SaltApi.Messages.SSH_RUNNER_ERROR.toString(),
                                saltApi.callToString(call), e.getMessage()));
                        throw new NoSuchElementException();
                    }
                )
        ).map(MgrK8sRunner.ContainersList::getContainers);
    }

    /**
     * Get the specified grains for a given minion.
     * @deprecated this function is too general and should be replaced by more specific functionality.
     * @param minionId id of the target minion
     * @param type  class type, result should be parsed into
     * @param grainNames list of grains names
     * @param <T> Type result should be parsed into
     * @return Optional containing the grains parsed into specified type
     */
    @Deprecated
    public <T> Optional<T> getGrains(String minionId, TypeToken<T> type, String... grainNames) {
        return saltApi.callSync(Grains.item(false, type, grainNames), minionId);
     }

    /**
     * Get the grains for a given minion.
     *
     * @deprecated this function is too general and should be replaced by more specific functionality.
     * @param minionId id of the target minion
     * @return map containing the grains
     */
    @Deprecated
    public Optional<Map<String, Object>> getGrains(String minionId) {
        return saltApi.callSync(Grains.items(false), minionId);
    }

    /**
     * Gets a minion's master hostname.
     *
     * @param minionId the minion id
     * @return the master hostname
     */
    public Optional<String> getMasterHostname(String minionId) {
        return saltApi.callSync(Config.get(Config.MASTER), minionId);
    }
    /**
     * Get the minion keys from salt with their respective status.
     *
     * @return the keys with their respective status as returned from salt
     */
    public Key.Names getKeys() {
        return saltApi.callSync(Key.listAll()).orElseThrow(NoWheelResultsException::new);
    }

    /**
     * Get the minion keys from salt with their respective status and fingerprint.
     *
     * @return the keys with their respective status and fingerprint as returned from salt
     */
    public Key.Fingerprints getFingerprints() {
        return saltApi.callSync(Key.finger("*")).orElseThrow(NoWheelResultsException::new);
    }

    /**
     * Return the result for a jobId
     *
     * @param jid the job id
     * @return map from minion to result
     */
    public Optional<Jobs.Info> getListJob(String jid) {
        return saltApi.callSync(Jobs.listJob(jid));
    }

    /**
     * Return the jobcache filtered by metadata
     *
     * @param metadata search metadata
     * @return list of running jobs
     */
    public Optional<Map<String, Jobs.ListJobsEntry>> getJobsByMetadata(Object metadata) {
        return saltApi.callSync(Jobs.listJobs(metadata));
    }

    /**
     * Return the jobcache filtered by metadata and start and end time.
     *
     * @param metadata search metadata
     * @param startTime jobs start time
     * @param endTime jobs end time
     * @return list of running jobs
     */
    public Optional<Map<String, Jobs.ListJobsEntry>> getJobsByMetadata(Object metadata,
            LocalDateTime startTime, LocalDateTime endTime) {
        return saltApi.callSync(Jobs.listJobs(metadata, startTime, endTime));
        }


    /**
     * Get pending resume information.
     * @param minionIds to target.
     * @return pending resume information.
     * @throws SaltException if anything goes wrong.
     */
    public Map<String, Result<Map<String, String>>> getPendingResume(List<String> minionIds) throws SaltException {
        return saltApi.callSync(
                MgrActionChains.getPendingResume(),
                new MinionList(minionIds));
    }

    /**
     * Returns the currently running jobs on the target
     *
     * @param target the target
     * @return list of running jobs
     */
    public Map<String, Result<List<SaltUtil.RunningInfo>>> getRunning(MinionList target) {
        try {
            return saltApi.callSync(SaltUtil.running(), target);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * Match minions synchronously using a compound matcher.
     * @param target compound matcher
     * @return list of minion ids
     */
    public List<String> getMatchCompoundSync(String target) {
        try {
            Map<String, Result<Boolean>> result =
                    saltApi.callSync(Match.compound(target, Optional.empty()), new Glob("*"));
            return result.entrySet().stream()
                    .filter(e -> e.getValue().result().isPresent() && Boolean.TRUE.equals(e.getValue().result().get()))
                    .map(Entry::getKey)
                    .collect(Collectors.toList());
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }


    /**
     * For a given minion id check if there is a key in any of the given status. If no status is given as parameter,
     * all the available status are considered.
     *
     * @param id the id to check for
     * @param statusIn array of key status to consider
     * @return true if there is a key with the given id, false otherwise
     */
    public boolean isKeyExists(String id, KeyStatus... statusIn) {
        final Set<KeyStatus> status = new HashSet<>(Arrays.asList(statusIn));
        if (status.isEmpty()) {
            status.addAll(Arrays.asList(KeyStatus.values()));
        }

        Key.Names keys = getKeys();
        return status.contains(KeyStatus.ACCEPTED) && keys.getMinions().contains(id) ||
                status.contains(KeyStatus.DENIED) && keys.getDeniedMinions().contains(id) ||
                status.contains(KeyStatus.UNACCEPTED) && keys.getUnacceptedMinions().contains(id) ||
                status.contains(KeyStatus.REJECTED) && keys.getRejectedMinions().contains(id);
    }


    /**
     * Match the given target expression asynchronously.
     * @param target the target expression
     * @param cancel  a future used to cancel waiting on return events
     * @return a map holding a {@link CompletionStage}s for each minion
     */
    public Map<String, CompletionStage<Result<Boolean>>> getMatchAsync(
            String target, CompletableFuture<GenericError> cancel) {
        try {
            return saltApi.completableAsyncCall(Match.glob(target), new Glob(target),
                    saltApi.getEventStream(), cancel).orElseGet(Collections::emptyMap);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * getMachineId
     * @param minionId
     * @return machine ID
     */
    public Optional<String> getMachineId(String minionId) {
        return getGrain(minionId, "machine_id").flatMap(grain -> {
          if (grain instanceof String) {
              return Optional.of((String) grain);
          }
          else {
              LOG.warn("Minion {} returned non string: {} as minion_id", minionId, grain);
              return Optional.empty();
          }
        });
    }

    /**
     * Get a given grain's value from a given minion.
     *
     * @param minionId id of the target minion
     * @param grain name of the grain
     * @return the grain value
     */
    private Optional<Object> getGrain(String minionId, String grain) {
        return saltApi.callSync(Grains.item(true, grain), minionId).flatMap(grains ->
           Optional.ofNullable(grains.get(grain))
        );
    }

}
