package il.cshaifasweng.OCSFMediatorExample.client;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class SecondaryController {
    private String[][] gameBoard = new String[3][3];
    private final Button[][] buttons = new Button[3][3];
    public SecondaryController() {
        EventBus.getDefault().register(this);  // Register to EventBus
    }
    @FXML
    public void initialize() {
        buttons[0][0] = btn00;
        buttons[0][1] = btn01;
        buttons[0][2] = btn02;
        buttons[1][0] = btn10;
        buttons[1][1] = btn11;
        buttons[1][2] = btn12;
        buttons[2][0] = btn20;
        buttons[2][1] = btn21;
        buttons[2][2] = btn22;
    }
    @FXML
    private Label gameState;
    @FXML
    private Button btn00, btn01, btn02, btn10, btn11, btn12, btn20, btn21, btn22, NewGame;

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }

    @FXML
    private void clicked(ActionEvent event) {
        Button button = (Button) event.getSource();
        String buttonId = button.getId();
        int row = Character.getNumericValue(buttonId.charAt(3));
        int col = Character.getNumericValue(buttonId.charAt(4));
        try {
            SimpleClient.getClient().sendToServer("new move " + row + " " + col);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void newGame(ActionEvent event) {
        try {
            SimpleClient.getClient().sendToServer("new Game");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void handleMessage(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof String) {
                gameState.setText(msg.toString());
            } else if (msg instanceof Object[]) {
                Object[] update = (Object[]) msg;
                int row = (int) update[0];
                int col = (int) update[1];
                String move = (String) update[2];
                gameState.setText(update[3].toString().contains("OVER") ? (String) update[3] : update[3] + "'s Turn");
                updateBoard(row, col, move);
            }
        });
    }

    @Subscribe
    public void handleNewGameMessage(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof String && msg.equals("new game")) {
                clearGameBoard();
                gameState.setText("New Game Started");
            }
        });
    }
    private void clearGameBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                gameBoard[i][j] = null;
                buttons[i][j].setText("");
            }
        }
    }


    private void updateBoard(int row, int col, String move) {
        gameBoard[row][col] = move;
        Button button = buttons[row][col];
        if (button != null) {
            button.setText(move);
        }
    }


}