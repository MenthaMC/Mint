package dev.bacteriawa.mint.config.serialization;

import java.lang.reflect.Field;

public abstract class BaseSerializer {
    public abstract Object serialize(Field field, Object object);
}
