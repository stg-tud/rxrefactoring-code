package de.tong.player;

import java.awt.event.KeyEvent;

public class Options {

    private Player currentOwner;

    private GameMode currentMode;

    private int rotation;
    private int fastDown;
    private int moveToLeft;
    private int moveToRight;
    private int accelerate;

    private int pl1Rotation;
    private int pl1FastDown;
    private int pl1MoveToLeft;
    private int pl1MoveToRight;
    private int pl1Accelerate;

    private int pl2Rotation;
    private int pl2FastDown;
    private int pl2MoveToLeft;
    private int pl2MoveToRight;
    private int pl2Accelerate;

    private int defaultLifes;
    private int defaultRounds;
    private int defaultPointsToReach;
    private int defaultBonusLifePointBorder;
    private int defaultPointPenalty;
    private int defaultBallSpeed;

    public int getDefaultPointPenalty() {
	return defaultPointPenalty;
    }

    public void setDefaultPointPenalty(int defaultPointPenalty) {
	this.defaultPointPenalty = defaultPointPenalty;
    }

    public int getDefaultBallSpeed() {
	return defaultBallSpeed;
    }

    public void setDefaultBallSpeed(int defaultBallSpeed) {
	this.defaultBallSpeed = defaultBallSpeed;
    }

    public Options(Player currentOwner) {

	this.currentOwner = currentOwner;

	// default Options

	currentMode = GameMode.LIFEMODE; // Change this in order to change the
	// mode

	rotation = KeyEvent.VK_UP; // default key bindings
	fastDown = KeyEvent.VK_SPACE;
	moveToLeft = KeyEvent.VK_LEFT;
	moveToRight = KeyEvent.VK_RIGHT;
	accelerate = KeyEvent.VK_DOWN;

	pl2Rotation = KeyEvent.VK_UP;
	pl2FastDown = KeyEvent.VK_NUMPAD0;
	pl2MoveToLeft = KeyEvent.VK_LEFT;
	pl2MoveToRight = KeyEvent.VK_RIGHT;
	pl2Accelerate = KeyEvent.VK_DOWN;

	pl1Rotation = KeyEvent.VK_W;
	pl1FastDown = KeyEvent.VK_SPACE;
	pl1MoveToLeft = KeyEvent.VK_A;
	pl1MoveToRight = KeyEvent.VK_D;
	pl1Accelerate = KeyEvent.VK_S;

	defaultLifes = 10; // default game variables
	defaultRounds = 10;
	defaultPointsToReach = 5000;
	defaultBonusLifePointBorder = 1000;
	defaultPointPenalty = 250;
	defaultBallSpeed = -50;

    }

    public int getDefaultLifes() {
	return defaultLifes;
    }

    public void setDefaultLifes(int defaultLifes) {
	this.defaultLifes = defaultLifes;
    }

    public int getDefaultRounds() {
	return defaultRounds;
    }

    public void setDefaultRounds(int defaultRounds) {
	this.defaultRounds = defaultRounds;
    }

    public int getDefaultPointsToReach() {
	return defaultPointsToReach;
    }

    public void setDefaultPointsToReach(int defaultPointsToReach) {
	this.defaultPointsToReach = defaultPointsToReach;
    }

    public int getDefaultBonusLifePointBorder() {
	return defaultBonusLifePointBorder;
    }

    public void setDefaultBonusLifePointBorder(int defaultBonusLifePointBorder) {
	this.defaultBonusLifePointBorder = defaultBonusLifePointBorder;
    }

    public int getDefaultPointLoss() {
	return defaultPointPenalty;
    }

    public void setDefaultPointLoss(int defaultPointLoss) {
	this.defaultPointPenalty = defaultPointLoss;
    }

    public Player getCurrentOwner() {
	return currentOwner;
    }

    public void setCurrentOwner(Player currentOwner) {
	this.currentOwner = currentOwner;
    }

    public GameMode getCurrentMode() {
	return currentMode;
    }

    public void setCurrentMode(GameMode currentMode) {
	this.currentMode = currentMode;
    }

    public int getRotation() {
	return rotation;
    }

    public void setRotation(int rotation) {
	this.rotation = rotation;
    }

    public int getFastDown() {
	return fastDown;
    }

    public void setFastDown(int fastDown) {
	this.fastDown = fastDown;
    }

    public int getMoveToLeft() {
	return moveToLeft;
    }

    public void setMoveToLeft(int moveToLeft) {
	this.moveToLeft = moveToLeft;
    }

    public int getMoveToRight() {
	return moveToRight;
    }

    public void setMoveToRight(int moveToRight) {
	this.moveToRight = moveToRight;
    }

    public int getAccelerate() {
	return accelerate;
    }

    public void setAccelerate(int accelerate) {
	this.accelerate = accelerate;
    }

    public int getPl1Rotation() {
	return pl1Rotation;
    }

    public void setPl1Rotation(int pl1Rotation) {
	this.pl1Rotation = pl1Rotation;
    }

    public int getPl1FastDown() {
	return pl1FastDown;
    }

    public void setPl1FastDown(int pl1FastDown) {
	this.pl1FastDown = pl1FastDown;
    }

    public int getPl1MoveToLeft() {
	return pl1MoveToLeft;
    }

    public void setPl1MoveToLeft(int pl1MoveToLeft) {
	this.pl1MoveToLeft = pl1MoveToLeft;
    }

    public int getPl1MoveToRight() {
	return pl1MoveToRight;
    }

    public void setPl1MoveToRight(int pl1MoveToRight) {
	this.pl1MoveToRight = pl1MoveToRight;
    }

    public int getPl1Accelerate() {
	return pl1Accelerate;
    }

    public void setPl1Accelerate(int pl1Accelerate) {
	this.pl1Accelerate = pl1Accelerate;
    }

    public int getPl2Rotation() {
	return pl2Rotation;
    }

    public void setPl2Rotation(int pl2Rotation) {
	this.pl2Rotation = pl2Rotation;
    }

    public int getPl2FastDown() {
	return pl2FastDown;
    }

    public void setPl2FastDown(int pl2FastDown) {
	this.pl2FastDown = pl2FastDown;
    }

    public int getPl2MoveToLeft() {
	return pl2MoveToLeft;
    }

    public void setPl2MoveToLeft(int pl2MoveToLeft) {
	this.pl2MoveToLeft = pl2MoveToLeft;
    }

    public int getPl2MoveToRight() {
	return pl2MoveToRight;
    }

    public void setPl2MoveToRight(int pl2MoveToRight) {
	this.pl2MoveToRight = pl2MoveToRight;
    }

    public int getPl2Accelerate() {
	return pl2Accelerate;
    }

    public void setPl2Accelerate(int pl2Accelerate) {
	this.pl2Accelerate = pl2Accelerate;
    }

}
