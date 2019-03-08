import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileSender extends Remote {
    void sendFileToServer(String fileExt, byte[] buffer) throws RemoteException;
}
