package part1.common.Message;

public enum MessageType {

    REQUEST(1),
    RESPONSE(2);

    private final int code;

    MessageType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MessageType fromCode(int code) {
        for (MessageType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new RuntimeException("Unsupported message type: " + code);
    }
}