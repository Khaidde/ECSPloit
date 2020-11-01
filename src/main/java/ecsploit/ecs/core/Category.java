package ecsploit.ecs.core;

import ecsploit.utils.collections.BitString;

/**
 * Group of entities which share the same components
 * <p>
 *     Note: there exists only one category instance per each unique identifierBitString.
 * </p>
 */
public class Category extends EntityGroup {

    private final BitString identifierBitString;

    Category(BitString identifierBitString) {
		this.identifierBitString = identifierBitString;
	}

    boolean matches(BitString otherBitString) {
        return otherBitString.equals(this.identifierBitString);
    }
}
