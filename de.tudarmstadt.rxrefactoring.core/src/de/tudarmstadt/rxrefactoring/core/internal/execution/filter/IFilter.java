/*
 * This file comes from one of my own projects and
 * is licensed under the Apache 2.0 license. Exclusive
 * permissions are hereby granted to use it under
 * whichever license is desired.
 */
package de.tudarmstadt.rxrefactoring.core.internal.execution.filter;

import java.io.Serializable;

/**
 * Specifies a filter, a class that judges whether or not to include certain
 * types of objects in a collection.
 * @author Nikolas Hanstein
 * @param <E> The type of object that will be judged.
 */
@FunctionalInterface
public interface IFilter<E> extends Serializable
{
    /**
     * Checks whether or not the specified element is accepted by this filter.
     * @param element The element to check.
     * @return {@code true} if the specified element is acceptable.
     */
    boolean accept(E element);
}
