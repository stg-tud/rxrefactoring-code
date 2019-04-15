/*
 * This file comes from one of my own projects and
 * is licensed under the Apache 2.0 license. Exclusive
 * permissions are hereby granted to use it under
 * whichever license is desired.
 */
package de.tudarmstadt.rxrefactoring.core.internal.execution.ipl.collect;

/**
 * An immutable implementation of {@link Pair}.
 * @author Nikolas Hanstein
 * @param <T1> The type of the first element.
 * @param <T2> The type of the second element.
 * @since 1.0
 */
public class ImmutablePair<T1, T2> extends Pair<T1, T2>
{
    /** The serial version UID. */
    private static final long serialVersionUID = 5421474567298108985L;

    /** The first element. */
    private T1 first;
    /** The second element. */
    private T2 second;

    /**
     * Constructs a new mutable pair containing two {@code null} elements.
     */
    public ImmutablePair()
    {
        this(null, null);
    }

    /**
     * Constructs a new mutable pair with the two elements that the specified
     * pair contains.
     * @param pair The pair whose elements this pair should contain.
     */
    public ImmutablePair(final Pair<T1, T2> pair)
    {
        this(pair.getFirst(), pair.getSecond());
    }

    /**
     * Constructs a new mutable pair with the two specified elements.
     * @param element1 The first element in this pair.
     * @param element2 The second element in this pair.
     */
    public ImmutablePair(final T1 element1, final T2 element2)
    {
        this.first = element1;
        this.second = element2;
    }

    /** {@inheritDoc} */
    public T1 getFirst()
    {
        return this.first;
    }

    /** {@inheritDoc} */
    public T2 getSecond()
    {
        return this.second;
    }

    /** {@inheritDoc} */
    @Override
    public void setFirst(final T1 value) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Elements contained within an immutable pair cannot be modified.");
    }

    /** {@inheritDoc} */
    @Override
    public void setSecond(final T2 value) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Elements contained within an immutable pair cannot be modified.");
    }
}
