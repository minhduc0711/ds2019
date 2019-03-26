import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    public static void main(String[] args) {
        FileSenderImpl fileSenderImpl = new FileSenderImpl();

        try {
            FileSender skeleton = (FileSender) UnicastRemoteObject.exportObject(fileSenderImpl, 2345);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("FileSender", skeleton);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server error");
            e.printStackTrace();
        }
    }
}
