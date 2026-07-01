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
import java.util.Optional;

public class TestCaseExtension {

    protected List<Field> listAllFields(Object obj) {
        List<Field> fieldList = new ArrayList<>();
        Class<? extends Object> tmpClass = obj.getClass();
        while (tmpClass != null) {
            fieldList.addAll(Arrays.asList(tmpClass.getDeclaredFields()));
            tmpClass = tmpClass.getSuperclass();
        }
        return fieldList;
    }

    protected void setInstanceField(Field field, Object instance, Object extensionObjectValue)
            throws IllegalAccessException {
        field.set(instance, extensionObjectValue);
    }

    protected void extendAnnotatedValue(ExtensionContext extensionContextIn,
                                            Class<? extends Annotation> annotationClass,
                                            Object extensionObjectValue,
                                            Class<?> extensionObjectClass) {
        extensionContextIn.getRequiredTestInstances().getAllInstances().forEach(instance -> {
            Optional<Field> extensionField = Optional.empty();
            List<Field> instanceFields = listAllFields(instance);
            for (Field field : instanceFields) {
                if (field.isAnnotationPresent(annotationClass)) {

                    if (!extensionObjectClass.equals(field.getType())) {
                        throw new IllegalStateException("Only fields of type %s can be annotated with @%s"
                                .formatted(extensionObjectClass.getSimpleName(), annotationClass.getSimpleName()));
                    }

                    if (extensionField.isPresent()) {
                        throw new ExtensionConfigurationException(
                                "More than one annotation @%s found in test class or parents ([%s] and [%s]) "
                                        .formatted(annotationClass.getSimpleName(),
                                                extensionField.get().getName(), field.getName()));
                    }

                    try {
                        field.setAccessible(true);
                        setInstanceField(field, instance, extensionObjectValue);
                        extensionField = Optional.of(field);
                    }
                    catch (Exception ex) {
                        throw new IllegalStateException("Unable to set extension object value for field %s"
                                .formatted(field.getName()), ex);
                    }
                }
            }
        });
    }

}
