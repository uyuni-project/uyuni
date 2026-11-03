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
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.common.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

/**
 * Mybatis data source factory backed by a C3P0 Pooled Data source
 */
public class C3P0DataSourceFactory extends UnpooledDataSourceFactory {

    /**
     * Default constructor.
     */
    public C3P0DataSourceFactory() {
        this.dataSource = new ComboPooledDataSource();
    }
}
