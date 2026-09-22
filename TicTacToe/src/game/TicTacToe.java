package game;

import ai.MiniMaxCombined;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class TicTacToe extends Application {

    private static GridPane gameBoard;
    private static Board board;

    private AnimationTimer gameTimer;

    private MenuBar menuBar;
    private Menu gameMenu;
    private MenuItem newGameOption;

    private BorderPane root;

    /*
     * Prevents the game-over dialog from opening multiple times.
     */
    private boolean gameOverHandled = false;

    public final static class Tile extends Button {

        private final int row;
        private final int col;
        private Mark mark;

        public Tile(int initRow, int initCol, Mark initMark) {

            row = initRow;
            col = initCol;
            mark = initMark;

            initialiseTile();
        }

        private void initialiseTile() {

            this.setOnMouseClicked(e -> {

                /*
                 * O = Player
                 *
                 * Player can play only when it is O's turn.
                 */
                if (!board.isCrossTurn()
                        && !board.isGameOver()) {

                    boolean placed =
                            board.placeMark(row, col);

                    if (placed) {
                        update();
                    }
                }
            });

            this.setStyle("-fx-font-size:70");

            this.setTextAlignment(
                    TextAlignment.CENTER
            );

            this.setMinSize(
                    150.0,
                    150.0
            );

            this.setText("" + mark);
        }

        public void update() {

            this.mark =
                    board.getMarkAt(row, col);

            this.setText("" + mark);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        root = new BorderPane();

        /*
         * Ask who should start.
         */
        boolean computerStarts =
                askWhoStarts();

        /*
         * Create board.
         */
        board =
                new Board(computerStarts);

        /*
         * Create GUI.
         */
        root.setCenter(
                generateGUI()
        );

        root.setTop(
                initialiseMenu()
        );

        Scene scene =
                new Scene(root);

        primaryStage.setTitle(
                "Tic Tac Toe"
        );

        primaryStage.setScene(scene);

        /*
         * Show window first.
         */
        primaryStage.show();

        /*
         * Start game loop.
         */
        runGameLoop();
    }

    /**
     * Ask who should start.
     *
     * @return true  = Computer starts
     *         false = Player starts
     */
    private boolean askWhoStarts() {

        ButtonType computerButton =
                new ButtonType(
                        "Computer Starts"
                );

        ButtonType playerButton =
                new ButtonType(
                        "I Start"
                );

        Alert startDialog =
                new Alert(
                        AlertType.CONFIRMATION
                );

        startDialog.setTitle(
                "New Game"
        );

        startDialog.setHeaderText(
                "Who should start?"
        );

        startDialog.setContentText(
                "Choose who makes the first move."
        );

        startDialog.getButtonTypes().setAll(
                computerButton,
                playerButton
        );

        /*
         * IMPORTANT:
         * No initOwner() here.
         */
        var result =
                startDialog.showAndWait();

        if (result.isPresent()
                && result.get() == playerButton) {

            return false;
        }

        return true;
    }

    /**
     * Creates the game board.
     */
    private static GridPane generateGUI() {

        gameBoard =
                new GridPane();

        gameBoard.setAlignment(
                Pos.CENTER
        );

        for (int row = 0;
             row < board.getWidth();
             row++) {

            for (int col = 0;
                 col < board.getWidth();
                 col++) {

                Tile tile =
                        new Tile(
                                row,
                                col,
                                board.getMarkAt(
                                        row,
                                        col
                                )
                        );

                GridPane.setConstraints(
                        tile,
                        col,
                        row
                );

                gameBoard.getChildren().add(
                        tile
                );
            }
        }

        return gameBoard;
    }

    /**
     * Creates Game menu.
     */
    private MenuBar initialiseMenu() {

        menuBar =
                new MenuBar();

        gameMenu =
                new Menu("Game");

        newGameOption =
                new MenuItem("New Game");

        gameMenu.getItems().add(
                newGameOption
        );

        menuBar.getMenus().add(
                gameMenu
        );

        newGameOption.setOnAction(e -> {

            Stage currentStage =
                    (Stage) root
                            .getScene()
                            .getWindow();

            resetGame(currentStage);
        });

        return menuBar;
    }

    /**
     * Main game loop.
     */
    private void runGameLoop() {

        /*
         * Stop old timer if one exists.
         */
        if (gameTimer != null) {
            gameTimer.stop();
        }

        gameTimer =
                new AnimationTimer() {

                    @Override
                    public void handle(long now) {

                        /*
                         * Game is over.
                         */
                        if (board.isGameOver()) {

                            /*
                             * Stop timer immediately.
                             */
                            stop();

                            /*
                             * Make sure endGame()
                             * is called only once.
                             */
                            if (!gameOverHandled) {

                                gameOverHandled = true;

                                /*
                                 * IMPORTANT:
                                 *
                                 * Do not show an Alert directly
                                 * inside AnimationTimer.
                                 *
                                 * Schedule it for after the
                                 * current animation pulse.
                                 */
                                Platform.runLater(
                                        () -> endGame()
                                );
                            }

                            return;
                        }

                        /*
                         * X = Computer
                         */
                        if (board.isCrossTurn()) {

                            playAI();
                        }
                    }
                };

        gameTimer.start();
    }

    /**
     * Computer makes a move.
     */
    private static void playAI() {

        int[] move =
                MiniMaxCombined.getBestMove(board);

        int row = move[0];
        int col = move[1];

        boolean placed =
                board.placeMark(
                        row,
                        col
                );

        if (!placed) {
            return;
        }

        /*
         * Update GUI tile.
         */
        for (Node child :
                gameBoard.getChildren()) {

            Integer tileRow =
                    GridPane.getRowIndex(child);

            Integer tileCol =
                    GridPane.getColumnIndex(child);

            if (tileRow != null
                    && tileCol != null
                    && tileRow == row
                    && tileCol == col) {

                Tile tile =
                        (Tile) child;

                tile.update();

                return;
            }
        }
    }

    /**
     * Starts a new game.
     */
    private void resetGame(Stage stage) {

        /*
         * Stop old timer.
         */
        if (gameTimer != null) {
            gameTimer.stop();
        }

        /*
         * Reset game-over flag.
         */
        gameOverHandled = false;

        /*
         * Ask who should start.
         */
        boolean computerStarts =
                askWhoStarts();

        /*
         * Create new board.
         */
        board =
                new Board(
                        computerStarts
                );

        /*
         * Create new GUI.
         */
        root.setCenter(
                generateGUI()
        );

        /*
         * Start new timer.
         */
        runGameLoop();
    }

    /**
     * Displays game result.
     */
    private void endGame() {

        Mark winner =
                board.getWinningMark();

        Alert gameOverAlert =
                new Alert(
                        AlertType.INFORMATION
                );

        gameOverAlert.setTitle(
                "Game Over"
        );

        gameOverAlert.setHeaderText(
                null
        );

        /*
         * Determine result.
         */
        if (winner == Mark.BLANK) {

            gameOverAlert.setContentText(
                    "Draw!"
            );

        } else if (winner == Mark.X) {

            gameOverAlert.setContentText(
                    "Computer (X) wins!"
            );

        } else {

            gameOverAlert.setContentText(
                    "You (O) win!"
            );
        }

        ButtonType newGameButton =
                new ButtonType(
                        "New Game"
                );

        gameOverAlert.getButtonTypes().setAll(
                newGameButton
        );

        /*
         * Show result.
         */
        gameOverAlert.showAndWait();

        /*
         * Start another game.
         */
        Stage stage =
                (Stage) root
                        .getScene()
                        .getWindow();

        resetGame(stage);
    }
}