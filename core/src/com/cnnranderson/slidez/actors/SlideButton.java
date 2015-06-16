package com.cnnranderson.slidez.actors;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class SlideButton extends TextButton {

    private int id;

    public SlideButton(String text, Skin skin, String style, int id) {
        super(text, skin, style);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
