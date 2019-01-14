package de.tudarmstadt.rxrefactoring.core.internal.execution.filter;

import java.io.Serializable;

public interface IFilter<E> extends Serializable
{
    boolean accept(E element);
}
