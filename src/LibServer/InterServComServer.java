package LibServer;


    import LibInterface.LibUserInterface;
    import model.DataModel;

    import java.io.*;
    import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
    import java.rmi.NotBoundException;
    import java.rmi.registry.LocateRegistry;
    import java.rmi.registry.Registry;

public class InterServComServer implements  Runnable{
    private int flag;
    private int port;
    int MCG = 13131;
    int MON = 13132;
    int CON = 13133;
    DatagramSocket activeSocket = null;

        public InterServComServer(int flag) {
         try {
             if(flag == 1 || flag == 4 || flag == 7){
                 activeSocket = new DatagramSocket(MCG);
                 System.out.println("mcg active");
             }
             else if(flag == 2 || flag == 5 || flag == 8) {
                 activeSocket = new DatagramSocket(CON);
                 System.out.println("con active");
             }
             else {
                 activeSocket = new DatagramSocket(MON);
                 System.out.println("mon active");
             }
            }catch(Exception e){
                e.printStackTrace();
            }

        }


    @Override
    public void run() {
        while(true) {
                byte[] buffer = new byte[65000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                ObjectInputStream iStream = null;
                try {
                    activeSocket.receive(request);
                    iStream = new ObjectInputStream(new ByteArrayInputStream(request.getData()));
                    DataModel pack = (DataModel) iStream.readObject();
                    iStream.close();
                    int op = pack.getFlag();
                    Registry registry = LocateRegistry.getRegistry(8089);
                    LibUserInterface mcgUser = null;
                    LibUserInterface conUser = null;
                    LibUserInterface monUser = null;

                    String reply = "";
                    switch (op) {
                        case 1:
                            System.out.println("I was here");
                            mcgUser = (LibUserInterface)registry.lookup(pack.getUserId().substring(0,3));
                            reply = mcgUser.borrowItem(pack.getUserId(),pack.getItemId(),pack.getDaysToBorrow());
                            break;
                        case 4:
                            reply = mcgUser.findItem(pack.getUserId(),pack.getItemName());
                            break;
                        case 7:
                            reply = mcgUser.ReturnItem(pack.getUserId(),pack.getItemId());
                            break;
                        case 2:
                            monUser = (LibUserInterface)registry.lookup(pack.getUserId().substring(0,3));
                            reply = monUser.borrowItem(pack.getUserId(),pack.getItemId(),pack.getDaysToBorrow());
                            break;
                        case 5:
                            reply = monUser.findItem(pack.getUserId(),pack.getItemName());
                            break;
                        case 8:
                            reply = monUser.ReturnItem(pack.getUserId(),pack.getItemId());
                            break;
                        case 3:
                            conUser = (LibUserInterface)registry.lookup(pack.getUserId().substring(0,3));
                            reply = conUser.borrowItem(pack.getUserId(),pack.getItemId(),pack.getDaysToBorrow());
                            break;
                        case 6:
                            reply = conUser.findItem(pack.getUserId(),pack.getItemName());
                            break;
                        case 9:
                            reply = conUser.ReturnItem(pack.getUserId(),pack.getItemId());
                            break;



                    }
                    DatagramPacket response;
                    System.out.println(reply);
                    byte[] rep = reply.getBytes();
                    response = new DatagramPacket(rep, rep.length,request.getAddress(),request.getPort());
                    activeSocket.send(response);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
        }
    }

}
