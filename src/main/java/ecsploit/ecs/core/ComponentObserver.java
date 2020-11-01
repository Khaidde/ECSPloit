package ecsploit.ecs.core;

public interface ComponentObserver<T extends Component> {

	void invoke(int entityID, ComponentType<T> componentType);

}
