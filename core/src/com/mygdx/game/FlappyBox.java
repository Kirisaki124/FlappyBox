package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

public class FlappyBox extends ApplicationAdapter {
	SpriteBatch batch;
	Texture background;
	Texture topTube;
	Texture bottomTube;
	Texture player;
	BitmapFont font;

	private float playerSize = 150;
	final float defaultVelocity = 1;
	private float velocity = defaultVelocity;
	private float playerY = 0;
	private int gravity = 2;
	private float gap = 450;
	Random random;
	private float tubeVelocity = 4;
	private int numberOfTubes = 4;
	private float[] tubeX = new float[numberOfTubes];
	private float []tubeOffSet = new float[numberOfTubes];
	float maxTubeOffSet;
	private float distance;
	int score = 0;
	int scoringTube = 0;

	Rectangle playerRec;
	Rectangle [] topTubesRec;
	Rectangle [] bottomTubesRec;

	boolean gameState = false;
	boolean isLose = false;

	public void setupNewGame () {
		scoringTube = 0;
		score = 0;
		font = new BitmapFont();
		font.setColor(Color.RED);
		font.getData().setScale(10);

		velocity = defaultVelocity;
		isLose = false;
		playerRec = new Rectangle();
		topTubesRec = new Rectangle[numberOfTubes];
		bottomTubesRec = new Rectangle[numberOfTubes];

		batch = new SpriteBatch();
		playerY = Gdx.graphics.getHeight() / 2 - playerSize / 2;

		maxTubeOffSet = Gdx.graphics.getHeight() / 2 - gap / 2 - 100;
		random = new Random();

		// ? / screen 1 tube
		distance = Gdx.graphics.getWidth() * 0.6f;
		this.setupTube();

	}

	private void setupTube() {
		for (int i = 0; i < numberOfTubes; i++) {
			tubeOffSet[i] = (random.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - 200);
			tubeX[i] = Gdx.graphics.getWidth() / 2 - topTube.getWidth() / 2 + i * distance + Gdx.graphics.getWidth() * 0.7f; // 0.7: Make the first tube away 0.7 time from the middle of the screen
			this.setupTubeCollision(i);
		}
	}

	private void setupTubeCollision(int i) {
		topTubesRec[i] = new Rectangle();
		bottomTubesRec[i] = new Rectangle();
	}



	private void loadImage() {
		background = new Texture("background.jpg");
		player = new Texture("player.png");
		topTube = new Texture("tube.png");
		bottomTube = new Texture("tube.png");
	}


//	First time load game
	@Override
	public void create () {
		this.loadImage();
		if (!isLose && !gameState) {
			this.setupNewGame();
		}
	}

	private void drawTubes() {
		for (int i = 0; i < numberOfTubes; i++) {
			batch.draw(topTube, tubeX[i],
					Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffSet[i]);
			batch.draw(bottomTube, tubeX[i],
					Gdx.graphics.getHeight() / 2 - gap / 2 - bottomTube.getHeight() + tubeOffSet[i]);

			topTubesRec[i] = new Rectangle(tubeX[i],
					Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffSet[i],
					topTube.getWidth(),
					topTube.getHeight());
			bottomTubesRec[i] = new Rectangle(tubeX[i],
					Gdx.graphics.getHeight() / 2 - gap / 2 - bottomTube.getHeight() + tubeOffSet[i],
					bottomTube.getWidth(),
					bottomTube.getHeight());
		}
	}

	private void recycleTubes(int i) {
		if (tubeX[i] < - topTube.getWidth()) {
			tubeX[i] += numberOfTubes * distance;
			tubeOffSet[i] = (random.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - 200);
		}
		else  {
			tubeX[i] -= tubeVelocity;
		}
	}

	private void generateTubes() {
		for (int i = 0; i < numberOfTubes; i++) {
			this.recycleTubes(i);
			this.drawTubes();
		}
	}

	private void playerMove() {
		// Player go up
		if (Gdx.input.justTouched()) {
			velocity = -30;
		}

		if (playerY > 0 || velocity < 0) {
			if (playerY > Gdx.graphics.getHeight() - playerSize) {
				playerY = Gdx.graphics.getHeight() - playerSize - 10;
			}else {
				// Player go down
				velocity += gravity;
				playerY -= velocity;
			}
		}
	}

	@Override
	public void render () {
		batch.begin();
		batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.drawTubes();
		if (gameState) {
			Gdx.app.log("Scoring Tube", String.valueOf(scoringTube));
			Gdx.app.log("Tube X", String.valueOf(tubeX[scoringTube]));
			Gdx.app.log("Middle", String.valueOf(Gdx.graphics.getWidth() / 2));
			if (tubeX[scoringTube] < Gdx.graphics.getWidth() / 2) {
				score ++;
				Gdx.app.log("Score", String.valueOf(score));
				if(scoringTube < numberOfTubes - 1) {
					scoringTube ++;
				}
				else {
					scoringTube = 0;
				}
			}
			this.generateTubes();
			this.playerMove();
		}else {
			// Game lose
			if (isLose) {
				if (playerY > 10) {
					velocity += gravity;
					playerY -= velocity;
				}
			}

			// New Game
			if (Gdx.input.justTouched()) {
				gameState = true;
			}
		}


		batch.draw(player, Gdx.graphics.getWidth() / 2 - playerSize / 2,
				playerY,
				playerSize, playerSize);
		font.draw(batch, String.valueOf(score), 200, 200);

		batch.end();

		playerRec.set(Gdx.graphics.getWidth() / 2 - playerSize / 2, playerY, playerSize, playerSize);
		this.checkCollision();
		if (playerY < 10) {
			gameState = false;
			isLose = true;
			if (Gdx.input.justTouched()) {
				this.setupNewGame();
			}
		}
	}

	public void checkCollision() {
		for (int i = 0; i < numberOfTubes; i++) {
			if (Intersector.overlaps(playerRec, topTubesRec[i]) || Intersector.overlaps(playerRec, bottomTubesRec[i])) {
				gameState = false;
				isLose = true;
				if (Gdx.input.justTouched()) {
					this.setupNewGame();
				}
			}
		}
	}
}
