import com.google.common.io.Files;

import java.io.FileInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Permission;

public class Client {
    private static final int BUFFER_SIZE = 4096;
    private static final String FILE_NAME = "anhlaai.mp3";

    private static class MySecurityManager extends SecurityManager {
        @Override
        public void checkPermission(Permission perm) {}
    }

    public static void main(String[] args) {
        try {
            SecurityManager sm = new MySecurityManager();
            System.setSecurityManager(sm);
            Registry registry = LocateRegistry.getRegistry(null);
            FileSender stub = (FileSender) registry.lookup("FileSender");

            byte[] buffer = new byte[BUFFER_SIZE];
            int numBytesRead;
            FileInputStream fileInputStream = new FileInputStream(FILE_NAME);

            while (true) {
                numBytesRead = fileInputStream.read(buffer);
                if (numBytesRead == -1) {
                    break;
                }
                stub.sendFileToServer(Files.getFileExtension(FILE_NAME), buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
