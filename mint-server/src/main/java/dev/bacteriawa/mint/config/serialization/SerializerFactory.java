package dev.bacteriawa.mint.config.serialization;

import dev.bacteriawa.mint.config.serialization.deserializers.BasedDeserializer;
import dev.bacteriawa.mint.config.serialization.serializers.BasedSerializer;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.WeakHashMap;

public class SerializerFactory {
    private static final Map<Class<?>, BaseSerializer> serializers = new WeakHashMap<>();
    private static final Map<Class<?>, BaseDeserializer> deserializers = new WeakHashMap<>();
    private static final BasedSerializer BASED_SERIALIZER = new BasedSerializer();
    private static final BasedDeserializer BASED_DESERIALIZER = new BasedDeserializer();

    public static Object deserialize(Field field, Object object) {
        BaseDeserializer deserializer = deserializers.get(field.getType());
        if (deserializer == null)
            return BASED_DESERIALIZER.deserialize(field, object);

        return deserializer.deserialize(field, object);
    }

    public static Object serialize(Field field, Object object) {
        BaseSerializer serializer = serializers.get(object.getClass());
        if (serializer == null)
            return BASED_SERIALIZER.serialize(field, object);

        return serializer.serialize(field, object);
    }
}
