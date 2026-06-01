package part1.Client.rpcClient;

import part1.common.Message.RpcRequest;
import part1.common.Message.RpcResponse;


public interface   RpcClient {

    RpcResponse sendRequest(RpcRequest request);
}
