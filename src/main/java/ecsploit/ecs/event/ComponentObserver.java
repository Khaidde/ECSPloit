package ecsploit.ecs.event;

import ecsploit.ecs.core.Component;
import ecsploit.ecs.core.ComponentType;

public interface ComponentObserver {

	void invoke(int entityID, ComponentType<? extends Component> componentType);

}
