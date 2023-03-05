package com.alaimos.MITHrIL.api.Data.Records;

import com.alaimos.MITHrIL.api.Commons.Utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class RecordRepository<R extends Record> {

    protected Map<String, R> records = new HashMap<>();

    /**
     * Create a new record instance
     *
     * @param key  the key of the record
     * @param data the other data of the record
     * @return a new record instance
     */
    protected abstract R newInstance(String key, String[] data);

    /**
     * Get the class of the record
     *
     * @return the class of the record
     */
    protected abstract Class<R> getRecordClass();

    protected abstract String getKey(R record);

    protected abstract String getDefaultValue();

    /**
     * Get a record by its key
     *
     * @param key the key
     * @return the record or null if not found
     */
    public R valueOf(String key) {
        return records.get(key);
    }

    /**
     * Search for a record using a string
     *
     * @param key the key
     * @return the record or a default value if not found
     */
    public R fromString(String key) {
        key = Utils.ENUM_PATTERN.matcher(key).replaceAll("_").toUpperCase();
        var value = valueOf(key);
        if (value == null) value = valueOf(getDefaultValue());
        return value;
    }

    /**
     * Get all the records as a collection
     *
     * @return a collection of records
     */
    public Collection<R> valuesCollection() {
        return records.values();
    }

    /**
     * Get all the records as an array
     *
     * @return an array of records
     */
    public R[] values() {
        return records.values().toArray(Utils.genericArray(records.size(), getRecordClass()));
    }

    /**
     * Add a new record
     *
     * @param key the key of the record
     * @return the new record or the existing one if one with the same key already exists
     */
    public R add(String key) {
        return add(key, new String[0]);
    }

    /**
     * Add a new record
     *
     * @param key  the key of the record
     * @param data the other data of the record
     * @return the new record or the existing one if one with the same key already exists
     */
    public R add(String key, String... data) {
        return records.computeIfAbsent(key, k -> newInstance(key, data));
    }

    /**
     * Add a new record
     *
     * @param record the record to add
     * @return the new record or the existing one if one with the same key already exists
     */
    public R add(R record) {
        return records.computeIfAbsent(getKey(record), k -> record);
    }

}
