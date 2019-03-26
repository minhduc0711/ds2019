package storage;

import login.Login;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class StorageServer implements Storage {
    private String namingAddress;

    private String storageAddress;
    private String STORAGE_DIR = "localStorage/";
    private Storage storageSkeleton;
    public String storageId;

    public StorageServer(String namingAddress, String storageAddress, int storagePort) {
        this.namingAddress = namingAddress;
        this.storageAddress = storageAddress;
        storageId = "Storage " + storageAddress + ":" + storagePort;
        try {
            storageSkeleton = (Storage) UnicastRemoteObject.exportObject(this, storagePort);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(storageId, storageSkeleton);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        ArrayList<String> pathList = new ArrayList<>();
        ArrayList<Boolean> isDirList = new ArrayList<>();
        try {
            Files.walk(Paths.get(STORAGE_DIR)).forEach(p -> {
                pathList.add("/" + p.toString().replace(STORAGE_DIR, ""));
                isDirList.add(Files.isDirectory(p));
            });
            isDirList.remove(0);
            pathList.remove(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Registry registry = LocateRegistry.getRegistry(this.namingAddress);
            Login loginStub = (Login) registry.lookup("Login");
            loginStub.register(pathList, isDirList, storageId, this.storageAddress);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] read(String path) throws IOException {
        System.out.println(storageId);
        Path p = Paths.get(path);
        Path localPath = convertToLocalPath(p);
        InputStream inputStream = new FileInputStream(localPath.toString());
        return IOUtils.toByteArray(inputStream);
    }

    @Override
    public boolean createDirectory(String path) throws RemoteException {
        Path p = Paths.get(path);
        Path localPath = convertToLocalPath(p);

        File dir = new File(localPath.toString());
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return false;
    }

    @Override
    public synchronized void write(String path, byte[] buffer, int numBytesRead) throws RemoteException {
        Path p = Paths.get(path);
        createDirectory(p.getParent().toString());
        Path localPath = convertToLocalPath(p);

        File file = new File(localPath.toString());
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            fileOutputStream.write(buffer, 0, numBytesRead);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean delete(String path) throws RemoteException {
        Path p = Paths.get(path);
        Path localPath = convertToLocalPath(p);
        File file = new File(localPath.toString());
        return file.delete();
    }

    @Override
    public String getId() throws RemoteException {
        return storageId;
    }

    private Path convertToLocalPath(Path remotePath) {
        return Paths.get(STORAGE_DIR + "/" + remotePath);
    }

    public static void main(String[] args) {
        StorageServer storageServer = new StorageServer("192.168.10.103", null, 11111);
        storageServer.start();
    }
}
