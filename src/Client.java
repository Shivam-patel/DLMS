import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import LibInterface.LibManagerInterface;
import LibInterface.LibUserInterface;

public class Client implements Runnable{
	
	public Client() {
		
	}

	@Override
	public void run() {
		Character userType = null;
		String clientId = "";
		String userServer = "";
		System.out.println("Enter 0 to exit.");
		System.out.println("To continue, enter your ID.");
		Scanner input = new Scanner(System.in);
		String clientInput1 = input.nextLine();
		//int option1 = Integer.parseInt(clientInput1);
		
		if(clientInput1.equals("0")) {
			System.out.println("Exiting the system...");
			System.exit(0);
		}
		else
		{
			clientId = clientId.concat(clientInput1);
			userType = clientId.charAt(3);
			userServer= userServer.concat(clientId.substring(0, 3));
			System.out.println(userType+"\n"+userServer + "\n" + clientId);
		}
		try {
			UserClient user;
			ManagerClient manager;
			Registry registry = LocateRegistry.getRegistry(8089);
			LibManagerInterface managerInt;
			LibUserInterface userInt;
		
		if(userType=='M') {
			
			managerInt = (LibManagerInterface)registry.lookup(clientId.substring(0,3));
			if((managerInt.validate(clientId, userType.toString())))
			{
				manager = new ManagerClient(clientId);
				Thread t = new Thread(manager);
				t.start();	
			}
			else {
				System.out.println("Wrong Manager Id. Try again!");
				System.exit(0);
			}
			
		}
		else if(userType == 'U'){
			userInt = (LibUserInterface) registry.lookup(clientId.substring(0,3));
			if((userInt.validate(clientId, userType.toString())))
			{
				user = new UserClient(clientId);
				Thread t = new Thread(user);
				t.start();	
			}
			else {
				System.out.println("Wrong User Id. Try again!");
				System.exit(0);
			}
			
		}
		else {
			System.out.println("Wrong client Id. Try again!");
			System.exit(0);
		}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	//input.close();
	}
	public static void main(String []args) {
		new Thread(new Client()).start();
	}
}