package ecsploit.ecs.core;

import ecsploit.utils.debug.ToStringBuilder;

public abstract class AbstractSystem {

	protected void init(Manager manager) {}
	
	protected abstract void execute();

	public String toString() {
		return ToStringBuilder.fromC(this.getClass().getSimpleName())
				.toString();
	}

}
