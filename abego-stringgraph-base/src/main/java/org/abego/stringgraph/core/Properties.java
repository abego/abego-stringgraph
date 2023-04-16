package org.abego.stringgraph.core;

import org.abego.stringgraph.core.exception.NoSuchPropertyException;
import org.eclipse.jdt.annotation.Nullable;

import java.util.stream.Stream;

public interface Properties extends Iterable<Property> {

    int getSize();

    Stream<Property> stream();

    default Stream<String> propertyNames() {
        return stream().map(Property::getName);
    }

    /**
     * Returns {@code true} when this object has a {@link Property} with the
     * given name, or {@code false} otherwise.
     */
    boolean hasProperty(String name);

    /**
     * Returns the {@link Property} with the given name or {@code null}
     * when no such property exists.
     */
    @Nullable
    Property getPropertyOrNull(String name);

    /**
     * Returns the {@link Property} with the given name or throws a 
     * {@link NoSuchPropertyException} when no such property exists.
     */
    Property getProperty(String name);

    /**
     * Returns the value of the {@link Property} with the given name or throws a 
     * {@link NoSuchPropertyException} when no such property exists.
     */
    String getValueOfProperty(String name);

    /**
     * Returns the value of the {@link Property} with the given name or the
     * {@code defaultValue} when no such property exists.
     */
    String getValueOfPropertyOrElse(String name, String defaultValue);
}
