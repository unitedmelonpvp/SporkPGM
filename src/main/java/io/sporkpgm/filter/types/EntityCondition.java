package io.sporkpgm.filter.types;

import io.sporkpgm.filter.Condition;
import io.sporkpgm.filter.FilterContext;
import io.sporkpgm.filter.State;
import org.bukkit.entity.EntityType;

public class EntityCondition extends Condition {

	private EntityType entity;

	public EntityCondition(String name, State state, EntityType entity) {
		super(name, state);
		this.entity = entity;
	}

	@Override
	public State match(FilterContext context) {
		if(context.getEntity() == null) {
			return State.ABSTAIN;
		} else if(context.getEntity() == entity) {
			return State.ALLOW;
		} else if(entity == null) {
			return State.fromBoolean(true);
		}
		return State.DENY;
	}

	public EntityType getEntity() {
		return entity;
	}

	@Override
	public String toString() {
		return "EntityCondition{entity=" + (entity != null ? entity.name() : "null") + ",state=" + getState().toString() + "}";
	}

}
