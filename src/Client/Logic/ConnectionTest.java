package Client.Logic;

public class ConnectionTest {
    public static void main(String[] args) {
        Connection con = new Connection("localhost", 8080);

        var data = con.connect("Hello from java client!");
        System.out.println("[Server] " + data);

        var data2 = Connection.connect("localhost", 8080, "Hello from java client2!");
        System.out.println("[Server] " + data2);

    }
}
