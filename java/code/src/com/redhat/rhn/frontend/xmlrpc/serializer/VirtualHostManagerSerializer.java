package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;
import org.apache.commons.lang.StringUtils;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

import java.io.IOException;
import java.io.Writer;

/**
 * todo javadoc
 */
public class VirtualHostManagerSerializer extends RhnXmlRpcCustomSerializer {


    @Override
    public Class getSupportedClass() {
        return VirtualHostManagerSerializer.class;
    }

    @Override
    protected void doSerialize(Object obj, Writer output, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        VirtualHostManager manager = (VirtualHostManager) obj;
        SerializerHelper helper = new SerializerHelper(serializer);

        helper.add("id", manager.getId());
        helper.add("label", manager.getLabel());
        helper.add("gathererModule", manager.getLabel());
        if (manager.getCredentials() != null
                && !StringUtils.isEmpty(manager.getCredentials().getUsername())) {
            helper.add("user", manager.getCredentials().getUsername()); // todo externalize
        }

        populateDetails(helper, manager);
        helper.writeTo(output);
    }

    private void populateDetails(SerializerHelper helper, VirtualHostManager manager) {
        if (manager.getConfigs() != null) {
            manager.getConfigs().stream().forEach(
                    config -> helper.add(config.getParameter(), config.getValue())
            );
        }
    }
}
