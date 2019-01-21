/*
 * This file comes from one of my own projects and
 * is licensed under the Apache 2.0 license. Exclusive
 * permissions are hereby granted to use it under
 * whichever license is desired.
 */
package de.tudarmstadt.rxrefactoring.core.internal.execution.collect;

import java.io.Serializable;

import com.google.common.base.Objects;

/**
 * A pair of two values. Can contain either one or two different types. <br>
 * Both mutable and immutable implementations are available, see below.
 * @author Nikolas Hanstein
 * @param <T1> The type pf the first element.
 * @param <T2> The type of the second element.
 * @see {@link MutablePair}, a mutable implementation
 * @see {@link ImmutablePair}, an immutable implementation
 * @since 1.0
 */
public abstract class Pair<T1, T2> implements Serializable
{
    /** The serial version UID. */
    private static final long serialVersionUID = -3197131641898309745L;

    /**
     * Get the value of the first element. May return {@code null}. This should
     * be supported by both mutable and immutable implementations.
     * @return The value of the first element.
     */
    public abstract T1 getFirst();

    /**
     * Get the value of the second element. May return {@code null}. This should
     * be supported by both mutable and immutable implementations.
     * @return The value of the second element.
     */
    public abstract T2 getSecond();

    /**
     * Sets the value of the first element in the pair to {@code value}. <br>
     * <br>
     * {@code value} may be {@code null}. This should be supported by mutable
     * implementations, immutable ones should throw an
     * {@code UnsupportedOperationException} when it is called.
     * @param value The value to set the value of the first element to.
     * @throws UnsupportedOperationException If the underlying implementation is
     *         immutable.
     */
    public abstract void setFirst(T1 value);

    /**
     * Sets the value of the second element in the pair to {@code value}. <br>
     * <br>
     * {@code value} may be {@code null}. This should be supported by mutable
     * implementations, immutable ones should throw an
     * {@code UnsupportedOperationException} when it is called.
     * @param value The value to set the value of the second element to.
     * @throws UnsupportedOperationException If the underlying implementation is
     *         immutable.
     */
    public abstract void setSecond(T2 value);

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object other)
    {
        if(other instanceof Pair)
        {
            @SuppressWarnings("unchecked") Pair<T1, T2> pair = (Pair<T1, T2>)other;
            return Objects.equal(this.getFirst(), pair.getFirst()) && Objects.equal(this.getSecond(), pair.getSecond());
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        int hash1 = this.getFirst() == null ? 0 : this.getFirst().hashCode();
        int hash2 = this.getSecond() == null ? 0 : this.getSecond().hashCode();
        return hash1 ^ hash2;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + "[first = " + this.getFirst() + ", second = " + this.getSecond() + "]";
    }
}
