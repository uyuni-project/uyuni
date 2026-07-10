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

package com.redhat.rhn.testing;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base helper for JUnit 5 test extensions that inject values into fields annotated
 * with a specific annotation.
 * <p>
 * Implementations define how values are resolved by overriding
 * {@link #resolveFieldValue(ExtensionContext, Field)}. The resolved value is then
 * assigned to each matching field through {@link #setFieldValue(Field, Object, Object)}.
 */
public abstract class TestCaseExtension {

    /**
     * Inject values into annotated fields, disallowing multiple matching fields and
     * including inherited fields.
     *
     * @param extensionContext the current JUnit extension context
     * @param targetAnnotation the annotation that marks fields to inject
     * @param expectedType the required Java type for annotated fields
     */
    protected void injectAnnotatedFields(ExtensionContext extensionContext,
                                            Class<? extends Annotation> targetAnnotation, Class<?> expectedType) {
        injectAnnotatedFields(extensionContext, targetAnnotation, expectedType, false, true);
    }

    /**
     * Inject values into fields annotated with {@code targetAnnotation}.
     * <p>
     * For every test instance in the current context, this method scans the declared
     * fields (and optionally parent-class fields), validates field types, resolves the
     * value via {@link #resolveFieldValue(ExtensionContext, Field)}, and assigns it.
     *
     * @param extensionContext the current JUnit extension context
     * @param targetAnnotation the annotation that marks fields to inject
     * @param expectedType the required Java type for annotated fields
     * @param allowsMultiple whether more than one annotated field is allowed across
     * parent and child classes for a single instance
     * @param includeInherited whether fields declared in superclasses are considered
     * @throws ExtensionConfigurationException if multiple annotated fields are found
     * while {@code allowsMultiple} is {@code false}
     * @throws IllegalStateException if a field has the wrong type or if value
     * assignment fails
     */
    protected void injectAnnotatedFields(ExtensionContext extensionContext,
                                            Class<? extends Annotation> targetAnnotation, Class<?> expectedType,
                                            boolean allowsMultiple, boolean includeInherited) {
        extensionContext.getRequiredTestInstances().getAllInstances().forEach(instance -> {
            String lastFieldSet = null;

            List<Field> fieldsToProcess = collectApplicableFields(instance, includeInherited);
            for (Field field : fieldsToProcess) {
                if (field.isAnnotationPresent(targetAnnotation)) {

                    if (!expectedType.equals(field.getType())) {
                        throw new IllegalStateException("Only fields of type %s can be annotated with @%s"
                                .formatted(expectedType.getSimpleName(), targetAnnotation.getSimpleName()));
                    }

                    if (!allowsMultiple && lastFieldSet != null) {
                        throw new ExtensionConfigurationException(
                                "More than one annotation @%s found in test class or parents ([%s] and [%s]) "
                                        .formatted(targetAnnotation.getSimpleName(), lastFieldSet, field.getName())
                        );
                    }

                    try {
                        field.setAccessible(true);

                        Object value = resolveFieldValue(extensionContext, field);
                        setFieldValue(field, instance, value);

                        lastFieldSet = field.getName();
                    }
                    catch (Exception ex) {
                        throw new IllegalStateException("Unable to set extension object value for field %s"
                                .formatted(field.getName()), ex);
                    }
                }
            }
        });
    }

    /**
     * Resolve the value to assign to a matching target field.
     *
     * @param extensionContext the current JUnit extension context
     * @param targetField the annotated field that will receive the value
     * @return the value to assign to {@code targetField}
     */
    protected abstract Object resolveFieldValue(ExtensionContext extensionContext, Field targetField);

    /**
     * Assign a resolved value to a target field on a specific instance.
     * <p>
     * Subclasses may override to customize assignment behavior.
     *
     * @param targetField the field to modify
     * @param targetInstance the instance that owns the field
     * @param fieldValue the value to assign
     * @throws IllegalAccessException if Java reflection denies write access
     */
    protected void setFieldValue(Field targetField, Object targetInstance, Object fieldValue)
            throws IllegalAccessException {
        targetField.set(targetInstance, fieldValue);
    }

    /**
     * Collect fields to inspect for injection.
     *
     * @param targetInstance the instance whose class fields are inspected
     * @param includeInherited whether to include superclass declared fields
     * @return the list of fields to process
     */
    private List<Field> collectApplicableFields(Object targetInstance, boolean includeInherited) {
        if (!includeInherited) {
            return Arrays.asList(targetInstance.getClass().getDeclaredFields());
        }

        List<Field> fieldList = new ArrayList<>();
        Class<? extends Object> tmpClass = targetInstance.getClass();
        while (tmpClass != null) {
            fieldList.addAll(Arrays.asList(tmpClass.getDeclaredFields()));
            tmpClass = tmpClass.getSuperclass();
        }

        return fieldList;
    }
}
