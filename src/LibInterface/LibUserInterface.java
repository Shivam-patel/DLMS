package LibInterface;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LibUserInterface extends Remote {
	public String borrowItem(String userId, String itemId, int numberOfDays) throws IOException;
	public String findItem(String userId, String itemName) throws IOException;
	public String ReturnItem(String userId, String itemId) throws IOException;
	public boolean validate(String userId, String clientType)throws RemoteException;
	public String addToWaitlist(String userId, String itemId, int numberOfDays)throws RemoteException;
}
