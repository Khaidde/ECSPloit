package ecsploit.ecs.core;

import ecsploit.utils.debug.ToStringBuilder;

public final class SystemManager {

	private final SystemGroup rootSystemGroup;
	
	private final Manager manager;
	
	SystemManager(Manager manager) {
		this.manager = manager;

		this.rootSystemGroup = SystemGroup.from("Root");
	}

	public void register(AbstractSystem system) {
		this.rootSystemGroup.init(manager);
		this.rootSystemGroup.insert(system);
	}
	
	public void update() {
		this.rootSystemGroup.execute();
	}

	public String toString() {
		return ToStringBuilder.from(this)
				.withObj("Root", rootSystemGroup)
				.toString();
	}

}
