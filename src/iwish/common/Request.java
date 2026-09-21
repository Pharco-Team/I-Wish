package iwish.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String action;
    public final Map<String, Object> params = new HashMap<>();

    public Request(String action) { this.action = action; }
    public Request put(String key, Object value) { params.put(key, value); return this; }
    public Object get(String key) { return params.get(key); }
}
