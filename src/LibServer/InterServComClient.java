package LibServer;

import model.DataModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InterServComClient {
   /* private final static Logger logger = Logger.getLogger(InterServComClient.class.getName());
    static private FileHandler fileTxt ;*/
    private final int port;
    private int flag ;
    public InterServComClient(int port, int flag) throws IOException {
        this.port = port;
        this.flag = flag;
      /*  logger.setLevel(Level.INFO);
        fileTxt = new FileHandler("InerServComServer.txt");
        logger.addHandler(fileTxt);*/

    }

    public String operate(DataModel pack){
        String defaultReply = "System Failure";
        try{
            DatagramSocket aSocket = new DatagramSocket();
            System.out.println("I am a caller. calling...");
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            ObjectOutput oo = new ObjectOutputStream(bStream);
            pack.setFlag(flag);
            oo.writeObject(pack);


            byte[] message = bStream.toByteArray();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, message.length, aHost, port);
            System.out.println("sending...");
            aSocket.send(request);

            byte [] buffer1 = new byte[1000];
            DatagramPacket rep = new DatagramPacket(buffer1, buffer1.length);
            aSocket.receive(rep);
           // buffer = reply.getData();

            String replyString = new String(rep.getData());
            System.out.println(replyString);
            return replyString;

        }catch(SocketException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return defaultReply;
    }
/*
    public String find(DataModel pack) {
        return null;
    }
    public String returnItem(DataModel pack) {
        return null;
    }
    public String borrowMCG(String userId, String itemId, int numberOfDays) {

        return null;
    }
    public String borrowMON(String userId, String itemId, int numberOfDays) {

        return null;
    }
    public String findMCG(String userId, String itemName) {
        return null;
    }
    public String findMON(String userId, String itemName) {
        return null;
    }

    public String returnMON(String userId, String itemId) {
        return null;
    }
    public String returnMCG(String userId, String itemId) {
        return null;
    }
*/

}
