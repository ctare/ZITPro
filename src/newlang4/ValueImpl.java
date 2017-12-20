package newlang4;

import java.util.HashMap;

/**
 * Created by ctare on 2017/11/07.
 */
public class ValueImpl implements Value {
    private Object value;
    private ValueType type;

    public ValueImpl(Object value, ValueType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public String getSValue() {
        return value.toString();
    }

    @Override
    public int getIValue() {
        return (int) value;
    }

    @Override
    public double getDValue() {
        return (double) value;
    }

    @Override
    public boolean getBValue() {
        return (boolean) value;
    }

    @Override
    public ValueType getType() {
        return type;
    }
}
