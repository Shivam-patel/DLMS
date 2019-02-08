package model;

import java.io.Serializable;

import java.util.HashMap;

public class DataModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userId;
	private String userName;
	/*private ArrayList<Integer> numberOfDays;*/
	private Integer daysToBorrow;
	private String itemName;
	private String itemId;
	private String ManagerId;
	private Integer flag;
	private Integer quantity;
	private Integer booksCon;
	private Integer booksMon;
	private Integer booksMcg;



	private HashMap<String, Integer> borrowedBooks = new HashMap<>();

	public DataModel() {
		this.userId = "";
		this.userName = "";
/*
		this.numberOfDays = new ArrayList<>();
*/
		this.itemName = "";
		this.itemId = "";
		ManagerId = "";
		this.flag = 0;
		this.quantity = 0;
		this.booksCon = 0;
		this.booksMon = 0;
		this.booksMcg = 0;


	}

/*public DataModel() {
		this.userId = null;
		this.numberOfDays = 0;
		this.itemName = null;
		this.quantity = -1;
		this.borrowedBooks=null;
	}*/

	public Integer getBooksCon() {
		return booksCon;
	}

	public void setBooksCon(Integer booksCon) {
		this.booksCon = booksCon;
	}

	public Integer getBooksMon() {
		return booksMon;
	}

	public void setBooksMon(Integer booksMon) {
		this.booksMon = booksMon;
	}

	public Integer getBooksMcg() {
		return booksMcg;
	}

	public void setBooksMcg(Integer booksMcg) {
		this.booksMcg = booksMcg;
	}

	public Integer getFlag() {
		return flag;
	}

	public void setFlag(Integer flag) {
		this.flag = flag;
	}

	public String getManagerId() {
		return ManagerId;
	}

	public void setManagerId(String managerId) {
		ManagerId = managerId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	/*public Integer getNumberOfDays(String itemId) {
		return numberOfDays;
	}

	public void setNumberOfDays(Integer numberOfDays) {
		this.numberOfDays.add(numberOfDays);
			}*/

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}


	/*public ArrayList<String> getBorrowedBooks() {
		return borrowedBooks;
	}

	public void setBorrowedBooks(String borrowedBooks) {
		this.borrowedBooks.add(borrowedBooks);
	}*/
	public void setBorrowedBooks(String itemId, Integer numberOfDays) {

		this.borrowedBooks.put(itemId, numberOfDays);
	}
	public HashMap<String,Integer> getBorrowedBooks(){
		return borrowedBooks;
	}

	public Integer getDaysToBorrow() {
		return daysToBorrow;
	}

	public void setDaysToBorrow(Integer daysToBorrow) {
		this.daysToBorrow = daysToBorrow;
	}
}
