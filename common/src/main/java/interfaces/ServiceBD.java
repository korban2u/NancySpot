package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServiceBD extends Remote {


    String getAllRestaurants() throws RemoteException;


    String getTablesLibres(int restaurantId) throws RemoteException;


    String reserverTable(String jsonReservation) throws RemoteException;


    boolean ping() throws RemoteException;
}