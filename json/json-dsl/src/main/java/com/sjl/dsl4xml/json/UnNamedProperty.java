package com.sjl.dsl4xml.json;

public interface UnNamedProperty<V,T> extends Content<T> {

    public T build(V aValue);

}
