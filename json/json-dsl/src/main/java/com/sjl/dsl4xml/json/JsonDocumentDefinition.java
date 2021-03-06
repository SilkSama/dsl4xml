package com.sjl.dsl4xml.json;

import com.sjl.dsl4xml.*;
import com.sjl.dsl4xml.support.*;
import com.sjl.dsl4xml.support.convert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JsonDocumentDefinition<T> implements DocumentDefinition<T>, ConverterRegistry {

    private static final List<Content<?>> NO_CONTENT = Collections.emptyList();

    private List<TypeSafeConverter<?,?>> converters;
    private Document<T> document;

    {
        converters = new ArrayList<TypeSafeConverter<?,?>>();

        registerConverters(
            new PrimitiveBooleanStringConverter(),
            new DecimalToByteStringConverter(),
            new DecimalToShortStringConverter(),
            new DecimalToIntStringConverter(),
            new DecimalToLongStringConverter(),
            new PrimitiveCharStringConverter(),
            new PrimitiveFloatStringConverter(),
            new PrimitiveDoubleStringConverter(),
            new BooleanStringConverter(),
            new ByteStringConverter(),
            new ShortStringConverter(),
            new IntegerStringConverter(),
            new LongStringConverter(),
            new CharacterStringConverter(),
            new FloatStringConverter(),
            new DoubleStringConverter(),
            new ClassStringConverter(),
            new StringStringConverter(),
            new StringBigIntegerConverter(),
            new StringBigDecimalConverter(),
            new NumberByteConverter(),
            new NumberShortConverter(),
            new NumberIntegerConverter(),
            new NumberLongConverter(),
            new NumberFloatConverter(),
            new NumberDoubleConverter(),
            new NumberBigIntegerConverter(),
            new NumberBigDecimalConverter(),
            new BooleanBooleanConverter(),
            new IdentityConverter(ArrayList.class)
        );
    }

    @Override
    public <R extends T> Builder<R> newBuilder() {
        if (document == null)
            throw new IllegalStateException("You haven't defined the document!");

        return document.newBuilder();
    }

    @Override
    public <F> ConverterRegistration<F,Object> converting(Class<F> aToConvert) {
        if (document != null)
            throw new IllegalStateException(
                "document already defined - too late to register general converters. Either register converters " +
                "before .mapping(class) or use .via(class, converter) to nominate a converter per object mapping.");

        return newConverterRegistration(aToConvert);
    }

    @Override
    public Name alias(final String aName, final String anAlias) {
        return new Name.Impl(aName, anAlias);
    }

    @Override
    public Document<T> mapping(final Class<? extends T> aType) {
        if (document != null)
            throw new IllegalStateException("target type is already set - don't call 'mapping' more than once!");

        return document = new Document<T>(){
            private Class<?> intermediate;
            private List<Content<?>> content = NO_CONTENT;
            private Converter<Object,T> converter;
            private ReflectorFactory reflector = new ReflectorFactory();

            @Override
            public Document<T> with(Content<?>... aContent) {
                content = new ArrayList<Content<?>>();
                for (Content<?> _c : aContent) {
                    content.add(_c);
                }
                return this;
            }

            @Override
            public <I> Document<I> via(Class<I> anIntermediateType) {
                intermediate = anIntermediateType;
                converter = (Converter<Object,T>)getConverter(anIntermediateType, aType);
                return (Document<I>)this;
            }

            @Override
            public <I> Document<I> via(Class<I> anIntermediateType, Converter<I,? extends T> aConverter) {
                intermediate = anIntermediateType;
                converter = (Converter<Object,T>)aConverter;
                return (Document<I>)this;
            }

            @Override
            @SuppressWarnings("rawtypes")
            public Builder<T> newBuilder() {
                List<Builder<?>> _nested = new ArrayList<Builder<?>>();
                Class<?> _attachTo = ReflectorFactory.maybeConvertToProxy((intermediate != null) ? intermediate : aType);
                for (Content<?> _c : content)
                {
                    _c.onAttach(_attachTo, reflector, JsonDocumentDefinition.this);
                    _nested.add(_c.newBuilder());
                }
                return new ReflectiveBuilder(
                    Name.MISSING, aType, intermediate, converter,
                    reflector.newReflector(), _nested, false);
            }
        };
    }

    @Override
    public <R> Ignored<R> ignoring(String aName) {
        return ignoring(alias(aName, aName));
    }

    @Override
    public <R> Ignored<R> ignoring(final Name aName) {
         return new Ignored.Impl<R>(aName);
    }

    @Override
    public <R> NamedObject<R> object(String aName) {
        return object(alias(aName, aName), null);
    }

    @Override
    public <R> NamedObject<R> object(Name aName) {
        return object(aName, null);
    }

    @Override
    public <R> NamedObject<R> object(String aName, Class<? extends R> aType) {
        return object(alias(aName, aName), aType);
    }

    @Override
    public <R> NamedObject<R> object(final Name aName, final Class<? extends R> aType) {
        return new NamedObject.Impl<R>(aName, aType);
    }

    @Override
    public <R> UnNamedObject<R> object(final Class<? extends R> aType) {
        return new UnNamedObject.Impl<R>(aType);
    }

    @Override
    public <R> NamedArray<R> array(String aName) {
        return array(alias(aName, aName));
    }

    @Override
    public <R> NamedArray<R> array(Name aName) {
        return (NamedArray<R>)array(aName, ArrayList.class);
    }

    @Override
    public <R> NamedArray<R> array(String aName, Class<? extends R> aType) {
        return array(alias(aName, aName), aType);
    }

    @Override
    public <R> NamedArray<R> array(final Name aName, final Class<? extends R> aType) {
        return new NamedArray.Impl<R>(aName, aType) {
            @Override
            public NamedArray<R> of(Class<?> aConvertableType) {
                return of(property(aConvertableType));
            }
        };
    }

    @Override
    public UnNamedArray<List> array() {
        return array(List.class);
    }

    @Override
    public <R> UnNamedArray<R> array(final Class<? extends R> aType) {
        return new UnNamedArray.Impl<R>(aType) {
            @Override
            public UnNamedArray<R> of(Class<?> aConvertableType) {
                return of(property(aConvertableType));
            }
        };
    }

    @Override
    public <R> UnNamedProperty<String,R> property(final Class<? extends R> aType) {
        return new UnNamedProperty<String,R>(){
            private StringConverter<? extends R> converter = getConverter(aType);

            @Override
            public void onAttach(Class<?> aContainerType, ReflectorFactory aReflector, ConverterRegistry aConverters) {
                aReflector.prepare(aContainerType, Name.MISSING, aType);
            }

            @Override
            public Name getName() {
                return Name.MISSING;
            }

            @Override
            public Builder<R> newBuilder() {
                return new PropertyBuilder<String,R>(Name.MISSING, aType, converter);
            }
        };
    }

    @Override
    public <R> NamedProperty<String,R> property(String aName) {
        return property(alias(aName, aName));
    }

    @Override
    public <R> NamedProperty<String,R> property(final Name aName) {
        return new NamedProperty<String,R>(){
            private Class<? extends R> type;
            private StringConverter<? extends R> converter;

            @Override
            public void onAttach(Class<?> aContainerType, ReflectorFactory aReflector, ConverterRegistry aConverters) {
                type = (Class<? extends R>) ReflectorFactory.getExpectedType(aContainerType, aName);
                aReflector.prepare(aContainerType, aName, type);
                converter = getConverter(type);
            }

            @Override
            public Name getName() {
                return aName;
            }

            @Override
            public Builder<R> newBuilder() {
                return new PropertyBuilder<String,R>(aName, type, converter);
            }
        };
    }

    @Override
    public <R> NamedProperty<Number,R> number(String aName, Class<R> aType) {
        return number(alias(aName, aName), aType);
    }

    @Override
    public <R> NamedProperty<Number,R> number(final Name aName, final Class<R> aType) {
        return new NamedProperty<Number,R>(){
            private Converter<Number,R> converter = getConverter(Number.class, aType);

            @Override
            public void onAttach(Class<?> aContainerType, ReflectorFactory aReflector, ConverterRegistry aConverters) {
                aReflector.prepare(aContainerType, aName, aType);
            }

            @Override
            public Name getName() {
                return aName;
            }

            @Override
            public Builder<R> newBuilder() {
                return new PropertyBuilder<Number,R>(aName, aType, converter);
            }
        };
    }

    @Override
    public <R> UnNamedProperty<Number,R> number(final Class<R> aType) {
        return new UnNamedProperty<Number,R>(){
            private Converter<Number,R> converter = getConverter(Number.class, aType);

            @Override
            public void onAttach(Class<?> aContainerType, ReflectorFactory aReflector, ConverterRegistry aConverters) {
                aReflector.prepare(aContainerType, Name.MISSING, aType);
            }

            @Override
            public Name getName() {
                return Name.MISSING;
            }

            @Override
            public Builder<R> newBuilder() {
                return new PropertyBuilder<Number,R>(Name.MISSING, aType, converter);
            }
        };
    }

    @Override
    public <R> NamedProperty<Boolean,R> bool(String aName) {
        return bool(alias(aName, aName));
    }

    @Override
    public <R> NamedProperty<Boolean,R> bool(final Name aName) {
        return bool(aName, null);
    }

    @Override
    public <R> NamedProperty<Boolean,R> bool(String aName, Class<R> aType) {
        return bool(alias(aName, aName), aType);
    }

    @Override
    public <R> NamedProperty<Boolean,R> bool(final Name aName, final Class<R> aType) {
        return new NamedProperty<Boolean,R>(){
            private Class<R> type = aType;
            private Converter<Boolean,R> converter;

            @Override
            public void onAttach(Class<?> aContainerType, ReflectorFactory aReflector, ConverterRegistry aConverters) {
                if (type == null)
                    type = (Class<R>)ReflectorFactory.getExpectedType(aContainerType, aName);
                converter = getConverter(Boolean.class, type);
                aReflector.prepare(aContainerType, aName, type);
            }

            @Override
            public Name getName() {
                return aName;
            }

            @Override
            public Builder<R> newBuilder() {
                return new PropertyBuilder<Boolean,R>(aName, type, converter);
            }
        };
    }

    @Override
    public <R> UnNamedProperty<Boolean,R> bool() {
        return (UnNamedProperty<Boolean,R>)bool(Boolean.class);
    }

    @Override
    public <R> UnNamedProperty<Boolean,R> bool(final Class<R> aType) {
        return new UnNamedProperty<Boolean,R>() {
            private Converter<Boolean,R> converter = getConverter(Boolean.class, aType);

            @Override
            public void onAttach(Class<?> aContainerType, ReflectorFactory aReflector, ConverterRegistry aConverters) {
                aReflector.prepare(aContainerType, Name.MISSING, aType);
            }

            @Override
            public Name getName() {
                return Name.MISSING;
            }

            @Override
            public Builder<R> newBuilder() {
                return new PropertyBuilder<Boolean,R>(Name.MISSING, aType, converter);
            }
        };
    }

    private <F> ConverterRegistration<F,Object> newConverterRegistration(final Class<F> aFrom) {
        return new ConverterRegistration<F,Object>(){
            private Class<?> to;
            private Converter<F,Object> converter;

            @Override
            public <R> ConverterRegistration<F, R> to(Class<R> aToType) {
                to = aToType;
                return (ConverterRegistration<F,R>)this;
            }

            @Override
            public ConverterRegistration<F, Object> using(Converter<F, Object> aConverter) {
                converter = aConverter;
                converters.add(new TypeSafeConverter<F,Object>(){
                    @Override
                    public boolean canConvertFrom(Class<?> aClass) {
                        return aFrom.isAssignableFrom(aClass);
                    }

                    @Override
                    public boolean canConvertTo(Class<?> aClass) {
                        return to.isAssignableFrom(aClass);
                    }

                    @Override
                    public Object convert(F aFrom) {
                        return converter.convert(aFrom);
                    }
                });
                return this;
            }

            @Override
            public Converter<F, Object> get() {
                return converter;
            }
        };
    }

    public void registerConverters(TypeSafeConverter<?,?>... aConverters) {
        converters.addAll(0, Arrays.asList(aConverters)); // always push on to front to allow overriding
    }

    public <T> StringConverter<T> getConverter(Class<T> aTo) {
        return (StringConverter<T>) getConverter(String.class, aTo);
    }

    public <F,T> TypeSafeConverter<F,T> getConverter(Class<F> aFromType, Class<T> aToType) {
        for (TypeSafeConverter<?,?> _c : converters) {
            if ((_c.canConvertFrom(aFromType)) && (_c.canConvertTo(aToType))) {
                return (TypeSafeConverter<F,T>) _c;
            }
        }

        throw new RuntimeException("no converter registered that can convert from " + aFromType + " to " + aToType);
    }

    static <T> T firstNonNull(T... aTs) {
        for (T _t : aTs)
            if (_t != null)
                return _t;
        return null;
    }
}
