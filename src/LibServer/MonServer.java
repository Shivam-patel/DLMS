package LibServer;

import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import model.DataModel;
import LibInterface.LibManagerInterface;
import LibInterface.LibUserInterface;


public class MonServer extends UnicastRemoteObject implements LibUserInterface, LibManagerInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, DataModel> conLibrary = new HashMap<String, DataModel>();
	private HashMap<String, ArrayList<DataModel>> conWaitlist = new HashMap<>();

	private HashMap<String,DataModel> itemsBorrowed = new HashMap<>();
	private ArrayList<String> users = new ArrayList<String>();
	private ArrayList<String> managers = new ArrayList<>();
	int MCG = 13131;
	int MON = 13132;
	int CON = 13133;

	public MonServer() throws Exception{
		super();
		DataModel book1 = new DataModel();
		DataModel book2 = new DataModel();
		DataModel book3 = new DataModel();
		book1.setItemName("CLRS");
		book2.setItemName("DS");
		book3.setItemName("PDA");
		book1.setQuantity(4);
		book2.setQuantity(2);
		book3.setQuantity(0);
		conLibrary.put("MON001", book1);
		conLibrary.put("MON002", book2);
		conLibrary.put("MON003", book3);
		System.out.println(book1);
		System.out.println(book2);
		System.out.println(book3);

		for(int i=1;i<11;i++) {
			users.add("MONU000"+i);
		}
		for(int i=1;i<3;i++) {
			managers.add("MONM000"+i);
		}


		ArrayList<DataModel> wait = new ArrayList<>();
		DataModel waitBook[] = new DataModel[3];
		for (int i=0;i<3;i++){
			waitBook[i] = new DataModel();
			waitBook[i].setUserId("MONU000"+(i+1));
			waitBook[i].setDaysToBorrow(25);
			wait.add(waitBook[i]);
		}

		conWaitlist.put("MON0001", wait);




	}


	@Override
	public String addUser(String managerId, String userId) {
		if(users.contains(userId)) {
			//		System.out.println("The user already exist in the library databsae.");
			return "Id already exist.";
		}
		else if(userId.substring(0, 3).equals("MON") && userId.substring(3,4).equals("U") && userId.substring(4).matches(".*\\d+.*")) {
			// make a new user
			users.add(userId);
			return "Success";
		}
		else {
			return "Failure";
		}
	}

	@Override
	public String addManager(String managerId, String newManagerId) {
		if(managers.contains(newManagerId)) {
			//		System.out.println("The manager already exist in the library databsae.");
			return "Id already exist.";
		}
		else if(managerId.substring(0, 3).equals("MON") && managerId.substring(3,4).equals("M") && managerId.substring(4).matches(".*\\d+.*")) {
			// make a new manager
			managers.add(newManagerId);
			return "Success";
		}
		else {
			return "Failure";
		}
	}

	@Override
	public String addItem(String managerId, String itemId, String itemName, int quantity) {
		boolean old = false;
		Integer quantity1 = quantity;
		for(String id : conLibrary.keySet()) {
			if (id.equals(itemId)) {
				old = true;
				break;
			}
		}
		if(old) {
			DataModel value = conLibrary.get(itemId);
			Integer itemCount = value.getQuantity();
			itemCount+=quantity;
			value.setQuantity(itemCount);
			return "Success.";
		}
		DataModel value = new DataModel();
		value.setItemName(itemName);
		value.setQuantity(quantity);
		conLibrary.put(itemId, value);
		return "Success";
	}

	@Override
	public String removeItem(String managerId, String itemId, int quantity) {
		try {
			DataModel value = conLibrary.get(itemId);

			Integer numb = value.getQuantity();
			if(quantity>= numb) {
				conLibrary.remove(itemId);

				/* Call a method to remove all the allocations of any removed books. or Ask the TA about what to do. */

				return "Success";
			}
			else {
				numb-=quantity;
				value.setQuantity(numb);
				return "Success";
			}
		}
		catch(Exception e) {
			return "Item not present in the library";
		}
	}
	//reference https://www.geeksforgeeks.org/iterate-map-java/
	@Override
	public String listItemAvailability(String managerId) {
		String reply = "";
		Iterator<Map.Entry<String, DataModel>>iter = conLibrary.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<String, DataModel> entry = iter.next();
			reply = reply.concat(entry.getKey());
			reply = reply.concat("  ");
			DataModel values = entry.getValue();
			reply = reply.concat(values.getItemName());
			reply = reply.concat("  ");
			reply = reply.concat(values.getQuantity().toString());
			reply = reply.concat("\n");
		}
		return reply;
	}

	@Override
	public String borrowItem(String userId, String itemId, int numberOfDays) {
		String reply;

		if(conLibrary.containsKey(itemId)) {
			DataModel value;
			value = conLibrary.get(itemId);
			System.out.println(value.toString());
			System.out.println(value.getItemId());
			System.out.println(value.getItemName());
			int quantity = value.getQuantity();
			if(quantity != 0) {
				quantity--;
				value.setQuantity(quantity);
				value.setQuantity(quantity);
				DataModel borrowed;
				if(itemsBorrowed.containsKey(userId)){
					borrowed = itemsBorrowed.get(userId);
					borrowed.setBorrowedBooks(itemId, numberOfDays);
				}
				else {
					borrowed = new DataModel();
					//	borrowed.setItemId(itemId);
					//	borrowed.setItemName(value.getItemName());
					//	borrowed.setNumberOfDays(numberOfDays);
					borrowed.setBorrowedBooks(itemId,numberOfDays);
					itemsBorrowed.put(userId,borrowed);
				}
				reply = "Success";
			}
			else {
				reply = "The item is not available right now. Do you want to be added to its waitlist?";
			}
			return reply;
		}
		else {

			if(itemId.startsWith("MCG")) {
				InterServComClient temp = new InterServComClient(MCG, 1);
				DataModel pack = new DataModel();
				pack.setUserId(userId);
				pack.setItemId(itemId);
				pack.setDaysToBorrow(numberOfDays);
				reply = temp.operate(pack);
			}
			else if(itemId.startsWith("MON")) {
				InterServComClient temp = new InterServComClient(CON, 3);
				DataModel pack = new DataModel();
				pack.setUserId(userId);
				pack.setItemId(itemId);
				pack.setDaysToBorrow(numberOfDays);
				reply = temp.operate(pack);
			}
			else {
				reply = "Invalid itemId";
			}

		}
		return reply;
	}

	@Override
	public String findItem(String userId, String itemName) {
		String reply = "";
		Iterator<Entry<String, DataModel>> iter = conLibrary.entrySet().iterator();
		int count=0;
		while(iter.hasNext()) {
			Entry<String,DataModel> pair = iter.next();
			DataModel value = pair.getValue();
			System.out.println(count++);
			if(value.getItemName().equals(itemName)) {
				reply = pair.getKey();
				reply = reply.concat("\t");
				reply = reply.concat(value.getQuantity().toString());
				reply = reply.concat("\n");
			}
		}
		InterServComClient temp = new InterServComClient(MCG,4);
		InterServComClient temp1 = new InterServComClient(CON,6);
		DataModel pack = new DataModel();
		pack.setUserId(userId);
		pack.setItemName(itemName);
		DataModel pack1 = new DataModel();
		pack1.setUserId(userId);
		pack1.setItemName(itemName);
		String replyMCG = temp.operate(pack);
		String replyMON = temp1.operate(pack1);
		reply+=replyMCG;
		reply+=replyMON;
		return reply;
	}

	@Override
	public String ReturnItem(String userId, String itemId) {

		String reply = null;
		if(itemId.startsWith("MON")) {

			if(itemsBorrowed.containsKey(userId))
			{
				DataModel value = itemsBorrowed.get(userId);
				if(value.getBorrowedBooks().containsKey(itemId)) {
					/*Iterator<Entry<String, Integer>> iter = value.getBorrowedBooks().entrySet().iterator();			//check for correct working of iterator
					while(iter.hasNext()) {
							Entry<String, Integer> pair = iter.next();
						if(pair.getKey().equals(itemId)) {
							// Remove the item here
							//Add the removed item to the library database (Create a separate method for that and handling everything else)
							break;
						}
					}*/
					value.getBorrowedBooks().remove(itemId);
					if(value.getBorrowedBooks().isEmpty()) {
						itemsBorrowed.remove(userId);
						reply = "Success";
					}
				}
			}
			else {
				reply = "You can not submit this book.";
			}
		}
		else if(itemId.startsWith("CON")){
			InterServComClient temp = new InterServComClient(CON,9);
			DataModel pack = new DataModel();
			pack.setUserId(userId);
			pack.setItemId(itemId);
			reply = temp.operate(pack);
		}
		else if(itemId.startsWith("MCG")) {
			InterServComClient temp = new InterServComClient(MCG,7);
			DataModel pack = new DataModel();
			pack.setUserId(userId);
			pack.setItemId(itemId);
			reply = temp.operate(pack);
		}

		return reply;
	}

	@Override
	public boolean validate(String userId, String userType) {
		if(userType.equals("U"))
			return users.contains(userId);
		else
			return managers.contains(userId);
	}


	@Override
	public String addToWaitlist(String userId, String itemId, int numberOfDays) {
		ArrayList<DataModel> value;
		DataModel pack = new DataModel();
		value = conWaitlist.get(itemId);
		pack.setUserId(userId);
		pack.setDaysToBorrow(numberOfDays);
		value.add(pack);
		return "Success";
	}

}
