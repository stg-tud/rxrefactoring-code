package de.tudarmstadt.rxrefactoring.core.internal.execution.filter;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An ArrayList that can filter elements added to it using an {@link IFilter}.
 * @param <E> The type of element that this FilteredArrayList should hold.
 * @author Nikolas Hanstein
 * @since 1.0
 */
public final class FilteredArrayList<E> extends ArrayList<E> implements IFilteredList<E>
{
    /** Serial version unique ID. */
    private static final long serialVersionUID = 1L;

    /**
     * The filter used to judge whether an element can be added to this list.
     */
    private final IFilter<E> filter;

    /**
     * Constructs an empty list with an initial capacity of ten.
     * @param filter The filter used to judge whether an element can be added to
     *        this list.
     * @see ArrayList#ArrayList()
     */
    public FilteredArrayList(final IFilter<E> filter)
    {
        this.filter = filter;
    }

    /**
     * Constructs an empty list with the specified initial capacity.
     * @param filter The filter used to judge whether an element can be added to
     *        this list.
     * @param capacity The initial capacity of the list.
     * @throws IllegalArgumentException if the specified initial capacity is
     *         negative
     * @see ArrayList#ArrayList(int)
     */
    public FilteredArrayList(final IFilter<E> filter, final int capacity)
    {
        super(capacity);
        this.filter = filter;
    }

    /**
     * Constructs a list containing all elements of the specified collection
     * that are accepted by the specified filter, in the order they are returned
     * by the collection's iterator.
     * @param filter The filter used to judge whether an element can be added to
     *        this list.
     * @param collection The collection whose elements are to be placed into
     *        this list.
     * @throws NullPointerException if the specified collection is {@code null}.
     * @see ArrayList#ArrayList(Collection)
     */
    public FilteredArrayList(final IFilter<E> filter, final Collection<? extends E> collection)
    {
        super(collection);
        this.filter = filter;
    }

    /**
     * Appends the specified element to the end of this list if this list's
     * filter accepts it.
     * @param element Element to be appended to this list.
     * @return {@code true} if the element was added, {@code false} if the
     *         filter rejected this element.
     * @see ArrayList#add(E)
     */
    @Override
    public boolean add(final E element)
    {
        if(this.filter.accept(element))
        {
            return super.add(element);
        }
        return false;
    }

    /**
     * Inserts the specified element at the specified position in this list if
     * this list's filter accepts it. Shifts the element currently at that
     * position (if any) and any subsequent elements to the right (adds one to
     * their indices).
     * @param element Element to be appended to this list.
     * @throws IndexOutOfBoundsException if the index is out of range
     *         {@code (index < 0
     *         || index > size())}
     * @see ArrayList#add(int, E)
     */
    @Override
    public void add(final int index, final E element)
    {
        if(this.filter.accept(element))
        {
            super.add(index, element);
        }
    }

    /**
     * Appends all of the elements in the specified collection that this list's
     * filter accepts to the end of this list, in the order that they are
     * returned by the specified collection's Iterator. The behavior of this
     * operation is undefined if the specified collection is modified while the
     * operation is in progress. (This implies that the behavior of this call is
     * undefined if the specified collection is this list, and this list is
     * nonempty.)
     * @param collection Collection containing elements to be added to this
     *        list.
     * @return {@code true} if this list changed as a result of the call.
     * @throws NullPointerException if the specified collection is {@code null}.
     * @see ArrayList#addAll(Collection)
     */
    @Override
    public boolean addAll(final Collection<? extends E> collection)
    {
        if(collection == null)
        {
            throw new NullPointerException("collection is null");
        }

        final ArrayList<E> temp = new ArrayList<>();
        for(final E element : collection)
        {
            if(this.filter.accept(element))
            {
                temp.add(element);
            }
        }
        return super.addAll(temp);
    }

    /**
     * Inserts all of the elements in the specified collection that this list's
     * filter accepts into this list, starting at the specified position. Shifts
     * the element currently at that position (if any) and any subsequent
     * elements to the right (increases their indices). The new elements will
     * appear in the list in the order that they are returned by the specified
     * collection's iterator.
     * @param index Index at which to insert the first element from the
     *        specified collection.
     * @return {@code true} if this list changed as a result of the call.
     * @throws IndexOutOfBoundsException if the index is out of range
     *         {@code (index < 0
     *         || index > size())}
     * @throws NullPointerException If the specified collection is {@code null}.
     * @see ArrayList#addAll(int, Collection)
     */
    @Override
    public boolean addAll(final int index, final Collection<? extends E> collection)
    {
        if(collection == null)
        {
            throw new NullPointerException("collection is null");
        }

        final ArrayList<E> temp = new ArrayList<>();
        for(final E element : collection)
        {
            if(this.filter.accept(element))
            {
                temp.add(element);
            }
        }
        return super.addAll(index, temp);
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element if this lists's filter accepts it.
     * @param index Index of the element to replace.
     * @param element Element to be stored at the specified position.
     * @return The element previously at the specified position, or {@code null}
     *         if this list's filter rejected the new element.
     * @throws IndexOutOfBoundsException if the index is out of range
     *         {@code (index < 0
     *         || index > size())}
     * @see ArrayList#set(int, Object)
     */
    @Override
    public E set(final int index, final E element)
    {
        if(this.filter.accept(element))
        {
            return super.set(index, element);
        }
        return null;
    }
}
