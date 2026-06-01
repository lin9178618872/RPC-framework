package part1.common.serializer.myCode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import part1.common.Message.MessageType;
import part1.common.Message.RpcRequest;
import part1.common.Message.RpcResponse;
import part1.common.serializer.mySerializer.Serializer;

public class MyEncoder extends MessageToByteEncoder<Object> {

    private final Serializer serializer;

    public MyEncoder(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        if (msg instanceof RpcRequest) {
            out.writeShort(MessageType.REQUEST.getCode());
        } else if (msg instanceof RpcResponse) {
            out.writeShort(MessageType.RESPONSE.getCode());
        } else {
            throw new RuntimeException("Unsupported message class: " + msg.getClass());
        }

        out.writeShort(serializer.getType());

        byte[] body = serializer.serialize(msg);

        out.writeInt(body.length);
        out.writeBytes(body);
    }
}
