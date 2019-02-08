import LibInterface.LibManagerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ManagerClient implements Runnable{
	private String managerId;
	public ManagerClient(String managerId) {
		this.managerId = managerId;
	}

	public void run() {
		System.out.println("Press 1 to add a new User in the library");
		System.out.println("Press 2 to add a new Manager in the library");
		System.out.println("Press 3 to add an item to the library");
		System.out.println("Press 4 to remove an item from the library");
		System.out.println("Press 5 to list item-availability of the library");
		System.out.println("Press 6 to exit");
		Scanner in = new Scanner(System.in);
		int op = in.nextInt();
		String libCode = "";
		libCode = libCode.concat(managerId.substring(0,3));
		System.out.println(libCode);
		String reply = null;
		try {
			Registry managerRegistry = LocateRegistry.getRegistry(8089);
			LibManagerInterface manager	;
			manager = (LibManagerInterface)managerRegistry.lookup(libCode);
			switch(op) {
			case 1:

				String userId;

				System.out.println("Please enter the Id of new user");
				userId = in.next();
				reply = manager.addUser(managerId, userId);
				break;
			case 2: 
				String newManagerId;
				System.out.println("Please enter the Id of new manager");
				newManagerId = in.next();
				reply = manager.addManager(managerId, newManagerId);
				break;
			case 3: 
				String itemId, itemName;
				int quantity;
				System.out.println("Enter the itemId, itemName, and quantity");
				itemId = in.next();
				itemName = in.next();
				quantity = in.nextInt();
				reply = manager.addItem(managerId, itemId, itemName, quantity);
				break;

			case 4:
				System.out.println("Enter the itemId, and quantity");
				itemId = in.next();
				quantity = in.nextInt();
				reply = manager.removeItem(managerId, itemId, quantity);
				break;

			case 5: 
				
				reply = manager.listItemAvailability(managerId);
				
			}
			System.out.println("The result of the operation is as follows: \n" + reply);

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		in.close();
	}
}