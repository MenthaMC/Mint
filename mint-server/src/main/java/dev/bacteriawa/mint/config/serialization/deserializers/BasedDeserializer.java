package dev.bacteriawa.mint.config.serialization.deserializers;

import dev.bacteriawa.mint.config.serialization.BaseDeserializer;
import dev.bacteriawa.mint.enums.EnumCollisionBehavior;
import dev.bacteriawa.mint.enums.EnumTripwireBehavior;

import java.lang.reflect.Field;

public class BasedDeserializer extends BaseDeserializer {
    @Override
    public Object deserialize(Field field, Object object) {
        if (object instanceof String string) {
            if (field.getType() == EnumTripwireBehavior.class) {
                return Enum.valueOf(EnumTripwireBehavior.class, string);
            } else if (field.getType() == EnumCollisionBehavior.class) {
                return Enum.valueOf(EnumCollisionBehavior.class, string);
            } else {
                return String.valueOf(object);
            }
        }

        return object;
    }
}
