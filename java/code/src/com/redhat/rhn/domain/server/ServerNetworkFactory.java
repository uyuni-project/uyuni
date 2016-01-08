package com.redhat.rhn.domain.server;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import org.hibernate.Session;

/**
 * Created by matei on 1/8/16.
 */
public class ServerNetworkFactory {

    public static ServerNetAddress4 findServerNetAddress4(Long interfaceId) {
        return (ServerNetAddress4) HibernateFactory.getSession()
                .getNamedQuery("ServerNetAddress4.lookup")
                .setParameter("interface_id", interfaceId)
                .uniqueResult();
    }

    public static ServerNetAddress6 findServerNetAddress6(Long interfaceId) {
        return (ServerNetAddress6) HibernateFactory.getSession()
                .getNamedQuery("ServerNetAddress6.lookup_by_id")
                .setParameter("interface_id", interfaceId)
                .uniqueResult();
    }
}
