package net.es.netshell.api;


/**
 * Generic type of persistent key/value pair.
 */
public class PersistentKeyValue {
    private String key;
    private String valueAsString;
    private int valueAsInt;
    private boolean valueAsBoolean;
    private boolean isString = false;
    private boolean isInteger = false;
    private boolean isBoolean = false;

    public PersistentKeyValue(String key, int value) {
        this.key = key;
        this.valueAsInt = value;
        this.isInteger = true;
    }

    public PersistentKeyValue(String key, String value) {
        this.key = key;
        this.valueAsString = value;
        this.isString = true;
    }

    public PersistentKeyValue(String key, Boolean value) {
        this.key = key;
        this.valueAsBoolean = value;
        this.isBoolean = true;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValueAsString() {
        return valueAsString;
    }

    public void setValueAsString(String valueAsString) {
        this.valueAsString = valueAsString;
    }

    public int getValueAsInt() {
        return valueAsInt;
    }

    public void setValueAsInt(int valueAsInt) {
        this.valueAsInt = valueAsInt;
    }

    public boolean isValueAsBoolean() {
        return valueAsBoolean;
    }

    public void setValueAsBoolean(boolean valueAsBoolean) {
        this.valueAsBoolean = valueAsBoolean;
    }

    public boolean isString() {
        return isString;
    }

    public void setIsString(boolean isString) {
        this.isString = isString;
    }

    public boolean isInteger() {
        return isInteger;
    }

    public void setIsInteger(boolean isInteger) {
        this.isInteger = isInteger;
    }

    public boolean isBoolean() {
        return isBoolean;
    }

    public void setIsBoolean(boolean isBoolean) {
        this.isBoolean = isBoolean;
    }
}
