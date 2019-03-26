package login;

import naming.DirectoryTreeNode;
import naming.NamingServer;
import storage.Storage;

import java.io.IOException;
import java.nio.file.Paths;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;

public class SessionImpl extends UnicastRemoteObject implements Session, Unreferenced {
    private NamingServer namingServer;
    DirectoryTreeNode currentNode;

    public SessionImpl(NamingServer namingServer) throws RemoteException {
        super();
        this.namingServer = namingServer;
        currentNode = namingServer.rootNode;
    }

    @Override
    public void changeDirectory(String path) throws RemoteException {
        String[] nodeNames = path.split("/");
        DirectoryTreeNode temp = currentNode;
        for (int i = 0; i < nodeNames.length; i += 1) {
            if ((nodeNames[i].equals("..")) && (temp.getParent() != null)) {
                temp = temp.getParent();
            }
            else if (temp.getChildren().containsKey(nodeNames[i])) {
                temp = temp.getChildren().get(nodeNames[i]);
            } else {
                throw new IllegalArgumentException();
            }
        }
        currentNode = temp;
    }

    @Override
    public String[] list() throws RemoteException {
        return currentNode.getChildren().keySet().toArray(new String[currentNode.getChildren().size()]);
    }

    @Override
    public String getCurrentDir() throws RemoteException {
        return currentNode.toString();
    }

    @Override
    public byte[] read(String path) throws IOException {
        String fullPath = Paths.get(currentNode.toString(), path).toString();
        Storage storage = namingServer.getStorage(fullPath);
        return storage.read(fullPath);
    }

    @Override
    public void delete(String path) throws RemoteException {
        String fullPath = Paths.get(currentNode.toString(), path).toString();
        namingServer.delete(fullPath);
    }

    @Override
    public void uploadFile(String path, byte[] buffer, int numBytesRead) throws RemoteException {
        namingServer.uploadFile(path, buffer, numBytesRead);
    }

    @Override
    public boolean createDirectory(String path) throws RemoteException {
        return namingServer.createDirectory(path);
    }

    public void logout() throws RemoteException {
        unexportObject(this, true);
    }

    public void unreferenced()
    {
        try {
            unexportObject(this, true);
        } catch (NoSuchObjectException e) {
            e.printStackTrace();
        }
    }
}
