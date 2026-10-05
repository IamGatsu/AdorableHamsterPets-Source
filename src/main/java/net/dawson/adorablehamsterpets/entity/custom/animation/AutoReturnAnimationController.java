package net.dawson.adorablehamsterpets.entity.custom.animation;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.object.PlayState;

/**
 * AnimationController that restores the GeckoLib 4 behaviour for triggered animations.
 *
 * <p>In GeckoLib 5 a triggered animation stays "triggered" after it has finished, and the state handler
 * is not called again until the animation is explicitly stopped. The mod was written against GeckoLib 4,
 * where control automatically returned to the state handler once a (non-looping) triggered animation
 * had finished. Without this, hamsters stay frozen in e.g. the settle-into-bed animation.
 *
 * <p>It also clears the controller when the state handler returns STOP, because a stopped controller in
 * GeckoLib 5 would otherwise keep holding its last pose on top of the other controllers.
 */
public class AutoReturnAnimationController<T extends GeoAnimatable> extends AnimationController<T> {

    public AutoReturnAnimationController(String name, int transitionTicks, AnimationStateHandler<T> stateHandler) {
        super(name, transitionTicks, test -> {
            AnimationController<T> controller = test.controller();
            if (controller instanceof AutoReturnAnimationController<T> self && self.isTriggerActive()) {
                if (!controller.hasAnimationFinished()) {
                    // Triggered animation still running (or just triggered): keep playing it
                    return PlayState.CONTINUE;
                }
                // Finished: hand control back to the normal state handler
                controller.stopTriggeredAnimation();
            }
            PlayState result = stateHandler.handle(test);
            if (result == PlayState.STOP) {
                // GeckoLib 5 keeps applying the last pose of a stopped controller, which overrides the
                // bones of other controllers (e.g. the turn animation freezing body_parent while begging).
                // GeckoLib 4 simply stopped contributing, so clear the controller here.
                controller.reset();
            }
            return result;
        });
        this.receiveTriggeredAnimations();
    }

    /** Whether a triggered animation is currently set on this controller (finished or not). */
    public boolean isTriggerActive() {
        return this.triggeredAnimTime != NOT_TRIGGERED;
    }
}
