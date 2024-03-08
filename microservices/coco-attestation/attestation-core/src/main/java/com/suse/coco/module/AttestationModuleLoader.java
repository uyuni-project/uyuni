/*
 * Copyright (c) 2024 SUSE LLC
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

package com.suse.coco.module;

import com.suse.coco.modules.AttestationModule;
import com.suse.coco.modules.AttestationWorker;
import com.suse.common.database.DatabaseSessionFactory;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Load {@link AttestationModule} from the classpath using Java {@link ServiceLoader} mechanism.
 */
public class AttestationModuleLoader {

    private static final Logger LOGGER = LogManager.getLogger(AttestationModuleLoader.class);

    private final Map<Integer, AttestationModule> workerFactoriesMap;

    /**
     * Default constructor.
     */
    public AttestationModuleLoader() {
        this.workerFactoriesMap = new HashMap<>();
    }

    /**
     * Load all the available implementations of {@link AttestationModule} from the classpath.
     *
     * @return the number of loaded attestation modules.
     */
    public long loadModules() {
        workerFactoriesMap.clear();

        Configuration mybatisConfig = DatabaseSessionFactory.getSessionFactory().getConfiguration();

        ServiceLoader.load(AttestationModule.class).forEach(module -> {
            // Loads additional Mybatis mapper from the attestation module, if any
            loadModuleMappers(mybatisConfig, module);

            workerFactoriesMap.put(module.getSupportedType(), module);
        });

        return workerFactoriesMap.size();
    }

    /**
     * Load Mybatis mappers into the current configuration
     *
     * @param config the current mybatis configuration
     * @param module the attestation module
     */
    private static void loadModuleMappers(Configuration config, AttestationModule module) {
        for (String resourceName : module.getAdditionalMappers()) {
            try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(resourceName)) {
                if (inputStream == null) {
                    throw new NullPointerException("Unable to find resource");
                }

                String qualifiedName = module.getName() + ":" + resourceName;
                var parser = new XMLMapperBuilder(inputStream, config, qualifiedName, config.getSqlFragments());
                parser.parse();
            }
            catch (Exception ex) {
                LOGGER.warn("Unable to load mappers from {}", resourceName, ex);
            }
        }
    }

    /**
     * Get the supported result types, aggregated from every {@link AttestationModule} loaded.
     * @return the set of supported result types
     */
    public Set<Integer> getSupportedResultTypes() {
        return Collections.unmodifiableSet(workerFactoriesMap.keySet());
    }

    /**
     * Create a worker to process an attestation result of the given type
     * @param resultType the result type of the attestation result under processing
     * @return a {@link AttestationWorker} implementation suitable for the result type
     */
    public AttestationWorker createWorker(int resultType) {
        AttestationModule attestationModule = workerFactoriesMap.get(resultType);
        if (attestationModule == null) {
            throw new IllegalArgumentException("Unsupported result type " + resultType);
        }

        return attestationModule.getWorker();
    }

}
