/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

package com.redhat.rhn.domain.org;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class provides a wrapper around an Org's trustedOrgs set to ensure that
 * operations on the set result in a bidirectional trust relationship.
 */
public class TrustSet implements Set<Org> {

    private final Org org;
    private final Set<Org> trusted;

    /**
     * Constructor
     * @param orgIn the org who is  trusted
     * @param trustedIn the set of orgs the org is trusted in
     */
    public TrustSet(Org orgIn, Set<Org> trustedIn) {
        org = orgIn;
        trusted = trustedIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(Org orgToAdd) {
        org.addTrust(orgToAdd);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(Collection<? extends Org> c) {
        for (Org o : c) {
            add(o);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        List<Org> list = new ArrayList<>(trusted);
        for (Org o : list) {
            remove(o);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object o) {
        return trusted.contains(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return trusted.containsAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return trusted.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Org> iterator() {
        return trusted.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object o) {
        if (o instanceof Org) {
            return remove((Org) o);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean remove(Org orgToRemove) {
        org.removeTrust(orgToRemove);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object o : c) {
            remove(o);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        for (Org o : trusted) {
            if (c.contains(o)) {
                continue;
            }
            remove(o);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return trusted.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        return trusted.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object[] toArray(Object[] a) {
        // NOT SUPPORTED
        return null;
    }

}
