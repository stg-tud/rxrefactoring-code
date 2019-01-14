package de.tudarmstadt.rxrefactoring.core.internal.execution.filter;

public final class LogicFilter
{
    public static final class And<E> implements IFilter<E>
    {
        /** Serial version unique ID. */
        private static final long serialVersionUID = 1L;

        private final IFilter<E> filter1;
        private final IFilter<E> filter2;

        public And(final IFilter<E> filter1, final IFilter<E> filter2)
        {
            this.filter1 = filter1;
            this.filter2 = filter2;
        }

        @Override
        public boolean accept(final E element)
        {
            return this.filter1.accept(element) && this.filter2.accept(element);
        }
    }

    public static final class Or<E> implements IFilter<E>
    {
        /** Serial version unique ID. */
        private static final long serialVersionUID = 1L;

        private final IFilter<E> filter1;
        private final IFilter<E> filter2;

        public Or(final IFilter<E> filter1, final IFilter<E> filter2)
        {
            this.filter1 = filter1;
            this.filter2 = filter2;
        }

        @Override
        public boolean accept(final E element)
        {
            return this.filter1.accept(element) || this.filter2.accept(element);
        }
    }

    public static final class XOR<E> implements IFilter<E>
    {
        /** Serial version unique ID. */
        private static final long serialVersionUID = 1L;

        private final IFilter<E> filter1;
        private final IFilter<E> filter2;

        public XOR(final IFilter<E> filter1, final IFilter<E> filter2)
        {
            this.filter1 = filter1;
            this.filter2 = filter2;
        }

        @Override
        public boolean accept(final E element)
        {
            return this.filter1.accept(element) ^ this.filter2.accept(element);
        }
    }

    public static final class Negate<E> implements IFilter<E>
    {
        /** Serial version unique ID. */
        private static final long serialVersionUID = 1L;

        private final IFilter<E> filter;

        public Negate(final IFilter<E> filter)
        {
            this.filter = filter;
        }

        @Override
        public boolean accept(final E element)
        {
            return !this.filter.accept(element);
        }
    }
}
