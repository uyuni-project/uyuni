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

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Utility methods for mutating bidirectional entity associations in place.
 * <p>
 * Methods in this helper are intended for persistent collections, especially associations
 * configured with {@code orphanRemoval}, where simply replacing the collection instance
 * can break Hibernate tracking.
 */
public final class EntityAssociationHelper {

    private EntityAssociationHelper() {
        // Prevent instantiation
    }

    /**
     * Reconciles {@code managedChildren} with {@code desiredChildren} by removing missing entries
     * and adding new ones while keeping child-to-parent references in sync.
     * <p>
     * If {@code desiredChildren} is {@code null}, it is treated as an empty set.
     *
     * @param parent owning parent entity
     * @param managedChildren managed target set to mutate
     * @param desiredChildren desired content of the set, may be {@code null}
     * @param setParent setter that assigns a child parent reference
     * @param <E> parent entity type
     * @param <C> child entity type
     */
    public static <E, C> void reconcile(E parent, Set<C> managedChildren, Set<C> desiredChildren,
                                        BiConsumer<C, E> setParent) {
        Objects.requireNonNull(parent, () -> "Parent entity must not be null");
        Objects.requireNonNull(managedChildren, () -> "Managed children set must not be null");
        Objects.requireNonNull(setParent, () -> "Parent reference setter must not be null");

        // If the source set is empty this is just a simple clear
        if (desiredChildren == null || desiredChildren.isEmpty()) {
            clear(managedChildren, setParent);
            return;
        }

        // Remove children no longer present in the desired set and clear their parent reference
        Iterator<C> it = managedChildren.iterator();
        while (it.hasNext()) {
            C existing = it.next();
            if (!desiredChildren.contains(existing)) {
                // To avoid equals/hashcode problem, mutate the child only after having removed it from the set
                it.remove();
                setParent.accept(existing, null);
            }
        }

        // Add any new children that are not yet tracked, assigning the parent reference
        desiredChildren.forEach(child -> addMember(parent, managedChildren, child, setParent));
    }

    /**
     * Adds {@code child} to {@code managedChildren} and assigns its parent reference.
     * <p>
     * If {@code child} is {@code null}, the method is a no-op. Parent assignment is performed
     * only when the child is effectively added to the set.
     *
     * @param parent owning parent entity
     * @param managedChildren managed target set to mutate
     * @param child child entity to add
     * @param setParent setter that assigns a child parent reference
     * @param <E> parent entity type
     * @param <C> child entity type
     */
    public static <E, C> void addMember(E parent, Set<C> managedChildren, C child, BiConsumer<C, E> setParent) {
        Objects.requireNonNull(parent, () -> "Parent entity must not be null");
        Objects.requireNonNull(managedChildren, () -> "Managed children set must not be null");
        Objects.requireNonNull(setParent, () -> "Parent reference setter must not be null");

        if (child == null) {
            return;
        }

        // Skip the parent assignment if the given child is already a member
        if (managedChildren.add(child)) {
            setParent.accept(child, parent);
        }
    }

    /**
     * Removes {@code child} from {@code managedChildren} and clears its parent reference.
     * <p>
     * If {@code child} is {@code null} or not present in {@code managedChildren}, the method is a no-op.
     *
     * @param managedChildren managed target set to mutate
     * @param child child entity to remove
     * @param setParent setter that assigns a child parent reference
     * @param <E> parent entity type
     * @param <C> child entity type
     */
    public static <E, C> void removeMember(Set<C> managedChildren, C child, BiConsumer<C, E> setParent) {
        Objects.requireNonNull(managedChildren, () -> "Managed children set must not be null");
        Objects.requireNonNull(setParent, () -> "Parent reference setter must not be null");

        if (child == null || !managedChildren.contains(child)) {
            return;
        }

        managedChildren.remove(child);
        setParent.accept(child, null);
    }

    /**
     * Removes all children from {@code managedChildren} and clears their parent references.
     *
     * @param managedChildren managed target set to mutate
     * @param setParent setter that assigns a child parent reference
     * @param <E> parent entity type
     * @param <C> child entity type
     */
    public static <E, C> void clear(Set<C> managedChildren, BiConsumer<C, E> setParent) {
        Objects.requireNonNull(managedChildren, () -> "Managed children set must not be null");
        Objects.requireNonNull(setParent, () -> "Parent reference setter must not be null");

        Iterator<C> it = managedChildren.iterator();
        while (it.hasNext()) {
            // To avoid equals/hashcode problem, mutate the child only after having removed it from the set
            C child = it.next();
            it.remove();
            setParent.accept(child, null);
        }
    }
}

