import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    private static DatagramSocket socket;
    private static String name;
    private static InetAddress ip;
    private static int port;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Incorrect arguments. Correct usage\n" +
                    "java Client ipaddress port\n" +
                    "where ipaddress and port are the same as the server");
            System.exit(0);
        }
        port = Integer.parseInt(args[1]);
        try {
            ip = InetAddress.getByName(args[0]);
            socket = new DatagramSocket();
        } catch (UnknownHostException | SocketException e) {
            System.out.println(e);
            System.exit(0);
        }
        System.out.println(">>>>>>>Network: Connection Established!<<<<<<<\n\n");
        System.out.println("Choose your username (no spaces):");
        Scanner input = new Scanner(System.in);
        name = input.next();

        System.out.println("CLIENT: Registering new user " + name + " on server...");
        Segment connectionRequest = new Segment(Segment.SegmentType.connectionRequest, name);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream to = new ObjectOutputStream(out);
        to.writeObject(connectionRequest);

        byte[] payload = out.toByteArray();
        DatagramPacket request = new DatagramPacket(payload, payload.length, ip, port);
        socket.send(request);

        byte[] response = new byte[512];
        DatagramPacket incomingResponse = new DatagramPacket(response, response.length);
        System.out.println("SERVER: Waiting for acknowledgment");
        socket.receive(incomingResponse);

        ByteArrayInputStream in = new ByteArrayInputStream(response);
        ObjectInputStream as = new ObjectInputStream(in);
        try {
            Segment responseSegment = (Segment) as.readObject();
            if (!name.equals(responseSegment.getName())) {
                System.out.println("*******CLIENT: Server gave wrong user acknowledgment - terminating*******");
                System.exit(0);
            }
        } catch (ClassNotFoundException e) {
            System.out.println(e);
            System.exit(0);
        }
        System.out.println("CLIENT: Ready to send messages to server! :)");
        while (true) {
            messagingThread();
        }
    }

    public static void messagingThread() {

        Callable<Void> receiveMessages = () -> {
            receiveMessages();
            return null;
        };

        Callable<Void> sendMessage = () -> {
            sendMessage();
            return null;
        };

        List<Callable<Void>> taskList = new ArrayList<>();
        taskList.add(receiveMessages);
        taskList.add(sendMessage);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            executor.invokeAll(taskList);
        } catch (InterruptedException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    private static void receiveMessages() throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        socket.receive(incoming);

        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        ObjectInputStream as = new ObjectInputStream(in);
        try {
            Segment message = (Segment) as.readObject();
            if (message.getType() == Segment.SegmentType.message) {
                System.out.printf("%s: %s", message.getName(), message.getMessage());
            } else {
            }
        } catch (ClassNotFoundException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    private static void sendMessage() throws IOException {
        Scanner s = new Scanner(System.in);
        String rawMessage = s.nextLine();
        if (rawMessage.equals("/exit")) {
            System.exit(0);
        }
        Segment message = new Segment(Segment.SegmentType.message, name, (rawMessage+"\n"));

        ByteArrayOutputStream to = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(to);
        out.writeObject(message);
        byte[] payload = to.toByteArray();

        DatagramPacket packet = new DatagramPacket(payload, payload.length, ip, port);
        socket.send(packet);
    }
}
