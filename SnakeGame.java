import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakeGame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.add(new GamePanel());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static class GamePanel extends JPanel {
        private static final int CELL_SIZE = 30;
        private static final int GRID_WIDTH = 20;
        private static final int GRID_HEIGHT = 20;
        private static final int TIMER_DELAY = 150;
        private List<Point> snake;
        private int dirX = 1;  // Current direction: 1 = right, -1 = left
        private int dirY = 0;  // Current direction: 1 = down, -1 = up
        private int nextDirX = 1;  // Buffered direction for next move
        private int nextDirY = 0;
        private Timer gameTimer;
        private Point food;
        private int score = 0;
        private boolean gameOver = false;
        private Random random = new Random();

        public GamePanel() {
            setBackground(new Color(50, 50, 50)); // Dark gray background
            setFocusable(true);
            initSnake();
            setupKeyListener();
            setupGameTimer();
        }

        private void initSnake() {
            snake = new ArrayList<>();
            // Starting snake: 3 segments facing right, near center
            snake.add(new Point(8, 10));
            snake.add(new Point(9, 10));
            snake.add(new Point(10, 10));
            dirX = 1;
            dirY = 0;
            nextDirX = 1;
            nextDirY = 0;
            score = 0;
            gameOver = false;
            spawnFood();
        }

        private void setupKeyListener() {
            addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
                        initSnake();
                        repaint();
                        return;
                    }

                    if (gameOver) return; // Don't allow movement when game is over

                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_UP:
                            // Don't allow reversing into down direction
                            if (dirY == 0) {
                                nextDirX = 0;
                                nextDirY = -1;
                            }
                            break;
                        case KeyEvent.VK_DOWN:
                            // Don't allow reversing into up direction
                            if (dirY == 0) {
                                nextDirX = 0;
                                nextDirY = 1;
                            }
                            break;
                        case KeyEvent.VK_LEFT:
                            // Don't allow reversing into right direction
                            if (dirX == 0) {
                                nextDirX = -1;
                                nextDirY = 0;
                            }
                            break;
                        case KeyEvent.VK_RIGHT:
                            // Don't allow reversing into left direction
                            if (dirX == 0) {
                                nextDirX = 1;
                                nextDirY = 0;
                            }
                            break;
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {}

                @Override
                public void keyTyped(KeyEvent e) {}
            });
        }

        private void setupGameTimer() {
            gameTimer = new Timer(TIMER_DELAY, e -> moveSnake());
            gameTimer.start();
        }

        private void moveSnake() {
            if (gameOver) return;

            // Update direction from buffered input
            dirX = nextDirX;
            dirY = nextDirY;

            // Calculate new head position
            Point head = snake.get(0);
            int newX = head.x + dirX;
            int newY = head.y + dirY;

            // Check collision with walls
            if (newX < 0 || newX >= GRID_WIDTH || newY < 0 || newY >= GRID_HEIGHT) {
                gameOver = true;
                repaint();
                return;
            }

            Point newHead = new Point(newX, newY);

            // Check collision with own body
            if (snake.contains(newHead)) {
                gameOver = true;
                repaint();
                return;
            }

            // Add new head
            snake.add(0, newHead);

            // Check if eating food
            if (newHead.equals(food)) {
                score += 10;
                spawnFood();
                // Don't remove tail when eating food
            } else {
                // Remove tail only if not eating
                snake.remove(snake.size() - 1);
            }

            // Repaint
            repaint();
        }

        private void spawnFood() {
            Point newFood;
            do {
                int x = random.nextInt(GRID_WIDTH);
                int y = random.nextInt(GRID_HEIGHT);
                newFood = new Point(x, y);
            } while (snake.contains(newFood));
            food = newFood;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawGrid(g);
            drawSnake(g);
            drawFood(g);
            drawScore(g);
            if (gameOver) {
                drawGameOver(g);
            }
        }

        private void drawGrid(Graphics g) {
            g.setColor(new Color(100, 100, 100)); // Grid lines
            for (int i = 0; i <= GRID_WIDTH; i++) {
                g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
            }
            for (int i = 0; i <= GRID_HEIGHT; i++) {
                g.drawLine(0, i * CELL_SIZE, GRID_WIDTH * CELL_SIZE, i * CELL_SIZE);
            }
        }

        private void drawSnake(Graphics g) {
            g.setColor(Color.GREEN);
            for (Point segment : snake) {
                g.fillRect(segment.x * CELL_SIZE, segment.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        private void drawFood(Graphics g) {
            if (food != null) {
                g.setColor(Color.RED);
                g.fillRect(food.x * CELL_SIZE, food.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        private void drawScore(Graphics g) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Score: " + score, 10, 20);
        }

        private void drawGameOver(Graphics g) {
            // Semi-transparent overlay
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());

            // Game Over text
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            String gameOverText = "Game Over";
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(gameOverText)) / 2;
            g.drawString(gameOverText, x, getHeight() / 2 - 40);

            // Score text
            g.setFont(new Font("Arial", Font.PLAIN, 32));
            String scoreText = "Final Score: " + score;
            fm = g.getFontMetrics();
            x = (getWidth() - fm.stringWidth(scoreText)) / 2;
            g.drawString(scoreText, x, getHeight() / 2 + 20);

            // Reset instruction
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            String resetText = "Press R to play again";
            fm = g.getFontMetrics();
            x = (getWidth() - fm.stringWidth(resetText)) / 2;
            g.drawString(resetText, x, getHeight() / 2 + 80);
        }
    }
}
