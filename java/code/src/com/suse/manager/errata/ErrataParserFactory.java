/*
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
package com.suse.manager.errata;

import com.redhat.rhn.domain.errata.Errata;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Factory to create concrete instances of {@link VendorSpecificErrataParser}.
 */
public final class ErrataParserFactory {

    /**
     * All supported vendors
     */
    private enum SupportedVendor {
        ALIBABA("alicloud-linux-os@service.aliyun.com", AlibabaErrataParser::new),
        ALMALINUX("packager@almalinux.org", AlmaLinuxErrataParser::new),
        AMAZON("linux-security@amazon.com", AmazonErrataParser::new),
        ORACLE("el-errata@oss.oracle.com", OracleErrataParser::new),
        REDHAT("release-engineering@redhat.com", RedhatErrataParser::new),
        ROCKYLINUX("releng@rockylinux.org", RockyLinuxErrataParser::new),
        SUSE_RES("res-maintenance@suse.de", SUSERESErrataParser::new),
        SUSE("maint-coord@suse.de", SUSEErrataParser::new);

        /** Email used in the errata */
        private final String vendorEmail;

        /** Supplier that builds the parser instance */
        private final Supplier<VendorSpecificErrataParser> parserSupplier;

        SupportedVendor(String vendorEmailIn, Supplier<VendorSpecificErrataParser> parserSupplierIn) {
            this.vendorEmail = vendorEmailIn;
            this.parserSupplier = parserSupplierIn;
        }

        /**
         * The vendor email used in the errata.
         *
         * @return the vendor email.
         */
        public String getVendorEmail() {
            return vendorEmail;
        }

        /**
         * Builds an instance of the parser specific for this vendor
         *
         * @return a concrete instance of {@link VendorSpecificErrataParser}.
         */
        public VendorSpecificErrataParser getParser() {
            return parserSupplier.get();
        }
    }

    private ErrataParserFactory() {
        // Prevent instantiation
    }

    /**
     * Returns a parser that is specific for the given errata, depending on the vendor.
     *
     * @param errata the errata
     * @return a parse for extracting vendor specific information
     *
     * @throws ErrataParsingException when it's not possible to build a parse for the errata.
     */
    public static VendorSpecificErrataParser getParser(Errata errata) throws ErrataParsingException {
        final String errataEmail = Optional.ofNullable(errata)
                                           .map(Errata::getErrataFrom)
                                           .map(StringUtils::trimToNull)
                                           .orElseThrow(() -> new ErrataParsingException(
                                                   "Unable identify the vendor to parse the errata"));

        for (SupportedVendor vendor : SupportedVendor.values()) {
            if (errataEmail.equals(vendor.getVendorEmail())) {
                return vendor.getParser();
            }
        }

        throw new ErrataParsingException("Unable identify the vendor to parse the errata");
    }
}
