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
package com.suse.manager.webui.services;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.manager.configuration.SaltConfigurable;
import com.suse.manager.webui.utils.gson.StateSourceDto;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Service class to get state inheritance information
 */
public class StateSourceService {
    private StateSourceService() { }

    /**
     * Collect a summary of all the Salt states (and formulas) assigned or inherited to a system, and their origin
     * information
     *
     * @param minion the Salt system
     * @return a list of {@link StateSourceDto} objects; one for each state assigned or inherited
     */
    public static List<StateSourceDto> getSystemStateSources(MinionServer minion) {
        // Keep track of the processed states to avoid listing same states from multiple sources
        Set<ConfigChannel> processedStates = new HashSet<>();
        Set<String> processedFormulas = new HashSet<>();

        // Index formulas for anchors
        List<String> activeFormulas = FormulaFactory.getCombinedFormulasByServerId(minion.getId());
        Map<String, Integer> formulaIndex = IntStream.range(0, activeFormulas.size()).boxed()
                .collect(Collectors.toMap(activeFormulas::get, i -> i));

        // System states
        Stream<StateSourceDto> stateOrigins = StateFactory.latestConfigChannels(minion).stream()
                .flatMap(c -> getStateSources(c, minion, processedStates));

        // System formulas
        stateOrigins = Stream.concat(stateOrigins, getFormulaSources(formulaIndex,
                FormulaFactory.getFormulasByMinionId(minion.getMinionId()), minion, processedFormulas));

        // Group states
        stateOrigins = Stream.concat(stateOrigins, minion.getGroups().stream()
                .flatMap(g -> StateFactory.latestConfigChannels(g).stream()
                        .flatMap(c -> getStateSources(c, g, processedStates))));

        // Group formulas
        stateOrigins = Stream.concat(stateOrigins, minion.getGroups().stream()
                .flatMap(g -> getFormulaSources(formulaIndex,
                        FormulaFactory.getFormulasByGroupId(g.getId()), g, processedFormulas)));

        // Org states
        stateOrigins = Stream.concat(stateOrigins, StateFactory.latestConfigChannels(minion.getOrg())
                .stream().flatMap(c -> getStateSources(c, minion.getOrg(), processedStates)));

        // Internal states
        stateOrigins = Stream.concat(stateOrigins, Stream.of(StateSourceDto.internalState()));

        return stateOrigins.collect(Collectors.toList());
    }

    private static Stream<StateSourceDto> getStateSources(List<ConfigChannel> channels, SaltConfigurable source,
            Set<ConfigChannel> processed) {
        return channels.stream()
                .filter(c -> !processed.contains(c))
                .map(c -> {
                    processed.add(c);
                    return StateSourceDto.sourceFrom(c, source);
                });
    }

    private static Stream<StateSourceDto> getFormulaSources(Map<String, Integer> formulaIndex, List<String> formulas,
            SaltConfigurable source, Set<String> processed) {
        return formulas.stream()
                .filter(c -> !processed.contains(c))
                .map(c -> {
                    processed.add(c);
                    return StateSourceDto.sourceFrom(formulaIndex.get(c), c, source);
                });
    }
}
