package login;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface Login extends Remote {
    Session login() throws RemoteException;

    void register(ArrayList<String> pathList, ArrayList<Boolean> isDirList, String storageId, String storageAddress) throws RemoteException;
}
