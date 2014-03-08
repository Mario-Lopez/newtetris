package com.mm.tetris.controller.input.action.gameplay;

import org.apache.commons.configuration.Configuration;

import javax.inject.Inject;

public class SlowDropAction extends GameplayAction {

    @Inject
    private Configuration config;

    @Override
    public void init() {
        int delay = config.getInt("movement/input/drop/@normal");
        ticker.setTickListener(this).setInterval(delay);
    }

    @Override
    public void tick() {
        inputController.dropOneRow();
    }
}