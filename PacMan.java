import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;
        Image baseImage;
        boolean isScared = false;

        int startX;
        int startY;
        char direction = 'U'; // U D L R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.baseImage = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize/4;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize/4;
            }
            else if (this.direction == 'L') {
                this.velocityX = -tileSize/4;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = tileSize/4;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    public int getBoardWidth() {
        return boardWidth;
    }

    public int getBoardHeight() {
        return boardHeight;
    }

    public void handleTunnelWrap(Block entity) {
        if (entity.x + entity.width <= 0) {
            entity.x = boardWidth - entity.width;
        } else if (entity.x >= boardWidth) {
            entity.x = 0;
        }
    }

    public char getOppositeDirection(char dir) {
        if (dir == 'U') return 'D';
        if (dir == 'D') return 'U';
        if (dir == 'L') return 'R';
        if (dir == 'R') return 'L';
        return ' ';
    }

    public boolean canMove(Block block, char dir) {
        int vx = 0;
        int vy = 0;
        if (dir == 'U') vy = -tileSize/4;
        else if (dir == 'D') vy = tileSize/4;
        else if (dir == 'L') vx = -tileSize/4;
        else if (dir == 'R') vx = tileSize/4;

        int nextX = block.x + vx;
        int nextY = block.y + vy;
        int w = block.width;
        int h = block.height;

        for (Block wall : walls) {
            if (nextX < wall.x + wall.width &&
                nextX + w > wall.x &&
                nextY < wall.y + wall.height &&
                nextY + h > wall.y) {
                return false;
            }
        }
        return true;
    }

    public java.util.List<Character> getValidDirections(Block block, boolean excludeReverse) {
        java.util.List<Character> valid = new java.util.ArrayList<Character>();
        char opp = getOppositeDirection(block.direction);
        for (char d : directions) {
            if (excludeReverse && d == opp) continue;
            if (canMove(block, d)) {
                valid.add(d);
            }
        }
        if (valid.isEmpty() && excludeReverse && canMove(block, opp)) {
            valid.add(opp);
        }
        return valid;
    }

    char nextDirection = ' ';

    public void setNextDirection(char dir) {
        this.nextDirection = dir;
    }

    public char getNextDirection() {
        return this.nextDirection;
    }

    public void updatePacmanImage() {
        if (pacman.direction == 'U') {
            pacman.image = pacmanUpImage;
        }
        else if (pacman.direction == 'D') {
            pacman.image = pacmanDownImage;
        }
        else if (pacman.direction == 'L') {
            pacman.image = pacmanLeftImage;
        }
        else if (pacman.direction == 'R') {
            pacman.image = pacmanRightImage;
        }
    }

    public void applyBufferedDirection() {
        if (nextDirection != ' ' && canMove(pacman, nextDirection)) {
            if (pacman.direction != nextDirection || (pacman.velocityX == 0 && pacman.velocityY == 0)) {
                pacman.direction = nextDirection;
                pacman.updateVelocity();
                updatePacmanImage();
            }
        }
    }

    private Image wallImage;
    private Image powerFoodImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;
    Image scaredGhostImage;
    int scaredTimer = 0;

    public int getScaredTimer() {
        return scaredTimer;
    }

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    //X = wall, O = skip, P = pac man, ' ' = food
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X*               *X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X*               *X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> powerFoods;

    public HashSet<Block> getPowerFoods() {
        return powerFoods;
    }
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'}; //up down left right
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        //load images
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        powerFoodImage = new ImageIcon(getClass().getResource("./powerFood.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
        scaredGhostImage = new ImageIcon(getClass().getResource("./scaredGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap();
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        //how long it takes to start timer, milliseconds gone between frames
        gameLoop = new Timer(50, this); //20fps (1000/50)
        gameLoop.start();

    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        powerFoods = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c*tileSize;
                int y = r*tileSize;

                if (tileMapChar == 'X') { //block wall
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') { //blue ghost
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') { //orange ghost
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') { //pink ghost
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') { //red ghost
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') { //pacman
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') { //food
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
                else if (tileMapChar == '*') { //power food
                    Block powerFood = new Block(powerFoodImage, x + 8, y + 8, 16, 16);
                    powerFoods.add(powerFood);
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        for (Block powerFood : powerFoods) {
            g.drawImage(powerFood.image, powerFood.x, powerFood.y, powerFood.width, powerFood.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }
        //score
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf(score), tileSize/2, tileSize/2);
        }
        else {
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize/2, tileSize/2);
        }
    }

    public void move() {
        if (scaredTimer > 0) {
            scaredTimer--;
            if (scaredTimer == 0) {
                for (Block ghost : ghosts) {
                    ghost.isScared = false;
                    ghost.image = ghost.baseImage;
                }
            }
        }
        applyBufferedDirection();
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;
        handleTunnelWrap(pacman);

        //check wall collisions
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        checkGhostCollisions();
        if (gameOver) return;
        for (Block ghost : ghosts) {

            if (ghost.y == tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            handleTunnelWrap(ghost);
            for (Block wall : walls) {
                if (collision(ghost, wall) || ((ghost.x < 0 || ghost.x + ghost.width > boardWidth) && ghost.y != tileSize*9)) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }
        checkGhostCollisions();
        checkFoodCollisions();
    }

        public void checkGhostCollisions() {
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                if (ghost.isScared) {
                    score += 200;
                    ghost.reset();
                    ghost.isScared = false;
                    ghost.image = ghost.baseImage;
                }
                else {
                    lives -= 1;
                    if (lives == 0) {
                        gameOver = true;
                        return;
                    }
                    resetPositions();
                    break;
                }
            }
        }
    }

    public void checkFoodCollisions() {
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
                break;
            }
        }
        if (foodEaten != null) {
            foods.remove(foodEaten);
        }

        Block powerFoodEaten = null;
        for (Block powerFood : powerFoods) {
            if (collision(pacman, powerFood)) {
                powerFoodEaten = powerFood;
                score += 50;
                scaredTimer = 160;
                for (Block ghost : ghosts) {
                    ghost.isScared = true;
                    ghost.image = scaredGhostImage;
                }
                break;
            }
        }
        if (powerFoodEaten != null) {
            powerFoods.remove(powerFoodEaten);
        }

        if (foods.isEmpty() && powerFoods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        pacman.image = pacmanRightImage;
        nextDirection = ' ';
        scaredTimer = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            ghost.isScared = false;
            ghost.image = ghost.baseImage;
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    public void handleKeyInput(int keyCode) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
            return;
        }

        char dir = ' ';
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            dir = 'U';
        }
        else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            dir = 'D';
        }
        else if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            dir = 'L';
        }
        else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            dir = 'R';
        }

        if (dir != ' ') {
            setNextDirection(dir);
            applyBufferedDirection();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        handleKeyInput(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        handleKeyInput(e.getKeyCode());
    }
}
