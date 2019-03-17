package io.openems.edge.evcs.keba.kecontact;

import io.openems.edge.common.channel.OptionsEnum;

public enum Plug implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	UNPLUGGED(0, "Unplugged"), //
	PLUGGED_ON_EVCS(1, "Plugged on EVCS"), //
	PLUGGED_ON_EVCS_AND_LOCKED(3, "Plugged on EVCS and locked"), //
	PLUGGED_ON_EVCS_AND_ON_EV(5, "Plugged on EVCS and on EV"), //
	PLUGGED_ON_EVCS_AND_ON_EV_AND_LOCKED(7, "Plugged on EVCS and on EV and locked");

	private final int value;
	private final String name;

	private Plug(int value, String name) {
		this.value = value;
		this.name = name;
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public OptionsEnum getUndefined() {
		return UNDEFINED;
	}
}