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
package com.suse.utils.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class EntityAssociationHelperTest {

    @Test
    @DisplayName("Reconciles set by removing orphans and adding missing children")
    void reconcilesSetByRemovingOrphansAndAddingMissingChildren() {
        Parent parent = new Parent("p1");
        Parent otherParent = new Parent("p2");

        Child keep = new Child("keep");
        keep.setParent(parent);
        Child remove = new Child("remove");
        remove.setParent(parent);

        Set<Child> managed = new HashSet<>(Set.of(keep, remove));

        Child add = new Child("add");
        Child keepEquivalent = new Child("keep");

        Set<Child> desired = new HashSet<>(Set.of(keepEquivalent, add));

        EntityAssociationHelper.reconcile(parent, managed, desired, Child::setParent);

        assertEquals(2, managed.size());
        assertTrue(managed.contains(keep));
        assertTrue(managed.contains(add));
        assertFalse(managed.contains(remove));
        assertEquals(parent, keep.getParent());
        assertEquals(parent, add.getParent());
        assertNull(remove.getParent());

        // Not added to managed, so helper must not mutate this equal but detached instance.
        keepEquivalent.setParent(otherParent);
        EntityAssociationHelper.addMember(parent, managed, keepEquivalent, Child::setParent);
        assertEquals(otherParent, keepEquivalent.getParent());
    }

    @Test
    @DisplayName("Clears managed set when desired children are null")
    void clearsManagedSetWhenDesiredChildrenAreNull() {
        Parent parent = new Parent("p1");
        Child c1 = new Child("c1");
        Child c2 = new Child("c2");
        c1.setParent(parent);
        c2.setParent(parent);

        Set<Child> managed = new HashSet<>(Set.of(c1, c2));

        EntityAssociationHelper.reconcile(parent, managed, null, Child::setParent);

        assertTrue(managed.isEmpty());
        assertNull(c1.getParent());
        assertNull(c2.getParent());
    }

    @Test
    @DisplayName("Adds child and assigns parent reference")
    void addsChildAndAssignsParentReference() {
        Parent parent = new Parent("p1");
        Set<Child> managed = new HashSet<>();
        Child child = new Child("c1");

        EntityAssociationHelper.addMember(parent, managed, child, Child::setParent);

        assertTrue(managed.contains(child));
        assertEquals(parent, child.getParent());
    }

    @Test
    @DisplayName("Removes child and clears parent reference")
    void removesChildAndClearsParentReference() {
        Parent parent = new Parent("p1");
        Child child = new Child("c1");
        child.setParent(parent);
        Set<Child> managed = new HashSet<>(Set.of(child));

        EntityAssociationHelper.removeMember(managed, child, Child::setParent);

        assertFalse(managed.contains(child));
        assertNull(child.getParent());
    }

    @Test
    @DisplayName("Clears set and nulls all parent references")
    void clearsSetAndNullsAllParentReferences() {
        Parent parent = new Parent("p1");
        Child c1 = new Child("c1");
        Child c2 = new Child("c2");
        c1.setParent(parent);
        c2.setParent(parent);

        Set<Child> managed = new HashSet<>(Set.of(c1, c2));

        EntityAssociationHelper.clear(managed, Child::setParent);

        assertTrue(managed.isEmpty());
        assertNull(c1.getParent());
        assertNull(c2.getParent());
    }

    @Test
    @DisplayName("Throws NullPointerException for required null arguments")
    void throwsNullPointerExceptionForRequiredNullArguments() {
        Parent parent = new Parent("p1");
        Set<Child> managed = new HashSet<>();
        Child child = new Child("c1");

        NullPointerException e1 = assertThrows(NullPointerException.class,
            () -> EntityAssociationHelper.reconcile(null, managed, Set.of(), Child::setParent));
        assertEquals("Parent entity must not be null", e1.getMessage());

        NullPointerException e2 = assertThrows(NullPointerException.class,
            () -> EntityAssociationHelper.addMember(parent, null, child, Child::setParent));
        assertEquals("Managed children set must not be null", e2.getMessage());

        NullPointerException e3 = assertThrows(NullPointerException.class,
            () -> EntityAssociationHelper.removeMember(managed, child, null));
        assertEquals("Parent reference setter must not be null", e3.getMessage());

        NullPointerException e4 = assertThrows(NullPointerException.class,
            () -> EntityAssociationHelper.clear(null, Child::setParent));
        assertEquals("Managed children set must not be null", e4.getMessage());
    }

    private static final class Parent {
        private final String id;

        private Parent(String idIn) {
            id = idIn;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Parent other)) {
                return false;
            }
            return id.equals(other.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    private static final class Child {
        private final String key;
        private Parent parent;

        private Child(String keyIn) {
            key = keyIn;
        }

        private Parent getParent() {
            return parent;
        }

        private void setParent(Parent parentIn) {
            parent = parentIn;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Child other)) {
                return false;
            }
            return key.equals(other.key);
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }
}
