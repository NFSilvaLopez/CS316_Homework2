package Client;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class UserTask implements Runnable {
    private static final String CLIENT_FOLDER = "ClientFiles";

    private final String choice;
    private final String serverIP;
    private final int serverPort;

    public UserTask(String choice, String serverIP, int serverPort) {
        this.choice = choice;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public void run() {
        try {
            SocketChannel channel = SocketChannel.open(new InetSocketAddress(serverIP, serverPort));
            InputStream in   = Channels.newInputStream(channel);
            OutputStream out = Channels.newOutputStream(channel);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            PrintWriter writer    = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
            Scanner scanner = new Scanner(System.in);

            new File(CLIENT_FOLDER).mkdirs();

            switch (choice) {
                case "1": // List
                    writer.println("LIST");
                    int count = Integer.parseInt(reader.readLine());
                    for (int i = 0; i < count; i++) {
                        System.out.println(reader.readLine());
                    }
                    break;
                case "2": // Delete
                    System.out.print("Filename: ");
                    String del = scanner.nextLine();
                    writer.println("DELETE|" + del);
                    System.out.println(reader.readLine());
                    break;
                case "3": // Rename
                    System.out.print("Old name: ");
                    String oldName = scanner.nextLine();
                    System.out.print("New name: ");
                    String newName = scanner.nextLine();
                    writer.println("RENAME|" + oldName + "|" + newName);
                    System.out.println(reader.readLine());
                    break;
                case "4": // Download
                    System.out.print("Filename: ");
                    String download = scanner.nextLine();
                    writer.println("DOWNLOAD|" + download);
                    long fileSize = Long.parseLong(reader.readLine());
                    if (fileSize == -1) {
                        System.out.println("File not found.");
                        break;
                    }
                    FileOutputStream fos = new FileOutputStream(new File(CLIENT_FOLDER, download));
                    byte[] buffer = new byte[4096];
                    long remaining = fileSize;
                    while (remaining > 0) {
                        int read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                        remaining -= read;
                        fos.write(buffer, 0, read);
                    }
                    fos.close();
                    System.out.println("Download complete.");
                    break;
                case "5": // Upload
                    System.out.print("Filename: ");
                    String upload = scanner.nextLine();
                    File file = new File(CLIENT_FOLDER, upload);
                    if (!file.exists()) {
                        System.out.println("File not found.");
                        break;
                    }
                    writer.println("UPLOAD|" + upload + "|" + file.length());
                    FileInputStream fis = new FileInputStream(file);
                    byte[] upBuffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(upBuffer)) != -1) {
                        out.write(upBuffer, 0, bytesRead);
                    }
                    out.flush();
                    fis.close();
                    System.out.println(reader.readLine());
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
            channel.close();
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }
}