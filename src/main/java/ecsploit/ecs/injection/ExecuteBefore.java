package ecsploit.ecs.injection;

import ecsploit.ecs.core.ExecuteSystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExecuteBefore {
    Class<? extends ExecuteSystem>[] value();
}
