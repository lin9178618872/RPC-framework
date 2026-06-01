package part1.common.serializer.myCode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import part1.common.Message.MessageType;
import part1.common.serializer.mySerializer.Serializer;

import java.util.List;

public class MyDecoder extends ByteToMessageDecoder {

    private static final int HEADER_LENGTH = 8;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < HEADER_LENGTH) {
            return;
        }

        in.markReaderIndex();

        short messageTypeCode = in.readShort();
        MessageType.fromCode(messageTypeCode);

        short serializerType = in.readShort();
        Serializer serializer = Serializer.getSerializerByCode(serializerType);

        if (serializer == null) {
            throw new RuntimeException("Unsupported serializer type: " + serializerType);
        }

        int length = in.readInt();

        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        byte[] body = new byte[length];
        in.readBytes(body);

        Object message = serializer.deserialize(body, messageTypeCode);
        out.add(message);
    }
}
