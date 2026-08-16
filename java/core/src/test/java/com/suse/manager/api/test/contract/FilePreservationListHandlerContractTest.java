/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.api.test.contract;

import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.FilePreservationDto;
import com.redhat.rhn.frontend.xmlrpc.kickstart.filepreservation.FilePreservationListHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FilePreservationListHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "kickstart.filepreservation";
    }

    @Override
    protected Class<FilePreservationListHandler> getHandlerClass() {
        return FilePreservationListHandler.class;
    }

    private FilePreservationListHandler handler() {
        return (FilePreservationListHandler) handlerMock;
    }

    private FilePreservationDto preservationList() {
        FilePreservationDto dto = new FilePreservationDto();
        dto.setId(10L);
        dto.setLabel("network-config");
        dto.setCreated(new Date());
        dto.setModified(new Date());
        return dto;
    }

    /**
     * The serializer walks the file names collection, so an empty list keeps the fixture free
     * of persisted ConfigFileName instances while still exercising the documented array.
     *
     * @return a file list with no file names
     */
    private FileList fileList() {
        FileList list = new FileList();
        list.setId(10L);
        list.setLabel("network-config");
        list.setFileNames(List.of());
        return list;
    }

    @Test
    public void testCreate() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "network-config");
        body.put("files", List.of("/etc/hosts", "/etc/resolv.conf"));

        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with("network-config"),
                    with(List.of("/etc/hosts", "/etc/resolv.conf")));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.filepreservation/create", "POST")
                .withBody(body)
                .onHandlerMethod("create", User.class, String.class, List.class);
    }

    @Test
    public void testDelete() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).delete(with(mockUser), with("network-config"));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.filepreservation/delete", "POST")
                .withBody(Map.of("name", "network-config"))
                .onHandlerMethod("delete", User.class, String.class);
    }

    @Test
    public void testGetDetails() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDetails(with(mockUser), with("network-config"));
            will(returnValue(fileList()));
        }});

        validateApiContract("/kickstart.filepreservation/getDetails", "GET")
                .withParams(Map.of("name", new String[] {"network-config"}))
                .onHandlerMethod("getDetails", User.class, String.class);
    }

    @Test
    public void testListAllFilePreservations() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listAllFilePreservations(with(mockUser));
            will(returnValue(List.of(preservationList())));
        }});

        validateApiContract("/kickstart.filepreservation/listAllFilePreservations", "GET")
                .onHandlerMethod("listAllFilePreservations", User.class);
    }
}
