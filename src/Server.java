import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class Server {

    private static DatagramSocket socket;
    private static ArrayList<User> users = new ArrayList<>();
    private static int port;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Incorrect arguments. Correct Usage:\n" +
                    "java Server port\n" +
                    "where port is for all clients and the server to bind to");
            System.exit(0);
        }
        port = Integer.parseInt(args[0]);
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.out.println(e.toString());
            System.exit(0);
        }
        System.out.println("SERVER: Listening on port " + port);
        while (true) {
            byte[] incoming = new byte[1024];
            DatagramPacket incomingPacket = new DatagramPacket(incoming, incoming.length);
            socket.receive(incomingPacket);

            ByteArrayInputStream in = new ByteArrayInputStream(incoming);
            ObjectInputStream as =new ObjectInputStream(in);
            try {
                Segment incomingSegment = (Segment) as.readObject();
                if (incomingSegment.getType() == Segment.SegmentType.connectionRequest) {
                    handleConnectionRequest(incomingSegment, incomingPacket.getAddress(), incomingPacket.getPort());
                } else if (incomingSegment.getType() == Segment.SegmentType.message) {
                    broadcastMessage(incomingSegment);
                }
            } catch (ClassNotFoundException e) {
                System.out.println(e);
                System.exit(0);
            }
        }
    }

    private static void handleConnectionRequest(Segment request, InetAddress ip, int port) throws IOException {
        System.out.println("SERVER: Received connection request from " + request.getName() + " at " + ip);
        User userToAdd = new User(request.getName(), ip, port);
        users.add(userToAdd);
        System.out.println("SERVER: Registered new user "+ request.getName());

        Segment response = new Segment(Segment.SegmentType.connectionAck, request.getName());

        ByteArrayOutputStream to = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(to);
        out.writeObject(response);
        byte[] payload = to.toByteArray();
        DatagramPacket responsePacket = new DatagramPacket(payload, payload.length, ip, port);

        socket.send(responsePacket);
        System.out.println("SERVER: Sent response!");
    }

    private static void broadcastMessage(Segment message) throws IOException {
        for (User user : users) {
            ByteArrayOutputStream to = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(to);
            out.writeObject(message);
            byte[] payload = to.toByteArray();
            DatagramPacket response = new DatagramPacket(payload, payload.length, user.getIp(), user.getPort());

            socket.send(response);
            System.out.println("SERVER: Sent \"" + message.getMessage() + "\" from " + message.getName() + " to " + user);
        }
    }
}
