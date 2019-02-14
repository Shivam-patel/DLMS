import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import LibInterface.LibUserInterface;

/**
 * <p>The class UserClient is a runnable class that recieves the handle whenever a clientid is identified as a User.</p>
 * @see Runnable
 * @author shiv
 *
 */
public class UserClient implements Runnable{
	private String userId;
	private final static Logger logger = Logger.getLogger(UserClient.class.getName());
	static private FileHandler fileTxt ;


	public UserClient(String userId) throws IOException {
		this.userId = userId;
		logger.setLevel(Level.INFO);
		fileTxt = new FileHandler("UserClientLog.txt");
		logger.addHandler(fileTxt);
	}


	public void run() {
		System.out.println("Press 1 to borrow an item from the library");
		System.out.println("Press 2 to find an item in the library");
		System.out.println("Press 3 to return an to from the library");
		System.out.println("Press 4 to exit");
		Scanner in = new Scanner(System.in);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		logger.info(" userClient: run");
		int op = 0;
		try {
			op = Integer.parseInt(br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		String libCode = "";
		libCode = libCode.concat(userId.substring(0,3));
		System.out.println(libCode);
		String reply = null;
		try {
			Registry userRegistry = LocateRegistry.getRegistry(8089);
			LibUserInterface user;
			user = (LibUserInterface)userRegistry.lookup(libCode);  
			switch(op) {
			case 1:
				String itemId;
				int numberOfDays;
				System.out.println("Enter corresponding itemId, and number of borrowing days");
				itemId = in.next();
				numberOfDays = in.nextInt();
				logger.info("borrowItem");
				reply = user.borrowItem(userId, itemId, numberOfDays);
				logger.info(userId+"\t"+itemId+"\t"+numberOfDays);
				logger.info(reply);
				if(reply.startsWith("waitlist")) {
					System.out.println(reply + "Press 1 to add, else press 0.");
					if(in.nextInt()==1) {
						logger.info("Add to waitlist");
						reply = user.addToWaitlist(userId, itemId, numberOfDays);
						logger.info(reply);
					}
					else {
						reply = "Exitting...";
						logger.info(reply);
					}
				}
				break;

			case 2: 
				String itemName;
				logger.info("findItem");
				System.out.println("Enter item name");
				itemName = in.next();
				logger.info(itemName);
				reply = user.findItem(userId, itemName);
					logger.info(reply);

				break;

			case 3:
				System.out.println("Enter itemId");
				itemId = in.next();
				logger.info("return item");
				logger.info(itemId);
				reply = user.ReturnItem(userId, itemId);
				break;

			case 4: 
				System.exit(0);

				reply = null;
			}
			logger.info(reply);
			System.out.println("The result of the operation is as follows \n" + reply);



			
		}catch (RemoteException e) {

			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		in.close();
	}

}
