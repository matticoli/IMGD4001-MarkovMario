package agents.MatticoliKumykova;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;

/**
 * IMGD 4100 - Diana Kumykova, Mikel Matticoli
 * Mario AI Agent developed with the goal of passing the turing test
 * Adapted from trondEllingsen agent - this agent had the most human-like behavior
 * from our observation of the provided agents, and included base code for detecting
 * and categorizing upcoming obstacles
 */

public class Agent implements MarioAgent {
    /**
     * Identify different things a player might jump over
     */
    private enum JumpType {
        ENEMY, GAP, WALL, NONE, POWERUP
        //TODO: Maybe add up jump state where we jump up from idle?
    }

    /**
     * Identify states that Mario can be in
     */
    private enum State {
        IDLE, // Sit and think
        ONWARDS, // Progress in the level normally to the right
        //TODO: maybe add another forward state that temporarily switches
        // to a-star so we look like we know what we're doing sometimes
        BACKWARDS, // Progress in the level towards the left
        BOLD, // Jump forward when faced with obstacles
        SKITTISH // Jump backwards when faced with obstacles
    }

    /**
     * Define Rectangle class for game object tracking
     */
    private class Rectangle {
        private float x, y, width, height;

        public Rectangle(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        // Checks if the specified rectangle is contained by `this` rectangle
        public boolean contains(float x, float y) {
            return x >= this.x && y >= this.y && x <= this.x + this.width && y <= this.y + this.height;
        }
    }

    /**
     * Define state variables
     */
    private JumpType jumpType = JumpType.NONE; // Current jump state
    private State state = State.IDLE; // Current behavior state
    private int jumpCount = 0, jumpSize = -1; // Jump counter and target jump height (while jumping)
    private float prevY = 0; // Previous y coordinate of Mario
    private boolean[] action; // Action array, returned by getActions
        // (defines Mario control input on each iteration of game loop)
    int skit_count = 0;
    int back_count = 0;
    int start_count = 0;


    /**
     * Initialize agent
     * @param model a forward model object so the agent can simulate or initialize some parameters based on it.
     * @param timer amount of time before the agent has to return
     */
    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        action = new boolean[MarioActions.numberOfActions()];
        action[MarioActions.RIGHT.getValue()] = true;
        action[MarioActions.SPEED.getValue()] = false;
    }

    /**
     * Utility function provided by trondEllingsen for getting height of given wall
     *
     * @param tileX      x-coordinate of obstructing wall tile
     * @param tileY      y-coordinate of obstructing wall tile
     * @param levelScene level reference
     * @return wall height in tiles
     */
    private int getWallHeight(int tileX, int tileY, int[][] levelScene) {
        int y = tileY + 1, wallHeight = 0;
        while (y-- > 0 && levelScene[tileX + 1][y] != 0) {
            wallHeight++;
        }
        return wallHeight;
    }

    /**
     * @param tileX
     * @param tileY
     * @param levelScene
     * @return true if there's a gap, else false
     */
    private boolean dangerOfGap(int tileX, int tileY, int[][] levelScene) {
        for (int y = tileY + 1; y < levelScene[0].length; y++) {
            if (levelScene[tileX + 1][y] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if any enemies overlap
     *
     * @param e
     * @param r
     * @return
     */
    private boolean enemyInRange(MarioForwardModel e, Rectangle r) {
        for (int i = 0; i < e.getEnemiesFloatPos().length; i += 3) {
            if (r.contains(e.getEnemiesFloatPos()[i + 1] - e.getMarioFloatPos()[0],
                    e.getMarioFloatPos()[1] - e.getEnemiesFloatPos()[i + 2])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if there's an overhead block
     * @param tileX Mario x coord
     * @param tileY Mario y coord
     * @param levelScene level
     * @return true if overhead block, else false
     */
    private boolean checkIfOverheadBlock(int tileX, int tileY, int[][] levelScene) {
        //TODO: Implement
//        for (int y = tileY + 1; y < levelScene[0].length; y++) {
//            if (levelScene[tileX + 1][y] != 0) {
//                return false;
//            }
//        }
        return true;
    }

    /**
     * Checks if block is powerup block
     * @param scene block x
     * @return true if block is powerup, else false
     */
    private boolean isPowerupBlock(int[][] scene) {
        //TODO: Implement
        int[] above = new int[]{getLocation(1, -1, scene), getLocation(1, -2, scene), getLocation(1, -3, scene)};

        for (int i = 0; i < above.length; i++) {
            if (above[i] == 24 || above[i] == 17) {
                return true;
            }
        }
        return false;
    }

    /**
     * Begin jump
     *
     * @param type jump type
     * @param size jump height
     */
    private final void setJump(final JumpType type, final int size) {
        jumpType = type;
        jumpSize = size;
        jumpCount = 0;
    }

    /**
     * Utility function for converting percent chance to boolean
     * @param percentChance percent chance of returning true
     * @return true <percentChance>% of the time, else false
     */
    private boolean maybe(int percentChance) {
        return Math.random() * 100 < percentChance;
    }

    /**
     * Utility function provided by michal; translates relative coordiantes
     * into actual location in level for block given
     *
     * @param relX
     * @param relY
     * @param scene level reference
     * @return block coords in level
     */
    private int getLocation(int relX, int relY, int[][] scene) {
        int realX = 8 + relX;
        int realY = 8 + relY;

        return scene[realX][realY];
    }

    /**
     * Utility function provided by michal; searches for pipes in front and below
     * Mario
     *
     * @param scene
     * @return true if obstacle of specified type exists; else false
     */
    private boolean thereIsObstacle(int[][] scene) {
        int[] inFrontOf = new int[] { getLocation(1, 0, scene), getLocation(2, 0, scene), getLocation(2, -1, scene) };

        for (int i = 0; i < inFrontOf.length; i++) {
            if (inFrontOf[i] == 17 || inFrontOf[i] == 23 || inFrontOf[i] == 24) {
                return true;
            }
        }

        return false;
    }

    private boolean obstacleBehind(int[][] scene) {
        int[] behind = new int[] { getLocation(-1, 0, scene), getLocation(-2, 0, scene) };

        for (int i = 0; i < behind.length; i++) {
            if (behind[i] == 17 || behind[i] == 23 || behind[i] == 24) {
                return true;
            }
        }

        return false;
    }

    /**
     * Utility function provided by michal; searches are in front of Mario for empty blocks
     *
     * @param scene
     * @return false if empty block; else true
     */
    private boolean thereIsStair(int[][] scene) {
        int[] below = new int[] { getLocation(1, 1, scene) , getLocation(1, 2, scene)};

        if(below[0] == 0 && below[1] != 0) {
            return true;
        }

        return false;
    }

    /**
     * Utility function provided by michal; searches for enemies directly in front
     * and below Mario
     *
     * @param enemies
     * @return true if enemy found; else false
     */
    private boolean enemyInFront(int[][] enemies) {
        for (int i = 0; i > -2; i--) {
            for (int j = 1; j < 2; j++) {
                if (getLocation(j, i, enemies) > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Game loop
     *
     * @param model a forward model object so the agent can simulate the future.
     * @param timer amount of time before the agent has to return the actions.
     * @return action array (mario controller input from SDK specification)
     */
    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
        // Get state of Mario's environment (detect obstacles/goals) - partly borrowed from trondEllingsen
        // TODO: Do the rectangles change based on moving left vs right?
        int[][] enemies = model.getMarioEnemiesObservation();
        int[][] scene = model.getMarioSceneObservation();
        final boolean obstacle = thereIsObstacle(scene);
        final boolean powerUp = isPowerupBlock(scene);
        final boolean enemyBehind = obstacleBehind(scene);
        final boolean stairs = thereIsStair(scene);
        final boolean overheadBlock = checkIfOverheadBlock(model.getMarioScreenTilePos()[0], model.getMarioScreenTilePos()[1], model.getScreenSceneObservation());//, world, model);


        final float marioSpeed = model.getMarioFloatVelocity()[0];
        final boolean dangerOfEnemy = enemyInRange(model, new Rectangle(-13, -57, 105, 87));
        final boolean dangerOfEnemyAbove = enemyInRange(model, new Rectangle(-28, 28, 58, 45));
        final boolean dangerOfGap = dangerOfGap(model.getMarioScreenTilePos()[0], model.getMarioScreenTilePos()[1],
                model.getScreenSceneObservation());
        if ((model.isMarioOnGround() || model.mayMarioJump()) && !jumpType.equals(JumpType.NONE)) {
            setJump(JumpType.NONE, -1);
        } else if (model.mayMarioJump()) {
            final int wallHeight = getWallHeight(model.getMarioScreenTilePos()[0], model.getMarioScreenTilePos()[1],
                    model.getScreenSceneObservation());
            System.out.println(wallHeight);
            if (dangerOfGap && marioSpeed > 0) {
                System.out.println("danger of gap wall!");
                setJump(JumpType.GAP, marioSpeed < 6 ? (int) (9 - marioSpeed) : 1);
            } else if (marioSpeed <= 1 && !dangerOfEnemyAbove && wallHeight > 0) {
                System.out.println("jumping bc wall!");
                setJump(JumpType.WALL, wallHeight >= 4 ? wallHeight + 3 : (wallHeight + (int)(Math.random() * 5)));
            } else if (dangerOfEnemy && !(dangerOfEnemyAbove && marioSpeed > 2)) {
                //action[MarioActions.LEFT.getValue()] = ((dangerOfEnemy && dangerOfEnemyAbove) || dangerOfGap);
                if(maybe(40)){
                    state = State.SKITTISH;
                } else {
                    setJump(JumpType.ENEMY, 6);
                }

            } else if(powerUp){
                if(maybe(3)){
                System.out.println("I detect a powerup!");
                    setJump(JumpType.POWERUP, 5);
                }
            } else if(!obstacle && !dangerOfEnemy && !dangerOfEnemyAbove && !dangerOfGap){
                //state = State.IDLE;
            } else if(stairs){
                System.out.println("this is a stair below me!");
                setJump(JumpType.WALL, 3);

            } else if(maybe(10) && jumpType == JumpType.NONE){
                setJump(JumpType.WALL, (int)(Math.random() * 7)+3);
            }
        } else {
            jumpCount++;
        }

        final boolean isFalling = prevY < model.getMarioFloatPos()[1] && jumpType.equals(JumpType.NONE);
        // Define action based on state
        switch(state) {
            case IDLE:
//                if(start_count > 2){
//                    System.out.println("onwards and upwards");
//                    state = State.ONWARDS;
//                    break;
//                }
//                start_count++;
                System.out.println("Am idle***********");
                // Sit and contemplate life (set all inputs to false)
                for(int i = 0; i < action.length; i++) {
                    action[i] = false;
                }
                // 50/50 change we'll make up our mind by next frame
                if(maybe(50)) {
                    if(maybe(70)) {
                        // if we're gonna do a thing, probably move forward
                        state = State.ONWARDS;
                    } else if(maybe(60)) {
                        // but maybe be dumb and go backwards
                        state = State.BACKWARDS;
                    } else {
                        // occasionally jump in place because we're humans and it's fun (if not already jumping)
                        if(jumpType.equals(JumpType.NONE)) {
                            jumpType = JumpType.WALL;
                            jumpCount = 0;
                            jumpSize = 4;
                        }
                    }
                }
                break;
            case SKITTISH:
                System.out.println("am skittish");

                if(enemyBehind){
                    System.out.println("wait there's an enemy behind you!");
                    state = State.ONWARDS;
                    skit_count = 0;
                    break;
                }
                if(skit_count < 2){
                    action[MarioActions.LEFT.getValue()] = true;
                    action[MarioActions.RIGHT.getValue()] = false;
                    action[MarioActions.SPEED.getValue()] = false;

                    skit_count++;
                } else {
                    state = State.ONWARDS;
                    skit_count = 0;
                }

                break;
            case BACKWARDS:
                System.out.println("moving backwards!");
                if(back_count < Math.random() * 3){
                    action[MarioActions.LEFT.getValue()] = true;
                    action[MarioActions.RIGHT.getValue()] = false;
                    back_count++;
                } else {
                    state = state.ONWARDS;
                    back_count = 0;
                }
                break;
            case ONWARDS:
                System.out.println("am going onwards!");

                System.out.println(jumpType + " " + jumpCount + " / " + jumpSize);
                //break;
            default:
                action[MarioActions.LEFT.getValue()] = isFalling && ((dangerOfEnemy && dangerOfEnemyAbove) || dangerOfGap);
                action[MarioActions.RIGHT.getValue()] = !isFalling && !(dangerOfEnemyAbove && jumpType == JumpType.WALL);
                action[MarioActions.JUMP.getValue()] = !jumpType.equals(JumpType.NONE) && jumpCount < jumpSize;
                action[MarioActions.SPEED.getValue()] = !(jumpType.equals(JumpType.ENEMY) && action[MarioActions.SPEED.getValue()] && model.getMarioMode() == 2);
                prevY = model.getMarioFloatPos()[1];
                break;
        }
        return action;
    }

    @Override
    public String getAgentName() {
        return "MatticoliKumykova";
    }
}
