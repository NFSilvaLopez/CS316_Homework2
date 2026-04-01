package Client;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class TCPFileClientMultithreaded {
    public static void main(String[] args) throws Exception {
        if(args.length != 2){
            System.out.println("Please specify <serverIP> and <serverPort>");
            return;
        }
        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);

        ExecutorService es = Executors.newFixedThreadPool(4);
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.println(
                    "1. List\n2. Delete\n3. Rename\n4. Download\n5. Upload\n6. Quit\nPlease choose an option: ");
            String choice = scanner.nextLine().trim();
            if (choice.equalsIgnoreCase("Q") || choice.equals("6")){
                break;
            }
            es.submit(new UserTask(choice,serverIP,serverPort));
        }
        es.shutdown();
    }
}
