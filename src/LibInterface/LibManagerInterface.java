package LibInterface;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LibManagerInterface extends Remote{

	public String addUser(String managerId,String userId)throws RemoteException;
	public String addManager(String managerId, String newManagerId)throws RemoteException;
	public String addItem(String managerId, String itemId, String itemName, int quantity)throws RemoteException;
	public String removeItem(String managerId, String itemId, int quantity)throws RemoteException;
	public String listItemAvailability(String managerId)throws RemoteException;
	public boolean validate(String managerId, String clientType)throws RemoteException;
	
	
}
