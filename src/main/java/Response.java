import java.io.Serializable;

public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean ok;
    private final String message;
    private final Object data;

    private Response(boolean ok, String message, Object data) {
        this.ok = ok;
        this.message = message;
        this.data = data;
    }

    public static Response ok(Object data) {
        return new Response(true, null, data);
    }

    public static Response message(String message) {
        return new Response(true, message, null);
    }

    public static Response fail(String message) {
        return new Response(false, message, null);
    }

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return (ok ? "OK" : "FAIL") + (message == null ? "" : " - " + message);
    }
}
