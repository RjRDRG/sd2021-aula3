package sd2021.aula3.clients;

import java.io.IOException;
import java.net.InetAddress;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import sd2021.aula3.api.User;
import sd2021.aula3.api.service.RestUsers;
import sd2021.aula3.discovery.Discovery;
import sd2021.aula3.server.UsersServer;

public class DeleteUserClient {

	public final static int MAX_RETRIES = 3;
	public final static long RETRY_PERIOD = 1000;
	public final static int CONNECTION_TIMEOUT = 1000;
	public final static int REPLY_TIMEOUT = 600;

	public static void main(String[] args) throws IOException {

		if( args.length != 2) {
			System.err.println( "Use: java sd2021.aula2.clients.DeleteUserClient userId password");
			return;
		}

		Discovery discovery = new Discovery( "DeleteUserClient", "http://" + InetAddress.getLocalHost().getHostAddress());
		discovery.startCollectingAnnouncements();

		String serverUrl = discovery.knownUrisOf(UsersServer.SERVICE).iterator().next().toString();
		String userId = args[0];
		String password = args[1];

		System.out.println("Sending request to server.");

		ClientConfig config = new ClientConfig();
		//how much time until we timeout when opening the TCP connection to the server
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT);
		//how much time do we wait for the reply of the server after sending the request
		config.property(ClientProperties.READ_TIMEOUT, REPLY_TIMEOUT);
		Client client = ClientBuilder.newClient(config);

		WebTarget target = client.target( serverUrl ).path( RestUsers.PATH );

		short retries = 0;
		boolean success = false;

		while(!success && retries < MAX_RETRIES) {

			try {
				Response r = target.path( userId).queryParam("password", password).request()
						.accept(MediaType.APPLICATION_JSON)
						.delete();

				if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() ) {
					System.out.println("Success:");
					User u = r.readEntity(User.class);
					System.out.println( "User : " + u);
				} else
					System.out.println("Error, HTTP error status: " + r.getStatus() );
				success = true;
			} catch (ProcessingException pe) {
				System.out.println("Timeout occurred");
				pe.printStackTrace();
				retries++;
				try { Thread.sleep( RETRY_PERIOD ); } catch (InterruptedException e) {
					//nothing to be done here, if this happens we will just retry sooner.
				}
				System.out.println("Retrying to execute request.");
			}
		}
	}

}
