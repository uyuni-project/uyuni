/*
 * Copyright (c) 2022 SUSE LLC
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

package com.redhat.rhn.manager.content;

import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.scc.SCCRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Class to hold the information of a product and a repository with PTF packages
 */
public class PtfProductRepositoryInfo {

    private final SUSEProduct product;

    private final SCCRepository repository;

    private final List<String> channelParts;

    private final String architecture;

    /**
     * Default constructor
     * @param productIn the SUSE product
     * @param repoIn    the repository
     * @param partsIn   the parts constructing the repository URL
     * @param archIn    the architecture
     */
    public PtfProductRepositoryInfo(SUSEProduct productIn, SCCRepository repoIn, List<String> partsIn, String archIn) {
        this.product = productIn;
        this.repository = repoIn;
        this.channelParts = Collections.unmodifiableList(partsIn);
        this.architecture = archIn;
    }

    public SUSEProduct getProduct() {
        return product;
    }

    public SCCRepository getRepository() {
        return repository;
    }

    public List<String> getChannelParts() {
        return channelParts;
    }

    public String getArchitecture() {
        return architecture;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PtfProductRepositoryInfo that = (PtfProductRepositoryInfo) o;
        return Objects.equals(product, that.product) && Objects.equals(repository,
            that.repository) && Objects.equals(channelParts, that.channelParts) && Objects.equals(
            architecture, that.architecture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, repository, channelParts, architecture);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PtfProductRepositoryInfo.class.getSimpleName() + "[", "]")
            .add("product=" + product)
            .add("repository=" + repository)
            .add("channelParts=" + channelParts)
            .add("architecture='" + architecture + "'")
            .toString();
    }
}
