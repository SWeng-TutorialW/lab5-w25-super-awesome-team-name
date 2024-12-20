package il.cshaifasweng.OCSFMediatorExample.client;

import org.greenrobot.eventbus.EventBus;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;

public class SimpleClient extends AbstractClient {
	private static SimpleClient client = null;

	private SimpleClient(String host, int port) {
		super(host, port);
	}

	@Override
	protected void handleMessageFromServer(Object msg) {
		if (msg instanceof Warning) {
			EventBus.getDefault().post(new WarningEvent((Warning) msg));
		} else {
			String message = msg.toString();
			if (message != null && message.startsWith("start game")) {
				javafx.application.Platform.runLater(() -> {
					try {
						PrimaryController.setWaitingForPlayer(false);
						PrimaryController.switchToSecondary();       // Switch to secondary screen
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			} else if (message != null && message.startsWith("update")) {
				String[] splittedStr = message.split(" ");
				int row = Integer.parseInt(splittedStr[2]);
				int col = Integer.parseInt(splittedStr[3]);
				EventBus.getDefault().post(new Object[]{row, col, splittedStr[4], splittedStr[6]});
			} else if (message != null && message.startsWith("done")) {
				String[] splittedStr = message.split(" ");
				EventBus.getDefault().post("THE WINNER IS " + splittedStr[3]);
			} else if (message != null && message.startsWith("over")) {
				String[] splittedStr = message.split(" ");
				int row = Integer.parseInt(splittedStr[1]);
				int col = Integer.parseInt(splittedStr[2]);
				EventBus.getDefault().post(new Object[]{row, col, splittedStr[3], "GAME OVER!!!"});
			}
			else if (message != null && message.startsWith("new game")) {
				javafx.application.Platform.runLater(() -> {
					EventBus.getDefault().post("new game"); // Notify the UI to clear the board
				});
			System.out.println(message);
		}
	}
	}

	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient("localhost", 3000);
		}
		return client;
	}

}
