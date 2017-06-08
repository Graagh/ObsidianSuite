package obsidianAPI.animation.wrapper;

import net.minecraft.entity.Entity;
import obsidianAPI.animation.AnimationSequence;

public interface IAnimationWrapper {

	/**
	 * @return The animation sequence this wrapper pertains to. 
	 */
	public AnimationSequence getAnimation();
	
	/**
	 * Work out if the current animation should be played.
	 */
	public boolean isActive(float swingTime, float swingMax, float clock, float lookX, float lookY, float f5, Entity entity);
	
	/**
	 * @return Priority of this animation. 0 highest, 100 lowest. 
	 */
	public int getPriority();
	
}
