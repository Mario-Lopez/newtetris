package amylopez.makelines.controller.input.action.gameplay;


import amylopez.makelines.controller.TickListener;
import amylopez.makelines.controller.Ticker;
import amylopez.makelines.controller.input.InputController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * All game play actions that are activated by a key press should extend this class
 */
public abstract class GameplayAction extends AbstractAction implements TickListener {

    private static Logger log = LoggerFactory.getLogger(GameplayAction.class);

    @Inject
    protected Ticker ticker;

    @Inject
    protected InputController inputController;

    protected boolean isKeyPressed = false;

    /**
     * Called when they key is pressed
     * @param e ActionEvent
     */
    @Override
    public final void actionPerformed(ActionEvent e) {
        if (!isKeyPressed) {
            isKeyPressed = true;
            keyPressed();
        }
    }

    /**
     * Controller should call this when it detects that the key was released
     */
    public final void stop() {
        isKeyPressed = false;
        keyReleased();
    }

    /**
     * If the action needs to initialize anything, it should be done here.
     * Action should either initialize the ticker or override the keyPressed and keyReleased
     * methods.
     */
    public abstract void init();

    /**
     * Key was pressed, default implementation since most actions are repeatable
     */
    public void keyPressed() {
        ticker.start();
    }

    /**
     * Key was released, default implementation since most actions are repeatable
     */
    public void keyReleased() {
        ticker.stop();
    }
}
