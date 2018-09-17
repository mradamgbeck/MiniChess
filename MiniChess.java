package A2MiniChess;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class MiniChess {

	private final int MIN = 0;
	private final int MAX = 1;
	private final int LIMIT = 5;
	private final int ROOK = 50;
	private final int BISHOP = 50;
	private final int KING = 100;

	private class Board {

		private final ArrayList<Character> COMPUTER = new ArrayList<Character>();
		private final ArrayList<Character> PLAYER = new ArrayList<Character>();
		private char[][] array;

		private Board() {
			this.COMPUTER.add('R');
			this.COMPUTER.add('B');
			this.COMPUTER.add('K');
			this.COMPUTER.add('B');
			this.COMPUTER.add('R');

			this.PLAYER.add('r');
			this.PLAYER.add('b');
			this.PLAYER.add('k');
			this.PLAYER.add('b');
			this.PLAYER.add('r');
			array = new char[6][6];

			for (int i = 0; i < 6; i++)
				for (int j = 0; j < 6; j++)
					array[i][j] = ' ';
		}
	}

	private class Move {
		int originX;
		int originY;
		int destinationX;
		int destinationY;

		public Move(int oX, int oY, int dX, int dY) {
			originX = oX;
			originY = oY;
			destinationX = dX;
			destinationY = dY;
		}
	}

	private Board board;
	private int size;

	public MiniChess() {

		this.size = 6;
		this.board = new Board();
		initializeFirstBoard();
	}

	private void initializeFirstBoard() {
		this.board.array[0][0] = 'R';
		this.board.array[0][1] = 'B';
		this.board.array[0][2] = ' ';
		this.board.array[0][3] = 'K';
		this.board.array[0][4] = 'B';
		this.board.array[0][5] = 'R';
		this.board.array[5][0] = 'r';
		this.board.array[5][1] = 'b';
		this.board.array[5][2] = 'k';
		this.board.array[5][3] = ' ';
		this.board.array[5][4] = 'b';
		this.board.array[5][5] = 'r';
	}

	public void play() {
		while (true) {
			this.board = playerMove(this.board);

			if (playerWin(this.board)) {
				System.out.println("Player Wins");
				break;
			}

			this.board = computerMove(this.board);

			if (computerWin(this.board)) {
				System.out.println("Computer Wins");
				break;
			}
		}
	}

	private Board playerMove(Board board) {
		System.out.println("Player Move: ");

		Scanner scanner = new Scanner(System.in);
		int originX = scanner.nextInt();
		int originY = scanner.nextInt();
		int destinationX = scanner.nextInt();
		int destinationY = scanner.nextInt();
		Move move = new Move(originX, originY, destinationX, destinationY);
		System.out.println("Player moved " + board.array[originX][originY] + " from [" + originX + " ," + originY
				+ "] to [" + destinationX + " ," + destinationY + "].");
		Board child = copy(board);
		child = applyMoveToBoard(child, move);
		displayBoard(child);
		return child;
	}

	private Board computerMove(Board board) {
		// For some reason, the computer will occasionally kill its own piece,
		// and I cannot figure out why.
		LinkedList<Board> children = generate(board, true);

		int maxIndex = 0;
		int maxValue = minMax(children.get(0), MIN, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);

		for (int i = 0; i < children.size(); i++) {
			int currentValue = minMax(children.get(i), MIN, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);

			if (currentValue > maxValue) {
				maxIndex = i;
				maxValue = currentValue;
			}
		}

		Board result = children.get(maxIndex);

		System.out.println("Computer Move: ");

		displayBoard(result);

		return result;
	}

	private int minMax(Board board, int level, int depth, int alpha, int beta) {
		if (computerWin(board) || playerWin(board) || depth >= LIMIT)
			return evaluate(board);
		else if (level == MAX) {
			int maxValue = Integer.MIN_VALUE;

			LinkedList<Board> children = generate(board, true);

			for (int i = 0; i < children.size(); i++) {
				int currentValue = minMax(children.get(i), MIN, depth + 1, alpha, beta);
				if (currentValue > maxValue)
					maxValue = currentValue;
				if (maxValue >= beta)
					return maxValue;
				if (maxValue > alpha)
					alpha = maxValue;
			}

			return maxValue;
		} else {
			int minValue = Integer.MAX_VALUE;
			LinkedList<Board> children = generate(board, false);

			for (int i = 0; i < children.size(); i++) {
				int currentValue = minMax(children.get(i), MAX, depth + 1, alpha, beta);

				if (currentValue < minValue)
					minValue = currentValue;
				if (minValue <= alpha)
					return minValue;
				if (minValue < beta)
					beta = minValue;
			}

			return minValue;
		}
	}

	private LinkedList<Board> generate(Board board, boolean isComputerTurn) {
		LinkedList<Board> children = new LinkedList<Board>();

		if (isComputerTurn) {
			children = generateComputerChildren(board, children);
		} else {
			children = generatePlayerChildren(board, children);
		}
		return children;
	}

	// generates computer children
	private LinkedList<Board> generateComputerChildren(Board board, LinkedList<Board> children) {
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++) {
				char piece = board.array[i][j];
				if (isComputerPiece(board, piece)) {
					ArrayList<Move> moves = findPossibleMoves(board, piece, i, j);
					for (Move move : moves) {
						Board child = copy(board);
						child = applyMoveToBoard(child, move);
						children.addLast(child);
					}
				}
			}
		return children;
	}

	// It took a while before I realized the computer couldn't generate player
	// child boards
	private LinkedList<Board> generatePlayerChildren(Board board, LinkedList<Board> children) {
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++) {
				char piece = board.array[i][j];
				if (isPlayerPiece(board, piece)) {
					ArrayList<Move> moves = findPossibleMoves(board, piece, i, j);
					for (Move move : moves) {
						Board child = copy(board);
						child = applyMoveToBoard(child, move);
						children.addLast(child);
					}
				}
			}
		return children;
	}

	// takes a board object and a move object and moves the piece, then returns
	// the new board
	private Board applyMoveToBoard(Board board, Move move) {
		if (board.array[move.destinationX][move.destinationY] != ' ') {
			if (isComputerPiece(board, board.array[move.destinationX][move.destinationY])) {
				board.COMPUTER.remove(board.COMPUTER.indexOf(board.array[move.destinationX][move.destinationY]));
			} else if (isPlayerPiece(board, board.array[move.destinationX][move.destinationY])) {
				board.PLAYER.remove(board.PLAYER.indexOf(board.array[move.destinationX][move.destinationY]));
			}
		}
		board.array[move.destinationX][move.destinationY] = board.array[move.originX][move.originY];
		board.array[move.originX][move.originY] = ' ';
		return board;
	}

	// computer wins if the player lost its king
	private boolean computerWin(Board board) {
		if (!board.PLAYER.contains('k'))
			return true;
		return false;
	}

	// player wins if the computer lost its king
	private boolean playerWin(Board board2) {
		if (!board.COMPUTER.contains('K'))
			return true;
		return false;
	}

	// evaluates if a board is a win, a lose, or in play and evaluates the value
	// of an in play board
	private int evaluate(Board board) {
		if (computerWin(board))
			return 10000;
		else if (playerWin(board))
			return 0;
		else {
			return calculatePieceValues(board);
		}
	}

	// this method actually adds up the values of each piece and returns the
	// value of a given board
	private int calculatePieceValues(Board board) {
		int computerScore = 0;
		int playerScore = 0;
		for (char piece : board.COMPUTER) {
			if (piece == 'R') {
				computerScore += ROOK;
			} else if (piece == 'B') {
				computerScore += BISHOP;
			} else if (piece == 'K') {
				computerScore += KING;
			}
		}

		for (char piece : board.PLAYER) {
			if (piece == 'r') {
				playerScore += ROOK;
			} else if (piece == 'b') {
				playerScore += BISHOP;
			} else if (piece == 'k') {
				playerScore += KING;
			}
		}
		return computerScore - playerScore;
	}

	// standard board copy method
	private Board copy(Board board) {
		Board result = new Board();
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				result.array[i][j] = board.array[i][j];

		return result;
	}

	// same display method
	private void displayBoard(Board board) {

		System.out.println();
		for (int i = 0; i < size; i++) {
			System.out.print("|");
			for (int j = 0; j < size; j++) {
				System.out.print(board.array[i][j]);
				System.out.print("|");
			}
			System.out.println();
		}
		System.out.println();
	}

	// this method just figures out what moves a certain piece can make
	private ArrayList<Move> findPossibleMoves(Board child, char piece, int x, int y) {
		ArrayList<Move> moves = new ArrayList<Move>();
		if (piece == 'R' || piece == 'r') {
			moves = findRookMoves(x, y);
		} else if (piece == 'B' || piece == 'b') {
			moves = findBishopMoves(x, y);
		} else if (piece == 'K' || piece == 'k') {
			moves = findKingMoves(x, y);
		}
		return moves;
	}

	// if this character is in the computer piece array, its a computer piece
	private boolean isComputerPiece(Board board, char c) {
		return board.COMPUTER.contains(c);
	}

	// if this character is in the player piece array, its a player piece
	private boolean isPlayerPiece(Board board, char c) {
		return board.PLAYER.contains(c);
	}

	// all the moves a king can possibly make
	private ArrayList<Move> findKingMoves(int x, int y) {
		ArrayList<Move> moves = new ArrayList<Move>();
		if (isInUpperLeftCorner(x, y)) {
			moves.add(new Move(0, 0, 0, 1));
			moves.add(new Move(0, 0, 1, 1));
			moves.add(new Move(0, 0, 1, 0));
		} else if (isInUpperRightCorner(x, y)) {
			moves.add(new Move(0, 5, 0, 4));
			moves.add(new Move(0, 5, 1, 4));
			moves.add(new Move(0, 5, 1, 5));
		} else if (isInLowerLeftCorner(x, y)) {
			moves.add(new Move(5, 0, 4, 0));
			moves.add(new Move(5, 0, 4, 1));
			moves.add(new Move(5, 0, 5, 1));
		} else if (isInLowerRightCorner(x, y)) {
			moves.add(new Move(5, 5, 5, 4));
			moves.add(new Move(5, 5, 4, 4));
			moves.add(new Move(5, 5, 4, 5));
		} else if (isAgainstTopEdge(x, y)) {
			moves.add(new Move(x, y, x, y - 1));
			moves.add(new Move(x, y, x + 1, y - 1));
			moves.add(new Move(x, y, x + 1, y));
			moves.add(new Move(x, y, x + 1, y + 1));
			moves.add(new Move(x, y, x, y + 1));
		} else if (isAgainstLeftEdge(x, y)) {
			moves.add(new Move(x, y, x - 1, y));
			moves.add(new Move(x, y, x - 1, y + 1));
			moves.add(new Move(x, y, x, y + 1));
			moves.add(new Move(x, y, x + 1, y + 1));
			moves.add(new Move(x, y, x + 1, y));
		} else if (isAgainstRightEdge(x, y)) {
			moves.add(new Move(x, y, x - 1, y));
			moves.add(new Move(x, y, x - 1, y - 1));
			moves.add(new Move(x, y, x, y - 1));
			moves.add(new Move(x, y, x + 1, y - 1));
			moves.add(new Move(x, y, x + 1, y));
		} else if (isAgainstBottomEdge(x, y)) {
			moves.add(new Move(x, y, x, y - 1));
			moves.add(new Move(x, y, x - 1, y - 1));
			moves.add(new Move(x, y, x - 1, y));
			moves.add(new Move(x, y, x - 1, y + 1));
			moves.add(new Move(x, y, x, y + 1));
		} else {
			moves.add(new Move(x, y, x - 1, y - 1));
			moves.add(new Move(x, y, x - 1, y));
			moves.add(new Move(x, y, x - 1, y + 1));
			moves.add(new Move(x, y, x, y - 1));
			moves.add(new Move(x, y, x, y + 1));
			moves.add(new Move(x, y, x + 1, y - 1));
			moves.add(new Move(x, y, x + 1, y));
			moves.add(new Move(x, y, x + 1, y + 1));
		}
		return moves;
	}

	// all the moves a bishop can possibly make
	private ArrayList<Move> findBishopMoves(int x, int y) {
		ArrayList<Move> moves = new ArrayList<Move>();
		if (isInUpperLeftCorner(x, y)) {
			moves.add(new Move(0, 0, 1, 1));
		} else if (isInUpperRightCorner(x, y)) {
			moves.add(new Move(0, 5, 1, 4));
		} else if (isInLowerLeftCorner(x, y)) {
			moves.add(new Move(5, 0, 4, 1));
		} else if (isInLowerRightCorner(x, y)) {
			moves.add(new Move(5, 5, 4, 4));
		} else if (isAgainstTopEdge(x, y)) {
			moves.add(new Move(x, y, x + 1, y - 1));
			moves.add(new Move(x, y, x + 1, y + 1));
		} else if (isAgainstLeftEdge(x, y)) {
			moves.add(new Move(x, y, x - 1, y + 1));
			moves.add(new Move(x, y, x + 1, y + 1));
		} else if (isAgainstRightEdge(x, y)) {
			moves.add(new Move(x, y, x - 1, y - 1));
			moves.add(new Move(x, y, x + 1, y - 1));
		} else if (isAgainstBottomEdge(x, y)) {
			moves.add(new Move(x, y, x - 1, y - 1));
			moves.add(new Move(x, y, x - 1, y + 1));
		} else {
			moves.add(new Move(x, y, x - 1, y - 1));
			moves.add(new Move(x, y, x - 1, y + 1));
			moves.add(new Move(x, y, x + 1, y - 1));
			moves.add(new Move(x, y, x + 1, y + 1));
		}
		return moves;
	}

	// all the moves a rook can possibly make
	private ArrayList<Move> findRookMoves(int x, int y) {
		ArrayList<Move> moves = new ArrayList<Move>();
		if (isInUpperLeftCorner(x, y)) {
			moves.add(new Move(0, 0, 1, 0));
			moves.add(new Move(0, 0, 0, 1));
		} else if (isInUpperRightCorner(x, y)) {
			moves.add(new Move(0, 5, 1, 5));
			moves.add(new Move(0, 5, 0, 4));
		} else if (isInLowerLeftCorner(x, y)) {
			moves.add(new Move(5, 0, 4, 0));
			moves.add(new Move(5, 0, 5, 1));
		} else if (isInLowerRightCorner(x, y)) {
			moves.add(new Move(5, 5, 4, 5));
			moves.add(new Move(5, 5, 5, 4));
		} else if (isAgainstTopEdge(x, y)) {
			moves.add(new Move(x, y, x + 1, y));
			moves.add(new Move(x, y, x, y + 1));
			moves.add(new Move(x, y, x, y - 1));
		} else if (isAgainstLeftEdge(x, y)) {
			moves.add(new Move(x, y, x - 1, y));
			moves.add(new Move(x, y, x, y + 1));
			moves.add(new Move(x, y, x + 1, y));
		} else if (isAgainstRightEdge(x, y)) {
			moves.add(new Move(x, y, x - 1, y));
			moves.add(new Move(x, y, x, y - 1));
			moves.add(new Move(x, y, x + 1, y));
		} else if (isAgainstBottomEdge(x, y)) {
			moves.add(new Move(x, y, x - 1, y));
			moves.add(new Move(x, y, x, y + 1));
			moves.add(new Move(x, y, x, y - 1));
		} else {
			moves.add(new Move(x, y, x, y + 1));
			moves.add(new Move(x, y, x, y - 1));
			moves.add(new Move(x, y, x + 1, y));
			moves.add(new Move(x, y, x - 1, y));
		}
		return moves;
	}

	// The following methods check for the literal edge cases
	private boolean isAgainstBottomEdge(int x, int y) {
		if (x == 5 && y > 0 && y < 5)
			return true;
		return false;
	}

	private boolean isAgainstRightEdge(int x, int y) {
		if (y == 5 && x > 0 && x < 5)
			return true;
		return false;
	}

	private boolean isAgainstLeftEdge(int x, int y) {
		if (y == 0 && x > 0 && x < 5)
			return true;
		return false;
	}

	private boolean isAgainstTopEdge(int x, int y) {
		if (x == 0 && y > 0 && y < 5)
			return true;
		return false;
	}

	private boolean isInLowerRightCorner(int x, int y) {
		if (x == 5 && y == 5)
			return true;
		return false;
	}

	private boolean isInLowerLeftCorner(int x, int y) {
		if (x == 5 && y == 0)
			return true;
		return false;
	}

	private boolean isInUpperLeftCorner(int x, int y) {
		if (x == 0 && y == 0)
			return true;
		return false;
	}

	private boolean isInUpperRightCorner(int x, int y) {
		if (x == 0 && y == 5)
			return true;
		return false;
	}
}
