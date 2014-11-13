/**
 * Copyright (c) 2014 SUSE
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
package com.suse.scc.client;

import com.redhat.rhn.domain.scc.SCCRepository;

import com.google.gson.Gson;
import com.suse.scc.model.SCCProduct;
import com.suse.scc.model.SCCSubscription;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

/**
 * SCC client that reads data from the filesystem, currently JSON files exported from SMT.
 */
public class SCCFileClient implements SCCClient {

    /** The config object. */
    private final SCCConfig config;

    /**
     * Constructor for connecting to scc.suse.com.
     * @param configIn the configuration object
     */
    public SCCFileClient(SCCConfig configIn) {
        config = configIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SCCRepository> listRepositories() throws SCCClientException {
        return getList("organizations_repositories.json",
                SCCRepository.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SCCProduct> listProducts() throws SCCClientException {
        return getList(
                "organizations_products_unscoped.json", SCCProduct.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SCCSubscription> listSubscriptions() throws SCCClientException {
        return getList("organizations_subscriptions.json",
                SCCSubscription.class);
    }

    /**
     * Returns a list from a serialized JSON file.
     *
     * @param <T> the generic type
     * @param filename the name of the file
     * @param resultType the type of the result
     * @return object of type given by resultType
     * @throws SCCClientException if I/O errors occur
     */
    private <T> List<T> getList(String filename, Type resultType)
        throws SCCClientException {
        try {
            return readJSON(filename, SCCClientUtils.toListType(resultType));
        }
        catch (IOException ex) {
            throw new SCCClientException(ex);
        }
    }

    /**
     * Read data from the file in the local directory.
     *
     * @param <T> the generic type
     * @param filename the filename
     * @param resultType the result type
     * @return object of type given by resultType
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("unchecked")
    private <T> T readJSON(String filename, Type resultType)
            throws IOException {
        return (T) new Gson().fromJson(
                new BufferedReader(new InputStreamReader(new FileInputStream(
                        new File(config.getLocalResourcePath() + "/" + filename)))),
                resultType);
    }
}
