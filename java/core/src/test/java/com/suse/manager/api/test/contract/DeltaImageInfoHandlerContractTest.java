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

import com.redhat.rhn.domain.image.DeltaImageInfo;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.image.DeltaImageInfoHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeltaImageInfoHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "image.delta";
    }

    @Override
    protected Class<DeltaImageInfoHandler> getHandlerClass() {
        return DeltaImageInfoHandler.class;
    }

    private DeltaImageInfoHandler handler() {
        return (DeltaImageInfoHandler) handlerMock;
    }

    /**
     * The serializer dereferences both image infos and the pillar, so all three have to be
     * present on the fixture.
     *
     * @return a delta image serialized by DeltaImageSerializer
     */
    private DeltaImageInfo deltaImage() {
        ImageInfo source = new ImageInfo();
        source.setId(1L);
        ImageInfo target = new ImageInfo();
        target.setId(2L);

        Map<String, Object> pillarData = new LinkedHashMap<>();
        pillarData.put("size", 1024L);

        DeltaImageInfo delta = new DeltaImageInfo();
        delta.setSourceImageInfo(source);
        delta.setTargetImageInfo(target);
        delta.setFile("/srv/delta/1-2.tar");
        delta.setPillar(new Pillar("delta", pillarData));
        return delta;
    }

    @Test
    public void testCreateDeltaImage() throws Exception {
        Map<String, Object> pillar = new LinkedHashMap<>();
        pillar.put("size", "1024");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sourceImageId", 1);
        body.put("targetImageId", 2);
        body.put("file", "/srv/delta/1-2.tar");
        body.put("pillar", pillar);

        context.checking(new Expectations() {{
            oneOf(handler()).createDeltaImage(with(mockUser), with(1), with(2),
                    with("/srv/delta/1-2.tar"), with(pillar));
            will(returnValue(1L));
        }});

        validateApiContract("/image.delta/createDeltaImage", "POST")
                .withBody(body)
                .onHandlerMethod("createDeltaImage", User.class, Integer.class, Integer.class,
                        String.class, Map.class);
    }

    @Test
    public void testGetDetails() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDetails(with(mockUser), with(1), with(2));
            will(returnValue(deltaImage()));
        }});

        validateApiContract("/image.delta/getDetails", "GET")
                .withParams(Map.of("sourceImageId", new String[] {"1"},
                        "targetImageId", new String[] {"2"}))
                .onHandlerMethod("getDetails", User.class, Integer.class, Integer.class);
    }

    @Test
    public void testListDeltas() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listDeltas(with(mockUser));
            will(returnValue(List.of(deltaImage())));
        }});

        validateApiContract("/image.delta/listDeltas", "GET")
                .onHandlerMethod("listDeltas", User.class);
    }
}
