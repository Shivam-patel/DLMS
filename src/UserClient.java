import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import LibInterface.LibUserInterface;

public class UserClient implements Runnable{
	private String userId;

	public UserClient(String userId) {
		this.userId = userId;
	}


	public void run() {
		System.out.println("Press 1 to borrow an item from the library");
		System.out.println("Press 2 to find an item in the library");
		System.out.println("Press 3 to return an to from the library");
		System.out.println("Press 4 to exit");
		Scanner in = new Scanner(System.in);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

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
				//		Scanner in = new Scanner(System.in);
				itemId = in.next();
				numberOfDays = in.nextInt();
				reply = user.borrowItem(userId, itemId, numberOfDays);
				if(!reply.equalsIgnoreCase("Success")) {
					System.out.println(reply + "Press 1 to add, else press 0.");
					if(in.nextInt()==1) {
						reply = user.addToWaitlist(userId, itemId, numberOfDays);
					}
					else {
						reply = "Exitting...";
					}
				}
				break;

			case 2: 
				String itemName;
				System.out.println("Enter item name");
				itemName = in.next();
				reply = user.findItem(userId, itemName);
				break;

			case 3:
				System.out.println("Enter itemId");
				itemId = in.next();
				reply = user.ReturnItem(userId, itemId);
				break;

			case 4: 
				System.exit(0);

				reply = null;
			} 
			System.out.println("The result of the operation is as follows " + reply);

			
		}catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		in.close();
	}

}
