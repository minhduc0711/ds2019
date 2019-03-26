package storage;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Storage extends Remote, Serializable {
    byte[] read(String path) throws IOException;

    boolean createDirectory(String path) throws RemoteException;

    void write(String path, byte[] buffer, int numBytesRead) throws RemoteException;

    boolean delete(String path) throws RemoteException;

    String getId() throws RemoteException;
}
