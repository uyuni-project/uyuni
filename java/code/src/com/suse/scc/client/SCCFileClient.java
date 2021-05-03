/**
 * Copyright (c) 2014 SUSE LLC
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

import com.redhat.rhn.manager.content.ProductTreeEntry;

import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.scc.model.SCCOrderJson;
import com.suse.scc.model.SCCProductJson;
import com.suse.scc.model.SCCRegisterSystemJson;
import com.suse.scc.model.SCCRepositoryJson;
import com.suse.scc.model.SCCSubscriptionJson;
import com.suse.scc.model.SCCSystemCredentialsJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
    public List<SCCRepositoryJson> listRepositories() throws SCCClientException {
        return getList("organizations_repositories.json",
                SCCRepositoryJson.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SCCProductJson> listProducts() throws SCCClientException {
        return getList(
                "organizations_products_unscoped.json", SCCProductJson.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SCCSubscriptionJson> listSubscriptions() throws SCCClientException {
        return getList("organizations_subscriptions.json",
                SCCSubscriptionJson.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SCCOrderJson> listOrders() throws SCCClientException {
        return getList("organizations_orders.json",
                SCCOrderJson.class);
    }

    @Override
    public List<ProductTreeEntry> productTree() throws SCCClientException {
        return getList("product_tree.json",
                ProductTreeEntry.class);
    }

    @Override
    public SCCSystemCredentialsJson createSystem(SCCRegisterSystemJson system, String username, String password)
            throws SCCClientException {
        return null;
    }

    @Override
    public void deleteSystem(long id, String username, String password) throws SCCClientException {
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
        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .create();
        return (T) gson.fromJson(
                new BufferedReader(new InputStreamReader(new FileInputStream(
                        new File(config.getLocalResourcePath() + "/" + filename)))),
                resultType);
    }
}
