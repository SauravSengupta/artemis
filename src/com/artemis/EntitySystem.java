package com.artemis;

//import com.artemis.utils.Bag;
//import com.artemis.utils.ImmutableBag;

/**
 * The most raw entity system. It should not typically be used, but you can create your own entity system handling by extending this. It is recommended that you use the other provided entity system implementations.
 * 
 * @author Arni Arent
 * 
 */
public abstract class EntitySystem {
	private long systemBit;

	private long typeFlags;

	protected World world;

	public EntitySystem() {
	}

	public EntitySystem(Class<? extends Component>... types) {
		for (Class<? extends Component> type : types) {
			ComponentType ct = ComponentTypeManager.getTypeFor(type);
			typeFlags |= ct.getBit();
		}
	}

	protected void setSystemBit(long bit) {
		this.systemBit = bit;
	}

	/**
	 * Called before processing of entities begins.
	 */
	protected void begin() {
	};

	public final void process() {
		if (checkProcessing()) {
			begin();
			processEntities();
			end();
		}
	}

	/**
	 * Called after the processing of entities ends.
	 */
	protected void end() {
	};

	/**
	 * Any implementing entity system must implement this method and the logic to process the given entities of the system.
	 * 
	 * @param entities
	 *            the entities this system contains.
	 */
	protected abstract void processEntities();

	/**
	 * Return true if the system should be processed, false otherwise.
	 */
	protected boolean checkProcessing() {
		return true;
	}

	/**
	 * Override to implement code that gets executed when systems are initialized.
	 */
	protected void initialize() {
	}

	/**
	 * Called if the system has received a entity it is interested in, e.g. created or a component was added to it.
	 * 
	 * @param e
	 *            the entity that was added to this system.
	 */
	protected void added(Entity e) {
	}

	/**
	 * Called if the entity was enabled.
	 * 
	 * @param e
	 *            the entity that was enabled.
	 */
	protected void enabled(Entity e) {
	}

	/**
	 * Called if the entity was disabled.
	 * 
	 * @param e
	 *            the entity that was disabled.
	 */
	protected void disabled(Entity e) {
	}

	/**
	 * Called if a entity was removed from this system, e.g. deleted or had one of it's components removed.
	 * 
	 * @param e
	 *            the entity that was removed from this system.
	 */
	protected void removed(Entity e) {
	}

	protected void change(Entity e) {
		boolean contains = (systemBit & e.getSystemBits()) == systemBit;
		boolean interest = (typeFlags & e.getTypeBits()) == typeFlags;
		boolean alreadyEnabled = (systemBit & e.getSystemEnabledBits()) == systemBit;

		if (interest && !contains && typeFlags > 0)
			add(e);
		else if (!interest && contains && typeFlags > 0)
			remove(e);
		else if (interest && contains && !alreadyEnabled && e.isEnabled() &&  typeFlags > 0)
			enable(e);
		else if (interest && contains && alreadyEnabled && !e.isEnabled() && typeFlags > 0)
			disable(e);

	}

	private void add(Entity e) {
		e.addSystemBit(systemBit);
		added(e);
		if (e.isEnabled())
			enable(e);
	}

	private void enable(Entity e) {
		e.addSystemEnabledBit(systemBit);
		enabled(e);
	}

	private void remove(Entity e) {
		e.removeSystemBit(systemBit);
		if (e.isEnabled())
			disable(e);
		removed(e);
	}

	private void disable(Entity e) {
		e.removeSystemEnabledBit(systemBit);
		disabled(e);
	}

	protected final void setWorld(World world) {
		this.world = world;
	}

	/**
	 * Merge together a required type and a array of other types. Used in derived systems.
	 * 
	 * @param requiredType
	 * @param otherTypes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected static Class<? extends Component>[] getMergedTypes(Class<? extends Component> requiredType, Class<? extends Component>[] otherTypes) {
		Class<? extends Component>[] types = new Class[1 + otherTypes.length];
		types[0] = requiredType;
		for (int i = 0; otherTypes.length > i; i++) {
			types[i + 1] = otherTypes[i];
		}
		return types;
	}

}
