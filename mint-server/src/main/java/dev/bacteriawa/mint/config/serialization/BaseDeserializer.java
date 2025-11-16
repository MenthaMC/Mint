package dev.bacteriawa.mint.config.serialization;

import java.lang.reflect.Field;

public abstract class BaseDeserializer {
    public abstract Object deserialize(Field field, Object object);
}
