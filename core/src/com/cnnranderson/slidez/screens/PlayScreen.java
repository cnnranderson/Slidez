package com.cnnranderson.slidez.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.cnnranderson.slidez.Application;
import com.cnnranderson.slidez.actors.SlideButton;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class PlayScreen implements Screen {

    // App reference
    private final Application app;

    // Stage vars
    private Stage stage;
    private Skin skin;

    // Game Grid
    private int boardSize = 4;
    private int holeX, holeY;
    private SlideButton[][] buttonGrid;

    // Nav-Buttons
    private TextButton buttonBack;

    // Info label
    private Label labelInfo;

    public PlayScreen(final Application app) {
        this.app = app;
        this.stage = new Stage(new FitViewport(Application.V_WIDTH, Application.V_HEIGHT, app.camera));
    }

    @Override
    public void show() {
        System.out.println("PLAY");
        Gdx.input.setInputProcessor(stage);
        stage.clear();

        this.skin = new Skin();
        this.skin.addRegions(app.assets.get("ui/uiskin.atlas", TextureAtlas.class));
        this.skin.add("default-font", app.font24);
        this.skin.load(Gdx.files.internal("ui/uiskin.json"));

        initNavigationButtons();
        initInfoLabel();
        initGrid();
        shuffle();
    }

    private void shuffle(){
        int swaps = 0; // debug variable
        int shuffles;
        // 99 is arbitrary
        for(shuffles = 0; shuffles < 99; shuffles++){
            // Choose a random spot in the grid and check if a valid move can be made
            int posX = MathUtils.random(0, boardSize - 1);
            int posY = MathUtils.random(0, boardSize - 1);
            if (holeX == posX || holeY == posX) {
                moveButtons(posX,posY);
                swaps++;
            }
        }
        System.out.println("Tried: " + shuffles + ", actual moves made: " + swaps); // Debug logging
    }

    private void update(float delta) {
        stage.act(delta);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(delta);

        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    // Initialize the back button
    private void initNavigationButtons() {
        buttonBack = new TextButton("Back", skin, "default");
        buttonBack.setPosition(20, app.camera.viewportHeight - 70);
        buttonBack.setSize(100, 50);
        buttonBack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                app.setScreen(app.mainMenuScreen);
            }
        });

        stage.addActor(buttonBack);
    }

    // Initialize the info label
    private void initInfoLabel() {
        labelInfo = new Label("Welcome! Click any number tile to begin!", skin, "default");
        labelInfo.setPosition(25, 310);
        labelInfo.setAlignment(Align.center);
        labelInfo.addAction(sequence(alpha(0f), delay(.5f), fadeIn(.5f)));
        stage.addActor(labelInfo);
    }

    // Initialize the game grid
    private void initGrid() {
        Array<Integer> nums = new Array<Integer>();
        buttonGrid = new SlideButton[boardSize][boardSize];

        // Initialize the grid array
        for(int i = 1; i < boardSize * boardSize; i++) {
            nums.add(i);
        }

        // Set the hole at the bottom right so the sequence is 1,2,3...,15,hole (solved state) from which to start shuffling.
        holeX = boardSize-1;
        holeY = boardSize-1;

        for(int i = 0; i < boardSize; i++) {
            for(int j = 0; j < boardSize; j++) {
                if(i != holeY || j != holeX) {
                    int id = nums.removeIndex(0);
                    buttonGrid[i][j] = new SlideButton(id + "", skin, "default", id);
                    buttonGrid[i][j].setPosition((app.camera.viewportWidth / 7) * 2 + 51 * j,
                            (app.camera.viewportHeight / 5) * 3 - 51 * i);
                    buttonGrid[i][j].setSize(50, 50);
                    buttonGrid[i][j].addAction(sequence(alpha(0), delay((j + 1 + (i * boardSize)) / 60f),
                            parallel(fadeIn(.5f), moveBy(0, -10, .25f, Interpolation.pow5Out))));

                    // Slide/Move Button
                    buttonGrid[i][j].addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            int buttonX = 0, buttonY = 0;
                            boolean buttonFound = false;
                            SlideButton selectedButton = (SlideButton) event.getListenerActor();

                            for(int i = 0; i < boardSize && !buttonFound; i++) {
                                for(int j = 0; j < boardSize && !buttonFound; j++) {
                                    if(buttonGrid[i][j] != null && selectedButton == buttonGrid[i][j]) {
                                        buttonX = j;
                                        buttonY = i;
                                        buttonFound = true;
                                    }
                                }
                            }

                            if(holeX == buttonX || holeY == buttonY) {
                                moveButtons(buttonX, buttonY);

                                if(solutionFound()) {
                                    labelInfo.clearActions();
                                    labelInfo.setText("Solution Found!");
                                    labelInfo.addAction(sequence(alpha(1f), delay(3f), fadeOut(2f, Interpolation.pow5Out)));
                                }
                            } else {
                                labelInfo.clearActions();
                                labelInfo.setText("Invalid Move!");
                                labelInfo.addAction(sequence(alpha(1f), delay(1f), fadeOut(1f, Interpolation.pow5Out)));
                            }
                        }
                    });
                    stage.addActor(buttonGrid[i][j]);
                }
            }
        }
    }

    private void moveButtons(int x, int y) {
        SlideButton button;
        if(x < holeX) {
            for(; holeX > x; holeX--) {
                button = buttonGrid[holeY][holeX - 1];
                button.addAction(moveBy(51, 0, .5f, Interpolation.pow5Out));
                buttonGrid[holeY][holeX] = button;
                buttonGrid[holeY][holeX - 1] = null;
            }
        } else {
            for(; holeX < x; holeX++) {
                button = buttonGrid[holeY][holeX + 1];
                button.addAction(moveBy(-51, 0, .5f, Interpolation.pow5Out));
                buttonGrid[holeY][holeX] = button;
                buttonGrid[holeY][holeX + 1] = null;
            }
        }

        if(y < holeY) {
            for(; holeY > y; holeY--) {
                button = buttonGrid[holeY - 1][holeX];
                button.addAction(moveBy(0, -51, .5f, Interpolation.pow5Out));
                buttonGrid[holeY][holeX] = button;
                buttonGrid[holeY - 1][holeX] = null;
            }
        } else {
            for(; holeY < y; holeY++) {
                button = buttonGrid[holeY + 1][holeX];
                button.addAction(moveBy(0, 51, .5f, Interpolation.pow5Out));
                buttonGrid[holeY][holeX] = button;
                buttonGrid[holeY + 1][holeX] = null;
            }
        }
    }

    private boolean solutionFound() {
        int idCheck = 1;
        for(int i = 0; i < boardSize; i++) {
            for(int j = 0; j < boardSize; j++) {
                if(buttonGrid[i][j] != null) {
                    if(buttonGrid[i][j].getId() == idCheck++) {
                        if(idCheck == 16) {
                            return true;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return false;
    }
}

