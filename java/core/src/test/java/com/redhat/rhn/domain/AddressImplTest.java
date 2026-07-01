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
package com.redhat.rhn.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.user.Address;
import com.redhat.rhn.domain.user.AddressImpl;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.Test;

/**
 * Tests for AddressImpl
 */
public class AddressImplTest extends RhnBaseTestCase {

    @Test
    public void testEqualsAndHashCode() {
        AddressImpl address1 = new AddressImpl();
        address1.setId(1L);
        address1.setAddress1("Praça Gil Vicente 6");
        address1.setCity("Almada");
        address1.setCountry("PT");

        AddressImpl address2 = new AddressImpl();
        address2.setId(1L);
        address2.setAddress1("Av. Rei Humberto II de Itália");
        address2.setCity("Cascais");
        address2.setCountry("PT");

        AddressImpl address3 = new AddressImpl();
        address3.setAddress1("Praça Gil Vicente 6");
        address3.setCity("Almada");
        address3.setCountry("PT");

        // Sharing an ID does not make two addresses equal
        assertNotEquals(address1, address2);

        // Addresses with different IDs can still be equal.
        assertEquals(address1, address3);

        // HashCode should be consistent
        assertEquals(address1.hashCode(), address3.hashCode());
    }

    /**
     * Tests that an Address can be persisted and retrieved correctly,
     * and that all fields are saved and loaded as expected.
     */
    @Test
    public void testPersistAddress() {
        User user = UserTestUtils.createUser();

        AddressImpl address = new AddressImpl();
        address.setAddress1("Av. João Dias Mourinha 82");
        address.setAddress2("2E");
        address.setCity("Trafaria");
        address.setState("Almada City");
        address.setCountry("PT");
        address.setZip("2825-897");
        address.setPhone("00351 21 123 4567");
        address.setEmail("teste@suse.com");
        address.setFax("211234567");
        address.setIsPoBox(true);

        user.setAddress(address);
        address.setUser(user);

        UserFactory.save(user);
        TestUtils.flushAndClearSession();

        User reloaded = UserFactory.lookupById(user.getId());
        Address reloadedAddress = reloaded.getAddress();

        assertNotNull(reloadedAddress);
        assertNotNull(reloadedAddress.getId());
        assertEquals("Av. João Dias Mourinha 82", reloadedAddress.getAddress1());
        assertEquals("2E", reloadedAddress.getAddress2());
        assertEquals("Trafaria", reloadedAddress.getCity());
        assertEquals("Almada City", reloadedAddress.getState());
        assertEquals("PT", reloadedAddress.getCountry());
        assertEquals("2825-897", reloadedAddress.getZip());
        assertEquals("00351 21 123 4567", reloadedAddress.getPhone());
        assertEquals("teste@suse.com", ((AddressImpl) reloadedAddress).getEmail());
        assertEquals("211234567", reloadedAddress.getFax());
        assertTrue(reloadedAddress.isPoBox());
    }

    /**
     * Tests that an Address with null optional fields can be persisted and retrieved correctly.
     */
    @Test
    public void testPersistAddressWithNullOptionalFields() {
        User user = UserTestUtils.createUser();

        AddressImpl address = new AddressImpl();
        address.setAddress1("Rua Professor Fernando da Fonseca");
        address.setCity("Lisboa");
        address.setCountry("PT");
        // Leave address2, state, zip, phone, fax as null

        user.setAddress(address);
        address.setUser(user);

        UserFactory.save(user);
        TestUtils.flushAndClearSession();

        User reloaded = UserFactory.lookupById(user.getId());
        Address reloadedAddress = reloaded.getAddress();

        assertNotNull(reloadedAddress);
        assertEquals("Rua Professor Fernando da Fonseca", reloadedAddress.getAddress1());
        assertEquals("Lisboa", reloadedAddress.getCity());
        assertEquals("PT", reloadedAddress.getCountry());
        assertNull(reloadedAddress.getAddress2());
        assertNull(reloadedAddress.getState());
        assertNull(reloadedAddress.getZip());
        assertNull(reloadedAddress.getPhone());
        assertNull(reloadedAddress.getFax());
        assertNull(((AddressImpl) reloadedAddress).getEmail());
        assertFalse(reloadedAddress.isPoBox());
    }

    /**
     * Tests that updating an existing Address works correctly and that the same
     * database record is updated rather than creating a new one.
     */
    @Test
    public void testUpdateAddress() {
        User user = UserTestUtils.createUser();

        AddressImpl address = new AddressImpl();
        address.setAddress1("Esplanada Dom Carlos I");
        address.setCity("Lisboa");
        address.setCountry("PT");

        user.setAddress(address);
        address.setUser(user);

        UserFactory.save(user);
        HibernateFactory.getSession().flush();
        Long addressId = address.getId();

        // Update the address
        address.setAddress1("Praça do Comércio 99");
        address.setCity("Lisboa");
        address.setZip("1100-148");

        UserFactory.save(user);
        TestUtils.flushAndClearSession();

        User reloaded = UserFactory.lookupById(user.getId());
        Address reloadedAddress = reloaded.getAddress();

        assertNotNull(reloadedAddress);
        assertEquals(addressId, reloadedAddress.getId()); // Same ID - updated, not recreated
        assertEquals("Praça do Comércio 99", reloadedAddress.getAddress1());
        assertEquals("Lisboa", reloadedAddress.getCity());
        assertEquals("1100-148", reloadedAddress.getZip());
    }

    /**
     * Tests that removing the association between a User and an Address does not delete
     * the Address from the database, since cascade delete is not configured.
     */
    @Test
    public void testCascadeDeleteAddress() {
        User user = UserTestUtils.createUser();

        AddressImpl address = new AddressImpl();
        address.setAddress1("Avenida da Liberdade 110");
        address.setCity("Lisboa");
        address.setCountry("PT");

        user.setAddress(address);
        address.setUser(user);

        UserFactory.save(user);
        HibernateFactory.getSession().flush();
        Long addressId = address.getId();
        Long userId = user.getId();

        // Remove address from user
        user.setAddress(null);
        UserFactory.save(user);
        TestUtils.flushAndClearSession();

        User reloaded = UserFactory.lookupById(userId);
        assertNull(reloaded.getAddress());

        // Address should still exist in DB (no cascade delete configured)
        AddressImpl orphanedAddress = HibernateFactory.getSession().find(AddressImpl.class, addressId);
        assertNotNull(orphanedAddress);
    }
}
