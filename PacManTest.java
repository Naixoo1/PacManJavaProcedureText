public class PacManTest {
    public static void testTunnelWrap() {
        PacMan game = new PacMan();
        if (game.gameLoop != null) game.gameLoop.stop();

        // Place pacman at left tunnel opening (Row 9, col 0) moving left past left edge
        game.pacman.x = -game.pacman.width;
        game.pacman.y = 9 * 32;
        game.pacman.velocityX = -8;
        game.pacman.velocityY = 0;
        
        game.handleTunnelWrap(game.pacman);
        if (game.pacman.x != game.getBoardWidth() - game.pacman.width) {
            throw new AssertionError("Pacman did not wrap from left to right. Expected: " + 
                (game.getBoardWidth() - game.pacman.width) + " but got: " + game.pacman.x);
        }

        // Place pacman at right tunnel opening moving right past right edge
        game.pacman.x = game.getBoardWidth();
        game.handleTunnelWrap(game.pacman);
        if (game.pacman.x != 0) {
            throw new AssertionError("Pacman did not wrap from right to left. Expected: 0 but got: " + game.pacman.x);
        }
        System.out.println("testTunnelWrap passed");
    }

    public static void testPowerPelletsLoaded() {
        PacMan game = new PacMan();
        if (game.gameLoop != null) game.gameLoop.stop();

        java.util.HashSet<PacMan.Block> powerFoods = game.getPowerFoods();
        if (powerFoods == null || powerFoods.size() != 4) {
            throw new AssertionError("Expected 4 power pellets, found: " + (powerFoods == null ? 0 : powerFoods.size()));
        }
        System.out.println("testPowerPelletsLoaded passed");
    }

    public static void testPowerPelletEatsGhost() {
        PacMan game = new PacMan();
        if (game.gameLoop != null) game.gameLoop.stop();

        int initialScore = game.score;
        int initialLives = game.lives;

        // Simulate eating power food
        PacMan.Block pf = game.getPowerFoods().iterator().next();
        game.pacman.x = pf.x;
        game.pacman.y = pf.y;
        game.checkFoodCollisions();

        if (game.getScaredTimer() <= 0) {
            throw new AssertionError("Scared timer not activated upon eating power pellet");
        }
        if (game.score != initialScore + 50) {
            throw new AssertionError("Score did not increase by 50. Current score=" + game.score);
        }

        // Now test eating a ghost while scared in an isolated position
        PacMan.Block ghost = game.ghosts.iterator().next();
        ghost.x = 200;
        ghost.y = 200;
        game.pacman.x = 200;
        game.pacman.y = 200;
        game.checkGhostCollisions();

        if (game.lives != initialLives) {
            throw new AssertionError("Lives decreased when eating scared ghost! lives=" + game.lives);
        }
        if (game.score != initialScore + 50 + 200) {
            throw new AssertionError("Score did not award 200 for eating scared ghost. score=" + game.score);
        }
        if (ghost.x != ghost.startX || ghost.y != ghost.startY) {
            throw new AssertionError("Eaten ghost did not reset to start position");
        }
        System.out.println("testPowerPelletEatsGhost passed");
    }

    public static void testGhostIntersectionTurning() {
        PacMan game = new PacMan();
        if (game.gameLoop != null) game.gameLoop.stop();

        PacMan.Block ghost = game.ghosts.iterator().next();
        
        // Position ghost at intersection (Row 5, Col 1) where multiple directions are open
        ghost.x = 1 * 32;
        ghost.y = 5 * 32;
        ghost.direction = 'U';
        ghost.updateVelocity();

        // Check available directions at this junction
        java.util.List<Character> openDirs = game.getValidDirections(ghost, true);
        if (!openDirs.contains('R')) {
            throw new AssertionError("Ghost cannot detect open right turn at intersection (Row 5, Col 1)");
        }
        System.out.println("testGhostIntersectionTurning passed");
    }

    public static void testInputBuffering() {
        PacMan game = new PacMan();
        if (game.gameLoop != null) game.gameLoop.stop();

        game.pacman.x = 9 * 32;
        game.pacman.y = 15 * 32; // Start position
        game.pacman.direction = 'L';
        game.pacman.updateVelocity();
        
        // Set nextDirection to 'R' (opposite or pending turn)
        game.setNextDirection('R');
        game.applyBufferedDirection();
        
        if (game.pacman.direction != 'R') {
            throw new AssertionError("Buffered direction 'R' was not applied when open. direction=" + game.pacman.direction);
        }
        System.out.println("testInputBuffering passed");
    }

    public static void testReviewFocusTunnelNoWallCollision() {
        PacMan game = new PacMan();
        if (game.gameLoop != null) game.gameLoop.stop();

        // PacMan at Row 9
        game.pacman.y = 9 * 32;
        int[] testX = {-32, -16, -8, 0, game.getBoardWidth() - 32, game.getBoardWidth(), game.getBoardWidth() + 16};
        for (int x : testX) {
            game.pacman.x = x;
            for (PacMan.Block wall : game.walls) {
                if (game.collision(game.pacman, wall)) {
                    throw new AssertionError("Pacman collided with wall during tunnel wrap at x=" + x);
                }
            }
        }
        System.out.println("testReviewFocusTunnelNoWallCollision passed");
    }

    public static void testReviewFocusDoubleEat() {
        PacMan game = new PacMan();
        if (game.gameLoop != null) game.gameLoop.stop();

        // Place food and powerFood at same spot
        PacMan.Block food = game.foods.iterator().next();
        PacMan.Block powerFood = game.getPowerFoods().iterator().next();
        game.pacman.x = food.x;
        game.pacman.y = food.y;
        powerFood.x = food.x;
        powerFood.y = food.y;

        int beforeFoods = game.foods.size();
        int beforePower = game.getPowerFoods().size();

        game.checkFoodCollisions();

        if (game.foods.size() != beforeFoods - 1 || game.getPowerFoods().size() != beforePower - 1) {
            throw new AssertionError("Both food and powerFood should be eaten cleanly without exception");
        }
        System.out.println("testReviewFocusDoubleEat passed");
    }

    public static void testReviewFocusEatenGhostImmunity() {
        PacMan game = new PacMan();
        if (game.gameLoop != null) game.gameLoop.stop();

        PacMan.Block ghost = game.ghosts.iterator().next();
        // Trigger scared mode
        game.scaredTimer = 100;
        ghost.isScared = true;
        ghost.image = game.scaredGhostImage;

        // Eat ghost once
        ghost.x = 200;
        ghost.y = 200;
        game.pacman.x = 200;
        game.pacman.y = 200;
        game.checkGhostCollisions();

        int scoreAfterFirstEat = game.score;

        // Verify ghost is no longer scared
        if (ghost.isScared) {
            throw new AssertionError("Eaten ghost should have isScared=false");
        }

        // Place Pac-Man at ghost spawn position inside house
        game.pacman.x = ghost.x;
        game.pacman.y = ghost.y;
        int livesBefore = game.lives;
        game.checkGhostCollisions();

        // Should NOT award another 200 points; should decrement life because ghost is normal
        if (game.score != scoreAfterFirstEat) {
            throw new AssertionError("Eaten ghost was re-eaten for points! score=" + game.score);
        }
        if (game.lives != livesBefore - 1) {
            throw new AssertionError("Ghost in house did not damage Pac-Man when no longer scared");
        }
        System.out.println("testReviewFocusEatenGhostImmunity passed");
    }

    public static void testReviewFocusGhostDeadEnd() {
        PacMan game = new PacMan();
        if (game.gameLoop != null) game.gameLoop.stop();

        PacMan.Block ghost = game.ghosts.iterator().next();
        // Position ghost facing Left, only Right is open
        ghost.x = 1 * 32;
        ghost.y = 1 * 32;
        ghost.direction = 'L'; // Moving Left into wall at x=0
        
        java.util.List<Character> dirs = game.getValidDirections(ghost, true);
        if (dirs.isEmpty()) {
            throw new AssertionError("Dead end must return at least the 180-degree turnaround direction");
        }
        System.out.println("testReviewFocusGhostDeadEnd passed");
    }

    public static void testReviewFocusGhostHouseExit() {
        PacMan game = new PacMan();
        if (game.gameLoop != null) game.gameLoop.stop();

        PacMan.Block ghost = game.ghosts.iterator().next();
        // At row 9, col 6: open upward passage to row 7
        ghost.x = 6 * 32;
        ghost.y = 9 * 32;
        if (!game.canMove(ghost, 'U')) {
            throw new AssertionError("Ghost at (col 6, row 9) cannot move Up out of ghost house");
        }
        System.out.println("testReviewFocusGhostHouseExit passed");
    }

    public static void testLeftAndRightArrowMovement() {
        PacMan game = new PacMan();
        if (game.gameLoop != null) game.gameLoop.stop();

        int startX = game.pacman.x;
        int startY = game.pacman.y;

        // Press Left arrow
        java.awt.event.KeyEvent leftPress = new java.awt.event.KeyEvent(
            game, java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_LEFT, ' '
        );
        game.keyPressed(leftPress);

        if (game.pacman.direction != 'L') {
            throw new AssertionError("Pressing Left arrow did not set direction to 'L'. Got: " + game.pacman.direction);
        }
        if (game.getNextDirection() != 'L') {
            throw new AssertionError("Pressing Left arrow did not set nextDirection to 'L'. Got: " + game.getNextDirection());
        }

        // Advance 1 game tick
        game.move();
        if (game.pacman.x != startX - 8) {
            throw new AssertionError("Pacman did not move left by 8 pixels. Expected: " + (startX - 8) + ", got: " + game.pacman.x);
        }
        if (game.pacman.direction != 'L') {
            throw new AssertionError("Pacman direction reverted from 'L'. Got: " + game.pacman.direction);
        }

        // Now press Right arrow while moving left
        java.awt.event.KeyEvent rightPress = new java.awt.event.KeyEvent(
            game, java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_RIGHT, ' '
        );
        game.keyPressed(rightPress);

        if (game.pacman.direction != 'R') {
            throw new AssertionError("Pressing Right arrow did not set direction to 'R'. Got: " + game.pacman.direction);
        }
        if (game.getNextDirection() != 'R') {
            throw new AssertionError("Pressing Right arrow did not set nextDirection to 'R'. Got: " + game.getNextDirection());
        }

        // Advance 1 game tick
        game.move();
        if (game.pacman.x != startX) {
            throw new AssertionError("Pacman did not move right by 8 pixels. Expected: " + startX + ", got: " + game.pacman.x);
        }
        if (game.pacman.direction != 'R') {
            throw new AssertionError("Pacman direction reverted from 'R'. Got: " + game.pacman.direction);
        }

        System.out.println("testLeftAndRightArrowMovement passed");
    }

    public static void testBufferedDirectionAtJunction() {
        PacMan game = new PacMan();
        if (game.gameLoop != null) game.gameLoop.stop();

        // PacMan at (7 * 32, 15 * 32), moving right towards (8 * 32, 15 * 32)
        // At col 7, row 14: 'X' (wall above)
        // At col 8, row 14: ' ' (open passage above)
        game.pacman.x = 7 * 32;
        game.pacman.y = 15 * 32;
        game.pacman.direction = 'R';
        game.pacman.updateVelocity();

        // Buffer UP turn while wall is above at col 7
        game.handleKeyInput(java.awt.event.KeyEvent.VK_UP);
        if (game.pacman.direction != 'R') {
            throw new AssertionError("Pacman turned into wall prematurely. direction=" + game.pacman.direction);
        }
        if (game.getNextDirection() != 'U') {
            throw new AssertionError("nextDirection was not buffered as 'U'");
        }

        // Move forward until the junction opens and buffered turn is executed
        int steps = 0;
        while (game.pacman.direction != 'U' && steps < 10) {
            game.move();
            steps++;
        }

        if (game.pacman.x != 8 * 32) {
            throw new AssertionError("Pacman did not arrive at junction at col 8. x=" + game.pacman.x);
        }
        if (game.pacman.direction != 'U') {
            throw new AssertionError("Pacman did not execute buffered UP turn at junction. direction=" + game.pacman.direction);
        }

        System.out.println("testBufferedDirectionAtJunction passed");
    }

    public static void main(String[] args) {
        testTunnelWrap();
        testPowerPelletsLoaded();
        testPowerPelletEatsGhost();
        testGhostIntersectionTurning();
        testInputBuffering();
        testLeftAndRightArrowMovement();
        testBufferedDirectionAtJunction();
        testReviewFocusTunnelNoWallCollision();
        testReviewFocusDoubleEat();
        testReviewFocusEatenGhostImmunity();
        testReviewFocusGhostDeadEnd();
        testReviewFocusGhostHouseExit();
        System.out.println("All Review Focus edge cases passed!");
    }
}
