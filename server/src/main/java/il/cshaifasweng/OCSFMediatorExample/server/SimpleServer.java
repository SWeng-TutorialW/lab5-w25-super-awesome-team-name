package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.util.ArrayList;

import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;

public class SimpleServer extends AbstractServer {
	private static ArrayList<SubscribedClient> SubscribersList = new ArrayList<>();
	private String[][] gameBoard = new String[3][3];
	private boolean isOTurn = true;

	public SimpleServer(int port) {
		super(port);
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		String msgString = msg.toString();
		if (msgString.startsWith("#warning")) {
			Warning warning = new Warning("Warning from server!");
			try {
				client.sendToClient(warning);
				System.out.format("Sent warning to client %s\n", client.getInetAddress().getHostAddress());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (msgString.startsWith("add client")) {
			SubscribedClient connection = new SubscribedClient(client);
			SubscribersList.add(connection);
			try {
				client.sendToClient("client added successfully");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else if (msgString.startsWith("remove client")) {
			if (!SubscribersList.isEmpty()) {
				for (SubscribedClient subscribedClient : SubscribersList) {
					if (subscribedClient.getClient().equals(client)) {
						SubscribersList.remove(subscribedClient);
						break;
					}
				}
			}
		} else if (msgString.startsWith("I'm here")) {
			if (SubscribersList.size() == 2) {
				sendToAllClients("start game");
			}
		} else if (msgString.startsWith("new move ")) {
			handleMove(msgString, client);
		}
		else if (msgString.startsWith("new Game")) {
			clearBoard(gameBoard);
		}
	}

	private void handleMove(String msgString, ConnectionToClient client) {
		String[] parts = msgString.split(" ");
		int row = Integer.parseInt(parts[2]);
		int col = Integer.parseInt(parts[3]);
		String player = isOTurn ? "X" : "O";
		String moveMsg = row + " " + col + " " + player;
		if ((isOTurn && !client.getName().equals(SubscribersList.get(0).getClient().getName())) || (!isOTurn && !client.getName().equals(SubscribersList.get(1).getClient().getName())))
			return;
		if (gameBoard[row][col] != null)
			return;
		gameBoard[row][col] = player;
		sendToAllClients("update board " + moveMsg + " Turn " + (player.equals("X") ? "O" : "X"));
		if (checkWin()) {
			sendToAllClients("done " + moveMsg);
			return;
		}
		if (isFullBoard()) {
			sendToAllClients("over " + moveMsg);
			return;
		}
		isOTurn = !isOTurn;
	}

	private boolean isFullBoard() {
		for (String[] row : gameBoard) {
			for (String cell : row) {
				if (cell == null) return false;
			}
		}
		return true;
	}

	private boolean checkWin() {
		for (int i = 0; i < 3; i++) {
			// rows
			if (gameBoard[i][0] != null && !gameBoard[i][0].isEmpty() &&
					gameBoard[i][0].equals(gameBoard[i][1]) && gameBoard[i][1].equals(gameBoard[i][2])) {
				return true;
			}
			// cols
			if (gameBoard[0][i] != null && !gameBoard[0][i].isEmpty() &&
					gameBoard[0][i].equals(gameBoard[1][i]) && gameBoard[1][i].equals(gameBoard[2][i])) {
				return true;
			}
		}
		// Check diagonals
		if (gameBoard[0][0] != null && !gameBoard[0][0].isEmpty() &&
				gameBoard[0][0].equals(gameBoard[1][1]) && gameBoard[1][1].equals(gameBoard[2][2])) {
			return true;
		}
		return (gameBoard[0][2] != null && !gameBoard[0][2].isEmpty() &&
				gameBoard[0][2].equals(gameBoard[1][1]) && gameBoard[1][1].equals(gameBoard[2][0]));
	}

	public void sendToAllClients(String message) {
		try {
			for (SubscribedClient subscribedClient : SubscribersList) {
				subscribedClient.getClient().sendToClient(message);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void clearBoard(String[][] board) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (board[i][j] != null) {
					board[i][j] = null;
				}
			}
			sendToAllClients("new game");
		}
	}

}
