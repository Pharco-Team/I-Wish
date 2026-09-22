import java.util.Scanner;

public class ServerMain {

    public static void main(String[] args) {
        Server server = new Server();
        Scanner scanner = new Scanner(System.in);

        DBConnection.initSchema();
        System.out.println("Commands: start | stop | additem | exit");

        while (true) {
            System.out.print("> ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                switch (command) {
                    case "start":
                        server.start();
                        break;
                    case "stop":
                        server.stop();
                        break;
                    case "additem":
                        addItem(scanner);
                        break;
                    case "exit":
                        if (server.isRunning()) {
                            server.stop();
                        }
                        return;
                    default:
                        System.out.println("Commands: start | stop | additem | exit");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void addItem(Scanner scanner) {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        System.out.print("Price: ");
        double price = Double.parseDouble(scanner.nextLine().trim());

        System.out.println(new AdminDAO().addCatalogItem(name, description, price)
                ? "Item added." : "Failed to add the item.");
    }
}
