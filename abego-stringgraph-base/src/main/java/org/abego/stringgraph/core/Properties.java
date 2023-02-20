package org.abego.stringgraph.core;

import org.eclipse.jdt.annotation.Nullable;

import java.util.stream.Stream;

public interface Properties extends Iterable<Property> {

    int getSize();
    
    Stream<Property> stream();

    boolean hasProperty(String name);

    @Nullable
    Property getPropertyOrNull(String name);

    Property getProperty(String name);

    String getValueOfProperty(String name);

    String getValueOfPropertyOrElse(String name, String defaultValue);
}
