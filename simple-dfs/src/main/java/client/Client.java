package client;

import login.Login;
import login.Session;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    static final String NAMING_SERVER_IP = null;

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry(NAMING_SERVER_IP);
            Login loginStub = (Login) registry.lookup("Login");
            Session session = loginStub.login();

            if (args.length == 0) {
                loop(session);
            }

            switch (args[0]) {
                case "upload":
                    if (args.length < 3) System.out.println("Not enough args");
                    uploadToServer(args[1], args[2], session);
                    break;
                default:
                    System.out.println("Invalid args");
            }

//            uploadToServer("/media/minhduc0711/Libraries/Pictures/Wallpapers/799035.jpg", "bar", session);

            session.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loop(Session session) throws IOException {
        Scanner scanner = new Scanner(System.in);
        ApplicationLoop:
        while (true) {
            System.out.print(session.getCurrentDir() + " $ ");
            String[] inputs = scanner.nextLine().split(" ");
            switch (inputs[0]) {
                case "ls":
                    String[] files = session.list();
                    System.out.println(Arrays.toString(files));
                    break;
                case "cd":
                    if (inputs.length < 2) {
                        System.out.println("Please specify a path");
                        break;
                    }
                    try {
                        session.changeDirectory(inputs[1]);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid path");
                    }
                    break;
                case "cat":
                    if (inputs.length < 2) {
                        System.out.println("Please specify a file");
                        break;
                    }
                    byte[] bytes = session.read(inputs[1]);
                    System.out.println(new String(bytes, StandardCharsets.UTF_8));
                    break;
                case "rm":
                    if (inputs.length < 2) {
                        System.out.println("Please specify a path");
                        break;
                    }
                    session.delete(inputs[1]);
                    break;
                case "mkdir":
                    if (inputs.length < 2) {
                        System.out.println("Please specify a path");
                        break;
                    }
                    session.createDirectory(inputs[1]);
                    break;
                case "q":
                    break ApplicationLoop;
                default:
                    System.out.println("Command not recognized");
            }
        }
    }

    private static void uploadToServer(String pathToFile, String dirOnServer, Session session) {
        Path pFile = Paths.get(pathToFile);
        Path pServer = Paths.get(dirOnServer, pFile.getFileName().toString());

        try {
            byte[] buffer = new byte[4096];
            FileInputStream fileInputStream = new FileInputStream(pathToFile);
            while (true) {
                int numBytesRead = fileInputStream.read(buffer);
                if (numBytesRead == -1) {
                    break;
                }
                session.uploadFile(pServer.toString(), buffer, numBytesRead);
            }
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
