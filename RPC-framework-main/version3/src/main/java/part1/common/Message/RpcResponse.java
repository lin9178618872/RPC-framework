package part1.common.Message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse<T> implements Serializable {

    private int code;

    private String message;

    private Class<?> dataType;

    private T data;

    public static <T> RpcResponse<T> success(T data) {
        return RpcResponse.<T>builder()
                .code(200)
                .message("success")
                .dataType(data == null ? null : data.getClass())
                .data(data)
                .build();
    }

    public static <T> RpcResponse<T> fail(String message) {
        return RpcResponse.<T>builder()
                .code(500)
                .message(message)
                .build();
    }

    public static <T> RpcResponse<T> fail() {
        return fail("server error");
    }
}
