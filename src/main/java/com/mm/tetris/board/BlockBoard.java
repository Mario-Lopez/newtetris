package com.mm.tetris.board;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BlockBoard {

    private static Logger log = LoggerFactory.getLogger(BlockBoard.class);
	
	@Inject
	private Configuration config;
	
	@Inject
	private TetrominoFactory tetrominoFactory;

	/**
	 * Number of blocks
	 */
	private int width = 10;
	private int height = 20;
	private int heightPadding = 3;
	
	/**
	 * The board
	 */
	private Block[][] board;
	
	/**
	 * The four falling blocks
	 */
	private LinkedList<Position> fallingBlocks;
	
	/**
	 * The length of the tetromino (2, 3 or 4).  Used when rotating the falling
	 * tetromino  
	 */
	private int tetrominoLength;
	
	/**
	 * The upper-left point of the tetromino area used when rotating
	 */
	private Position rotatePosition;
	
	/**
	 * The next piece
	 */
	private Tetromino nextPiece;

	
	/**
	 * Constructor
	 */
	public BlockBoard() {
		// do nothing
	}
	
	public void init() {
		tetrominoFactory.init();
		board = new Block[width][height + heightPadding];
        rotatePosition = new Position();
	}
	
	/**
	 * Start the game
	 */
	public void start() {
		board = new Block[width][height + heightPadding];
        putNewPieceOnBlockBoard(tetrominoFactory.getRandomTetromino());
		nextPiece = tetrominoFactory.getRandomTetromino();
	}
	
	/**
	 * Puts the specified tetromino on the board
	 * @param tetromino Tetromino
	 */
	private void putNewPieceOnBlockBoard(Tetromino tetromino) {
		fallingBlocks = new LinkedList<>();
		int deltaX, deltaY;
		
		// position the piece in the middle with one row shown
		rotatePosition.setX(deltaX = (width / 2) - (tetromino.getLength() / 2));
		rotatePosition.setY(deltaY = 1 - tetromino.getLength());
		
		// set the falling blocks and update the board with the block textures
		for (Position pos : tetromino.getPositions()) {
			int x = pos.getX() + deltaX;
			int y = pos.getY() + deltaY;
			fallingBlocks.add(new Position(x, y));
            setBlockAt(tetromino.getBlock(), x, y);
		}
	}

    /**
     * Drop the falling blocks one row.  Return false if unable to.
     * @return TetrominoDropResult DROPPED if tetromino dropped, SET if tetromino was set and the
     * next Tetromino was placed on the board
     */
    public TetrominoDropResult dropOneRow() {
        if (canCurrentTetrominoFall()) {
            dropCurrentTetromino();
            return TetrominoDropResult.DROPPED;
        } else {
            setCurrentTetromino();
            return TetrominoDropResult.SET;
        }
    }

    /**
     * Check if the currently falling blocks are allowed to move down one row.
     * @return boolean true if falling pieces can fall
     */
    private boolean canCurrentTetrominoFall() {
        for (Position fallingBlock : fallingBlocks) {
            if (fallingBlock.getY() == height - 1)
                return false;

            Block nextBlock = getBlockAt(fallingBlock.getX(), fallingBlock.getY() + 1);
            if (nextBlock != null && !nextBlock.isFalling())
                return false;
        }

        return true;
    }

    /**
     * Drop the current falling blocks one row
     */
    private void dropCurrentTetromino() {
        // save a block
        Block block = getBlockAt(fallingBlocks.get(0).getX(), fallingBlocks.get(0).getY());

        // clear the current blocks
        for (Position fallingBlock : fallingBlocks) {
            setBlockAt(null, fallingBlock.getX(), fallingBlock.getY());
            fallingBlock.setY(fallingBlock.getY() + 1);
        }

        // set new positions for falling blocks
        for (Position fallingBlock : fallingBlocks) {
            setBlockAt(block, fallingBlock.getX(), fallingBlock.getY());
        }

        // move the rotation position down one row
        rotatePosition.setY(rotatePosition.getY() + 1);
    }

    /**
     * Stops the current falling blocks
     */
    private void setCurrentTetromino() {
        Block block = getBlockAt(fallingBlocks.get(0).getX(), fallingBlocks.get(0).getY()).clone();
        block.stopFalling();

        for (Position fallingBlock : fallingBlocks) {
            setBlockAt(block, fallingBlock.getX(), fallingBlock.getY());
        }
    }

    /**
     * Puts the Next Tetromino on the board and creates a new next tetromino
     */
    public void loadNextTetromino() {
        putNewPieceOnBlockBoard(nextPiece);
        nextPiece = tetrominoFactory.getRandomTetromino();
    }

    /**
     * Returns the list of completed rows
     * @return List<Integer> rows that are complete, empty list if no rows are complete
     */
    public List<Integer> getCompletedRows() {
        List<Integer> completedRows = new LinkedList<>();

        for (int row = 0; row < height; row++) {
            boolean rowComplete = true;

            for (int col = 0; col < width; col++) {
                if (getBlockAt(col, row) == null) {
                    rowComplete = false;
                    break;
                }
            }

            if (rowComplete)
                completedRows.add(row);
        }

        return completedRows;
    }

    /**
     * Remove the specified rows and move the rows above it down
     * @param rows List<Integer>
     */
    public void clearRows(final List<Integer> rows) {
        if (rows.isEmpty()) {
            return;
        }

        // check if we can quickly clear a chunk of rows
        boolean isContinuousRows = rows.size() == 1 || rows.size() == 4 ||
                (rows.size() == 2 && diffIsOne(rows.get(0), rows.get(1))) ||
                (rows.size() == 3 && diffIsOne(rows.get(0), rows.get(1)) && diffIsOne(rows.get(1), rows.get(2)));

        if (isContinuousRows) {
            // start from the row above the one(s) we want to clear
            for (int row = rows.get(0) - rows.size(); row >= 0; row--) {
                for (int col = 0; col < width; col++) {
                    setBlockAt(getBlockAt(col, row), col, row + rows.size());
                }
            }
        } else {
            // slower method, clears each row one by one
            for (int rowToClear : rows) {
                for (int row = rowToClear - 1; row >= 0; row--) {
                    for (int col = 0; col < width; col++) {
                        setBlockAt(getBlockAt(col, row), col, row + 1);
                    }
                }
            }
        }
    }

    /**
     * Determines if the difference between the two specified integers is 1.
     * @param x1 int
     * @param x2 int
     * @return boolean true if the difference is 1
     */
    private boolean diffIsOne(int x1, int x2) {
        return (x1 - x2 == 1 || x1 - x2 == -1);
    }
	
	
	
	
	
	
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public Block getBlockAt(final int x, final int y) {
		return board[x][y + heightPadding];
	}

    private void setBlockAt(Block block, int x, int y) {
        board[x][y + heightPadding] = block;
    }

    public static enum TetrominoDropResult {
        DROPPED,
        SET
    }
}