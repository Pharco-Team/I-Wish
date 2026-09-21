package iwish.common;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;
    public final boolean ok;
    public final String message;
    public final Serializable data;

    public Response(boolean ok, String message, Serializable data) {
        this.ok = ok; this.message = message; this.data = data;
    }
    public static Response ok(String msg, Serializable data) { return new Response(true, msg, data); }
    public static Response fail(String msg) { return new Response(false, msg, null); }
}
