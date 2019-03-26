package naming;

import storage.Storage;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class NamingServer implements Naming {
    private static final int NUM_REPLICAS = 2;

    Naming namingSkeleton;
    ArrayList<Storage> connectedStorages;
    public DirectoryTreeNode rootNode;

    public NamingServer() {
        rootNode = new DirectoryTreeNode("/");
        connectedStorages = new ArrayList<>();
    }

    @Override
    public void register(ArrayList<String> pathList, ArrayList<Boolean> isDirList, String storageId, String storageAddress) throws RemoteException {
        assert pathList.size() == isDirList.size();

        try {
            Registry registry = LocateRegistry.getRegistry(storageAddress);
            Storage storageStub = (Storage) registry.lookup(storageId);

            for (int i = 0; i < pathList.size(); i += 1) {
                Path p = Paths.get(pathList.get(i));
                DirectoryTreeNode node = rootNode.getLastNodeInPath(p);
                if (node == null) {
                    rootNode.addPath(p, isDirList.get(i), storageStub);
                } else if (!isDirList.get(i)) {
                    node.getStorageList().add(storageStub);
                }
            }
            System.out.println(storageStub.getId() + " registered successfully!");
            connectedStorages.add(storageStub);
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Storage getStorage(String path) throws RemoteException, FileNotFoundException {
        Path p = Paths.get(path);
        DirectoryTreeNode fileNode = rootNode.getLastNodeInPath(p);
        if (fileNode == null) {
            throw new FileNotFoundException();
        }
        List<Storage> storageList = fileNode.getStorageList();

        for (Storage storage : storageList) {
            try {
                storage.getId();
                return storage;
            } catch (RemoteException e) {
                continue;
            }
        }
        return null;
    }

    @Override
    public synchronized void uploadFile(String path, byte[] buffer, int numBytesRead) throws RemoteException {
        Path p = Paths.get(path);

        // Delete if file exists
        DirectoryTreeNode fileNode;
//        if (fileNode != null) delete(path);

//        if (connectedStorages.size() < NUM_REPLICAS) {
//            throw new IllegalStateException("Not enough servers are running to replicate");
//        }

        List<Storage> luckyStorages = new ArrayList<>();
        while (luckyStorages.size() < NUM_REPLICAS) {
            int randomId = new Random().nextInt(connectedStorages.size());
            Storage storage = connectedStorages.get(randomId);
            if (!luckyStorages.contains(storage)) {
                luckyStorages.add(storage);
            }
        }

        for (Storage storage : luckyStorages) {
            fileNode = rootNode.getLastNodeInPath(p);
            if (fileNode == null) {
                rootNode.addPath(p, false, storage);
            } else {
                fileNode.getStorageList().add(storage);
            }
            try {
                storage.write(path, buffer, numBytesRead);
            } catch (RemoteException e) {
                continue;
            }
        }
    }

    public void delete(String path) throws RemoteException {
        Path p = Paths.get(path);
        DirectoryTreeNode node = rootNode.getLastNodeInPath(p);
        if ((node == null) || (node.toString().equals("/"))) {
            throw new IllegalArgumentException();
        } else if (node.isDir()) {
            Iterator<Map.Entry<String, DirectoryTreeNode>> it = node.getChildren().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, DirectoryTreeNode> entry = it.next();
                delete(entry.getValue().toString());
                it.remove();
            }
        } else {
            for (Storage storage : node.getStorageList()) {
                storage.delete(node.toString());
            }
        }
    }

    @Override
    public boolean isDirectory(String path) throws RemoteException {
        Path p = Paths.get(path);
        return rootNode.getLastNodeInPath(p).isDir();
    }

    @Override
    public boolean createDirectory(String path) throws RemoteException {
        boolean ret = true;
        Path p = Paths.get(path);
        for (Storage storage : connectedStorages) {
            if (!storage.createDirectory(path)) {
                ret = false;
            }
        }
        return (ret && rootNode.addPath(p, true, null));
    }

//    public static void main(String[] args) {
//        NamingServer namingServer = new NamingServer();
//        namingServer.start();
//    }
}
