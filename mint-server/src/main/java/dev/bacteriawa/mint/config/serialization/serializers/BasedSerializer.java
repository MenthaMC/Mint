package dev.bacteriawa.mint.config.serialization.serializers;

import dev.bacteriawa.mint.config.serialization.BaseSerializer;

import java.lang.reflect.Field;

public class BasedSerializer extends BaseSerializer {
    @Override
    public Object serialize(Field field, Object object) {
        if (object instanceof Enum<?> e)
            return e.name();

        return object;
    }
}
