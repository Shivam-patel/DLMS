package LibServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerDriver {
    /** This method is the main driver method that initializes and instantiates all the servers and abstractions required for effortless communication
     * (client-server as well as Inter-Server communication)
     * This method creates the RMI registry as well and uploads the required server objects.
     * @param args
     * @throws Exception
     */
    public static void main(String[]args) throws Exception{
        ConServer cs = new ConServer();
        McgServer mc = new McgServer();
        MonServer ms = new MonServer();
        InterServComServer mcg = new InterServComServer(1);
        InterServComServer mon = new InterServComServer(2);
        InterServComServer con = new InterServComServer(3);
        Registry registry = LocateRegistry.createRegistry(8089);

        registry.bind("CON",cs);
        registry.bind("MCG",mc);
        registry.bind("MON", ms);
       Thread interServCon = new Thread(con);
       interServCon.start();
        Thread interServMon = new Thread(mon);
        interServMon.start();
        Thread interServmcg = new Thread(mcg);
        interServmcg.start();
        Thread mont = new Thread(ms);
        mont.start();
        Thread conc = new Thread(cs);
        conc.start();
        Thread mcgi = new Thread(mc);
        mcgi.start();

        System.out.println("Server ready");
    }
}
