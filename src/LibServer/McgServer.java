package LibServer;

import java.io.IOException;
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


public class McgServer extends UnicastRemoteObject implements LibUserInterface, LibManagerInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, DataModel> mcgLibrary = new HashMap<String, DataModel>();
	private HashMap<String, ArrayList<DataModel>> mcgWaitlist = new HashMap<>();
	private ArrayList<String> removedItems = new ArrayList<>();
	private HashMap<String,DataModel> itemsBorrowed = new HashMap<>();
	private ArrayList<DataModel> users = new ArrayList<>();
	private ArrayList<String> managers = new ArrayList<>();
	int MCG = 13131;
	int MON = 13132;
	int CON = 13133;

    private final static Logger logger = Logger.getLogger(McgServer.class.getName());
    static private FileHandler fileTxt;

	public McgServer() throws Exception{
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
		mcgLibrary.put("MCG0001", book1);
		mcgLibrary.put("MCG0002", book2);
		mcgLibrary.put("MCG0003", book3);

        logger.setLevel(Level.INFO);
        fileTxt = new FileHandler("McgServerLog.txt");
        logger.addHandler(fileTxt);
		System.out.println(book1);
		System.out.println(book2);
		System.out.println(book3);

		for(int i=1;i<10;i++) {
            DataModel user = new DataModel();
            user.setUserId("MCGU000"+i);
            users.add(user);
		}
		for(int i=1;i<3;i++) {
			managers.add("MCGM000"+i);
		}


		ArrayList<DataModel> wait = new ArrayList<>();
		ArrayList<DataModel> wait02 = new ArrayList<>();
		ArrayList<DataModel> wait03 = new ArrayList<>();
		DataModel waitBook[] = new DataModel[3];
		for (int i=0;i<3;i++){
			waitBook[i] = new DataModel();
			waitBook[i].setUserId("MCGU000"+(i+1));
			waitBook[i].setDaysToBorrow(25);
			wait.add(waitBook[i]);
		}
		mcgWaitlist.put("MCG0003", wait03);
		mcgWaitlist.put("MCG0002", wait02);
		mcgWaitlist.put("MCG0001", wait);




	}


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
        if(userId.substring(0, 3).equals("MCG") && userId.charAt(3)=='U' && userId.substring(4).matches(".*\\d+.*")) {
            // make a new user
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

    @Override
    public String addManager(String managerId, String newManagerId) {

        logger.info("addMananger");

        logger.info(managerId +"\t" + newManagerId);
        if(managers.contains(newManagerId)) {
            //		System.out.println("The manager already exist in the library databsae.");
            return "Id already exist.";
        }
        else if(managerId.substring(0, 3).equals("MCG") && managerId.substring(3,4).equals("M") && managerId.substring(4).matches(".*\\d+.*")) {
            // make a new manager
            managers.add(newManagerId);
            logger.info("Success");

            return "Success";
        }
        else {
            logger.info("Failure");

            return "Failure";
        }
    }

    @Override
    public String addItem(String managerId, String itemId, String itemName, int quantity) {
        boolean old = false;
        logger.info("addItem");
        logger.info(managerId +"\t" + itemId+"\t" + itemName+"\t" + quantity);
        for(String id : mcgLibrary.keySet()) {
            if (id.equals(itemId)) {
                old = true;
                break;
            }
        }
        if(old) {
            DataModel value = mcgLibrary.get(itemId);
            Integer itemCount = value.getQuantity();
            itemCount+=quantity;
            value.setQuantity(itemCount);
            logger.info("Success");

            return "Success.";
        }
        DataModel value = new DataModel();
        value.setItemName(itemName);
        value.setQuantity(quantity);
        mcgLibrary.put(itemId, value);
        logger.info("Success");

        return "Success";
    }

    @Override
    public String removeItem(String managerId, String itemId, int quantity) {
        try {
            DataModel value = mcgLibrary.get(itemId);
            logger.info("removeItem");
            logger.info(managerId +"\t" + itemId+"\t" + quantity);

            Integer numb = value.getQuantity();
            if(quantity>= numb) {
                mcgLibrary.remove(itemId);

                /* Call a method to remove all the allocations of any removed books. or Ask the TA about what to do. */
                removeFromWaitlist(itemId);
                logger.info("Success");

                return "Success";
            }
            else {
                numb-=quantity;
                value.setQuantity(numb);
                logger.info("Success");

                return "Success";
            }
        }
        catch(Exception e) {
            logger.info("tem not present in the library");

            return "Item not present in the library";
        }
    }
    //reference https://www.geeksforgeeks.org/iterate-map-java/
    @Override
    public String listItemAvailability(String managerId) {
        String reply = "";

        logger.info("listItemAvailability");
        logger.info(managerId );
        Iterator<Map.Entry<String, DataModel>>iter = mcgLibrary.entrySet().iterator();
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

    @Override
    public String borrowItem(String userId, String itemId, int numberOfDays) throws IOException {
        String reply;

        logger.info("borrowItem");
        logger.info(userId+"\t"+itemId+"\t"+numberOfDays);

        if(mcgLibrary.containsKey(itemId)) {
            DataModel value;
            value = mcgLibrary.get(itemId);
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
                reply = "waitlist";            }
            logger.info(reply);
            return reply;
        }
        else {
            DataModel user = new DataModel();
            if(itemId.startsWith("CON")) {
                Iterator<DataModel> iter = users.iterator();
                while(iter.hasNext()){
                    user = iter.next();
                    if(user.getUserId().startsWith(userId)){
                        if(user.getBooksCon()==1){
                            logger.info("you can not get two books from a foreign library");
                            return "you can not get two books from a foreign library";
                        }
                        break;
                    }
                }
                logger.info("requesting Concordia server");
                InterServComClient temp = new InterServComClient(CON, 3);
                DataModel pack = new DataModel();
                pack.setUserId(userId);
                pack.setItemId(itemId);
                pack.setDaysToBorrow(numberOfDays);
                reply = temp.operate(pack);
                if(reply.startsWith("Succ")){
                    user.setBooksCon(1);
                }
            }
            else if(itemId.startsWith("MON")) {
                Iterator<DataModel> iter = users.iterator();
                while(iter.hasNext()){
                    user = iter.next();
                    if(user.getUserId().startsWith(userId)){
                        if(user.getBooksMon()==1){
                            logger.info("you can not get two books from a foreign library");
                            return "you can not get two books from a foreign library";
                        }
                        break;
                    }
                }
                logger.info("requesting Montreal server");

                InterServComClient temp = new InterServComClient(MON, 2);
                DataModel pack = new DataModel();
                pack.setUserId(userId);
                pack.setItemId(itemId);
                pack.setDaysToBorrow(numberOfDays);
                reply = temp.operate(pack);
                if(reply.startsWith("Succ")){
                    user.setBooksMon(1);
                }

            }
            else {
                reply = "Invalid itemId";
            }

        }
        logger.info(reply);

        return reply;
    }

    @Override
    public String findItem(String userId, String itemName) throws IOException {
        String reply = "";
        logger.info("findItem");
        logger.info(userId+"\t"+itemName);
        Iterator<Entry<String, DataModel>> iter = mcgLibrary.entrySet().iterator();
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
        if(users.contains(userId)) {
            logger.info("calling Concordia Server");
            InterServComClient temp = new InterServComClient(CON, 6);
            logger.info("calling Montreal Server");
            InterServComClient temp1 = new InterServComClient(MON, 5);
            DataModel pack = new DataModel();
            pack.setUserId(userId);
            pack.setItemName(itemName);
            DataModel pack1 = new DataModel();
            pack1.setUserId(userId);
            pack1.setItemName(itemName);
            String replyCON = temp.operate(pack);
            String replyMON = temp1.operate(pack1);
            reply += replyCON;
            reply += replyMON;
        }
        logger.info(reply);
        return reply;
    }

    @Override
    public String ReturnItem(String userId, String itemId) throws IOException {
        logger.info("ReturnItem");

        logger.info(userId+"\t"+itemId);
        String reply = null;
        if(itemId.startsWith("MCG")) {

            if(itemsBorrowed.containsKey(userId))
            {
                DataModel value = itemsBorrowed.get(userId);
                if(value.getBorrowedBooks().containsKey(itemId)) {
                    value.getBorrowedBooks().remove(itemId);
                    DataModel item = mcgLibrary.get(itemId);
                    int quantity = item.getQuantity();
                    item.setQuantity(quantity+1);
                    reply = this.moveWaitlist(itemId);

                    if(value.getBorrowedBooks().isEmpty()) {
                        itemsBorrowed.remove(userId);
                        reply = "Success";
                    }

                }
            }
            else if(removedItems.contains(itemId)){
                reply = "Success";
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
        else if(itemId.startsWith("CON")) {
            logger.info("Calling Concordia Server");

            InterServComClient temp = new InterServComClient(CON,9);
            DataModel pack = new DataModel();
            pack.setUserId(userId);
            pack.setItemId(itemId);
            reply = temp.operate(pack);
        }
        logger.info(reply);

        return reply;
    }

    @Override
    public boolean validate(String userId, String userType)
    {
        logger.info("Validate");
        logger.info(userId+"\t"+userType);
        if(userType.equals("U")) {
            Iterator<DataModel> iter = users.iterator();
            while (iter.hasNext()) {
                if (iter.next().getUserId().startsWith(userId)) {
                    return true;
                }
            }
            return false;
        }
        else
            return managers.contains(userId);
    }


    @Override
    public String addToWaitlist(String userId, String itemId, int numberOfDays) {
        logger.info("addToWaitlist");
        logger.info(userId+"\t"+itemId+"\t"+numberOfDays);
        ArrayList<DataModel> value;
        DataModel pack = new DataModel();
        value = mcgWaitlist.get(itemId);
        try {
            if (value.isEmpty()) {
                value = new ArrayList<>();
            }
        }catch(NullPointerException e)
        {
            value = new ArrayList<>();

        }
        pack.setUserId(userId);
        pack.setDaysToBorrow(numberOfDays);
        value.add(pack);
        mcgWaitlist.put(itemId,value);
        logger.info("Success");
        return "Success";
    }
    public String moveWaitlist(String itemId) throws IOException {
        logger.info("moveWaitList");
        logger.info(itemId);

        ArrayList<DataModel> list = mcgWaitlist.get(itemId);
        int quantity = mcgLibrary.get(itemId).getQuantity();
        String reply = null;
        Iterator<DataModel> iter = list.iterator();
        while(mcgLibrary.get(itemId).getQuantity()!=0 && !list.isEmpty() && iter.hasNext()){
            DataModel user = iter.next();
            reply = this.borrowItem( user.getUserId(),itemId,user.getDaysToBorrow());
        }
        logger.info(reply);
        return reply;
    }
    public void removeFromWaitlist(String itemId){
        logger.info("removeFromWaitlist");
        logger.info(itemId);
        mcgWaitlist.remove(itemId);
        removedItems.add(itemId);
        logger.info("Success");
    }

}
