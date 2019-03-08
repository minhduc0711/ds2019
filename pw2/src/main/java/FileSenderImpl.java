import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;

public class FileSenderImpl implements FileSender {

    @Override
    public void sendFileToServer(String fileExt, byte[] buffer) throws RemoteException {
        File receivedFile = new File("received." + fileExt);
        try {
            receivedFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(receivedFile, true);
            fileOutputStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
