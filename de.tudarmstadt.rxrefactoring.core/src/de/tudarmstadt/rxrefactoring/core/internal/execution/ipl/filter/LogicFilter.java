/*
 * This file comes from one of my own projects and
 * is licensed under the Apache 2.0 license. Exclusive
 * permissions are hereby granted to use it under
 * whichever license is desired.
 */
package de.tudarmstadt.rxrefactoring.core.internal.execution.ipl.filter;

/**
 * A utility class for composing {@link IFilter} instances to form more complex
 * filters.
 * @author Nikolas Hanstein
 * @see IFilter
 */
public final class LogicFilter
{
    /**
     * Creates a new filter that accepts items if {@code filter1} and
     * {@code filter2} accept them.
     * @param filter1 The first filter to combine.
     * @param filter2 The second filter to combine.
     * @return A new {@link IFilter} instance that combines {@code filter1} and
     *         {@code filter2}.
     */
    public static <E> IFilter<E> and(IFilter<E> filter1, IFilter<E> filter2)
    {
        return new And<>(filter1, filter2);
    }

    /**
     * Creates a new filter that accepts items if {@code filter1} or
     * {@code filter2} accepts them.
     * @param filter1 The first filter to combine.
     * @param filter2 The second filter to combine.
     * @return A new {@link IFilter} instance that combines {@code filter1} and
     *         {@code filter2}.
     */
    public static <E> IFilter<E> or(IFilter<E> filter1, IFilter<E> filter2)
    {
        return new Or<>(filter1, filter2);
    }

    /**
     * Creates a new filter that accepts items if only {@code filter1} or
     * {@code filter2} accepts them, but not both.
     * @param filter1 The first filter to combine.
     * @param filter2 The second filter to combine.
     * @return A new {@link IFilter} instance that combines {@code filter1} and
     *         {@code filter2}.
     */
    public static <E> IFilter<E> xor(IFilter<E> filter1, IFilter<E> filter2)
    {
        return new XOR<>(filter1, filter2);
    }

    /**
     * Creates a new filter that accepts items if {@code filter} does not accept
     * them.
     * @param filter The filter to negate.
     * @return A new {@link IFilter} instance based on {@code filter}.
     */
    public static <E> IFilter<E> not(IFilter<E> filter)
    {
        return new Negate<>(filter);
    }

    /**
     * An implementation of {@link IFilter} used for
     * {@link LogicFilter#and(IFilter, IFilter)}.
     * @author Nikolas Hanstein
     * @param <E> The type of object that will be judged.
     */
    private static final class And<E> implements IFilter<E>
    {
        /** Serial version unique ID. */
        private static final long serialVersionUID = 1L;

        /* The first filter to use. */
        private final IFilter<E> filter1;
        /* The second filter to use. */
        private final IFilter<E> filter2;

        /**
         * Constructs a new instance of LogicFilter.And with the specified
         * filters.
         * @param filter1 The first filter to use.
         * @param filter2 The second filter to use.
         */
        private And(final IFilter<E> filter1, final IFilter<E> filter2)
        {
            this.filter1 = filter1;
            this.filter2 = filter2;
        }

        /** {@inheritDoc} */
        @Override
        public boolean accept(final E element)
        {
            return this.filter1.accept(element) && this.filter2.accept(element);
        }
    }

    /**
     * An implementation of {@link IFilter} used for
     * {@link LogicFilter#or(IFilter, IFilter)}.
     * @author Nikolas Hanstein
     * @param <E> The type of object that will be judged.
     */
    private static final class Or<E> implements IFilter<E>
    {
        /** Serial version unique ID. */
        private static final long serialVersionUID = 1L;

        /* The first filter to use. */
        private final IFilter<E> filter1;
        /* The second filter to use. */
        private final IFilter<E> filter2;

        /**
         * Constructs a new instance of LogicFilter.Or with the specified
         * filters.
         * @param filter1 The first filter to use.
         * @param filter2 The second filter to use.
         */
        private Or(final IFilter<E> filter1, final IFilter<E> filter2)
        {
            this.filter1 = filter1;
            this.filter2 = filter2;
        }

        /** {@inheritDoc} */
        @Override
        public boolean accept(final E element)
        {
            return this.filter1.accept(element) || this.filter2.accept(element);
        }
    }

    /**
     * An implementation of {@link IFilter} used for
     * {@link LogicFilter#xor(IFilter, IFilter)}.
     * @author Nikolas Hanstein
     * @param <E> The type of object that will be judged.
     */
    private static final class XOR<E> implements IFilter<E>
    {
        /** Serial version unique ID. */
        private static final long serialVersionUID = 1L;

        /* The first filter to use. */
        private final IFilter<E> filter1;
        /* The second filter to use. */
        private final IFilter<E> filter2;

        /**
         * Constructs a new instance of LogicFilter.XOR with the specified
         * filters.
         * @param filter1 The first filter to use.
         * @param filter2 The second filter to use.
         */
        private XOR(final IFilter<E> filter1, final IFilter<E> filter2)
        {
            this.filter1 = filter1;
            this.filter2 = filter2;
        }

        /** {@inheritDoc} */
        @Override
        public boolean accept(final E element)
        {
            return this.filter1.accept(element) ^ this.filter2.accept(element);
        }
    }

    /**
     * An implementation of {@link IFilter} used for
     * {@link LogicFilter#not(IFilter)}.
     * @author Nikolas Hanstein
     * @param <E> The type of object that will be judged.
     */
    private static final class Negate<E> implements IFilter<E>
    {
        /** Serial version unique ID. */
        private static final long serialVersionUID = 1L;

        /* The filter to negate. */
        private final IFilter<E> filter;

        /**
         * Constructs a new instance of LogicFilter.Negate with the specified
         * filter.
         * @param filter The filter to negate.
         */
        private Negate(final IFilter<E> filter)
        {
            this.filter = filter;
        }

        /** {@inheritDoc} */
        @Override
        public boolean accept(final E element)
        {
            return !this.filter.accept(element);
        }
    }
}
