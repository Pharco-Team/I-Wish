import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String action;
    private final Map<String, Object> data = new HashMap<>();

    public Request(String action) {
        this.action = action;
    }

    public Request put(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public String getAction() {
        return action;
    }

    public int getInt(String key) {
        return ((Number) data.get(key)).intValue();
    }

    public double getDouble(String key) {
        return ((Number) data.get(key)).doubleValue();
    }

    public String getString(String key) {
        return (String) data.get(key);
    }
}
