package com.alaimos.MITHrIL.api.Enums.DynamicEnums;

import com.alaimos.MITHrIL.api.Commons.Utils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is a dynamic implementation of the enum type. It allows runtime loading of its members.
 *
 * @author alaimos
 * @version 2.0.0.0
 * @since 29/12/2015
 */
public abstract class DynamicEnum<E extends DynamicEnum<E>> implements Comparable<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 318007039220161161L;

    protected static Map<Class<? extends DynamicEnum<?>>, Integer> lastOrd = new LinkedHashMap<>();
    private static final Map<Class<? extends DynamicEnum<?>>, Map<String, DynamicEnum<?>>> elements = new LinkedHashMap<>();

    /**
     * The name of this dynamicEnum constant.
     */
    private final String name;

    /**
     * The ordinal of this enumeration constant (its position in the dynamicEnum declaration, where the initial
     * constant is assigned an ordinal of zero).
     */
    private final int ordinal;

    /**
     * Returns the name of this dynamicEnum constant, exactly as declared in its dynamicEnum declaration.
     *
     * @return the name of this dynamicEnum constant
     */
    @Contract(pure = true)
    public final String name() {
        return name;
    }

    /**
     * Returns the ordinal of this enumeration constant (its position in its dynamicEnum declaration, where the initial
     * constant is assigned an ordinal of zero).
     *
     * @return the ordinal of this enumeration constant
     */
    @Contract(pure = true)
    public final int ordinal() {
        return ordinal;
    }

    /**
     * Constructor.  Programmers must invoke this constructor.
     *
     * @param ordinal The ordinal of this enumeration constant (its position in the dynamicEnum declaration, where the
     *                initial constant is assigned an ordinal of zero).
     * @param name    The name of this dynamicEnum constant, which is the identifier used to declare it.
     */
    protected DynamicEnum(int ordinal, String name) {
        this.name = name;
        this.ordinal = ordinal;
        Class<? extends DynamicEnum<?>> clazz = getDynamicEnumClass();
        Map<String, DynamicEnum<?>> typeElements = elements.computeIfAbsent(clazz, k -> new LinkedHashMap<>());
        typeElements.put(name, this);
        if (ordinal > lastOrd.getOrDefault(clazz, -1)) {
            lastOrd.put(clazz, ordinal);
        }
    }

    /**
     * Returns the name of this dynamicEnum constant, as contained in the declaration.
     *
     * @return the name of this dynamicEnum constant
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns true if the specified object is equal to this dynamicEnum constant.
     *
     * @param other the object to be compared for equality with this object.
     * @return true, if the specified object is equal to this dynamicEnum constant.
     */
    @Override
    @Contract(pure = true)
    public final boolean equals(Object other) {
        return this == other;
    }

    /**
     * Returns a hash code for this dynamicEnum constant.
     *
     * @return a hash code for this dynamicEnum constant.
     */
    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * Throws CloneNotSupportedException.  This guarantees that dynamicEnums are never cloned, which is necessary to
     * preserve their "singleton" status.
     *
     * @return (never returns)
     */
    @Override
    @Contract(" -> fail")
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Compares this dynamicEnum with the specified object for order. Returns a negative integer, zero, or a positive
     * integer as this object is less than, equal to, or greater than the specified object.
     * <p>
     * DynamicEnum constants are only comparable to other dynamicEnum constants of the same dynamicEnum type.  The
     * natural order implemented by this method is the order in which the constants are declared.
     */
    @Override
    public final int compareTo(@NotNull E other) {
        DynamicEnum<?> self = this;
        if (self.getClass() != other.getClass() && self.getDeclaringClass() != other.getDeclaringClass()) {
            throw new ClassCastException();
        }
        return self.ordinal - other.ordinal();
    }

    /**
     * Returns the Class object corresponding to this dynamicEnum constant's dynamicEnum type. Two dynamicEnum
     * constants e1 and e2 are of the same dynamicEnum type if and only if e1.getDeclaringClass() == e2
     * .getDeclaringClass().
     *
     * @return the Class object corresponding to this dynamicEnum constant's dynamicEnum type
     */
    @SuppressWarnings("unchecked")
    protected final Class<E> getDeclaringClass() {
        Class<?> clazz = getClass();
        Class<?> zuper = clazz.getSuperclass();
        return (zuper == DynamicEnum.class) ? (Class<E>) clazz : (Class<E>) zuper;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends DynamicEnum<?>> getDynamicEnumClass() {
        return (Class<? extends DynamicEnum<?>>) getClass();
    }

    /**
     * Returns the dynamicEnum constant of the specified dynamicEnum type with the specified name. The name must
     * match exactly an identifier used to declare an dynamicEnum constant in this type.  (Extraneous whitespace
     * characters aren't permitted.)
     *
     * @param <T>      The dynamicEnum type whose constant is to be returned
     * @param enumType the {@code Class} object of the dynamicEnum type from which
     *                 to return a constant
     * @param name     the name of the constant to return
     * @return the dynamicEnum constant of the specified dynamicEnum type with the
     * specified name
     */
    @SuppressWarnings("unchecked")
    public static <T extends DynamicEnum<T>> T valueOf(Class<? extends DynamicEnum<?>> enumType, String name) {
        return (T) elements.get(enumType).get(name);
    }

    public static <T extends DynamicEnum<T>> T valueOf(String name) {
        throw new IllegalStateException("Sub class of DynamicEnum must implement method valueOf()");
    }

    /**
     * prevent default deserialization
     */
    @Serial
    @Contract("_ -> fail")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("can't deserialize dynamicEnum");
    }

    @Serial
    @Contract(" -> fail")
    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("can't deserialize dynamicEnum");
    }

    public static <E> DynamicEnum<? extends DynamicEnum<?>>[] values() {
        throw new IllegalStateException("Sub class of DynamicEnum must implement method values()");
    }

    @SuppressWarnings("unchecked")
    public static <E> E[] values(Class<E> enumType) {
        Collection<DynamicEnum<?>> values = elements.get(enumType).values();
        int n = values.size();
        E[] typedValues = (E[]) Array.newInstance(enumType, n);
        int i = 0;
        for (DynamicEnum<?> value : values) {
            Array.set(typedValues, i, value);
            i++;
        }
        return typedValues;
    }

    /**
     * A common method for all enums since they can't have another base class
     *
     * @param <T>          Enum type
     * @param string       case insensitive
     * @param defaultValue The default value when a match is not found
     * @return corresponding enum, or null
     */
    protected static <T extends DynamicEnum<T>> T fromString(Class<? extends DynamicEnum<?>> c, String string,
                                                             String defaultValue) {
        string = Utils.ENUM_PATTERN.matcher(string).replaceAll("_").toUpperCase();
        T value = valueOf(c, string);
        if (value == null) value = valueOf(c, defaultValue);
        return value;
    }

    public static <T extends DynamicEnum<T>> T fromString(String string) {
        throw new IllegalStateException("Sub class of DynamicEnum must implement method fromString()");
    }

    public static void printByClass(Class<? extends DynamicEnum<?>> clazz) {
        System.out.println("------------------------------------------------------------------------");
        System.out.println("Enum: " + clazz.getSimpleName());
        System.out.println("Last ordinal: " + lastOrd.get(clazz).toString());
        System.out.println("Elements: " + elements.get(clazz).toString());
        System.out.println("------------------------------------------------------------------------");
    }

}
