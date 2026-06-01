package part1.common.serializer.mySerializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import part1.common.Message.MessageType;
import part1.common.Message.RpcRequest;
import part1.common.Message.RpcResponse;

public class JsonSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) {
        return JSONObject.toJSONBytes(obj);
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        if (messageType == MessageType.REQUEST.getCode()) {
            RpcRequest request = JSON.parseObject(bytes, RpcRequest.class);

            Object[] params = request.getParams();
            Class<?>[] paramTypes = request.getParamsType();

            if (params != null && paramTypes != null) {
                Object[] convertedParams = new Object[params.length];

                for (int i = 0; i < params.length; i++) {
                    if (params[i] instanceof JSONObject) {
                        convertedParams[i] =
                                JSONObject.toJavaObject((JSONObject) params[i], paramTypes[i]);
                    } else if (paramTypes[i] != null && !paramTypes[i].isInstance(params[i])) {
                        convertedParams[i] = JSON.parseObject(JSON.toJSONString(params[i]), paramTypes[i]);
                    } else {
                        convertedParams[i] = params[i];
                    }
                }

                request.setParams(convertedParams);
            }

            return request;
        }

        if (messageType == MessageType.RESPONSE.getCode()) {
            RpcResponse response = JSON.parseObject(bytes, RpcResponse.class);

            if (response.getData() != null && response.getDataType() != null) {
                Object data = response.getData();

                if (data instanceof JSONObject) {
                    response.setData(
                            JSONObject.toJavaObject((JSONObject) data, response.getDataType())
                    );
                } else if (!response.getDataType().isInstance(data)) {
                    response.setData(JSON.parseObject(JSON.toJSONString(data), response.getDataType()));
                }
            }

            return response;
        }

        throw new RuntimeException("Unsupported message type: " + messageType);
    }

    @Override
    public int getType() {
        return 1;
    }
}
