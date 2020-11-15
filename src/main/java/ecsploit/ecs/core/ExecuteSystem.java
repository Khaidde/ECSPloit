package ecsploit.ecs.core;

import ecsploit.utils.debug.ToStringBuilder;

public abstract class ExecuteSystem implements BaseSystem {
	
	protected abstract void execute();

	public String toString() {
		return ToStringBuilder.fromC(this.getClass().getSimpleName())
				.toString();
	}

}
