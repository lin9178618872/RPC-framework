package part1.common.serializer.mySerializer;

import java.io.*;

public class ObjectSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) {

        if (obj == null) {
            return new byte[0];
        }

        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos)
        ) {
            oos.writeObject(obj);
            oos.flush();

            return bos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Object serialize failed", e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {

        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bis)
        ) {
            return ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Object deserialize failed", e);
        }
    }

    @Override
    public int getType() {
        return 0;
    }
}