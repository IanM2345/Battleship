// Ian Mwai Gachoki
//3132394
package griffith;

import java.io.*;
import java.util.*;

public class Battleship {

    private static final String SAVE_FILE_NAME = "battleship_singlePlayer_save.txt";
    private static final String TWO_PLAYER_SAVE_FILE_NAME = "battleship_twoPlayer_save.txt";

    private static final char WATER = '~';
    private static final char SHIP = 'S';
    private static final char HIT = 'X';
    private static final char MISS = '0';
    
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        boolean exitGame = false;

        while (!exitGame) {
            displayMainMenu(); //calls display menu method to display the menu upon running the console
            int choice = scanner.nextInt();
            scanner.nextLine(); 

            switch (choice) {
                case 1:

                    startSinglePlayerGame(scanner); // Start The single player if the ser inputs 1
                    break;
                case 2:
                    startTwoPlayerGame(scanner);// Start Two Player game if the user inputs 2
                    break;
                case 3:
                    loadSinglePlayerGame(scanner);// Load saved single-player game
                    break;
                case 4:
                    loadTwoPlayerGame();// Load saved two-player game
                    break;
                case 5:
                    exitGame = true;
                    System.out.println("Exiting game. Goodbye!");
                    break;// Exit the game
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 5.");
                    break;
            }
        }

        scanner.close();
    }

   // Method toDisplay the main menu options
    private static void displayMainMenu() {
        System.out.println("\nWelcome To Battleship");
        System.out.println("1. Start new single player game (plays against computer)");
        System.out.println("2. Start new two player game (game allows multiplayer)");
        System.out.println("3. Load saved single player game");
        System.out.println("4. Load saved two player game");
        System.out.println("5. Exit game");
        System.out.print("Enter your choice: ");
    }
    
    // Start a new single-player game against the computer
    private static void startSinglePlayerGame(Scanner scanner) {
        System.out.println("Starting a new single player game...");

        int boardSize = selectDifficulty(scanner);//Sets board size according to difficulty chosen

        // Create game boards for player and computer
        char[][] playerBoard = createGameBoard(boardSize, WATER);
        char[][] computerView = createGameBoard(boardSize, WATER);
        char[][] computerBoard = createGameBoard(boardSize, WATER);

        
        // Check if there's a saved game state
        boolean continueGame = checkSavedGameState(scanner);

        if (!continueGame) {
            placeShipsManually(playerBoard, scanner, boardSize);//Call method to make user input users manually
            placeShipsRandomly(computerBoard);// Call method to place computer ships
        } else {
            boolean gameCompleted = loadGameState(playerBoard, computerView, computerBoard);
            if (gameCompleted) {
                // If the loaded game was completed, start a new game
                System.out.println("The loaded game was already completed. Starting a new game...");
                placeShipsManually(playerBoard, scanner, boardSize);
                placeShipsRandomly(computerBoard);
            } else {
                System.out.println("Previous game loaded successfully.");
            }
        }
       // Call method to start the game
        playGame(scanner, playerBoard, computerView, computerBoard, boardSize);
    }

    
   // Method Choose the game difficulty (board size)
    private static int selectDifficulty(Scanner scanner) {
        int boardSize = 8; // Default to medium board size

        while (true) {
            System.out.println("Choose difficulty level:");
            System.out.println("1. Easy (5x5 board)");
            System.out.println("2. Medium (8x8 board)");
            System.out.println("3. Hard (10x10 board)");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine(); 

                switch (choice) {
                    case 1:
                        boardSize = 5;
                        break;
                    case 2:
                        boardSize = 8;
                        break;
                    case 3:
                        boardSize = 10;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 3.");
                        continue; // Restart loop to prompt correct user input 
                }

                break; 
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); 
            }
        }

        return boardSize;
    }

    //Method to check for a saved game state
    private static boolean checkSavedGameState(Scanner scanner) {
        File savedGameStateFile = new File(SAVE_FILE_NAME);
        if (savedGameStateFile.exists()) {
            System.out.println("A saved game state has been found.");
            System.out.println("Do you want to continue the previous game? (yes/no)");

            while (true) {
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("yes")) {
                    return true;
                } else if (input.equals("no")) {
                    return false;
                } else {
                    System.out.println("Invalid input. Please enter 'yes' or 'no'.");
                }
            }
        }
        return false;
    }

    //Method to start the game loop
    private static void playGame(Scanner scanner, char[][] playerBoard, char[][] computerView,
            char[][] computerBoard, int boardSize) {

				Set<String> playerGuesses = new HashSet<>();
				Set<String> computerGuesses = new HashSet<>();
				
				boolean playerTurn = true; // True if it's player's turn, false if it's computer's turn
				boolean gameEnded = false;
				
				while (!gameEnded) {
				// Display game boards
				System.out.println("\nPlayer's Board:");
				printGameBoard(playerBoard, computerBoard, boardSize);
				System.out.println("\nComputer's View:");
				printGameBoard(computerView, computerBoard, boardSize);
				
				if (playerTurn) {
				// Player's turn
				System.out.println("\nPlayer's turn:");
				String playerMove = getUserMove(boardSize, scanner);
				
				int[] playerGuess = parseMove(playerMove, boardSize);
				if (playerGuess == null) {
				System.out.println("Invalid move format. Please use format like 'b1', 'a4'.");
				continue; // Prompts the user to input correctly
				}
				
                // Process players guess to make sure its valid
				int row = playerGuess[0];
				int column = playerGuess[1];
				String playerGuessKey = row + "," + column;

				if (computerView[row][column] != WATER || playerGuesses.contains(playerGuessKey)) {
				System.out.println("Invalid move. Please try again.");
				continue; // Prompts the user to input correctly
				}
				
				playerGuesses.add(playerGuessKey);
				char playerTarget = evaluateGuessAndGetTheTarget(playerGuess, computerBoard, WATER, HIT, MISS);
				updateGameBoard(computerView, playerGuess, playerTarget); // Update computer's view
				
				if (playerTarget == HIT) {
				System.out.println("You hit a ship!");
				if (countShipsRemaining(computerBoard, SHIP) == 0) {
				   System.out.println("\nCongratulations! You sank all of the computer's ships!");
				   gameEnded = true;
				}
				} else {
				System.out.println("You missed.");
				}
				} else {
				// Computer's turn
				System.out.println("\nComputer's turn:");
				int[] computerGuess = generateShipCoordinates(boardSize);
				String computerGuessKey = computerGuess[0] + "," + computerGuess[1];
				while (computerGuesses.contains(computerGuessKey)) {
				computerGuess = generateShipCoordinates(boardSize);
				computerGuessKey = computerGuess[0] + "," + computerGuess[1];
				}
				computerGuesses.add(computerGuessKey);
				
				char computerTarget = evaluateGuessAndGetTheTarget(computerGuess, playerBoard, WATER, HIT, MISS);
				updateGameBoard(playerBoard, computerGuess, computerTarget);
				
				if (computerTarget == HIT) {
				System.out.println("Computer hit one of your ships!");
				if (countShipsRemaining(playerBoard, SHIP) == 0) {
				   System.out.println("\nWoomp Woomp! Computer wins. Better luck next time!");
				   gameEnded = true;
				}
				} else {
				System.out.println("Computer missed.");
				}
				}
				
				playerTurn = !playerTurn; // Switch turn between player and computer until the game ends
				
				// Logic to Save game state after each turn
				boolean saveSuccessful = saveGameState(playerBoard, computerView, computerBoard);
				if (!saveSuccessful) {
				System.out.println("Game state not saved.");
				}
				
				
				System.out.println("Press Enter to continue...");
				scanner.nextLine();
				}
}


     // Method that starts a new two-player game
    private static void startTwoPlayerGame(Scanner scanner) {
        System.out.println("Starting a new two player game...");

        // Initialize game boards for both players and their respective views
        int boardSize = 5;
        char[][] player1Board = createGameBoard(boardSize, WATER);
        char[][] player2Board = createGameBoard(boardSize, WATER);
        char[][] player1View = createHiddenGameBoard(boardSize);
        char[][] player2View = createHiddenGameBoard(boardSize);

        // Methods to Place ships randomly for both players
        placeShipsRandomly(player1Board);
        placeShipsRandomly(player2Board);

        
        boolean gameOver = false;
        boolean player1Turn = true;

        while (!gameOver) {
           // Find the active player and their boards/views.
            char[][] currentPlayerBoard;
            char[][] currentPlayerView;
            char[][] opponentBoard;
            char[][] opponentView;

            if (player1Turn) {
                currentPlayerBoard = player1Board;
                currentPlayerView = player1View;
                opponentBoard = player2Board;
                opponentView = player2View;
                System.out.println("Player 1's turn:");
            } else {
                currentPlayerBoard = player2Board;
                currentPlayerView = player2View;
                opponentBoard = player1Board;
                opponentView = player1View;
                System.out.println("Player 2's turn:");
            }

            // Print opponent's view with the ships hidden
            System.out.println("Opponent's Board:");
            printHiddenGameBoard(opponentView, boardSize);//Method to print gameboard

            // Get player's inout and validate the move
            System.out.print("Enter your move (e.g., 'b1', 'a4'): ");
            String playerMove = scanner.nextLine().trim().toLowerCase();
            int[] guessCoordinates = parseMove(playerMove, boardSize);

            if (guessCoordinates == null) {
                System.out.println("Invalid move format. Please use format like 'b1', 'a4'.");
                continue;// Prompt user to input again if the input is invalid
            }

            int row = guessCoordinates[0];
            int column = guessCoordinates[1];

            // Code to Verify the validity of the estimated position.
            if (opponentView[row][column] != WATER) {
                System.out.println("Invalid move. Please try again.");
                continue;
            }

            
            char target = evaluateGuessAndGetTheTarget(guessCoordinates, opponentBoard, WATER, HIT, MISS);
            opponentView[row][column] = target;// Refresh the opponent's perspective with the guess's outcome.

            // Check if all opponent's ships sunk
            if (countShipsRemaining(opponentBoard, SHIP) == 0) {
                System.out.println("All opponent's ships sunk! Player " + (player1Turn ? "1" : "2") + " wins!");
                gameOver = true;
            }

            // Switch turns
            player1Turn = !player1Turn;

            
            System.out.println("Press Enter to continue...");
            scanner.nextLine(); // Wait for user to press Enter
        }
    }



    

    private static char[][] createGameBoard(int boardSize, char defaultValue) {
        char[][] gameBoard = new char[boardSize][boardSize];
        for (char[] row : gameBoard) {
            Arrays.fill(row, defaultValue);
        }
        return gameBoard;
    }

   // The player can manually arrange ships on the game board using this method.
private static void placeShipsManually(char[][] gameBoard, Scanner scanner, int boardSize) {
    int shipCount = 0;
    char water = WATER;
    char ship = SHIP;  

   // Repeat until all three ships are in position.
    while (shipCount < 3) {
        System.out.println("Place your ship " + (shipCount + 1) + ":");

        // Examine the player's ship placement suggestions.
        String shipPlacement = scanner.nextLine().trim().toLowerCase();
        
       // Convert the input into row and column coordinates.
        int[] coordinates = parseMove(shipPlacement, boardSize);
        
        // Verify that the parsing was successful.
        if (coordinates == null || coordinates.length < 2) {
            System.out.println("Invalid ship placement format. Please try again.");
            continue; 
        }

        int row = coordinates[0];
        int column = coordinates[1];

        // Verify that the ship placement is inside the authorized board boundaries.
        if (isValidPosition(row, column, boardSize)) {
            if (gameBoard[row][column] == water) {
                // Position the vessel on the gaming board.
                gameBoard[row][column] = ship;
                shipCount++; // Increase in ship count
                System.out.println("Ship placed successfully (" + shipCount + "/3 ships placed)");
            } else {
                System.out.println("Invalid ship placement. Please choose an empty position.");
            }
        } else {
            System.out.println("Invalid ship placement. Position is out of board bounds.");
        }
    }
}

// This method checks the validity of a specific row or column on the game board.
private static boolean isValidPosition(int row, int column, int boardSize) {
    return row >= 0 && row < boardSize && column >= 0 && column < boardSize;
}

// The player's ship placement input is parsed into row and column indices using this method.

private static int[] parseMove(String move, int boardSize) {
    if (move.length() < 2) {
        return null; 
    }

    char[] chars = move.toCharArray();
    int column, row;

    
    if (Character.isLetter(chars[0])) {
        column = Character.toLowerCase(chars[0]) - 'a'; // Convert letter to column index (0-indexed)
    } else {
        return null; 
    }

    
    try {
        row = Integer.parseInt(String.valueOf(chars[1])) - 1; // Convert number to row index (0-indexed)
    } catch (NumberFormatException e) {
        return null; 
    }

    // Check if the parsed coordinates match the board's measurements.
    if (row < 0 || row >= boardSize || column < 0 || column >= boardSize) {
        return null; 
    }

    return new int[]{row, column}; 
}


   // This method returns the input after asking the user to enter their move
private static String getUserMove(int boardSize, Scanner scanner) {
    System.out.print("Enter your move (e.g., 'b1', 'a4'): ");
    return scanner.nextLine().trim().toLowerCase(); // Read and return user input in lowercase
}

// In this method  In this way, ships are arranged on the play board at random.
private static void placeShipsRandomly(char[][] gameBoard) {
    Random random = new Random();
    int shipNumber = 3;
    char water = WATER;
    char ship = SHIP;

    int placedShips = 0;
    while (placedShips < shipNumber) {
        // Create arbitrary column and row indices.
        int row = random.nextInt(gameBoard.length);
        int column = random.nextInt(gameBoard.length);
        

        if (gameBoard[row][column] == water) {
            // Create arbitrary column and row indices.
            gameBoard[row][column] = ship;
            placedShips++;
        }
    }
}

// This method generates board and the computer's view are printed using this function.
private static void printGameBoard(char[][] playerBoard, char[][] computerView, int boardSize) {
    System.out.println(ANSI_BLUE + "   Player's Board        Computer's View   " + ANSI_RESET);

    // Print capital-letter headers for the computer's view and the player's board.
    System.out.print("  ");
    for (int i = 0; i < boardSize; i++) {
        System.out.print(" " + Character.toUpperCase((char) ('a' + i)) + " ");
    }
    System.out.print("       ");
    for (int i = 0; i < boardSize; i++) {
        System.out.print(" " + Character.toUpperCase((char) ('a' + i)) + " ");
    }
    System.out.println();

    // Create row-by-row gaming boards.
    for (int row = 0; row < boardSize; row++) {
        // Print row on the player's board.
        System.out.print((row + 1) + " ");
        for (int column = 0; column < boardSize; column++) {
            char playerCell = playerBoard[row][column];
            System.out.print(" ");
            
            if (playerCell == WATER) {
                System.out.print(ANSI_BLUE + WATER + " " + ANSI_RESET); // Water symbol
            } else if (playerCell == SHIP) {
                System.out.print(ANSI_YELLOW + SHIP + " " + ANSI_RESET); // Ship symbol
            } else if (playerCell == HIT) {
                System.out.print(ANSI_RED + HIT + " " + ANSI_RESET); // Hit symbol
            } else if (playerCell == MISS) {
                System.out.print(ANSI_GREEN + MISS + " " + ANSI_RESET); // Miss symbol
            }
        }

        // Print spacing between player's board and computer's view
        System.out.print("      ");

        // Print computer's view row
        System.out.print((row + 1) + " ");
        for (int column = 0; column < boardSize; column++) {
            char computerCell = computerView[row][column];
            System.out.print(" ");
            if (computerCell == WATER || computerCell == SHIP) {
                System.out.print(ANSI_BLUE + WATER + " " + ANSI_RESET); // Water or ship symbol
            } else if (computerCell == HIT) {
                System.out.print(ANSI_RED + HIT + " " + ANSI_RESET); // Hit symbol
            } else if (computerCell == MISS) {
                System.out.print(ANSI_GREEN + MISS + " " + ANSI_RESET); // Miss symbol
            }
        }

        System.out.println(); // Move to the next line after printing each row
    }
    System.out.println(); // Add an extra line after printing both boards
}


   // This method to print a hidden game board filled with water.
private static char[][] createHiddenGameBoard(int boardSize) {
    char[][] gameBoard = new char[boardSize][boardSize];
    for (char[] row : gameBoard) {
        Arrays.fill(row, WATER); 
    }
    return gameBoard;
}

// This method prints the opponent's game board with the ships hiding.
private static void printHiddenGameBoard(char[][] gameBoard, int boardSize) {
    System.out.println(ANSI_BLUE + "   Opponent's Board   " + ANSI_RESET);

    // Use capital letters when printing the board's headers.
    System.out.print("  ");
    for (int i = 0; i < boardSize; i++) {
        System.out.print(" " + Character.toUpperCase((char) ('a' + i)) + " ");
    }
    System.out.println();

    // Print the game board, hiding the ships row by row.
    for (int row = 0; row < boardSize; row++) {
        System.out.print((row + 1) + " ");
        for (int column = 0; column < boardSize; column++) {
            char cell = gameBoard[row][column];
            if (cell == WATER || cell == SHIP) {
                System.out.print(" " + ANSI_BLUE + WATER + " " + ANSI_RESET); // Display water or ship
            } else {
                System.out.print(" " + ANSI_BLUE + cell + " " + ANSI_RESET); // Hide ships
            }
        }
        System.out.println(); 
    }
}

// This method updates the game board in accordance with the method's evaluation of a player's guess.
private static char evaluateGuessAndGetTheTarget(int[] guessCoordinates, char[][] gameBoard,
                                                 char water, char hit, char miss) {
    int row = guessCoordinates[0];
    int column = guessCoordinates[1];

    char result;
    if (row >= 0 && row < gameBoard.length && column >= 0 && column < gameBoard[0].length) {
        char target = gameBoard[row][column];
        if (target == SHIP) {
            System.out.println("Hit!");
            gameBoard[row][column] = HIT;
            result = HIT; // Set the result to HIT if the guess hits a ship
        } else {
            System.out.println("Miss!");
            gameBoard[row][column] = MISS;
            result = MISS; // Set the result to MISS if the guess misses
        }
    } else {
        System.out.println("Invalid coordinates provided!");
        result = MISS; 
    }
    return result; 
}

// This method updates the game board with the target result at the guessed coordinates.
private static void updateGameBoard(char[][] gameBoard, int[] guessCoordinates, char target) {
    int row = guessCoordinates[0];
    int column = guessCoordinates[1];
    gameBoard[row][column] = target; 
}

// This method generates random ship coordinates within the game board size.
private static int[] generateShipCoordinates(int boardSize) {
    Random random = new Random();
    int row = random.nextInt(boardSize);
    int column = random.nextInt(boardSize);
    return new int[]{row, column}; 
}


    // This method counts the number of ships remaining on the game board.
private static int countShipsRemaining(char[][] board, char ship) {
    int count = 0;
    for (char[] row : board) {
        for (char cell : row) {
            if (cell == ship) {
                count++; // Increment count for each ship found
            }
        }
    }
    return count; 
}

// This method saves the current game state to a file.
public static boolean saveGameState(char[][] playerBoard, char[][] computerView, char[][] computerBoard) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE_NAME))) {
        // Write player board
        writeBoardToFile(playerBoard, writer);
        writer.newLine();

        // Write computer view
        writeBoardToFile(computerView, writer);
        writer.newLine();

        // Write computer board
        writeBoardToFile(computerBoard, writer);

        return true; // Game state saved successfully
    } catch (IOException e) {
        System.err.println("Error saving game state: " + e.getMessage());
        return false; // Game state not saved
    }
}

//Method to write a game board to a file.
private static void writeBoardToFile(char[][] board, BufferedWriter writer) throws IOException {
    for (char[] row : board) {
        writer.write(row); // Write each row of the board to the file
        writer.newLine(); // Move to the next line in the file
    }
}

// This method loads a game state from a file.
public static boolean loadGameState(char[][] playerBoard, char[][] computerView, char[][] computerBoard) {
    try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE_NAME))) {
        // Load player board
        readBoardFromFile(playerBoard, reader);

        // Load computer view
        readBoardFromFile(computerView, reader);

        // Load computer board
        readBoardFromFile(computerBoard, reader);

        // Check if the loaded game state represents a completed game (all ships sunk)
        if (countShipsRemaining(playerBoard, SHIP) == 0 || countShipsRemaining(computerBoard, SHIP) == 0) {
            System.out.println("This saved game state represents a completed game.");
            return true; // Indicate that the loaded game is completed
        }

        return false; // Game state loaded successfully
    } catch (IOException e) {
        System.err.println("Error loading game state: " + e.getMessage());
        return false; // Game state not loaded
    }
}

//Method to read a game board from a file.
private static void readBoardFromFile(char[][] board, BufferedReader reader) throws IOException {
    for (int i = 0; i < board.length; i++) {
        String line = reader.readLine();
        if (line != null && line.length() == board[i].length) {
            board[i] = line.toCharArray(); // Convert the read line to a character array and assign it to the board row
        } else {
            // Handle invalid board format or end of file
            Arrays.fill(board[i], WATER); 
        }
    }
}

    
private static void loadSinglePlayerGame(Scanner scanner) {
    System.out.println("Loading saved single player game...");

    // Set up game board and load saved game state
    int boardSize = 5;
    char[][] playerBoard = createGameBoard(boardSize, WATER);       // Create player's board
    char[][] computerView = createGameBoard(boardSize, WATER);      // Create computer's view of player's board
    char[][] computerBoard = createGameBoard(boardSize, WATER);     // Create computer's hidden board

    // Attempt to load the saved game state
    boolean loadSuccessful = loadGameState(playerBoard, computerView, computerBoard);

    if (loadSuccessful) {
        System.out.println("Game loaded successfully.");

        // Game loop for playing the loaded game
        Set<String> playerGuesses = new HashSet<>();  // Set to track player's guesses

        while (true) {
            // Print game boards
            printGameBoard(playerBoard, computerView, boardSize);

            // Player's turn
            boolean validPlayerMove = false;
            while (!validPlayerMove) {
                System.out.println("\nPlayer's turn:");
                String playerMove = getUserMove(boardSize, scanner);  // Get player's move input

                int[] playerGuess = parseMove(playerMove, boardSize);  // Parse player's move
                if (playerGuess == null) {
                    System.out.println("Invalid move format. Please use format like 'b1', 'a4'.");
                    continue;
                }

                int row = playerGuess[0];
                int column = playerGuess[1];

                String playerGuessKey = row + "," + column;
                if (computerView[row][column] != WATER || playerGuesses.contains(playerGuessKey)) {
                    System.out.println("Invalid move. Please try again.");
                } else {
                    playerGuesses.add(playerGuessKey);
                    char playerTarget = evaluateGuessAndGetTheTarget(playerGuess, computerBoard, WATER, HIT, MISS);
                    updateGameBoard(computerView, playerGuess, playerTarget); // Update computer's view

                    if (playerTarget == HIT) {
                        if (countShipsRemaining(computerBoard, SHIP) == 0) {
                            System.out.println("\nCongratulations! You sank all of the computer's ships!");
                            break;
                        }
                    }

                    validPlayerMove = true;
                }
            }

            // Computer's turn
            System.out.println("\nComputer's turn:");
            int[] computerGuess = generateShipCoordinates(boardSize);  // Generate computer's move
            char computerTarget = evaluateGuessAndGetTheTarget(computerGuess, playerBoard, WATER, HIT, MISS);
            updateGameBoard(playerBoard, computerGuess, computerTarget);  // Update player's board

            if (computerTarget == HIT) {
                if (countShipsRemaining(playerBoard, SHIP) == 0) {
                    System.out.println("\nWoomp Woomp! Computer wins. Better luck next time!");
                    break;
                }
            }

            
            boolean saveSuccessful = saveGameState(playerBoard, computerView, computerBoard);
            if (!saveSuccessful) {
                System.out.println("Warning: Game state not saved.");
            }

            
            System.out.println("Press Enter to continue...");
            scanner.nextLine(); // Wait for user to press Enter
        }
    } else {
        // If loading failed, start a new game
        System.out.println("Failed to load the game state. Starting a new game...");
        placeShipsRandomly(playerBoard);    // Randomly place ships on player's board
        placeShipsRandomly(computerBoard);  // Randomly place ships on computer's board

        // Start a new single-player game
        startSinglePlayerGame(scanner);
    }
}


    public static boolean saveTwoPlayerGameState(char[][] player1Board, char[][] player2Board) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TWO_PLAYER_SAVE_FILE_NAME))) {
            // Write player 1's board
            writeBoardToFile(player1Board, writer);
            writer.newLine();

            // Write player 2's board
            writeBoardToFile(player2Board, writer);

            return true; // Game state saved successfully
        } catch (IOException e) {
            System.err.println("Error saving game state: " + e.getMessage());
            return false; // Game state not saved
        }
    }

    public static boolean loadTwoPlayerGameState(char[][] player1Board, char[][] player2Board) {
        try (BufferedReader reader = new BufferedReader(new FileReader(TWO_PLAYER_SAVE_FILE_NAME))) {
            // Load player 1's board
            readBoardFromFile(player1Board, reader);

            // Load player 2's board
            readBoardFromFile(player2Board, reader);

            return true; // Game state loaded successfully
        } catch (IOException e) {
            System.err.println("Error loading game state: " + e.getMessage());
            return false; // Game state not loaded
        }
    }


    private static void loadTwoPlayerGame() {
        System.out.println("Loading saved two-player game...");

        int boardSize = 5;
        char[][] player1Board = createGameBoard(boardSize, WATER);
        char[][] player2Board = createGameBoard(boardSize, WATER);
        char[][] player1View = createHiddenGameBoard(boardSize);
        char[][] player2View = createHiddenGameBoard(boardSize);

        // Try loading the game from a file.
        boolean loadSuccessful = loadTwoPlayerGameState(player1Board, player2Board);

        if (loadSuccessful) {
            System.out.println("Two-player game state loaded successfully.");
        } else {
            System.out.println("Failed to load two-player game state. Starting a new game...");
            placeShipsRandomly(player1Board);
            placeShipsRandomly(player2Board);
        }

        // Game loop
        boolean gameOver = false;
        boolean player1Turn = true;

        while (!gameOver) {
            // Determine current player and respective boards
            char[][] currentPlayerBoard;
            char[][] currentPlayerView;
            char[][] opponentBoard;
            char[][] opponentView;

            if (player1Turn) {
                currentPlayerBoard = player1Board;
                currentPlayerView = player1View;
                opponentBoard = player2Board;
                opponentView = player2View;
                System.out.println("Player 1's turn:");
            } else {
                currentPlayerBoard = player2Board;
                currentPlayerView = player2View;
                opponentBoard = player1Board;
                opponentView = player1View;
                System.out.println("Player 2's turn:");
            }

            // Print opponent's view 
            System.out.println("Opponent's Board:");
            printHiddenGameBoard(opponentView, boardSize);

            // Get player's move
            Scanner scanner = new Scanner(System.in);
            String playerMove = getUserMove(boardSize, scanner);
            int[] guessCoordinates = parseMove(playerMove, boardSize);

            if (guessCoordinates == null) {
                System.out.println("Invalid move format. Please use format like 'b1', 'a4'.");
                continue;
            }

            int row = guessCoordinates[0];
            int column = guessCoordinates[1];

            // Check if the guessed position is valid
            if (opponentView[row][column] != WATER) {
                System.out.println("Invalid move. Please try again.");
                continue;
            }

            
            char target = evaluateGuessAndGetTheTarget(guessCoordinates, opponentBoard, WATER, HIT, MISS);
            opponentView[row][column] = target;

            // Check if the game is over 
            if (countShipsRemaining(opponentBoard, SHIP) == 0) {
                System.out.println("All opponent's ships sunk! " + (player1Turn ? "Player 1" : "Player 2") + " wins!");
                gameOver = true;
            }

            // Toggle turns
            player1Turn = !player1Turn;

            
            System.out.println("Press Enter to continue...");
            scanner.nextLine(); // Wait for user to press Enter
        }
    }

   

}
