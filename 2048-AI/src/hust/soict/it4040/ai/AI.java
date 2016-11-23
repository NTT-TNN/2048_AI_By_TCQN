/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hust.soict.it4040.ai;

import hust.soict.it4040.Board;
import hust.soict.it4040.dataenum.Direction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author TCQN
 */
public class AI {

    public enum Player {
        COMPUTER_RANDOM,
        AI_PLAY;
    }

    private static int[][] W = {{6, 5, 4, 1}, {5, 4, 1, 0}, {4, 1, 0, -1}, {1, 0, -1, -2}};

    public static Direction findBestMove(Board theBoard, int depth) throws CloneNotSupportedException {
        Map<String, Object> result = expectimax(theBoard, depth, Player.AI_PLAY);
        
        return (Direction) result.get("Direction");
    }

    private static Map<String, Object> expectimax(Board theBoard, int depth, Player player) throws CloneNotSupportedException {
        Map<String, Object> result = new HashMap<>();

        Direction bestDirection = null;
        float bestScore;

        if (depth == 0 || theBoard.isGameTerminated()) {
            bestScore = heuristicScore(calculateClusteringScore(theBoard.getBoardArray()), calculatePositonScore(theBoard.getBoardArray()), theBoard.getScore(), theBoard.getNumberOfEmptyCells());
        } else if (player == Player.AI_PLAY) {
            bestScore = Integer.MIN_VALUE;

            for (Direction direction : Direction.values()) {
                Board newBoard = (Board) theBoard.clone();

                int points = newBoard.move(direction);

                if (points == 0 && newBoard.isEqual(theBoard.getBoardArray(), newBoard.getBoardArray())) {
                    continue;
                }

                Map<String, Object> currentResult = expectimax(newBoard, depth - 1, Player.COMPUTER_RANDOM);
                float currentScore = ((Number) currentResult.get("Score")).floatValue();
                if (currentScore > bestScore) { //maximize score
                    bestScore = currentScore;
                    bestDirection = direction;
                }
            }

        } else {
            bestScore = 0;

            List<Integer> moves = theBoard.getEmptyCellIds();
            if (moves.isEmpty()) {
                bestScore = 0;
            }

            int i, j;

            for (Integer cellId : moves) {
                i = cellId / Board.BOARD_SIZE;
                j = cellId % Board.BOARD_SIZE;

                Board newBoard = (Board) theBoard.clone();
                newBoard.setEmptyCell(i, j, 2);
                float newScore = ((Number) expectimax(newBoard, depth - 1, Player.AI_PLAY).get("Score")).floatValue();
                if ((int) newScore == Integer.MIN_VALUE) {
                    bestScore += 0;
                } else {
                    bestScore += (0.9 * newScore);
                }

                newBoard = (Board) theBoard.clone();
                newBoard.setEmptyCell(i, j, 4);

                newScore = ((Number) expectimax(newBoard, depth - 1, Player.AI_PLAY).get("Score")).floatValue();

                if ((int) newScore == Integer.MIN_VALUE) {
                    bestScore += 0;
                } else {
                    bestScore += (0.1 * newScore);
                }
            }
            bestScore /= moves.size();

        }

        result.put("Score", bestScore);
        result.put("Direction", bestDirection);

        return result;
    }

    private static int heuristicScore(int clusteringScore, int positionScore, int gameScore, int numberOfEmptyCell) {
        int score = (int) (positionScore - clusteringScore + Math.log(gameScore) * numberOfEmptyCell * numberOfEmptyCell);
        return ((score > 0) ? score : 0);
    }

    private static int calculateClusteringScore(int[][] boardArray) {
        int clusteringScore = 0;

        int[] neighbors = {-1, 0, 1};
        for (int i = 0; i < boardArray.length; ++i) {
            for (int j = 0; j < boardArray.length; ++j) {
                if (boardArray[i][j] == 0) {
                    continue;
                }

                int sum = 0;
                for (int k : neighbors) {
                    int x = i + k;
                    if (x < 0 || x >= boardArray.length) {
                        continue;
                    }
                    for (int l : neighbors) {
                        int y = j + l;
                        if (y < 0 || y >= boardArray.length) {
                            continue;
                        }

                        if (boardArray[x][y] > 0) {
                            sum += Math.abs(boardArray[i][j] - boardArray[x][y]);
                        }

                    }
                }

                clusteringScore = clusteringScore + sum;

            }
        }

        return clusteringScore;

    }

    private static int calculatePositonScore(int[][] boardArray) {
        int score = 0;

        for (int i = 0; i < boardArray.length; i++) {
            for (int j = 0; j < boardArray.length; j++) {
                score += W[i][j] * boardArray[i][j] * boardArray[i][j];
            }
        }
        return score;
    }

}
