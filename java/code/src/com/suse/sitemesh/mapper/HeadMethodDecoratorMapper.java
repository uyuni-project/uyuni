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
package com.suse.sitemesh.mapper;

import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.mapper.AbstractDecoratorMapper;

import javax.servlet.http.HttpServletRequest;

/**
 * Sitemesh Decorator Mapper to avoid apply decorator in head request method
 */
public class HeadMethodDecoratorMapper extends AbstractDecoratorMapper {

    /**
     * Empty constructor
     */
    public HeadMethodDecoratorMapper() {
    }

    /**
     * Decorator resolver which skips decorator apply to HEAD request method
     *
     * @param request
     * @param page
     * @return if request is HEAD returns null, otherwise delegates in the next decorator mapper
     */
    @Override
    public Decorator getDecorator(HttpServletRequest request, Page page) {
        if (request.getMethod().equalsIgnoreCase("HEAD")) {
            return null;
        }
        return super.getDecorator(request, page);
    }
}
