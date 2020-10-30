package ecsploit.main.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ecsploit.utils.debug.ToStringBuilder;

public class ToStringBuilderTest {

	private static class Universe {
		private final int TOTAL = 14;
		private final Position center = new Position(0, 0);
		private final World world;
		private final String[] galaxyNames = {"galaxyA", "starGalaxy", "nullGalaxy"};
		private final int[] starsPerGalaxy = {3, 400, -1};

		private final boolean goingToEnd = true;
		
		private Universe(World world) {
			this.world = world;
		}
		
		public String toString() {
			List<List<Integer>> test = new ArrayList<>();
			List<Integer> a = new ArrayList<>();
			a.add(2);
			a.add(4);
			a.add(8);
			test.add(a);
			List<Integer> b = new ArrayList<>();
			b.add(5);
			b.add(2);
			b.add(78);
			test.add(b);
			return ToStringBuilder.from(this).depth(6)
					.withPrim("total", TOTAL)
					.withObj("center", center)
					.withObj("world", world)
					.withList("stars", new ArrayList<>())
					.withMap("galaxies", new HashMap<>())
					.withStringArray("galaxyNames", galaxyNames)
					.withIntArray("starsPerGalaxy", starsPerGalaxy)
					.withList("randomList", test)
					.withPrim("goingToEnd", goingToEnd)
					.toString();
		}
	}
	
	private static class World {
		private final List<Player> players = new ArrayList<>();
		private final Map<String, Entity> entities = new HashMap<>();
		private final Entity[] rocks = new Entity[3];
		
		public World() {
			players.add(new Player(false, "steve", 1, 2));
			players.add(new Player(true, "jeff", -21, 98));
			players.add(null);
			players.add(null);
			
			entities.put("0101", new Entity("test1", 123, 93));
			entities.put("nullTest", null);
			entities.put("1101", new Entity("abcd", 5, 35));

			rocks[0] = new Entity("bill", -1, -1);
			rocks[1] = new Entity("cipher", 30, 20);
			rocks[2] = new Entity("jill", 12, 34);
		}
		
		public String toString() {
			return ToStringBuilder.from(this).depth(5)
					.withList("players", players)
					.withMap("entities", entities)
					.withArray("rocks", rocks)
					.toString();
		}
	}
	
	private static class Player extends Entity{
		private final boolean isWalking;
		
		public Player(boolean isWalking, String name, int x, int y) {
			super(name, x, y);
			this.isWalking = isWalking;
		}
		
		public String toString() {
			return ToStringBuilder.from(this)
					.withPrim("name", super.name)
					.withObj("pos", super.pos)
					.withPrim("isWalking", isWalking)
					.toString();
		}
	}
	
	private static class Entity {
		private final String name;
		private final Position pos;
		
		public Entity(String name, int x, int y) {
			this.name = name;
			this.pos = new Position(x, y);
		}
		
		public String toString() {
			return ToStringBuilder.from(this)
					.withPrim("name", name)
					.withObj("pos", pos)
					.toString();
		}
	}
	
	private static class Position {
		private final int x;
		private final int y;
		private final Networked networked;
		
		public Position(int x, int y) {
			this.x = x;
			this.y = y;
			
			this.networked = new Networked(this);
		}
		
		public String toString() {
			return ToStringBuilder.from(this)
					.withPrim("x", x)
					.withPrim("y", y)
					.withObj("networked", networked)
					.toString();
		}
	}

	private static class Networked {
		private final boolean networked = true;
		private final Position position;
		
		public Networked(Position position) {
			this.position = position;
		}
		
		public String toString() {
			return ToStringBuilder.from(this)
					.withPrim("networked", networked)
					.withObj("position", position)
					.toString();
		}
	}
	
	public static void main(String[] args) {
		Player eric = new Player(true, "eric", 12, 23);
		System.out.println(eric);
		
		Entity test = new Entity("test", 100, -23);
		System.out.println(test);
		
		Position pos = new Position(12, 13);
		System.out.println(pos);
		
		
		World world = new World();
		System.out.println(world);
		
		Universe universe = new Universe(world);
		System.out.println(universe);
		
		System.out.println("======================================================");
	}
}
