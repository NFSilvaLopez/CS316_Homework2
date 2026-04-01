package Server;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private static final String SERVER_FOLDER = "ServerFiles";

    private final SocketChannel serveChannel;
    private BufferedReader reader;
    private PrintWriter writer;
    private InputStream in;
    private OutputStream out;
    private boolean running;

    public ClientHandler(SocketChannel serveChannel) {
        this.serveChannel = serveChannel;
    }

    public void run() {
        try {
            in     = Channels.newInputStream(serveChannel);
            out    = Channels.newOutputStream(serveChannel);
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);

            new File(SERVER_FOLDER).mkdirs();
            running = true;

            while (running) {
                String request = reader.readLine();
                if (request == null) break;
                String[] parts = request.split("\\|");
                String command = parts[0];

                switch (command) {
                    case "LIST":
                        handleList(writer);
                        break;
                    case "DELETE":
                        handleDelete(parts[1], writer);
                        break;
                    case "RENAME":
                        handleRename(parts[1], parts[2], writer);
                        break;
                    case "DOWNLOAD":
                        handleDownload(parts[1], out, writer);
                        break;
                    case "UPLOAD":
                        handleUpload(parts[1], Long.parseLong(parts[2]), in, writer);
                        break;
                    case "QUIT":
                        running = false;
                        break;
                    default:
                        writer.println("ERROR|Unknown command");
                }
            }
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            try { serveChannel.close(); } catch (IOException ignored) {}
        }
    }

    private void handleList(PrintWriter writer) {
        File folder = new File(SERVER_FOLDER);
        File[] files = folder.listFiles();
        if (files == null) files = new File[0];
        writer.println(files.length);
        for (File file : files) {
            writer.println(file.getName());
        }
    }

    private void handleDelete(String filename, PrintWriter writer) {
        File file = new File(SERVER_FOLDER, filename);
        if (file.exists() && file.delete()) {
            writer.println("SUCCESS");
        } else {
            writer.println("FAIL");
        }
    }

    private void handleRename(String oldName, String newName, PrintWriter writer) {
        File oldFile = new File(SERVER_FOLDER, oldName);
        File newFile = new File(SERVER_FOLDER, newName);
        if (oldFile.exists() && oldFile.renameTo(newFile)) {
            writer.println("SUCCESS");
        } else {
            writer.println("FAIL");
        }
    }

    private void handleDownload(String filename, OutputStream out, PrintWriter writer) throws IOException {
        File file = new File(SERVER_FOLDER, filename);
        if (!file.exists()) {
            writer.println("-1");
            return;
        }
        writer.println(file.length());
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
        fis.close();
    }

    private void handleUpload(String filename, long fileSize,
                              InputStream in, PrintWriter writer) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(SERVER_FOLDER, filename));
        byte[] buffer = new byte[4096];
        long remaining = fileSize;
        while (remaining > 0) {
            int read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            remaining -= read;
            fos.write(buffer, 0, read);
        }
        fos.close();
        writer.println("SUCCESS");
    }
}