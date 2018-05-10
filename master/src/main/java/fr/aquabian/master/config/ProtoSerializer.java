package fr.aquabian.master.config;

import com.google.protobuf.AbstractMessageLite;
import org.axonframework.serialization.*;
import org.axonframework.serialization.json.JacksonSerializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProtoSerializer implements Serializer {

    private static final String PARSE_FROM = "parseFrom";

    private JacksonSerializer jacksonSerializer;

    public ProtoSerializer() {
        this.jacksonSerializer = new JacksonSerializer();
    }


    @Override
    public <T> SerializedObject<T> serialize(Object object, Class<T> expectedRepresentation) {
        if (object instanceof AbstractMessageLite && byte[].class.equals(expectedRepresentation)) {
            byte[] bytes = ((AbstractMessageLite) object).toByteArray();
            return new SimpleSerializedObject<>((T) bytes, expectedRepresentation,
                    typeForClass(object.getClass()));
        }
        return jacksonSerializer.serialize(object, expectedRepresentation);
    }

    @Override
    public <T> boolean canSerializeTo(Class<T> expectedRepresentation) {
        return byte[].class.equals(expectedRepresentation);
    }

    @Override
    public <S, T> T deserialize(SerializedObject<S> serializedObject) {
        try {
            Class<T> clazz = classForType(serializedObject.getType());
            if (byte[].class.equals(serializedObject.getContentType())) {
                Method method = clazz.getDeclaredMethod(PARSE_FROM, byte[].class);
                T result = (T) method.invoke(null, serializedObject.getData());
                return result;
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return jacksonSerializer.deserialize(serializedObject);
        }
        return jacksonSerializer.deserialize(serializedObject);
    }

    @Override
    public Class classForType(SerializedType type) throws UnknownSerializedTypeException {
        try {
            return getClass().getClassLoader().loadClass(resolveClassName(type));
        } catch (ClassNotFoundException e) {
            throw new UnknownSerializedTypeException(type, e);
        }
    }

    @Override
    public SerializedType typeForClass(Class type) {
        return new SimpleSerializedType(type.getName(), null);
    }


    private String resolveClassName(SerializedType serializedType) {
        return serializedType.getName();
    }

    @Override
    public Converter getConverter() {
        return jacksonSerializer.getConverter();
    }
}
