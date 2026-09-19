public class Friend {

    private int userId;
    private String username;

    public Friend(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }

    @Override
    public String toString() {
        return username;
    }
}
