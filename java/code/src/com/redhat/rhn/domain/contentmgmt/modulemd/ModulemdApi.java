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

package com.redhat.rhn.domain.contentmgmt.modulemd;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.Modules;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * modulemd Python API service
 */
public class ModulemdApi {

    private static final String MOUNT_POINT_PATH = Config.get().getString(ConfigDefaults.MOUNT_POINT);
    private static final String API_EXE = "mgr-libmod";
    public static final Gson GSON =
            new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    /**
     * Get all defined modules in a channel's metadata
     *
     * @param channel the modular channel
     * @return a map of module name to stream lists
     */
    public Map<String, ModuleStreams> getAllModulesInChannel(Channel channel) throws ModulemdApiException {
        ModulemdApiResponse res = callSync(ModulemdApiRequest.listModulesRequest(getMetadataPath(channel)));
        return res.getListModules().getModules();
    }

    /**
     * Get all modular packages in the specified source channels
     *
     * @param sources the modular source channels
     * @return a list of modular packages in NEVRA format
     */
    public List<String> getAllPackages(List<Channel> sources) throws ModulemdApiException {
        List<String> metadataPaths = getMetadataPaths(sources);
        ModulemdApiResponse res = callSync(ModulemdApiRequest.listPackagesRequest(metadataPaths));
        return res.getListPackages().getPackages();
    }

    /**
     * Get all rpms and apis from selected streams as two separate lists, resolving and enabling modular dependencies.
     *
     * @param sources the modular source channels
     * @param selectedModules the selected module streams
     * @return the response object with selected rpms, apis and module information
     * @throws ConflictingStreamsException if more then one stream for a module is selected
     * @throws ModuleNotFoundException if a selected module is not found
     */
    public ModulePackagesResponse getPackagesForModules(List<Channel> sources, List<Module> selectedModules)
            throws ModulemdApiException {
        List<String> mdPaths = getMetadataPaths(sources);

        Map<String, List<Module>> moduleMap = selectedModules.stream().collect(Collectors.groupingBy(Module::getName));
        for (Map.Entry<String, List<Module>> m : moduleMap.entrySet()) {
            if (m.getValue().size() > 1) {
                throw new ConflictingStreamsException(m.getValue().get(0), m.getValue().get(1));
            }
        }

        ModulemdApiResponse res = callSync(ModulemdApiRequest.modulePackagesRequest(mdPaths, selectedModules));
        return res.getModulePackages();
    }

    /**
     * Get 'modules.yaml' file paths for the specified sources on the server
     *
     * @param sources the modular source channels
     * @return a list of 'modules.yaml' paths
     * @throws RepositoryNotModularException if a source channel is not modular
     */
    private static List<String> getMetadataPaths(List<Channel> sources) throws RepositoryNotModularException {
        return sources.stream().map(ModulemdApi::getMetadataPath).collect(Collectors.toList());
    }

    /**
     * Get 'modules.yaml' file path for the specified source on the server
     *
     * @param channel the modular source channel
     * @return the 'modules.yaml' path
     * @throws RepositoryNotModularException if the source channel is not modular
     */
    private static String getMetadataPath(Channel channel) throws RepositoryNotModularException {
        if (!channel.isModular()) {
            throw new RepositoryNotModularException();
        }
        Modules metadata = channel.getModules();
        return new File(MOUNT_POINT_PATH, metadata.getRelativeFilename()).getAbsolutePath();
    }

    /**
     * Make a call to the mgr-libmod Python API in a subprocess with the specified request payload
     *
     * @param request the request data
     * @return the response object
     */
    private ModulemdApiResponse callSync(ModulemdApiRequest request) throws ModulemdApiException {
        ProcessBuilder pb = new ProcessBuilder(API_EXE).redirectErrorStream(true);

        try {
            Process proc = pb.start();

            // Write JSON to stdin
            BufferedWriter procWriter = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
            GSON.toJson(request, new TypeToken<ModulemdApiRequest>() { }.getType(), procWriter);
            procWriter.close();

            // Read JSON from stdout
            BufferedReader procReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            ModulemdApiResponse res = GSON.fromJson(procReader, new TypeToken<ModulemdApiResponse>() { }.getType());

            proc.waitFor();

            // Handle possible errors
            if (res.isError()) {
                switch (res.getErrorCode()) {
                    case ModulemdApiResponse.CONFLICTING_STREAMS:
                        List<Module> conflictingModules = res.getData().getStreams();
                        throw new ConflictingStreamsException(conflictingModules.get(0), conflictingModules.get(1));
                    case ModulemdApiResponse.MODULE_NOT_FOUND:
                        throw new ModuleNotFoundException(res.getData().getStreams());
                    case ModulemdApiResponse.DEPENDENCY_RESOLUTION_ERROR:
                        throw new ModuleDependencyException(res.getData().getStreams());
                    default:
                        throw new ModulemdApiException(String.format("%s (%s)", res.getException(),
                                res.getErrorCode()));
                }
            }
            return res;

        }
        catch (IOException | InterruptedException e) {
            throw new ModulemdApiException(e);
        }
    }
}
