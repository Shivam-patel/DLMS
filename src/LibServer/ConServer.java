package LibServer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.DataModel;
import LibInterface.LibManagerInterface;
import LibInterface.LibUserInterface;

import javax.xml.crypto.Data;

/**
 * This class is an abstraction for the Concordia library that handles all the client requests directed to concordia library.
 */
public class ConServer extends UnicastRemoteObject implements LibUserInterface, LibManagerInterface,Runnable {


	private static final long serialVersionUID = 1L;
	private HashMap<String, DataModel> conLibrary = new HashMap<String, DataModel>();
	private HashMap<String, ArrayList<DataModel>> conWaitlist = new HashMap<>();
	private ArrayList<String> removedItems = new ArrayList<>();
	private HashMap<String,DataModel> itemsBorrowed = new HashMap<>();
	private ArrayList<DataModel> users = new ArrayList<DataModel>();
	private ArrayList<String> managers = new ArrayList<>();
	private Object lock;
	int MCG = 13131;
	int MON = 13132;


	private final static Logger logger = Logger.getLogger(ConServer.class.getName());
	static private FileHandler fileTxt;

	/**
	 * The class constructor that initiates and engenders new books, users, and managers at the very beginning.
	 */
	public ConServer() throws Exception{
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
		conLibrary.put("CON0001", book1);
		conLibrary.put("CON0002", book2);
		conLibrary.put("CON0003", book3);
		System.out.println(book1);
		System.out.println(book2);
		System.out.println(book3);
		lock = new Object();
		logger.setLevel(Level.INFO);
		fileTxt = new FileHandler("ConServerLog.txt");
		logger.addHandler(fileTxt);

		for(int i=1;i<10;i++) {
			DataModel user = new DataModel();
			user.setUserId("CONU000"+i);
			users.add(user);
		}
		for(int i=1;i<=3;i++) {
			managers.add("CONM000"+i);
		}


		ArrayList<DataModel> wait = new ArrayList<>();
		ArrayList<DataModel> wait02 = new ArrayList<>();
		ArrayList<DataModel> wait03 = new ArrayList<>();
		DataModel waitBook[] = new DataModel[3];
		for (int i=0;i<3;i++){
			waitBook[i] = new DataModel();
			waitBook[i].setUserId("CONU000"+(i+1));
			waitBook[i].setDaysToBorrow(25);
			wait.add(waitBook[i]);
		}
		conWaitlist.put("CON0003", wait03);
		conWaitlist.put("CON0002", wait02);
		conWaitlist.put("CON0001", wait);

		new Thread(this);



	}


	public void run() {
		try {
			this.getWaitRequest();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * This method performs the interserver waitlisting for all the libraries.
	 */
	public void getWaitRequest() throws IOException, ClassNotFoundException {
		DatagramSocket aSocket = new DatagramSocket(9988);
		byte[] buffer = new byte[1000];
		DatagramPacket request = new DatagramPacket(buffer,buffer.length);
		System.out.println("conwait ready");
		aSocket.receive(request);
		ObjectInputStream iStream ;
		iStream = new ObjectInputStream(new ByteArrayInputStream(request.getData()));
		DataModel pack = (DataModel) iStream.readObject();
		iStream.close();
		String reply;
		reply = this.addToWaitlist(pack.getUserId(),pack.getItemId(),pack.getDaysToBorrow());
		byte[] response = reply.getBytes();
		DatagramPacket re = new DatagramPacket(response, response.length,request.getAddress(),request.getPort());
		aSocket.send(re);
	}

	/**This method adds a new user when called by a manager of corresponding server.
	 * @param managerId
	 * @param userId
	 * @return
	 */
	@Override
	public String addUser(String managerId, String userId) {
		logger.info("addUser");
		logger.info(managerId +"\t" + userId);
		Iterator<DataModel> iter = users.iterator();
		while(iter.hasNext()){
			if(iter.next().getUserId().startsWith(userId)) {
				return "Id already exist.";
			}
		}

		if(userId.substring(0, 3).equals("CON") && userId.charAt(3)=='U' && userId.substring(4).matches(".*\\d+.*")) {

			DataModel user = new DataModel();
			user.setUserId(userId);
			users.add(user);
			logger.info("Success");
			return "Success";
		}
		else {
			String reply = "Wrong userId format. Please try again";
			logger.info(reply);
			return reply;
		}
	}

	/** This method adds a new manager if the manager does not already exist in the library.
	 * @param managerId
	 * @param newManagerId
	 * @return
	 */
	@Override
	public String addManager(String managerId, String newManagerId) {

		logger.info("addMananger");

		logger.info(managerId +"\t" + newManagerId);
		if(managers.contains(newManagerId)) {
			return "Id already exist.";
		}
		else if(managerId.substring(0, 3).equals("CON") && managerId.substring(3,4).equals("M") && managerId.substring(4).matches(".*\\d+.*")) {
			managers.add(newManagerId);
			logger.info("Success");

			return "Success";
		}
		else {
			logger.info("Failure");

			return "Failure";
		}
	}

	/**This method adds a new item in the current library when called by one of the managers of the library.
	 * If the item already exists, it adds to the quantity of item, the input quantity
	 * @param managerId
	 * @param itemId
	 * @param itemName
	 * @param quantity
	 * @return
	 * @throws IOException
	 */
	@Override
	public String addItem(String managerId, String itemId, String itemName, int quantity) throws IOException {
		boolean old = false;
		logger.info("addItem");
		logger.info(managerId +"\t" + itemId+"\t" + itemName+"\t" + quantity);
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
			logger.info("Success");
			this.moveWaitlist(itemId);
			return "Success.";
		}
		DataModel value = new DataModel();
		value.setItemName(itemName);
		value.setQuantity(quantity);
		conLibrary.put(itemId, value);
		logger.info("Success");
		this.moveWaitlist(itemId);
		return "Success";
	}

	/**
	 * This method removes an item from the library or it decreases the availability of that item as per the parameters passed by the manager.
	 * @param managerId
	 * @param itemId
	 * @param quantity
	 * @return
	 */
	@Override
	public String removeItem(String managerId, String itemId, int quantity) {
		try {
			DataModel value = conLibrary.get(itemId);
			logger.info("removeItem");
			logger.info(managerId +"\t" + itemId+"\t" + quantity);

			Integer numb = value.getQuantity();
			if(quantity>numb)
				return "Incorrect Quantity";
			if(quantity== numb || quantity == -1) {
				synchronized (lock) {
					conLibrary.remove(itemId);
					removeFromWaitlist(itemId);
				}
				logger.info("Success");
				return "Success";
			}
			else {
				synchronized (lock) {
					numb -= quantity;
					value.setQuantity(numb);
				}
				logger.info("Success");

				return "Success";
			}
		}
		catch(Exception e) {
			logger.info("tem not present in the library");

			return "Item not present in the library";
		}
	}

	/**This method lists all the item with its availability in the current library when called by a manager
	 * @param managerId
	 * @return
	 */
	@Override
	public String listItemAvailability(String managerId) {
		String reply = "";

		logger.info("listItemAvailability");
		logger.info(managerId );
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

		logger.info(reply);
		return reply;
	}

	/**This method is invoked when a user wants to borrow a book and has the necessary information to borrow that book.
	 * All the preconditions to borrow a book as mentioned in provided literature is applied in this method.
	 * @param userId
	 * @param itemId
	 * @param numberOfDays
	 * @return
	 * @throws IOException
	 */
	@Override
	public String borrowItem(String userId, String itemId, int numberOfDays) throws IOException {
		String reply;

		logger.info("borrowItem");
		logger.info(userId+"\t"+itemId+"\t"+numberOfDays);

		if(conLibrary.containsKey(itemId)) {
			DataModel value;
			value = conLibrary.get(itemId);
			System.out.println(value.toString());
			System.out.println(value.getItemId());
			System.out.println(value.getItemName());
			int quantity = value.getQuantity();
			if(quantity != 0) {
				quantity--;
				synchronized (lock) {
					value.setQuantity(quantity);
					value.setQuantity(quantity);
				}
				DataModel borrowed;
				if(itemsBorrowed.containsKey(userId)) {
					borrowed = itemsBorrowed.get(userId);
					synchronized (lock) {
						borrowed.setBorrowedBooks(itemId, numberOfDays);
					}
				}
				else {
				borrowed = new DataModel();
					synchronized (lock) {
						borrowed.setBorrowedBooks(itemId, numberOfDays);
						itemsBorrowed.put(userId, borrowed);
					}
				}

				reply = "Success";
			}
			else {
				reply = "waitlist";
			}
			logger.info(reply);
			return reply;
		}
		else {
			DataModel user = new DataModel();
			if(itemId.startsWith("MCG")) {
				synchronized (lock) {
					Iterator<DataModel> iter = users.iterator();
					while (iter.hasNext()) {
						user = iter.next();
						if (user.getUserId().startsWith(userId)) {
							if (user.getBooksMcg() == 1) {
								logger.info("you can not get two books from a foreign library");
								return "you can not get two books from a foreign library";
							}
							break;
						}
					}
					logger.info("requesting McGill server");
				}
				InterServComClient temp = new InterServComClient(MCG, 1);
				DataModel pack = new DataModel();

				pack.setUserId(userId);
				pack.setItemId(itemId);
				pack.setDaysToBorrow(numberOfDays);
				reply = temp.operate(pack);
				if(reply.startsWith("Succ")){
					synchronized (lock) {
						user.setBooksMcg(1);
					}
				}
			}
			else if(itemId.startsWith("MON")) {
				synchronized (lock) {
					Iterator<DataModel> iter = users.iterator();

					while (iter.hasNext()) {
						user = iter.next();
						if (user.getUserId().startsWith(userId)) {
							if (user.getBooksMon() == 1) {
								logger.info("you can not get two books from a foreign library");
								return "you can not get two books from a foreign library";
							}
							break;
						}
					}
					logger.info("requesting Montreal server");
				}
				InterServComClient temp = new InterServComClient(MON, 2);
				DataModel pack = new DataModel();
				pack.setUserId(userId);
				pack.setItemId(itemId);
				pack.setDaysToBorrow(numberOfDays);
				reply = temp.operate(pack);
				if(reply.startsWith("Succ")){
					synchronized (lock) {
						user.setBooksMon(1);
					}
				}

			}
			else {
				reply = "Invalid itemId";
			}

		}
		logger.info(reply);

		return reply;
	}

	/**This method is called when a user wants to find a particular item by its name.
	 * The method returns books with specified name across
	 * @param userId
	 * @param itemName
	 * @throws IOException
	 */
	@Override
	public String findItem(String userId, String itemName) throws IOException {
		String reply = "";
		logger.info("findItem");
		logger.info(userId+"\t"+itemName);
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
		boolean home = false;
		for(DataModel di:users){
			if(di.getUserId().startsWith(userId))
				home = true;
		}
		if(home) {
			logger.info("calling McGill Server");
			InterServComClient temp = new InterServComClient(MCG, 4);
			logger.info("calling Montreal Server");
			InterServComClient temp1 = new InterServComClient(MON, 5);
			DataModel pack = new DataModel();
			pack.setUserId(userId);
			pack.setItemName(itemName);
			DataModel pack1 = new DataModel();
			pack1.setUserId(userId);
			pack1.setItemName(itemName);
			String replyMCG = temp.operate(pack);
			String replyMON = temp1.operate(pack1);
			reply += replyMCG;
			reply += replyMON;
		}
		logger.info(reply);
		return reply;
	}

	/**This method enables user to return any library item possessed by the user after checking all the preconditions.
	 * @param userId
	 * @param itemId
	 * @return
	 * @throws IOException
	 */
	@Override
	public String ReturnItem(String userId, String itemId) throws IOException {
		logger.info("ReturnItem");

		logger.info(userId+"\t"+itemId);
		String reply = null;
		if(itemId.startsWith("CON")) {
			if(removedItems.contains(itemId)){
				reply = "Success";
				synchronized (lock) {
				DataModel value = itemsBorrowed.get(userId);
				if(value.getBorrowedBooks().containsKey(itemId)) {

						value.getBorrowedBooks().remove(itemId);
					}
				}
			}
			else if(itemsBorrowed.containsKey(userId))
			{
				DataModel value = itemsBorrowed.get(userId);
				if(value.getBorrowedBooks().containsKey(itemId)) {
					synchronized (lock) {
						value.getBorrowedBooks().remove(itemId);
						DataModel item = conLibrary.get(itemId);
						int quantity = item.getQuantity();
						item.setQuantity(quantity + 1);
					}
					reply = this.moveWaitlist(itemId);

					if(value.getBorrowedBooks().isEmpty()) {
						synchronized (lock) {
							itemsBorrowed.remove(userId);
							reply = "Success";
						}
					}

				}
			}

			else {
				reply = "You can not submit this book.";
			}
		}
		else if(itemId.startsWith("MON")){
			logger.info("Calling Montreal Server");

			InterServComClient temp = new InterServComClient(MON,8);
			DataModel pack = new DataModel();
			pack.setUserId(userId);
			pack.setItemId(itemId);
			reply = temp.operate(pack);
		}
		else if(itemId.startsWith("MCG")) {
			logger.info("Calling McGill Server");

			InterServComClient temp = new InterServComClient(MCG,7);
			DataModel pack = new DataModel();
			pack.setUserId(userId);
			pack.setItemId(itemId);
			reply = temp.operate(pack);
		}
		logger.info(reply);

		return reply;
	}

	/**This method checks if the Id provided by the client is valid or not.
	 * @param userId
	 * @param userType
	 * @return
	 */
	@Override
	public boolean validate(String userId, String userType)
	{
		logger.info("Validate");
		logger.info(userId+"\t"+userType);
		if(userType.equals("U")) {
			Iterator<DataModel> iter = users.iterator();
			while(iter.hasNext()){
				if (iter.next().getUserId().startsWith(userId)){
					return true;
				}
			}
			return false;

		}
		else
			return managers.contains(userId);
	}


	/**This method is called when a user wants to borrow a book but the availability is zero and the users wishes to be added to the waitlist of that book.
	 * @param userId
	 * @param itemId
	 * @param numberOfDays
	 * @return
	 */
	@Override
	public String addToWaitlist(String userId, String itemId, int numberOfDays) {
		logger.info("addToWaitlist");
		logger.info(userId+"\t"+itemId+"\t"+numberOfDays);
		if(conLibrary.containsKey(itemId)) {
			ArrayList<DataModel> value;
			DataModel pack = new DataModel();
			value = conWaitlist.get(itemId);
			try {
				if (value.isEmpty()) {
					value = new ArrayList<>();
				}
			} catch (NullPointerException e) {
				value = new ArrayList<>();

			}
			pack.setUserId(userId);
			pack.setDaysToBorrow(numberOfDays);
			value.add(pack);
			synchronized (lock) {
				conWaitlist.put(itemId, value);
				logger.info("Success");
			}
			return "Success";
		}
		else
		{
			try {
				int monPort = 9986;
				int mcgPort = 9987;
				DatagramSocket aSocket = new DatagramSocket();
				DataModel pack = new DataModel();
				pack.setUserId(userId);
				pack.setDaysToBorrow(numberOfDays);
				pack.setItemId(itemId);
				ByteArrayOutputStream bStream = new ByteArrayOutputStream();
				ObjectOutput oo = new ObjectOutputStream(bStream);
				oo.writeObject(pack);
				byte[] request = bStream.toByteArray();
				InetAddress aHost = InetAddress.getLocalHost();
				if(itemId.startsWith("MCG")){
					DatagramPacket req = new DatagramPacket(request, request.length,aHost,mcgPort);
					aSocket.send(req);
					byte [] buffer1 = new byte[1000];
					DatagramPacket rep = new DatagramPacket(buffer1, buffer1.length);
					aSocket.receive(rep);
					String replyString = new String(rep.getData());
					return replyString;
				}
				else if(itemId.startsWith("MON")){
					DatagramPacket req = new DatagramPacket(request, request.length,aHost,monPort);
					aSocket.send(req);
					System.out.println("request sent");
					byte [] buffer1 = new byte[1000];
					DatagramPacket rep = new DatagramPacket(buffer1, buffer1.length);
					aSocket.receive(rep);
					String replyString = new String(rep.getData());
					return replyString;
				}
				aSocket.close();
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "Error. Please check the inputs.";
		}


	}

	/**This method is not directly associated with any user input but it is called whenever the item is made available(availability becomes non zero).
	 * @param itemId
	 * @return
	 * @throws IOException
	 */
	public String moveWaitlist(String itemId) throws IOException {
		logger.info("moveWaitList");
		logger.info(itemId);
		synchronized (lock) {
		ArrayList<DataModel> list = conWaitlist.get(itemId);

		String reply = null;
		Iterator<DataModel> iter = list.iterator();

			while (conLibrary.get(itemId).getQuantity() != 0 && !list.isEmpty() && iter.hasNext()) {
				DataModel user = iter.next();
				reply = this.borrowItem(user.getUserId(), itemId, user.getDaysToBorrow());
				if (reply.startsWith("Succ")) {
					list.remove(user);
				}
			}
			logger.info(reply);
			return reply;
		}

	}

	/**This method is called when a manager requests to remove an item and the availability reduces to zero.
	 * @param itemId
	 */
	public void removeFromWaitlist(String itemId){
		logger.info("removeFromWaitlist");
		logger.info(itemId);
		synchronized (lock) {
			conWaitlist.remove(itemId);
			removedItems.add(itemId);
			logger.info("Success");
		}
	}

}
