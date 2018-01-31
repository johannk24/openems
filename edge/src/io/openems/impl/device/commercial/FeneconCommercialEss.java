/*******************************************************************************
 * OpenEMS - Open Source Energy Management System
 * Copyright (c) 2016, 2017 FENECON GmbH and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *   FENECON GmbH - initial API and implementation and initial documentation
 *******************************************************************************/
package io.openems.impl.device.commercial;

import io.openems.api.channel.ConfigChannel;
import io.openems.api.channel.ReadChannel;
import io.openems.api.channel.StaticValueChannel;
import io.openems.api.channel.StatusBitChannel;
import io.openems.api.channel.thingstate.ThingStateChannel;
import io.openems.api.device.Device;
import io.openems.api.device.nature.ess.SymmetricEssNature;
import io.openems.api.doc.ThingInfo;
import io.openems.api.exception.ConfigException;
import io.openems.impl.protocol.modbus.ModbusBitWrappingChannel;
import io.openems.impl.protocol.modbus.ModbusDeviceNature;
import io.openems.impl.protocol.modbus.ModbusReadLongChannel;
import io.openems.impl.protocol.modbus.ModbusWriteLongChannel;
import io.openems.impl.protocol.modbus.internal.DummyElement;
import io.openems.impl.protocol.modbus.internal.ModbusProtocol;
import io.openems.impl.protocol.modbus.internal.SignedWordElement;
import io.openems.impl.protocol.modbus.internal.UnsignedDoublewordElement;
import io.openems.impl.protocol.modbus.internal.UnsignedWordElement;
import io.openems.impl.protocol.modbus.internal.WordOrder;
import io.openems.impl.protocol.modbus.internal.range.ModbusRegisterRange;
import io.openems.impl.protocol.modbus.internal.range.WriteableModbusRegisterRange;

@ThingInfo(title = "FENECON Commercial ESS")
public class FeneconCommercialEss extends ModbusDeviceNature implements SymmetricEssNature {

	private ThingStateChannel thingState;

	/*
	 * Constructors
	 */
	public FeneconCommercialEss(String thingId, Device parent) throws ConfigException {
		super(thingId, parent);
		minSoc.addUpdateListener((channel, newValue) -> {
			// If chargeSoc was not set -> set it to minSoc minus 2
			if (channel == minSoc && !chargeSoc.valueOptional().isPresent()) {
				chargeSoc.updateValue((Integer) newValue.get() - 2, false);
			}
		});
		this.thingState = new ThingStateChannel(this);
	}

	/*
	 * Config
	 */
	private ConfigChannel<Integer> minSoc = new ConfigChannel<Integer>("minSoc", this);
	private ConfigChannel<Integer> chargeSoc = new ConfigChannel<Integer>("chargeSoc", this);

	@Override
	public ConfigChannel<Integer> minSoc() {
		return minSoc;
	}

	@Override
	public ConfigChannel<Integer> chargeSoc() {
		return chargeSoc;
	}

	/*
	 * Inherited Channels
	 */
	private ModbusReadLongChannel soc;
	private ModbusReadLongChannel activePower;
	private ModbusReadLongChannel allowedCharge;
	private ModbusReadLongChannel allowedDischarge;
	private ModbusReadLongChannel apparentPower;
	private ModbusReadLongChannel gridMode;
	private ModbusReadLongChannel reactivePower;
	private ModbusReadLongChannel systemState;
	private ModbusWriteLongChannel setActivePower;
	private ModbusWriteLongChannel setReactivePower;
	private ModbusWriteLongChannel setWorkState;
	private StaticValueChannel<Long> maxNominalPower = new StaticValueChannel<>("maxNominalPower", this, 40000L)
			.unit("VA");
	private StaticValueChannel<Long> capacity = new StaticValueChannel<>("capacity", this, 40000L).unit("Wh");

	@Override
	public ModbusReadLongChannel soc() {
		return soc;
	}

	@Override
	public ModbusReadLongChannel activePower() {
		return activePower;
	}

	@Override
	public ModbusReadLongChannel allowedCharge() {
		return allowedCharge;
	}

	@Override
	public ModbusReadLongChannel allowedDischarge() {
		return allowedDischarge;
	}

	@Override
	public ModbusReadLongChannel apparentPower() {
		return apparentPower;
	}

	@Override
	public ModbusReadLongChannel gridMode() {
		return gridMode;
	}

	@Override
	public ModbusReadLongChannel reactivePower() {
		return reactivePower;
	}

	@Override
	public ModbusReadLongChannel systemState() {
		return systemState;
	}

	@Override
	public ModbusWriteLongChannel setActivePower() {
		return setActivePower;
	}

	@Override
	public ModbusWriteLongChannel setReactivePower() {
		return setReactivePower;
	}

	@Override
	public ModbusWriteLongChannel setWorkState() {
		return setWorkState;
	}

	@Override
	public ModbusReadLongChannel allowedApparent() {
		return allowedApparent;
	}

	@Override
	public ReadChannel<Long> maxNominalPower() {
		return maxNominalPower;
	}

	/*
	 * This Channels
	 */
	public ModbusReadLongChannel controlMode;
	public ModbusReadLongChannel batteryMaintenanceState;
	public ModbusReadLongChannel inverterState;
	public ModbusReadLongChannel protocolVersion;
	public ModbusReadLongChannel systemManufacturer;
	public ModbusReadLongChannel systemType;
	public StatusBitChannel switchState;
	public ModbusReadLongChannel batteryVoltage;
	public ModbusReadLongChannel batteryCurrent;
	public ModbusReadLongChannel batteryPower;
	public ModbusReadLongChannel acChargeEnergy;
	public ModbusReadLongChannel acDischargeEnergy;
	public ModbusReadLongChannel currentL1;
	public ModbusReadLongChannel currentL2;
	public ModbusReadLongChannel currentL3;
	public ModbusReadLongChannel voltageL1;
	public ModbusReadLongChannel voltageL2;
	public ModbusReadLongChannel voltageL3;
	public ModbusReadLongChannel frequency;
	public ModbusReadLongChannel inverterVoltageL1;
	public ModbusReadLongChannel inverterVoltageL2;
	public ModbusReadLongChannel inverterVoltageL3;
	public ModbusReadLongChannel inverterCurrentL1;
	public ModbusReadLongChannel inverterCurrentL2;
	public ModbusReadLongChannel inverterCurrentL3;
	public ModbusReadLongChannel ipmTemperatureL1;
	public ModbusReadLongChannel ipmTemperatureL2;
	public ModbusReadLongChannel ipmTemperatureL3;
	public ModbusReadLongChannel transformerTemperatureL2;
	public ModbusReadLongChannel allowedApparent;
	public ModbusReadLongChannel gridActivePower;
	public ModbusReadLongChannel soh;
	public ModbusReadLongChannel batteryCellAverageTemperature;
	public StatusBitChannel suggestiveInformation1;
	public StatusBitChannel suggestiveInformation2;
	public StatusBitChannel suggestiveInformation3;
	public StatusBitChannel suggestiveInformation4;
	public StatusBitChannel suggestiveInformation5;
	public StatusBitChannel suggestiveInformation6;
	public StatusBitChannel suggestiveInformation7;
	public StatusBitChannel abnormity1;
	public StatusBitChannel abnormity2;
	public StatusBitChannel abnormity3;
	public StatusBitChannel abnormity4;
	public StatusBitChannel abnormity5;
	public ModbusReadLongChannel batteryCell1Voltage;
	public ModbusReadLongChannel batteryCell2Voltage;
	public ModbusReadLongChannel batteryCell3Voltage;
	public ModbusReadLongChannel batteryCell4Voltage;
	public ModbusReadLongChannel batteryCell5Voltage;
	public ModbusReadLongChannel batteryCell6Voltage;
	public ModbusReadLongChannel batteryCell7Voltage;
	public ModbusReadLongChannel batteryCell8Voltage;
	public ModbusReadLongChannel batteryCell9Voltage;
	public ModbusReadLongChannel batteryCell10Voltage;
	public ModbusReadLongChannel batteryCell11Voltage;
	public ModbusReadLongChannel batteryCell12Voltage;
	public ModbusReadLongChannel batteryCell13Voltage;
	public ModbusReadLongChannel batteryCell14Voltage;
	public ModbusReadLongChannel batteryCell15Voltage;
	public ModbusReadLongChannel batteryCell16Voltage;
	public ModbusReadLongChannel batteryCell17Voltage;
	public ModbusReadLongChannel batteryCell18Voltage;
	public ModbusReadLongChannel batteryCell19Voltage;
	public ModbusReadLongChannel batteryCell20Voltage;
	public ModbusReadLongChannel batteryCell21Voltage;
	public ModbusReadLongChannel batteryCell22Voltage;
	public ModbusReadLongChannel batteryCell23Voltage;
	public ModbusReadLongChannel batteryCell24Voltage;
	public ModbusReadLongChannel batteryCell25Voltage;
	public ModbusReadLongChannel batteryCell26Voltage;
	public ModbusReadLongChannel batteryCell27Voltage;
	public ModbusReadLongChannel batteryCell28Voltage;
	public ModbusReadLongChannel batteryCell29Voltage;
	public ModbusReadLongChannel batteryCell30Voltage;
	public ModbusReadLongChannel batteryCell31Voltage;
	public ModbusReadLongChannel batteryCell32Voltage;
	public ModbusReadLongChannel batteryCell33Voltage;
	public ModbusReadLongChannel batteryCell34Voltage;
	public ModbusReadLongChannel batteryCell35Voltage;
	public ModbusReadLongChannel batteryCell36Voltage;
	public ModbusReadLongChannel batteryCell37Voltage;
	public ModbusReadLongChannel batteryCell38Voltage;
	public ModbusReadLongChannel batteryCell39Voltage;
	public ModbusReadLongChannel batteryCell40Voltage;
	public ModbusReadLongChannel batteryCell41Voltage;
	public ModbusReadLongChannel batteryCell42Voltage;
	public ModbusReadLongChannel batteryCell43Voltage;
	public ModbusReadLongChannel batteryCell44Voltage;
	public ModbusReadLongChannel batteryCell45Voltage;
	public ModbusReadLongChannel batteryCell46Voltage;
	public ModbusReadLongChannel batteryCell47Voltage;
	public ModbusReadLongChannel batteryCell48Voltage;
	public ModbusReadLongChannel batteryCell49Voltage;
	public ModbusReadLongChannel batteryCell50Voltage;
	public ModbusReadLongChannel batteryCell51Voltage;
	public ModbusReadLongChannel batteryCell52Voltage;
	public ModbusReadLongChannel batteryCell53Voltage;
	public ModbusReadLongChannel batteryCell54Voltage;
	public ModbusReadLongChannel batteryCell55Voltage;
	public ModbusReadLongChannel batteryCell56Voltage;
	public ModbusReadLongChannel batteryCell57Voltage;
	public ModbusReadLongChannel batteryCell58Voltage;
	public ModbusReadLongChannel batteryCell59Voltage;
	public ModbusReadLongChannel batteryCell60Voltage;
	public ModbusReadLongChannel batteryCell61Voltage;
	public ModbusReadLongChannel batteryCell62Voltage;
	public ModbusReadLongChannel batteryCell63Voltage;
	public ModbusReadLongChannel batteryCell64Voltage;
	public ModbusReadLongChannel batteryCell65Voltage;
	public ModbusReadLongChannel batteryCell66Voltage;
	public ModbusReadLongChannel batteryCell67Voltage;
	public ModbusReadLongChannel batteryCell68Voltage;
	public ModbusReadLongChannel batteryCell69Voltage;
	public ModbusReadLongChannel batteryCell70Voltage;
	public ModbusReadLongChannel batteryCell71Voltage;
	public ModbusReadLongChannel batteryCell72Voltage;
	public ModbusReadLongChannel batteryCell73Voltage;
	public ModbusReadLongChannel batteryCell74Voltage;
	public ModbusReadLongChannel batteryCell75Voltage;
	public ModbusReadLongChannel batteryCell76Voltage;
	public ModbusReadLongChannel batteryCell77Voltage;
	public ModbusReadLongChannel batteryCell78Voltage;
	public ModbusReadLongChannel batteryCell79Voltage;
	public ModbusReadLongChannel batteryCell80Voltage;
	public ModbusReadLongChannel batteryCell81Voltage;
	public ModbusReadLongChannel batteryCell82Voltage;
	public ModbusReadLongChannel batteryCell83Voltage;
	public ModbusReadLongChannel batteryCell84Voltage;
	public ModbusReadLongChannel batteryCell85Voltage;
	public ModbusReadLongChannel batteryCell86Voltage;
	public ModbusReadLongChannel batteryCell87Voltage;
	public ModbusReadLongChannel batteryCell88Voltage;
	public ModbusReadLongChannel batteryCell89Voltage;
	public ModbusReadLongChannel batteryCell90Voltage;
	public ModbusReadLongChannel batteryCell91Voltage;
	public ModbusReadLongChannel batteryCell92Voltage;
	public ModbusReadLongChannel batteryCell93Voltage;
	public ModbusReadLongChannel batteryCell94Voltage;
	public ModbusReadLongChannel batteryCell95Voltage;
	public ModbusReadLongChannel batteryCell96Voltage;
	public ModbusReadLongChannel batteryCell97Voltage;
	public ModbusReadLongChannel batteryCell98Voltage;
	public ModbusReadLongChannel batteryCell99Voltage;
	public ModbusReadLongChannel batteryCell100Voltage;
	public ModbusReadLongChannel batteryCell101Voltage;
	public ModbusReadLongChannel batteryCell102Voltage;
	public ModbusReadLongChannel batteryCell103Voltage;
	public ModbusReadLongChannel batteryCell104Voltage;
	public ModbusReadLongChannel batteryCell105Voltage;
	public ModbusReadLongChannel batteryCell106Voltage;
	public ModbusReadLongChannel batteryCell107Voltage;
	public ModbusReadLongChannel batteryCell108Voltage;
	public ModbusReadLongChannel batteryCell109Voltage;
	public ModbusReadLongChannel batteryCell110Voltage;
	public ModbusReadLongChannel batteryCell111Voltage;
	public ModbusReadLongChannel batteryCell112Voltage;
	public ModbusReadLongChannel batteryCell113Voltage;
	public ModbusReadLongChannel batteryCell114Voltage;
	public ModbusReadLongChannel batteryCell115Voltage;
	public ModbusReadLongChannel batteryCell116Voltage;
	public ModbusReadLongChannel batteryCell117Voltage;
	public ModbusReadLongChannel batteryCell118Voltage;
	public ModbusReadLongChannel batteryCell119Voltage;
	public ModbusReadLongChannel batteryCell120Voltage;
	public ModbusReadLongChannel batteryCell121Voltage;
	public ModbusReadLongChannel batteryCell122Voltage;
	public ModbusReadLongChannel batteryCell123Voltage;
	public ModbusReadLongChannel batteryCell124Voltage;
	public ModbusReadLongChannel batteryCell125Voltage;
	public ModbusReadLongChannel batteryCell126Voltage;
	public ModbusReadLongChannel batteryCell127Voltage;
	public ModbusReadLongChannel batteryCell128Voltage;
	public ModbusReadLongChannel batteryCell129Voltage;
	public ModbusReadLongChannel batteryCell130Voltage;
	public ModbusReadLongChannel batteryCell131Voltage;
	public ModbusReadLongChannel batteryCell132Voltage;
	public ModbusReadLongChannel batteryCell133Voltage;
	public ModbusReadLongChannel batteryCell134Voltage;
	public ModbusReadLongChannel batteryCell135Voltage;
	public ModbusReadLongChannel batteryCell136Voltage;
	public ModbusReadLongChannel batteryCell137Voltage;
	public ModbusReadLongChannel batteryCell138Voltage;
	public ModbusReadLongChannel batteryCell139Voltage;
	public ModbusReadLongChannel batteryCell140Voltage;
	public ModbusReadLongChannel batteryCell141Voltage;
	public ModbusReadLongChannel batteryCell142Voltage;
	public ModbusReadLongChannel batteryCell143Voltage;
	public ModbusReadLongChannel batteryCell144Voltage;
	public ModbusReadLongChannel batteryCell145Voltage;
	public ModbusReadLongChannel batteryCell146Voltage;
	public ModbusReadLongChannel batteryCell147Voltage;
	public ModbusReadLongChannel batteryCell148Voltage;
	public ModbusReadLongChannel batteryCell149Voltage;
	public ModbusReadLongChannel batteryCell150Voltage;
	public ModbusReadLongChannel batteryCell151Voltage;
	public ModbusReadLongChannel batteryCell152Voltage;
	public ModbusReadLongChannel batteryCell153Voltage;
	public ModbusReadLongChannel batteryCell154Voltage;
	public ModbusReadLongChannel batteryCell155Voltage;
	public ModbusReadLongChannel batteryCell156Voltage;
	public ModbusReadLongChannel batteryCell157Voltage;
	public ModbusReadLongChannel batteryCell158Voltage;
	public ModbusReadLongChannel batteryCell159Voltage;
	public ModbusReadLongChannel batteryCell160Voltage;
	public ModbusReadLongChannel batteryCell161Voltage;
	public ModbusReadLongChannel batteryCell162Voltage;
	public ModbusReadLongChannel batteryCell163Voltage;
	public ModbusReadLongChannel batteryCell164Voltage;
	public ModbusReadLongChannel batteryCell165Voltage;
	public ModbusReadLongChannel batteryCell166Voltage;
	public ModbusReadLongChannel batteryCell167Voltage;
	public ModbusReadLongChannel batteryCell168Voltage;
	public ModbusReadLongChannel batteryCell169Voltage;
	public ModbusReadLongChannel batteryCell170Voltage;
	public ModbusReadLongChannel batteryCell171Voltage;
	public ModbusReadLongChannel batteryCell172Voltage;
	public ModbusReadLongChannel batteryCell173Voltage;
	public ModbusReadLongChannel batteryCell174Voltage;
	public ModbusReadLongChannel batteryCell175Voltage;
	public ModbusReadLongChannel batteryCell176Voltage;
	public ModbusReadLongChannel batteryCell177Voltage;
	public ModbusReadLongChannel batteryCell178Voltage;
	public ModbusReadLongChannel batteryCell179Voltage;
	public ModbusReadLongChannel batteryCell180Voltage;
	public ModbusReadLongChannel batteryCell181Voltage;
	public ModbusReadLongChannel batteryCell182Voltage;
	public ModbusReadLongChannel batteryCell183Voltage;
	public ModbusReadLongChannel batteryCell184Voltage;
	public ModbusReadLongChannel batteryCell185Voltage;
	public ModbusReadLongChannel batteryCell186Voltage;
	public ModbusReadLongChannel batteryCell187Voltage;
	public ModbusReadLongChannel batteryCell188Voltage;
	public ModbusReadLongChannel batteryCell189Voltage;
	public ModbusReadLongChannel batteryCell190Voltage;
	public ModbusReadLongChannel batteryCell191Voltage;
	public ModbusReadLongChannel batteryCell192Voltage;
	public ModbusReadLongChannel batteryCell193Voltage;
	public ModbusReadLongChannel batteryCell194Voltage;
	public ModbusReadLongChannel batteryCell195Voltage;
	public ModbusReadLongChannel batteryCell196Voltage;
	public ModbusReadLongChannel batteryCell197Voltage;
	public ModbusReadLongChannel batteryCell198Voltage;
	public ModbusReadLongChannel batteryCell199Voltage;
	public ModbusReadLongChannel batteryCell200Voltage;
	public ModbusReadLongChannel batteryCell201Voltage;
	public ModbusReadLongChannel batteryCell202Voltage;
	public ModbusReadLongChannel batteryCell203Voltage;
	public ModbusReadLongChannel batteryCell204Voltage;
	public ModbusReadLongChannel batteryCell205Voltage;
	public ModbusReadLongChannel batteryCell206Voltage;
	public ModbusReadLongChannel batteryCell207Voltage;
	public ModbusReadLongChannel batteryCell208Voltage;
	public ModbusReadLongChannel batteryCell209Voltage;
	public ModbusReadLongChannel batteryCell210Voltage;
	public ModbusReadLongChannel batteryCell211Voltage;
	public ModbusReadLongChannel batteryCell212Voltage;
	public ModbusReadLongChannel batteryCell213Voltage;
	public ModbusReadLongChannel batteryCell214Voltage;
	public ModbusReadLongChannel batteryCell215Voltage;
	public ModbusReadLongChannel batteryCell216Voltage;
	public ModbusReadLongChannel batteryCell217Voltage;
	public ModbusReadLongChannel batteryCell218Voltage;
	public ModbusReadLongChannel batteryCell219Voltage;
	public ModbusReadLongChannel batteryCell220Voltage;
	public ModbusReadLongChannel batteryCell221Voltage;
	public ModbusReadLongChannel batteryCell222Voltage;
	public ModbusReadLongChannel batteryCell223Voltage;
	public ModbusReadLongChannel batteryCell224Voltage;

	/*
	 * Methods
	 */
	@Override
	protected ModbusProtocol defineModbusProtocol() throws ConfigException {
		return new ModbusProtocol( //
				new ModbusRegisterRange(0x0101, //
						new UnsignedWordElement(0x0101, //
								systemState = new ModbusReadLongChannel("SystemState", this) //
								.label(2, STOP) //
								.label(4, "PV-Charge") //
								.label(8, "Standby") //
								.label(16, START) //
								.label(32, FAULT) //
								.label(64, "Debug")), //
						new UnsignedWordElement(0x0102, //
								controlMode = new ModbusReadLongChannel("ControlMode", this) //
								.label(1, "Remote") //
								.label(2, "Local")), //
						new DummyElement(0x0103), // WorkMode: RemoteDispatch
						new UnsignedWordElement(0x0104, //
								batteryMaintenanceState = new ModbusReadLongChannel("BatteryMaintenanceState", this) //
								.label(0, OFF) //
								.label(1, ON)), //
						new UnsignedWordElement(0x0105, //
								inverterState = new ModbusReadLongChannel("InverterState", this) //
								.label(0, "Init") //
								.label(2, "Fault") //
								.label(4, STOP) //
								.label(8, STANDBY) //
								.label(16, "Grid-Monitor") // ,
								.label(32, "Ready") //
								.label(64, START) //
								.label(128, "Debug")), //
						new UnsignedWordElement(0x0106, //
								gridMode = new ModbusReadLongChannel("GridMode", this) //
								.label(1, OFF_GRID) //
								.label(2, ON_GRID)), //
						new DummyElement(0x0107), //
						new UnsignedWordElement(0x0108, //
								protocolVersion = new ModbusReadLongChannel("ProtocolVersion", this)), //
						new UnsignedWordElement(0x0109, //
								systemManufacturer = new ModbusReadLongChannel("SystemManufacturer", this) //
								.label(1, "BYD")), //
						new UnsignedWordElement(0x010A, //
								systemType = new ModbusReadLongChannel("SystemType", this) //
								.label(1, "CESS")), //
						new DummyElement(0x010B, 0x010F), //
						new UnsignedWordElement(0x0110, //
								new ModbusBitWrappingChannel("SuggestiveInformation1", this, this.thingState) //
								.warningBit(2, WarningEss.EmergencyStop) // EmergencyStop
								.warningBit(6, WarningEss.KeyManualStop)), // KeyManualStop
						new UnsignedWordElement(0x0111, //
								new ModbusBitWrappingChannel("SuggestiveInformation2", this, this.thingState) //
								.warningBit(3, WarningEss.TransformerPhaseBTemperatureSensorInvalidation) // Transformer
								// phase
								// B
								// temperature
								// sensor
								// invalidation
								.warningBit(12, WarningEss.SDMemoryCardInvalidation)), // SD memory card
						// invalidation
						new DummyElement(0x0112, 0x0124), //
						new UnsignedWordElement(0x0125, //
								new ModbusBitWrappingChannel("SuggestiveInformation3", this, this.thingState)//
								.warningBit(0, WarningEss.InverterCommunicationAbnormity)//
								.warningBit(1, WarningEss.BatteryStackCommunicationAbnormity)//
								.warningBit(2, WarningEss.MultifunctionalAmmeterCommunicationAbnormity)//
								.warningBit(4, WarningEss.RemoteCommunicationAbnormity)//
								.warningBit(8, WarningEss.PVDC1CommunicationAbnormity)//
								.warningBit(9, WarningEss.PVDC2CommunicationAbnormity)//
								), //

						new UnsignedWordElement(0x0126, //
								new ModbusBitWrappingChannel("SuggestiveInformation4", this, this.thingState)//
								.warningBit(3, WarningEss.TransformerSevereOvertemperature)//
								), //

						new DummyElement(0x0127, 0x014F), //
						new UnsignedWordElement(0x0150, //
								switchState = new StatusBitChannel("BatteryStringSwitchState", this) //
								.label(1, "Main contactor") //
								.label(2, "Precharge contactor") //
								.label(4, "FAN contactor") //
								.label(8, "BMU power supply relay") //
								.label(16, "Middle relay"))//
						), //
				new ModbusRegisterRange(0x0180, //
						new UnsignedWordElement(0x0180, //
								new ModbusBitWrappingChannel("Abnormity1", this, this.thingState)//
								.faultBit(0, FaultEss.DCPrechargeContactorCloseUnsuccessfully)//
								.faultBit(1, FaultEss.ACPrechargeContactorCloseUnsuccessfully)//
								.faultBit(2, FaultEss.ACMainContactorCloseUnsuccessfully)//
								.faultBit(3, FaultEss.DCElectricalBreaker1CloseUnsuccessfully)//
								.faultBit(4, FaultEss.DCMainContactorCloseUnsuccessfully)//
								.faultBit(5, FaultEss.ACBreakerTrip)//
								.faultBit(6, FaultEss.ACMainContactorOpenWhenRunning)//
								.faultBit(7, FaultEss.DCMainContactorOpenWhenRunning)//
								.faultBit(8, FaultEss.ACMainContactorOpenUnsuccessfully)//
								.faultBit(9, FaultEss.DCElectricalBreaker1OpenUnsuccessfully)//
								.faultBit(10, FaultEss.DCMainContactorOpenUnsuccessfully)//
								.faultBit(11, FaultEss.HardwarePDPFault)//
								.faultBit(12, FaultEss.MasterStopSuddenly)//
								),

						new DummyElement(0x0181), new UnsignedWordElement(0x0182, //
								new ModbusBitWrappingChannel("Abnormity2", this, this.thingState)//
								.faultBit(0, FaultEss.DCShortCircuitProtection)//
								.faultBit(1, FaultEss.DCOvervoltageProtection)//
								.faultBit(2, FaultEss.DCUndervoltageProtection)//
								.faultBit(3, FaultEss.DCInverseNoConnectionProtection)//
								.faultBit(4, FaultEss.DCDisconnectionProtection)//
								.faultBit(5, FaultEss.CommutingVoltageAbnormityProtection)//
								.faultBit(6, FaultEss.DCOvercurrentProtection)//
								.faultBit(7, FaultEss.Phase1PeakCurrentOverLimitProtection)//
								.faultBit(8, FaultEss.Phase2PeakCurrentOverLimitProtection)//
								.faultBit(9, FaultEss.Phase3PeakCurrentOverLimitProtection)//
								.faultBit(10,FaultEss.Phase1GridVoltageSamplingInvalidation)//
								.faultBit(11, FaultEss.Phase2VirtualCurrentOverLimitProtection)//
								.faultBit(12, FaultEss.Phase3VirtualCurrentOverLimitProtection)//
								.faultBit(13, FaultEss.Phase1GridVoltageSamplingInvalidation2)// TODO same as
								// above
								.faultBit(14, FaultEss.Phase2ridVoltageSamplingInvalidation)//
								.faultBit(15, FaultEss.Phase3GridVoltageSamplingInvalidation)//
								), //

						new UnsignedWordElement(0x0183, //
								new ModbusBitWrappingChannel("Abnormity3", this, this.thingState)//
								.faultBit(0, FaultEss.Phase1InvertVoltageSamplingInvalidation)//
								.faultBit(1, FaultEss.Phase2InvertVoltageSamplingInvalidation)//
								.faultBit(2, FaultEss.Phase3InvertVoltageSamplingInvalidation)//
								.faultBit(3, FaultEss.ACCurrentSamplingInvalidation)//
								.faultBit(4, FaultEss.DCCurrentSamplingInvalidation)//
								.faultBit(5, FaultEss.Phase1OvertemperatureProtection)//
								.faultBit(6, FaultEss.Phase2OvertemperatureProtection)//
								.faultBit(7, FaultEss.Phase3OvertemperatureProtection)//
								.faultBit(8, FaultEss.Phase1TemperatureSamplingInvalidation)//
								.faultBit(9, FaultEss.Phase2TemperatureSamplingInvalidation)//
								.faultBit(10, FaultEss.Phase3TemperatureSamplingInvalidation)//
								.faultBit(11, FaultEss.Phase1PrechargeUnmetProtection)//
								.faultBit(12, FaultEss.Phase2PrechargeUnmetProtection)//
								.faultBit(13, FaultEss.Phase3PrechargeUnmetProtection)//
								.faultBit(14, FaultEss.UnadaptablePhaseSequenceErrorProtection)//
								.faultBit(15, FaultEss.DSPProtection)//
								), //

						new UnsignedWordElement(0x0184, //
								new ModbusBitWrappingChannel("Abnormity4", this, this.thingState)//
								.faultBit(0, FaultEss.Phase1GridVoltageSevereOvervoltageProtection)//
								.faultBit(1, FaultEss.Phase1GridVoltageGeneralOvervoltageProtection)//
								.faultBit(2, FaultEss.Phase2GridVoltageSevereOvervoltageProtection)//
								.faultBit(3, FaultEss.Phase2GridVoltageGeneralOvervoltageProtection)//
								.faultBit(4, FaultEss.Phase3GridVoltageSevereOvervoltageProtection)//
								.faultBit(5, FaultEss.Phase3GridVoltageGeneralOvervoltageProtection)//
								.faultBit(6, FaultEss.Phase1GridVoltageSevereUndervoltageProtection)//
								.faultBit(7, FaultEss.Phase1GridVoltageGeneralUndervoltageProtection)//
								.faultBit(8, FaultEss.Phase2GridVoltageSevereUndervoltageProtection)//
								.faultBit(9, FaultEss.Phase2GridVoltageGeneralUndervoltageProtection)//
								.faultBit(10, FaultEss.Phase3GridVoltageSevereUndervoltageProtection)//
								.faultBit(11, FaultEss.Phase3GridVoltageGeneralUndervoltageProtection)//
								.faultBit(12, FaultEss.SevereOverfrequncyProtection)//
								.faultBit(13, FaultEss.GeneralOverfrequncyProtection)//
								.faultBit(14, FaultEss.SevereUnderfrequncyProtection)//
								.faultBit(15, FaultEss.GeneralsUnderfrequncyProtection)//
								), //

						new UnsignedWordElement(0x0185, //
								new ModbusBitWrappingChannel("Abnormity5", this, this.thingState)//
								.faultBit(0, FaultEss.Phase1Gridloss)//
								.faultBit(1, FaultEss.Phase2Gridloss)//
								.faultBit(2, FaultEss.Phase3Gridloss)//
								.faultBit(3, FaultEss.IslandingProtection)//
								.faultBit(4, FaultEss.Phase1UnderVoltageRideThrough)//
								.faultBit(5, FaultEss.Phase2UnderVoltageRideThrough)//
								.faultBit(6, FaultEss.Phase3UnderVoltageRideThrough)//
								.faultBit(7, FaultEss.Phase1InverterVoltageSevereOvervoltageProtection)//
								.faultBit(8, FaultEss.Phase1InverterVoltageGeneralOvervoltageProtection)//
								.faultBit(9, FaultEss.Phase2InverterVoltageSevereOvervoltageProtection)//
								.faultBit(10, FaultEss.Phase2InverterVoltageGeneralOvervoltageProtection)//
								.faultBit(11, FaultEss.Phase3InverterVoltageSevereOvervoltageProtection)//
								.faultBit(12, FaultEss.Phase3InverterVoltageGeneralOvervoltageProtection)//
								.faultBit(13, FaultEss.InverterPeakVoltageHighProtectionCauseByACDisconnect)//
								), //

						new UnsignedWordElement(0x0186, //
								new ModbusBitWrappingChannel("SuggestiveInformation5", this, this.thingState)//
								.warningBit(0, WarningEss.DCPrechargeContactorInspectionAbnormity)//
								.warningBit(1, WarningEss.DCBreaker1InspectionAbnormity)//
								.warningBit(2, WarningEss.DCBreaker2InspectionAbnormity)//
								.warningBit(3, WarningEss.ACPrechargeContactorInspectionAbnormity)//
								.warningBit(4, WarningEss.ACMainontactorInspectionAbnormity)//
								.warningBit(5, WarningEss.ACBreakerInspectionAbnormity)//
								.warningBit(6, WarningEss.DCBreaker1CloseUnsuccessfully)//
								.warningBit(7, WarningEss.DCBreaker2CloseUnsuccessfully)//
								.warningBit(8, WarningEss.ControlSignalCloseAbnormallyInspectedBySystem)//
								.warningBit(9, WarningEss.ControlSignalOpenAbnormallyInspectedBySystem)//
								.warningBit(10, WarningEss.NeutralWireContactorCloseUnsuccessfully)//
								.warningBit(11, WarningEss.NeutralWireContactorOpenUnsuccessfully)//
								.warningBit(12, WarningEss.WorkDoorOpen)//
								.warningBit(13, WarningEss.Emergency1Stop)//
								.warningBit(14, WarningEss.ACBreakerCloseUnsuccessfully)//
								.warningBit(15, WarningEss.ControlSwitchStop)//
								), //

						new UnsignedWordElement(0x0187, //
								new ModbusBitWrappingChannel("SuggestiveInformation6", this, this.thingState)//
								.warningBit(0, WarningEss.GeneralOverload)//
								.warningBit(1, WarningEss.SevereOverload)//
								.warningBit(2, WarningEss.BatteryCurrentOverLimit)//
								.warningBit(3, WarningEss.PowerDecreaseCausedByOvertemperature)//
								.warningBit(4, WarningEss.InverterGeneralOvertemperature)//
								.warningBit(5, WarningEss.ACThreePhaseCurrentUnbalance)//
								.warningBit(6, WarningEss.RestoreFactorySettingUnsuccessfully)//
								.warningBit(7, WarningEss.PoleBoardInvalidation)//
								.warningBit(8, WarningEss.SelfInspectionFailed)//
								.warningBit(9, WarningEss.ReceiveBMSFaultAndStop)//
								.warningBit(10, WarningEss.RefrigerationEquipmentinvalidation)//
								.warningBit(11, WarningEss.LargeTemperatureDifferenceAmongIGBTThreePhases)//
								.warningBit(12, WarningEss.EEPROMParametersOverRange)//
								.warningBit(13, WarningEss.EEPROMParametersBackupFailed)//
								.warningBit(14, WarningEss.DCBreakerCloseunsuccessfully)//
								), //
						new UnsignedWordElement(0x0188, //
								new ModbusBitWrappingChannel("SuggestiveInformation7", this, this.thingState)//
								.warningBit(0, WarningEss.CommunicationBetweenInverterAndBSMUDisconnected)//
								.warningBit(1, WarningEss.CommunicationBetweenInverterAndMasterDisconnected)//
								.warningBit(2, WarningEss.CommunicationBetweenInverterAndUCDisconnected)//
								.warningBit(3, WarningEss.BMSStartOvertimeControlledByPCS)//
								.warningBit(4, WarningEss.BMSStopOvertimeControlledByPCS)//
								.warningBit(5, WarningEss.SyncSignalInvalidation)//
								.warningBit(6, WarningEss.SyncSignalContinuousCaputureFault)//
								.warningBit(7, WarningEss.SyncSignalSeveralTimesCaputureFault))),

				new ModbusRegisterRange(0x0200, //
						new SignedWordElement(0x0200, //
								batteryVoltage = new ModbusReadLongChannel("BatteryVoltage", this).unit("mV")
								.multiplier(2)),
						new SignedWordElement(0x0201, //
								batteryCurrent = new ModbusReadLongChannel("BatteryCurrent", this).unit("mA")
								.multiplier(2)),
						new SignedWordElement(0x0202, //
								batteryPower = new ModbusReadLongChannel("BatteryPower", this).unit("W").multiplier(2)),
						new DummyElement(0x0203, 0x0207), //
						new UnsignedDoublewordElement(0x0208, //
								acChargeEnergy = new ModbusReadLongChannel("AcChargeEnergy", this).unit("Wh")
								.multiplier(2)).wordOrder(WordOrder.LSWMSW),
						new UnsignedDoublewordElement(0x020A, //
								acDischargeEnergy = new ModbusReadLongChannel("AcDischargeEnergy", this).unit("Wh")
								.multiplier(2)).wordOrder(WordOrder.LSWMSW),
						new DummyElement(0x020C, 0x020F), new SignedWordElement(0x0210, //
								gridActivePower = new ModbusReadLongChannel("GridActivePower", this).unit("W")
								.multiplier(2)),
						new SignedWordElement(0x0211, //
								reactivePower = new ModbusReadLongChannel("ReactivePower", this).unit("var")
								.multiplier(2)),
						new UnsignedWordElement(0x0212, //
								apparentPower = new ModbusReadLongChannel("ApparentPower", this).unit("VA")
								.multiplier(2)),
						new SignedWordElement(0x0213, //
								currentL1 = new ModbusReadLongChannel("CurrentL1", this).unit("mA").multiplier(2)),
						new SignedWordElement(0x0214, //
								currentL2 = new ModbusReadLongChannel("CurrentL2", this).unit("mA").multiplier(2)),
						new SignedWordElement(0x0215, //
								currentL3 = new ModbusReadLongChannel("CurrentL3", this).unit("mA").multiplier(2)),
						new DummyElement(0x0216, 0x218), //
						new UnsignedWordElement(0x0219, //
								voltageL1 = new ModbusReadLongChannel("VoltageL1", this).unit("mV").multiplier(2)),
						new UnsignedWordElement(0x021A, //
								voltageL2 = new ModbusReadLongChannel("VoltageL2", this).unit("mV").multiplier(2)),
						new UnsignedWordElement(0x021B, //
								voltageL3 = new ModbusReadLongChannel("VoltageL3", this).unit("mV").multiplier(2)),
						new UnsignedWordElement(0x021C, //
								frequency = new ModbusReadLongChannel("Frequency", this).unit("mHZ").multiplier(1))),
				new ModbusRegisterRange(0x0222, //
						new UnsignedWordElement(0x0222, //
								inverterVoltageL1 = new ModbusReadLongChannel("InverterVoltageL1", this).unit("mV")
								.multiplier(2)), //
						new UnsignedWordElement(0x0223, //
								inverterVoltageL2 = new ModbusReadLongChannel("InverterVoltageL2", this).unit("mV")
								.multiplier(2)), //
						new UnsignedWordElement(0x0224, //
								inverterVoltageL3 = new ModbusReadLongChannel("InverterVoltageL3", this).unit("mV")
								.multiplier(2)), //
						new UnsignedWordElement(0x0225, //
								inverterCurrentL1 = new ModbusReadLongChannel("InverterCurrentL1", this).unit("mA")
								.multiplier(2)), //
						new UnsignedWordElement(0x0226, //
								inverterCurrentL2 = new ModbusReadLongChannel("InverterCurrentL2", this).unit("mA")
								.multiplier(2)), //
						new UnsignedWordElement(0x0227, //
								inverterCurrentL3 = new ModbusReadLongChannel("InverterCurrentL3", this).unit("mA")
								.multiplier(2)), //
						new SignedWordElement(0x0228, //
								activePower = new ModbusReadLongChannel("ActivePower", this).unit("W").multiplier(2)), //
						new DummyElement(0x0229, 0x022F), new SignedWordElement(0x0230, //
								allowedCharge = new ModbusReadLongChannel("AllowedCharge", this).unit("W")
								.multiplier(2)), //
						new UnsignedWordElement(0x0231, //
								allowedDischarge = new ModbusReadLongChannel("AllowedDischarge", this).unit("W")
								.multiplier(2)), //
						new UnsignedWordElement(0x0232, //
								allowedApparent = new ModbusReadLongChannel("AllowedApparent", this).unit("VA")
								.multiplier(2)), //
						new DummyElement(0x0233, 0x23F), new SignedWordElement(0x0240, //
								ipmTemperatureL1 = new ModbusReadLongChannel("IpmTemperatureL1", this).unit("�C")), //
						new SignedWordElement(0x0241, //
								ipmTemperatureL2 = new ModbusReadLongChannel("IpmTemperatureL2", this).unit("�C")), //
						new SignedWordElement(0x0242, //
								ipmTemperatureL3 = new ModbusReadLongChannel("IpmTemperatureL3", this).unit("�C")), //
						new DummyElement(0x0243, 0x0248), new SignedWordElement(0x0249, //
								transformerTemperatureL2 = new ModbusReadLongChannel("TransformerTemperatureL2", this)
								.unit("�C"))),
				new WriteableModbusRegisterRange(0x0500, //
						new UnsignedWordElement(0x0500, //
								setWorkState = new ModbusWriteLongChannel("SetWorkState", this) //
								.label(4, STOP) //
								.label(32, STANDBY) //
								.label(64, START))),
				new WriteableModbusRegisterRange(0x0501, //
						new SignedWordElement(0x0501, //
								setActivePower = new ModbusWriteLongChannel("SetActivePower", this).unit("W")
								.multiplier(2).minWriteChannel(allowedCharge)
								.maxWriteChannel(allowedDischarge)),
						new SignedWordElement(0x0502, //
								setReactivePower = new ModbusWriteLongChannel("SetReactivePower", this).unit("var")
								.multiplier(2).minWriteChannel(allowedCharge)
								.maxWriteChannel(allowedDischarge))),
				new ModbusRegisterRange(0x1402, //
						new UnsignedWordElement(0x1402,
								soc = new ModbusReadLongChannel("Soc", this).unit("%").interval(0, 100)),
						new UnsignedWordElement(0x1403,
								soh = new ModbusReadLongChannel("Soh", this).unit("%").interval(0, 100)),
						new UnsignedWordElement(0x1404,
								batteryCellAverageTemperature = new ModbusReadLongChannel(
										"BatteryCellAverageTemperature", this).unit("°C"))),
				new ModbusRegisterRange(0x1500, //
						new UnsignedWordElement(0x1500,
								batteryCell1Voltage = new ModbusReadLongChannel("Cell1Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1501,
								batteryCell2Voltage = new ModbusReadLongChannel("Cell2Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1502,
								batteryCell3Voltage = new ModbusReadLongChannel("Cell3Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1503,
								batteryCell4Voltage = new ModbusReadLongChannel("Cell4Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1504,
								batteryCell5Voltage = new ModbusReadLongChannel("Cell5Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1505,
								batteryCell6Voltage = new ModbusReadLongChannel("Cell6Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1506,
								batteryCell7Voltage = new ModbusReadLongChannel("Cell7Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1507,
								batteryCell8Voltage = new ModbusReadLongChannel("Cell8Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1508,
								batteryCell9Voltage = new ModbusReadLongChannel("Cell9Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1509,
								batteryCell10Voltage = new ModbusReadLongChannel("Cell10Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x150A,
								batteryCell11Voltage = new ModbusReadLongChannel("Cell11Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x150B,
								batteryCell12Voltage = new ModbusReadLongChannel("Cell12Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x150C,
								batteryCell13Voltage = new ModbusReadLongChannel("Cell13Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x150D,
								batteryCell14Voltage = new ModbusReadLongChannel("Cell14Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x150E,
								batteryCell15Voltage = new ModbusReadLongChannel("Cell15Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x150F,
								batteryCell16Voltage = new ModbusReadLongChannel("Cell16Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1510,
								batteryCell17Voltage = new ModbusReadLongChannel("Cell17Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1511,
								batteryCell18Voltage = new ModbusReadLongChannel("Cell18Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1512,
								batteryCell19Voltage = new ModbusReadLongChannel("Cell19Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1513,
								batteryCell20Voltage = new ModbusReadLongChannel("Cell20Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1514,
								batteryCell21Voltage = new ModbusReadLongChannel("Cell21Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1515,
								batteryCell22Voltage = new ModbusReadLongChannel("Cell22Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1516,
								batteryCell23Voltage = new ModbusReadLongChannel("Cell23Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1517,
								batteryCell24Voltage = new ModbusReadLongChannel("Cell24Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1518,
								batteryCell25Voltage = new ModbusReadLongChannel("Cell25Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1519,
								batteryCell26Voltage = new ModbusReadLongChannel("Cell26Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x151A,
								batteryCell27Voltage = new ModbusReadLongChannel("Cell27Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x151B,
								batteryCell28Voltage = new ModbusReadLongChannel("Cell28Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x151C,
								batteryCell29Voltage = new ModbusReadLongChannel("Cell29Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x151D,
								batteryCell30Voltage = new ModbusReadLongChannel("Cell30Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x151E,
								batteryCell31Voltage = new ModbusReadLongChannel("Cell31Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x151F,
								batteryCell32Voltage = new ModbusReadLongChannel("Cell32Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1520,
								batteryCell33Voltage = new ModbusReadLongChannel("Cell33Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1521,
								batteryCell34Voltage = new ModbusReadLongChannel("Cell34Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1522,
								batteryCell35Voltage = new ModbusReadLongChannel("Cell35Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1523,
								batteryCell36Voltage = new ModbusReadLongChannel("Cell36Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1524,
								batteryCell37Voltage = new ModbusReadLongChannel("Cell37Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1525,
								batteryCell38Voltage = new ModbusReadLongChannel("Cell38Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1526,
								batteryCell39Voltage = new ModbusReadLongChannel("Cell39Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1527,
								batteryCell40Voltage = new ModbusReadLongChannel("Cell40Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1528,
								batteryCell41Voltage = new ModbusReadLongChannel("Cell41Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1529,
								batteryCell42Voltage = new ModbusReadLongChannel("Cell42Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x152A,
								batteryCell43Voltage = new ModbusReadLongChannel("Cell43Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x152B,
								batteryCell44Voltage = new ModbusReadLongChannel("Cell44Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x152C,
								batteryCell45Voltage = new ModbusReadLongChannel("Cell45Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x152D,
								batteryCell46Voltage = new ModbusReadLongChannel("Cell46Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x152E,
								batteryCell47Voltage = new ModbusReadLongChannel("Cell47Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x152F,
								batteryCell48Voltage = new ModbusReadLongChannel("Cell48Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1530,
								batteryCell49Voltage = new ModbusReadLongChannel("Cell49Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1531,
								batteryCell50Voltage = new ModbusReadLongChannel("Cell50Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1532,
								batteryCell51Voltage = new ModbusReadLongChannel("Cell51Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1533,
								batteryCell52Voltage = new ModbusReadLongChannel("Cell52Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1534,
								batteryCell53Voltage = new ModbusReadLongChannel("Cell53Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1535,
								batteryCell54Voltage = new ModbusReadLongChannel("Cell54Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1536,
								batteryCell55Voltage = new ModbusReadLongChannel("Cell55Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1537,
								batteryCell56Voltage = new ModbusReadLongChannel("Cell56Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1538,
								batteryCell57Voltage = new ModbusReadLongChannel("Cell57Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1539,
								batteryCell58Voltage = new ModbusReadLongChannel("Cell58Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x153A,
								batteryCell59Voltage = new ModbusReadLongChannel("Cell59Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x153B,
								batteryCell60Voltage = new ModbusReadLongChannel("Cell60Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x153C,
								batteryCell61Voltage = new ModbusReadLongChannel("Cell61Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x153D,
								batteryCell62Voltage = new ModbusReadLongChannel("Cell62Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x153E,
								batteryCell63Voltage = new ModbusReadLongChannel("Cell63Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x153F,
								batteryCell64Voltage = new ModbusReadLongChannel("Cell64Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1540,
								batteryCell65Voltage = new ModbusReadLongChannel("Cell65Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1541,
								batteryCell66Voltage = new ModbusReadLongChannel("Cell66Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1542,
								batteryCell67Voltage = new ModbusReadLongChannel("Cell67Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1543,
								batteryCell68Voltage = new ModbusReadLongChannel("Cell68Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1544,
								batteryCell69Voltage = new ModbusReadLongChannel("Cell69Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1545,
								batteryCell70Voltage = new ModbusReadLongChannel("Cell70Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1546,
								batteryCell71Voltage = new ModbusReadLongChannel("Cell71Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1547,
								batteryCell72Voltage = new ModbusReadLongChannel("Cell72Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1548,
								batteryCell73Voltage = new ModbusReadLongChannel("Cell73Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1549,
								batteryCell74Voltage = new ModbusReadLongChannel("Cell74Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x154A,
								batteryCell75Voltage = new ModbusReadLongChannel("Cell75Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x154B,
								batteryCell76Voltage = new ModbusReadLongChannel("Cell76Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x154C,
								batteryCell77Voltage = new ModbusReadLongChannel("Cell77Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x154D,
								batteryCell78Voltage = new ModbusReadLongChannel("Cell78Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x154E,
								batteryCell79Voltage = new ModbusReadLongChannel("Cell79Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x154F,
								batteryCell80Voltage = new ModbusReadLongChannel("Cell80Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1550,
								batteryCell81Voltage = new ModbusReadLongChannel("Cell81Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1551,
								batteryCell82Voltage = new ModbusReadLongChannel("Cell82Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1552,
								batteryCell83Voltage = new ModbusReadLongChannel("Cell83Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1553,
								batteryCell84Voltage = new ModbusReadLongChannel("Cell84Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1554,
								batteryCell85Voltage = new ModbusReadLongChannel("Cell85Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1555,
								batteryCell86Voltage = new ModbusReadLongChannel("Cell86Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1556,
								batteryCell87Voltage = new ModbusReadLongChannel("Cell87Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1557,
								batteryCell88Voltage = new ModbusReadLongChannel("Cell88Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1558,
								batteryCell89Voltage = new ModbusReadLongChannel("Cell89Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1559,
								batteryCell90Voltage = new ModbusReadLongChannel("Cell90Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x155A,
								batteryCell91Voltage = new ModbusReadLongChannel("Cell91Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x155B,
								batteryCell92Voltage = new ModbusReadLongChannel("Cell92Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x155C,
								batteryCell93Voltage = new ModbusReadLongChannel("Cell93Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x155D,
								batteryCell94Voltage = new ModbusReadLongChannel("Cell94Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x155E,
								batteryCell95Voltage = new ModbusReadLongChannel("Cell95Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x155F,
								batteryCell96Voltage = new ModbusReadLongChannel("Cell96Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1560,
								batteryCell97Voltage = new ModbusReadLongChannel("Cell97Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1561,
								batteryCell98Voltage = new ModbusReadLongChannel("Cell98Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1562,
								batteryCell99Voltage = new ModbusReadLongChannel("Cell99Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1563,
								batteryCell100Voltage = new ModbusReadLongChannel("Cell100Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1564,
								batteryCell101Voltage = new ModbusReadLongChannel("Cell101Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1565,
								batteryCell102Voltage = new ModbusReadLongChannel("Cell102Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1566,
								batteryCell103Voltage = new ModbusReadLongChannel("Cell103Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1567,
								batteryCell104Voltage = new ModbusReadLongChannel("Cell104Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1568,
								batteryCell105Voltage = new ModbusReadLongChannel("Cell105Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1569,
								batteryCell106Voltage = new ModbusReadLongChannel("Cell106Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x156A,
								batteryCell107Voltage = new ModbusReadLongChannel("Cell107Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x156B,
								batteryCell108Voltage = new ModbusReadLongChannel("Cell108Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x156C,
								batteryCell109Voltage = new ModbusReadLongChannel("Cell109Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x156D,
								batteryCell110Voltage = new ModbusReadLongChannel("Cell110Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x156E,
								batteryCell111Voltage = new ModbusReadLongChannel("Cell111Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x156F,
								batteryCell112Voltage = new ModbusReadLongChannel("Cell112Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1570,
								batteryCell113Voltage = new ModbusReadLongChannel("Cell113Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1571,
								batteryCell114Voltage = new ModbusReadLongChannel("Cell114Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1572,
								batteryCell115Voltage = new ModbusReadLongChannel("Cell115Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1573,
								batteryCell116Voltage = new ModbusReadLongChannel("Cell116Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1574,
								batteryCell117Voltage = new ModbusReadLongChannel("Cell117Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1575,
								batteryCell118Voltage = new ModbusReadLongChannel("Cell18Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1576,
								batteryCell119Voltage = new ModbusReadLongChannel("Cell119Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1577,
								batteryCell120Voltage = new ModbusReadLongChannel("Cell120Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1578,
								batteryCell121Voltage = new ModbusReadLongChannel("Cell121Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1579,
								batteryCell122Voltage = new ModbusReadLongChannel("Cell122Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x157A,
								batteryCell123Voltage = new ModbusReadLongChannel("Cell123Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x157B,
								batteryCell124Voltage = new ModbusReadLongChannel("Cell124Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x157C,
								batteryCell125Voltage = new ModbusReadLongChannel("Cell125Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x157D,
								batteryCell126Voltage = new ModbusReadLongChannel("Cell126Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x157E,
								batteryCell127Voltage = new ModbusReadLongChannel("Cell127Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x157F,
								batteryCell128Voltage = new ModbusReadLongChannel("Cell128Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1580,
								batteryCell129Voltage = new ModbusReadLongChannel("Cell129Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1581,
								batteryCell130Voltage = new ModbusReadLongChannel("Cell130Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1582,
								batteryCell131Voltage = new ModbusReadLongChannel("Cell131Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1583,
								batteryCell132Voltage = new ModbusReadLongChannel("Cell132Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1584,
								batteryCell133Voltage = new ModbusReadLongChannel("Cell133Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1585,
								batteryCell134Voltage = new ModbusReadLongChannel("Cell134Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1586,
								batteryCell135Voltage = new ModbusReadLongChannel("Cell135Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1587,
								batteryCell136Voltage = new ModbusReadLongChannel("Cell136Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1588,
								batteryCell137Voltage = new ModbusReadLongChannel("Cell137Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1589,
								batteryCell138Voltage = new ModbusReadLongChannel("Cell138Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x158A,
								batteryCell139Voltage = new ModbusReadLongChannel("Cell139Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x158B,
								batteryCell140Voltage = new ModbusReadLongChannel("Cell140Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x158C,
								batteryCell141Voltage = new ModbusReadLongChannel("Cell141Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x158D,
								batteryCell142Voltage = new ModbusReadLongChannel("Cell142Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x158E,
								batteryCell143Voltage = new ModbusReadLongChannel("Cell143Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x158F,
								batteryCell144Voltage = new ModbusReadLongChannel("Cell144Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1590,
								batteryCell145Voltage = new ModbusReadLongChannel("Cell145Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1591,
								batteryCell146Voltage = new ModbusReadLongChannel("Cell146Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1592,
								batteryCell147Voltage = new ModbusReadLongChannel("Cell147Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1593,
								batteryCell148Voltage = new ModbusReadLongChannel("Cell148Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1594,
								batteryCell149Voltage = new ModbusReadLongChannel("Cell149Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1595,
								batteryCell150Voltage = new ModbusReadLongChannel("Cell150Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1596,
								batteryCell151Voltage = new ModbusReadLongChannel("Cell151Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1597,
								batteryCell152Voltage = new ModbusReadLongChannel("Cell152Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1598,
								batteryCell153Voltage = new ModbusReadLongChannel("Cell153Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x1599,
								batteryCell154Voltage = new ModbusReadLongChannel("Cell154Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x159A,
								batteryCell155Voltage = new ModbusReadLongChannel("Cell155Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x159B,
								batteryCell156Voltage = new ModbusReadLongChannel("Cell156Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x159C,
								batteryCell157Voltage = new ModbusReadLongChannel("Cell157Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x159D,
								batteryCell158Voltage = new ModbusReadLongChannel("Cell158Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x159E,
								batteryCell159Voltage = new ModbusReadLongChannel("Cell159Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x159F,
								batteryCell160Voltage = new ModbusReadLongChannel("Cell160Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15A0,
								batteryCell161Voltage = new ModbusReadLongChannel("Cell161Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15A1,
								batteryCell162Voltage = new ModbusReadLongChannel("Cell162Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15A2,
								batteryCell163Voltage = new ModbusReadLongChannel("Cell163Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15A3,
								batteryCell164Voltage = new ModbusReadLongChannel("Cell164Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15A4,
								batteryCell165Voltage = new ModbusReadLongChannel("Cell165Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15A5,
								batteryCell166Voltage = new ModbusReadLongChannel("Cell166Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15A6,
								batteryCell167Voltage = new ModbusReadLongChannel("Cell167Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15A7,
								batteryCell168Voltage = new ModbusReadLongChannel("Cell168Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15A8,
								batteryCell169Voltage = new ModbusReadLongChannel("Cell169Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15A9,
								batteryCell170Voltage = new ModbusReadLongChannel("Cell170Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15AA,
								batteryCell171Voltage = new ModbusReadLongChannel("Cell171Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15AB,
								batteryCell172Voltage = new ModbusReadLongChannel("Cell172Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15AC,
								batteryCell173Voltage = new ModbusReadLongChannel("Cell173Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15AD,
								batteryCell174Voltage = new ModbusReadLongChannel("Cell174Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15AE,
								batteryCell175Voltage = new ModbusReadLongChannel("Cell175Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15AF,
								batteryCell176Voltage = new ModbusReadLongChannel("Cell176Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15B0,
								batteryCell177Voltage = new ModbusReadLongChannel("Cell177Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15B1,
								batteryCell178Voltage = new ModbusReadLongChannel("Cell178Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15B2,
								batteryCell179Voltage = new ModbusReadLongChannel("Cell179Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15B3,
								batteryCell180Voltage = new ModbusReadLongChannel("Cell180Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15B4,
								batteryCell181Voltage = new ModbusReadLongChannel("Cell181Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15B5,
								batteryCell182Voltage = new ModbusReadLongChannel("Cell182Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15B6,
								batteryCell183Voltage = new ModbusReadLongChannel("Cell183Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15B7,
								batteryCell184Voltage = new ModbusReadLongChannel("Cell184Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15B8,
								batteryCell185Voltage = new ModbusReadLongChannel("Cell185Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15B9,
								batteryCell186Voltage = new ModbusReadLongChannel("Cell186Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15BA,
								batteryCell187Voltage = new ModbusReadLongChannel("Cell187Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15BB,
								batteryCell188Voltage = new ModbusReadLongChannel("Cell188Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15BC,
								batteryCell189Voltage = new ModbusReadLongChannel("Cell189Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15BD,
								batteryCell190Voltage = new ModbusReadLongChannel("Cell190Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15BE,
								batteryCell191Voltage = new ModbusReadLongChannel("Cell191Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15BF,
								batteryCell192Voltage = new ModbusReadLongChannel("Cell192Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15C0,
								batteryCell193Voltage = new ModbusReadLongChannel("Cell193Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15C1,
								batteryCell194Voltage = new ModbusReadLongChannel("Cell194Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15C2,
								batteryCell195Voltage = new ModbusReadLongChannel("Cell195Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15C3,
								batteryCell196Voltage = new ModbusReadLongChannel("Cell196Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15C4,
								batteryCell197Voltage = new ModbusReadLongChannel("Cell197Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15C5,
								batteryCell198Voltage = new ModbusReadLongChannel("Cell198Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15C6,
								batteryCell199Voltage = new ModbusReadLongChannel("Cell199Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15C7,
								batteryCell200Voltage = new ModbusReadLongChannel("Cell200Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15C8,
								batteryCell201Voltage = new ModbusReadLongChannel("Cell201Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15C9,
								batteryCell202Voltage = new ModbusReadLongChannel("Cell202Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15CA,
								batteryCell203Voltage = new ModbusReadLongChannel("Cell203Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15CB,
								batteryCell204Voltage = new ModbusReadLongChannel("Cell204Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15CC,
								batteryCell205Voltage = new ModbusReadLongChannel("Cell205Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15CD,
								batteryCell206Voltage = new ModbusReadLongChannel("Cell206Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15CE,
								batteryCell207Voltage = new ModbusReadLongChannel("Cell207Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15CF,
								batteryCell208Voltage = new ModbusReadLongChannel("Cell208Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15D0,
								batteryCell209Voltage = new ModbusReadLongChannel("Cell209Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15D1,
								batteryCell210Voltage = new ModbusReadLongChannel("Cell210Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15D2,
								batteryCell211Voltage = new ModbusReadLongChannel("Cell211Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15D3,
								batteryCell212Voltage = new ModbusReadLongChannel("Cell212Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15D4,
								batteryCell213Voltage = new ModbusReadLongChannel("Cell213Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15D5,
								batteryCell214Voltage = new ModbusReadLongChannel("Cell214Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15D6,
								batteryCell215Voltage = new ModbusReadLongChannel("Cell215Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15D7,
								batteryCell216Voltage = new ModbusReadLongChannel("Cell216Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15D8,
								batteryCell217Voltage = new ModbusReadLongChannel("Cell217Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15D9,
								batteryCell218Voltage = new ModbusReadLongChannel("Cell218Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15DA,
								batteryCell219Voltage = new ModbusReadLongChannel("Cell219Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15DB,
								batteryCell220Voltage = new ModbusReadLongChannel("Cell220Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15DC,
								batteryCell221Voltage = new ModbusReadLongChannel("Cell221Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15DD,
								batteryCell222Voltage = new ModbusReadLongChannel("Cell222Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15DE,
								batteryCell223Voltage = new ModbusReadLongChannel("Cell223Voltage", this).unit("mV")
								),//
						new UnsignedWordElement(0x15DF,
								batteryCell224Voltage = new ModbusReadLongChannel("Cell224Voltage", this).unit("mV")
								)));

	}

	@Override
	public StaticValueChannel<Long> capacity() {
		return capacity;
	}

	@Override
	public ThingStateChannel getStateChannel() {
		return thingState;
	}

	// @IsChannel(id = "BatteryAccumulatedCharge")
	// public final ModbusReadChannel _batteryAccumulatedCharge = new OldModbusChannelBuilder().nature(this).unit("Wh")
	// .build();
	// @IsChannel(id = "BatteryAccumulatedDischarge")
	// public final ModbusReadChannel _batteryAccumulatedDischarge = new
	// OldModbusChannelBuilder().nature(this).unit("Wh")
	// .build();
	// @IsChannel(id = "BatteryChargeCycles")
	// public final ModbusReadChannel _batteryChargeCycles = new OldModbusChannelBuilder().nature(this).build();

	// @IsChannel(id = "BatteryPower")
	// public final ModbusReadChannel _batteryPower = new
	// OldModbusChannelBuilder().nature(this).unit("W").multiplier(100)
	// .build();
	// @IsChannel(id = "BatteryStringTotalCurrent")
	// public final ModbusReadChannel _batteryStringTotalCurrent = new OldModbusChannelBuilder().nature(this).unit("mA")
	// .multiplier(100).build();
	// @IsChannel(id = "BatteryStringAbnormity1")
	// public final ModbusReadChannel _batteryStringAbnormity1 = new OldModbusChannelBuilder().nature(this) //
	// .label(4, "Battery string voltage sampling route invalidation") //
	// .label(16, "Battery string voltage sampling route disconnected") //
	// .label(32, "Battery string temperature sampling route disconnected") //
	// .label(64, "Battery string inside CAN disconnected") //
	// .label(512, "Battery string current sampling circuit abnormity") //
	// .label(1024, "Battery string battery cell invalidation") //
	// .label(2048, "Battery string main contactor inspection abnormity") //
	// .label(4096, "Battery string precharge contactor inspection abnormity") //
	// .label(8192, "Battery string negative contactor inspection abnormity") //
	// .label(16384, "Battery string power supply relay inspection abnormity")//
	// .label(132768, "Battery string middle relay abnormity").build();
	// @IsChannel(id = "BatteryStringAbnormity2")
	// public final ModbusReadChannel _batteryStringAbnormity2 = new OldModbusChannelBuilder().nature(this) //
	// .label(4, "Battery string severe overtemperature") //
	// .label(128, "Battery string smog fault") //
	// .label(256, "Battery string blown fuse indicator fault") //
	// .label(1024, "Battery string general leakage") //
	// .label(2048, "Battery string severe leakage") //
	// .label(4096, "Communication between BECU and periphery CAN disconnected") //
	// .label(16384, "Battery string power supply relay contactor disconnected").build();
	// @IsChannel(id = "BatteryStringCellAverageTemperature")
	// public final ModbusReadChannel _batteryStringCellAverageTemperature = new OldModbusChannelBuilder().nature(this)
	// .unit("�C").multiplier(100).build();
	// @IsChannel(id = "BatteryStringChargeCurrentLimit")
	// public final ModbusReadChannel _batteryStringChargeCurrentLimit = new OldModbusChannelBuilder().nature(this)
	// .unit("mA").multiplier(100).build();
	// @IsChannel(id = "BatteryStringDischargeCurrentLimit")
	// public final ModbusReadChannel _batteryStringDischargeCurrentLimit = new OldModbusChannelBuilder().nature(this)
	// .unit("mA").multiplier(100).build();
	// @IsChannel(id = "BatteryStringPeripheralIoState")
	// public final ModbusReadChannel _batteryStringPeripheralIoState = new OldModbusChannelBuilder().nature(this)
	// .label(1, "Fuse state") //
	// .label(2, "Isolated switch state").build();
	// @IsChannel(id = "BatteryStringSOH")
	// public final ModbusReadChannel _batteryStringSOH = new OldModbusChannelBuilder().nature(this).unit("%")
	// .multiplier(100).build();
	// @IsChannel(id = "BatteryStringSuggestiveInformation")
	// public final ModbusReadChannel _batteryStringSuggestiveInformation = new OldModbusChannelBuilder().nature(this)
	// .label(1, "Battery string charge general overcurrent") //
	// .label(2, "Battery string discharge general overcurrent") //
	// .label(4, "Battery string charge current over limit") //
	// .label(8, "Battery string discharge current over limit") //
	// .label(16, "Battery string general overvoltage") //
	// .label(32, "Battery string general undervoltage") //
	// .label(128, "Battery string general over temperature") //
	// .label(256, "Battery string general under temperature") //
	// .label(1024, "Battery string severe overvoltage") //
	// .label(2048, "Battery string severe under voltage") //
	// .label(4096, "Battery string severe under temperature") //
	// .label(8192, "Battery string charge severe overcurrent") //
	// .label(16384, "Battery string discharge severe overcurrent")//
	// .label(132768, "Battery string capacity abnormity").build();

	// @IsChannel(id = "BatteryStringTotalVoltage")
	// public final ModbusReadChannel _batteryStringTotalVoltage = new OldModbusChannelBuilder().nature(this).unit("mV")
	// .multiplier(100).build();
	// @IsChannel(id = "BatteryStringWorkState")
	// public final ModbusReadChannel _batteryStringWorkState = new OldModbusChannelBuilder().nature(this) //
	// .label(1, "Initial") //
	// .label(2, "Stop") //
	// .label(4, "Starting up") //
	// .label(8, "Running") //
	// .label(16, "Fault").build();

	// private final OldConfigChannel _minSoc = new OldConfigChannelBuilder().nature(this).defaultValue(DEFAULT_MINSOC)
	// .percentType().build();

	// @IsChannel(id = "Abnormity1")

	// @IsChannel(id = "SwitchState")
	// public final ModbusReadChannel _switchState = new OldModbusChannelBuilder().nature(this) //
	// .label(2, "DC main contactor state") //
	// .label(4, "DC precharge contactor state") //
	// .label(8, "AC breaker state") //
	// .label(16, "AC main contactor state") //
	// .label(32, "AC precharge contactor state").build();

	// @IsChannel(id = "TotalDateEnergy")
	// public final ModbusReadChannel _totalDateEnergy = new OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalEnergy")
	// public final ModbusReadChannel _totalEnergy = new OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy0")
	// public final ModbusReadChannel _totalHourEnergy0 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy1")
	// public final ModbusReadChannel _totalHourEnergy1 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy10")
	// public final ModbusReadChannel _totalHourEnergy10 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy11")
	// public final ModbusReadChannel _totalHourEnergy11 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy12")
	// public final ModbusReadChannel _totalHourEnergy12 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy13")
	// public final ModbusReadChannel _totalHourEnergy13 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy14")
	// public final ModbusReadChannel _totalHourEnergy14 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy15")
	// public final ModbusReadChannel _totalHourEnergy15 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy16")
	// public final ModbusReadChannel _totalHourEnergy16 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy17")
	// public final ModbusReadChannel _totalHourEnergy17 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy18")
	// public final ModbusReadChannel _totalHourEnergy18 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy19")
	// public final ModbusReadChannel _totalHourEnergy19 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy2")
	// public final ModbusReadChannel _totalHourEnergy2 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy20")
	// public final ModbusReadChannel _totalHourEnergy20 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy21")
	// public final ModbusReadChannel _totalHourEnergy21 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy22")
	// public final ModbusReadChannel _totalHourEnergy22 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy23")
	// public final ModbusReadChannel _totalHourEnergy23 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy3")
	// public final ModbusReadChannel _totalHourEnergy3 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy4")
	// public final ModbusReadChannel _totalHourEnergy4 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy5")
	// public final ModbusReadChannel _totalHourEnergy5 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy6")
	// public final ModbusReadChannel _totalHourEnergy6 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy7")
	// public final ModbusReadChannel _totalHourEnergy7 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy8")
	// public final ModbusReadChannel _totalHourEnergy8 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalHourEnergy9")
	// public final ModbusReadChannel _totalHourEnergy9 = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalMonthEnergy")
	// public final ModbusReadChannel _totalMonthEnergy = new
	// OldModbusChannelBuilder().nature(this).unit("kWh").build();
	// @IsChannel(id = "TotalYearEnergy")
	// public final ModbusReadChannel _totalYearEnergy = new OldModbusChannelBuilder().nature(this).unit("kWh").build();

	// @IsChannel(id = "MaxVoltageCellNo")
	// public final ModbusReadChannel _maxVoltageCellNo = new OldModbusChannelBuilder().nature(this).build();
	// @IsChannel(id = "MaxVoltageCellVoltage")
	// public final ModbusReadChannel _maxVoltageCellVoltage = new OldModbusChannelBuilder().nature(this).unit("mV")
	// .build();
	// @IsChannel(id = "MaxVoltageCellTemp")
	// public final ModbusReadChannel _maxVoltageCellTemp = new
	// OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "MinVoltageCellNo")
	// public final ModbusReadChannel _minVoltageCellNo = new OldModbusChannelBuilder().nature(this).build();
	// @IsChannel(id = "MinVoltageCellVoltage")
	// public final ModbusReadChannel _minVoltageCellVoltage = new OldModbusChannelBuilder().nature(this).unit("mV")
	// .build();
	// @IsChannel(id = "MinVoltageCellTemp")
	// public final ModbusReadChannel _minVoltageCellTemp = new
	// OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "MaxTempCellNo")
	// public final ModbusReadChannel _maxTempCellNo = new OldModbusChannelBuilder().nature(this).build();
	// @IsChannel(id = "MaxTempCellVoltage")
	// public final ModbusReadChannel _maxTempCellVoltage = new
	// OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "MaxTempCellTemp")
	// public final ModbusReadChannel _maxTempCellTemp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "MinTempCellNo")
	// public final ModbusReadChannel _minTempCellNo = new OldModbusChannelBuilder().nature(this).build();
	// @IsChannel(id = "MinTempCellVoltage")
	// public final ModbusReadChannel _minTempCellVoltage = new
	// OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "MinTempCellTemp")
	// public final ModbusReadChannel _minTempCellTemp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell1Voltage")
	// public final ModbusReadChannel _cell1Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell2Voltage")
	// public final ModbusReadChannel _cell2Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell3Voltage")
	// public final ModbusReadChannel _cell3Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell4Voltage")
	// public final ModbusReadChannel _cell4Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell5Voltage")
	// public final ModbusReadChannel _cell5Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell6Voltage")
	// public final ModbusReadChannel _cell6Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell7Voltage")
	// public final ModbusReadChannel _cell7Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell8Voltage")
	// public final ModbusReadChannel _cell8Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell9Voltage")
	// public final ModbusReadChannel _cell9Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell10Voltage")
	// public final ModbusReadChannel _cell10Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell11Voltage")
	// public final ModbusReadChannel _cell11Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell12Voltage")
	// public final ModbusReadChannel _cell12Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell13Voltage")
	// public final ModbusReadChannel _cell13Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell14Voltage")
	// public final ModbusReadChannel _cell14Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell15Voltage")
	// public final ModbusReadChannel _cell15Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell16Voltage")
	// public final ModbusReadChannel _cell16Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell17Voltage")
	// public final ModbusReadChannel _cell17Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell18Voltage")
	// public final ModbusReadChannel _cell18Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell19Voltage")
	// public final ModbusReadChannel _cell19Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell20Voltage")
	// public final ModbusReadChannel _cell20Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell21Voltage")
	// public final ModbusReadChannel _cell21Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell22Voltage")
	// public final ModbusReadChannel _cell22Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell23Voltage")
	// public final ModbusReadChannel _cell23Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell24Voltage")
	// public final ModbusReadChannel _cell24Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell25Voltage")
	// public final ModbusReadChannel _cell25Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell26Voltage")
	// public final ModbusReadChannel _cell26Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell27Voltage")
	// public final ModbusReadChannel _cell27Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell28Voltage")
	// public final ModbusReadChannel _cell28Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell29Voltage")
	// public final ModbusReadChannel _cell29Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell30Voltage")
	// public final ModbusReadChannel _cell30Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell31Voltage")
	// public final ModbusReadChannel _cell31Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell32Voltage")
	// public final ModbusReadChannel _cell32Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell33Voltage")
	// public final ModbusReadChannel _cell33Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell34Voltage")
	// public final ModbusReadChannel _cell34Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell35Voltage")
	// public final ModbusReadChannel _cell35Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell36Voltage")
	// public final ModbusReadChannel _cell36Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell37Voltage")
	// public final ModbusReadChannel _cell37Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell38Voltage")
	// public final ModbusReadChannel _cell38Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell39Voltage")
	// public final ModbusReadChannel _cell39Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell40Voltage")
	// public final ModbusReadChannel _cell40Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell41Voltage")
	// public final ModbusReadChannel _cell41Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell42Voltage")
	// public final ModbusReadChannel _cell42Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell43Voltage")
	// public final ModbusReadChannel _cell43Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell44Voltage")
	// public final ModbusReadChannel _cell44Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell45Voltage")
	// public final ModbusReadChannel _cell45Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell46Voltage")
	// public final ModbusReadChannel _cell46Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell47Voltage")
	// public final ModbusReadChannel _cell47Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell48Voltage")
	// public final ModbusReadChannel _cell48Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell49Voltage")
	// public final ModbusReadChannel _cell49Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell50Voltage")
	// public final ModbusReadChannel _cell50Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell51Voltage")
	// public final ModbusReadChannel _cell51Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell52Voltage")
	// public final ModbusReadChannel _cell52Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell53Voltage")
	// public final ModbusReadChannel _cell53Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell54Voltage")
	// public final ModbusReadChannel _cell54Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell55Voltage")
	// public final ModbusReadChannel _cell55Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell56Voltage")
	// public final ModbusReadChannel _cell56Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell57Voltage")
	// public final ModbusReadChannel _cell57Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell58Voltage")
	// public final ModbusReadChannel _cell58Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell59Voltage")
	// public final ModbusReadChannel _cell59Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell60Voltage")
	// public final ModbusReadChannel _cell60Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell61Voltage")
	// public final ModbusReadChannel _cell61Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell62Voltage")
	// public final ModbusReadChannel _cell62Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell63Voltage")
	// public final ModbusReadChannel _cell63Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	// @IsChannel(id = "Cell64Voltage")
	// public final ModbusReadChannel _cell64Voltage = new OldModbusChannelBuilder().nature(this).unit("mV").build();
	//
	// @IsChannel(id = "Cell1Temp")
	// public final ModbusReadChannel _cell1Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell2Temp")
	// public final ModbusReadChannel _cell2Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell3Temp")
	// public final ModbusReadChannel _cell3Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell4Temp")
	// public final ModbusReadChannel _cell4Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell5Temp")
	// public final ModbusReadChannel _cell5Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell6Temp")
	// public final ModbusReadChannel _cell6Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell7Temp")
	// public final ModbusReadChannel _cell7Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell8Temp")
	// public final ModbusReadChannel _cell8Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell9Temp")
	// public final ModbusReadChannel _cell9Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell10Temp")
	// public final ModbusReadChannel _cell10Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell11Temp")
	// public final ModbusReadChannel _cell11Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell12Temp")
	// public final ModbusReadChannel _cell12Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell13Temp")
	// public final ModbusReadChannel _cell13Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell14Temp")
	// public final ModbusReadChannel _cell14Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell15Temp")
	// public final ModbusReadChannel _cell15Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell16Temp")
	// public final ModbusReadChannel _cell16Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell17Temp")
	// public final ModbusReadChannel _cell17Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell18Temp")
	// public final ModbusReadChannel _cell18Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell19Temp")
	// public final ModbusReadChannel _cell19Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell20Temp")
	// public final ModbusReadChannel _cell20Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell21Temp")
	// public final ModbusReadChannel _cell21Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell22Temp")
	// public final ModbusReadChannel _cell22Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell23Temp")
	// public final ModbusReadChannel _cell23Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell24Temp")
	// public final ModbusReadChannel _cell24Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell25Temp")
	// public final ModbusReadChannel _cell25Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell26Temp")
	// public final ModbusReadChannel _cell26Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell27Temp")
	// public final ModbusReadChannel _cell27Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell28Temp")
	// public final ModbusReadChannel _cell28Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell29Temp")
	// public final ModbusReadChannel _cell29Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell30Temp")
	// public final ModbusReadChannel _cell30Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell31Temp")
	// public final ModbusReadChannel _cell31Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell32Temp")
	// public final ModbusReadChannel _cell32Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell33Temp")
	// public final ModbusReadChannel _cell33Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell34Temp")
	// public final ModbusReadChannel _cell34Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell35Temp")
	// public final ModbusReadChannel _cell35Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell36Temp")
	// public final ModbusReadChannel _cell36Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell37Temp")
	// public final ModbusReadChannel _cell37Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell38Temp")
	// public final ModbusReadChannel _cell38Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell39Temp")
	// public final ModbusReadChannel _cell39Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell40Temp")
	// public final ModbusReadChannel _cell40Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell41Temp")
	// public final ModbusReadChannel _cell41Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell42Temp")
	// public final ModbusReadChannel _cell42Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell43Temp")
	// public final ModbusReadChannel _cell43Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell44Temp")
	// public final ModbusReadChannel _cell44Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell45Temp")
	// public final ModbusReadChannel _cell45Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell46Temp")
	// public final ModbusReadChannel _cell46Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell47Temp")
	// public final ModbusReadChannel _cell47Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell48Temp")
	// public final ModbusReadChannel _cell48Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell49Temp")
	// public final ModbusReadChannel _cell49Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell50Temp")
	// public final ModbusReadChannel _cell50Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell51Temp")
	// public final ModbusReadChannel _cell51Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell52Temp")
	// public final ModbusReadChannel _cell52Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell53Temp")
	// public final ModbusReadChannel _cell53Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell54Temp")
	// public final ModbusReadChannel _cell54Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell55Temp")
	// public final ModbusReadChannel _cell55Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell56Temp")
	// public final ModbusReadChannel _cell56Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell57Temp")
	// public final ModbusReadChannel _cell57Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell58Temp")
	// public final ModbusReadChannel _cell58Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell59Temp")
	// public final ModbusReadChannel _cell59Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell60Temp")
	// public final ModbusReadChannel _cell60Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell61Temp")
	// public final ModbusReadChannel _cell61Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell62Temp")
	// public final ModbusReadChannel _cell62Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell63Temp")
	// public final ModbusReadChannel _cell63Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();
	// @IsChannel(id = "Cell64Temp")
	// public final ModbusReadChannel _cell64Temp = new OldModbusChannelBuilder().nature(this).unit("�C").build();

	// @Override
	// protected ModbusProtocol defineModbusProtocol() throws ConfigException {
	//

	// new ModbusRange(0x0300, //
	// new ElementBuilder().address(0x0300).channel(_totalEnergy).doubleword().build(),
	// new ElementBuilder().address(0x0302).channel(_totalYearEnergy).doubleword().build(),
	// new ElementBuilder().address(0x0304).channel(_totalMonthEnergy).doubleword().build(),
	// new ElementBuilder().address(0x0306).channel(_totalDateEnergy).build(),
	// new ElementBuilder().address(0x0307).channel(_totalHourEnergy0).build(),
	// new ElementBuilder().address(0x0308).channel(_totalHourEnergy1).build(),
	// new ElementBuilder().address(0x0309).channel(_totalHourEnergy2).build(),
	// new ElementBuilder().address(0x030A).channel(_totalHourEnergy3).build(),
	// new ElementBuilder().address(0x030B).channel(_totalHourEnergy4).build(),
	// new ElementBuilder().address(0x030C).channel(_totalHourEnergy5).build(),
	// new ElementBuilder().address(0x030D).channel(_totalHourEnergy6).build(),
	// new ElementBuilder().address(0x030E).channel(_totalHourEnergy7).build(),
	// new ElementBuilder().address(0x030F).channel(_totalHourEnergy8).build(),
	// new ElementBuilder().address(0x0310).channel(_totalHourEnergy9).build(),
	// new ElementBuilder().address(0x0311).channel(_totalHourEnergy10).build(),
	// new ElementBuilder().address(0x0312).channel(_totalHourEnergy11).build(),
	// new ElementBuilder().address(0x0313).channel(_totalHourEnergy12).build(),
	// new ElementBuilder().address(0x0314).channel(_totalHourEnergy13).build(),
	// new ElementBuilder().address(0x0315).channel(_totalHourEnergy14).build(),
	// new ElementBuilder().address(0x0316).channel(_totalHourEnergy15).build(),
	// new ElementBuilder().address(0x0317).channel(_totalHourEnergy16).build(),
	// new ElementBuilder().address(0x0318).channel(_totalHourEnergy17).build(),
	// new ElementBuilder().address(0x0319).channel(_totalHourEnergy18).build(),
	// new ElementBuilder().address(0x031A).channel(_totalHourEnergy19).build(),
	// new ElementBuilder().address(0x031B).channel(_totalHourEnergy20).build(),
	// new ElementBuilder().address(0x031C).channel(_totalHourEnergy21).build(),
	// new ElementBuilder().address(0x031D).channel(_totalHourEnergy22).build(),
	// new ElementBuilder().address(0x031E).channel(_totalHourEnergy23).build()),

	// new ModbusRange(0x1100, //
	// new ElementBuilder().address(0x1100).channel(_batteryStringWorkState).build(),
	// new ElementBuilder().address(0x1101).channel(_batteryStringSwitchState).build(),
	// new ElementBuilder().address(0x1102).channel(_batteryStringPeripheralIoState).build(),
	// new ElementBuilder().address(0x1103).channel(_batteryStringSuggestiveInformation).build(),
	// new ElementBuilder().address(0x1104).dummy().build(),
	// new ElementBuilder().address(0x1105).channel(_batteryStringAbnormity1).build(),
	// new ElementBuilder().address(0x1106).channel(_batteryStringAbnormity2).build()),
	// new ModbusRange(0x1400, //
	// new ElementBuilder().address(0x1400).channel(_batteryStringTotalVoltage).build(),
	// new ElementBuilder().address(0x1401).channel(_batteryStringTotalCurrent).signed().build(),
	// new ElementBuilder().address(0x1402).channel(_soc).build(),
	// new ElementBuilder().address(0x1403).channel(_batteryStringSOH).build(),
	// new ElementBuilder().address(0x1404).channel(_batteryStringCellAverageTemperature).signed()
	// .build(),
	// new ElementBuilder().address(0x1405).dummy().build(),
	// new ElementBuilder().address(0x1406).channel(_batteryStringChargeCurrentLimit).build(),
	// new ElementBuilder().address(0x1407).channel(_batteryStringDischargeCurrentLimit).build(),
	// new ElementBuilder().address(0x1408).dummy(0x140A - 0x1408).build(),
	// new ElementBuilder().address(0x140A).channel(_batteryChargeCycles).doubleword().build(),
	// new ElementBuilder().address(0x140C).dummy(0x1418 - 0x140C).build(),
	// new ElementBuilder().address(0x1418).channel(_batteryAccumulatedCharge).doubleword().build(),
	// new ElementBuilder().address(0x141A).channel(_batteryAccumulatedDischarge).doubleword().build(),
	// new ElementBuilder().address(0x141C).dummy(0x1420 - 0x141C).build(),
	// new ElementBuilder().address(0x1420).channel(_batteryPower).signed().build(),
	// new ElementBuilder().address(0x1421).dummy(0x1430 - 0x1421).build(),
	// new ElementBuilder().address(0x1430).channel(_maxVoltageCellNo).build(),
	// new ElementBuilder().address(0x1431).channel(_maxVoltageCellVoltage).build(),
	// new ElementBuilder().address(0x1432).channel(_maxVoltageCellTemp).signed().build(),
	// new ElementBuilder().address(0x1433).channel(_minVoltageCellNo).build(),
	// new ElementBuilder().address(0x1434).channel(_minVoltageCellVoltage).build(),
	// new ElementBuilder().address(0x1435).channel(_minVoltageCellTemp).signed().build(),
	// new ElementBuilder().address(0x1436).dummy(0x143A - 0x1436).build(),
	// new ElementBuilder().address(0x143A).channel(_maxTempCellNo).build(),
	// new ElementBuilder().address(0x143B).channel(_maxTempCellTemp).signed().build(),
	// new ElementBuilder().address(0x143C).channel(_maxTempCellVoltage).build(),
	// new ElementBuilder().address(0x143D).channel(_minTempCellNo).build(),
	// new ElementBuilder().address(0x143E).channel(_minTempCellTemp).signed().build(),
	// new ElementBuilder().address(0x143F).channel(_minTempCellVoltage).build()), //
	// new ModbusRange(0x1500, new ElementBuilder().address(0x1500).channel(_cell1Voltage).build(),
	// new ElementBuilder().address(0x1501).channel(_cell2Voltage).build(),
	// new ElementBuilder().address(0x1502).channel(_cell3Voltage).build(),
	// new ElementBuilder().address(0x1503).channel(_cell4Voltage).build(),
	// new ElementBuilder().address(0x1504).channel(_cell5Voltage).build(),
	// new ElementBuilder().address(0x1505).channel(_cell6Voltage).build(),
	// new ElementBuilder().address(0x1506).channel(_cell7Voltage).build(),
	// new ElementBuilder().address(0x1507).channel(_cell8Voltage).build(),
	// new ElementBuilder().address(0x1508).channel(_cell9Voltage).build(),
	// new ElementBuilder().address(0x1509).channel(_cell10Voltage).build(),
	// new ElementBuilder().address(0x150a).channel(_cell11Voltage).build(),
	// new ElementBuilder().address(0x150b).channel(_cell12Voltage).build(),
	// new ElementBuilder().address(0x150c).channel(_cell13Voltage).build(),
	// new ElementBuilder().address(0x150d).channel(_cell14Voltage).build(),
	// new ElementBuilder().address(0x150e).channel(_cell15Voltage).build(),
	// new ElementBuilder().address(0x150f).channel(_cell16Voltage).build(),
	// new ElementBuilder().address(0x1510).channel(_cell17Voltage).build(),
	// new ElementBuilder().address(0x1511).channel(_cell18Voltage).build(),
	// new ElementBuilder().address(0x1512).channel(_cell19Voltage).build(),
	// new ElementBuilder().address(0x1513).channel(_cell20Voltage).build(),
	// new ElementBuilder().address(0x1514).channel(_cell21Voltage).build(),
	// new ElementBuilder().address(0x1515).channel(_cell22Voltage).build(),
	// new ElementBuilder().address(0x1516).channel(_cell23Voltage).build(),
	// new ElementBuilder().address(0x1517).channel(_cell24Voltage).build(),
	// new ElementBuilder().address(0x1518).channel(_cell25Voltage).build(),
	// new ElementBuilder().address(0x1519).channel(_cell26Voltage).build(),
	// new ElementBuilder().address(0x151a).channel(_cell27Voltage).build(),
	// new ElementBuilder().address(0x151b).channel(_cell28Voltage).build(),
	// new ElementBuilder().address(0x151c).channel(_cell29Voltage).build(),
	// new ElementBuilder().address(0x151d).channel(_cell30Voltage).build(),
	// new ElementBuilder().address(0x151e).channel(_cell31Voltage).build(),
	// new ElementBuilder().address(0x151f).channel(_cell32Voltage).build(),
	// new ElementBuilder().address(0x1520).channel(_cell33Voltage).build(),
	// new ElementBuilder().address(0x1521).channel(_cell34Voltage).build(),
	// new ElementBuilder().address(0x1522).channel(_cell35Voltage).build(),
	// new ElementBuilder().address(0x1523).channel(_cell36Voltage).build(),
	// new ElementBuilder().address(0x1524).channel(_cell37Voltage).build(),
	// new ElementBuilder().address(0x1525).channel(_cell38Voltage).build(),
	// new ElementBuilder().address(0x1526).channel(_cell39Voltage).build(),
	// new ElementBuilder().address(0x1527).channel(_cell40Voltage).build(),
	// new ElementBuilder().address(0x1528).channel(_cell41Voltage).build(),
	// new ElementBuilder().address(0x1529).channel(_cell42Voltage).build(),
	// new ElementBuilder().address(0x152a).channel(_cell43Voltage).build(),
	// new ElementBuilder().address(0x152b).channel(_cell44Voltage).build(),
	// new ElementBuilder().address(0x152c).channel(_cell45Voltage).build(),
	// new ElementBuilder().address(0x152d).channel(_cell46Voltage).build(),
	// new ElementBuilder().address(0x152e).channel(_cell47Voltage).build(),
	// new ElementBuilder().address(0x152f).channel(_cell48Voltage).build(),
	// new ElementBuilder().address(0x1530).channel(_cell49Voltage).build(),
	// new ElementBuilder().address(0x1531).channel(_cell50Voltage).build(),
	// new ElementBuilder().address(0x1532).channel(_cell51Voltage).build(),
	// new ElementBuilder().address(0x1533).channel(_cell52Voltage).build(),
	// new ElementBuilder().address(0x1534).channel(_cell53Voltage).build(),
	// new ElementBuilder().address(0x1535).channel(_cell54Voltage).build(),
	// new ElementBuilder().address(0x1536).channel(_cell55Voltage).build(),
	// new ElementBuilder().address(0x1537).channel(_cell56Voltage).build(),
	// new ElementBuilder().address(0x1538).channel(_cell57Voltage).build(),
	// new ElementBuilder().address(0x1539).channel(_cell58Voltage).build(),
	// new ElementBuilder().address(0x153a).channel(_cell59Voltage).build(),
	// new ElementBuilder().address(0x153b).channel(_cell60Voltage).build(),
	// new ElementBuilder().address(0x153c).channel(_cell61Voltage).build(),
	// new ElementBuilder().address(0x153d).channel(_cell62Voltage).build(),
	// new ElementBuilder().address(0x153e).channel(_cell63Voltage).build(),
	// new ElementBuilder().address(0x153f).channel(_cell64Voltage).build()),
	// new ModbusRange(0x1700, //
	// new ElementBuilder().address(0x1700).channel(_cell1Temp).build(),
	// new ElementBuilder().address(0x1701).channel(_cell2Temp).build(),
	// new ElementBuilder().address(0x1702).channel(_cell3Temp).build(),
	// new ElementBuilder().address(0x1703).channel(_cell4Temp).build(),
	// new ElementBuilder().address(0x1704).channel(_cell5Temp).build(),
	// new ElementBuilder().address(0x1705).channel(_cell6Temp).build(),
	// new ElementBuilder().address(0x1706).channel(_cell7Temp).build(),
	// new ElementBuilder().address(0x1707).channel(_cell8Temp).build(),
	// new ElementBuilder().address(0x1708).channel(_cell9Temp).build(),
	// new ElementBuilder().address(0x1709).channel(_cell10Temp).build(),
	// new ElementBuilder().address(0x170a).channel(_cell11Temp).build(),
	// new ElementBuilder().address(0x170b).channel(_cell12Temp).build(),
	// new ElementBuilder().address(0x170c).channel(_cell13Temp).build(),
	// new ElementBuilder().address(0x170d).channel(_cell14Temp).build(),
	// new ElementBuilder().address(0x170e).channel(_cell15Temp).build(),
	// new ElementBuilder().address(0x170f).channel(_cell16Temp).build(),
	// new ElementBuilder().address(0x1710).channel(_cell17Temp).build(),
	// new ElementBuilder().address(0x1711).channel(_cell18Temp).build(),
	// new ElementBuilder().address(0x1712).channel(_cell19Temp).build(),
	// new ElementBuilder().address(0x1713).channel(_cell20Temp).build(),
	// new ElementBuilder().address(0x1714).channel(_cell21Temp).build(),
	// new ElementBuilder().address(0x1715).channel(_cell22Temp).build(),
	// new ElementBuilder().address(0x1716).channel(_cell23Temp).build(),
	// new ElementBuilder().address(0x1717).channel(_cell24Temp).build(),
	// new ElementBuilder().address(0x1718).channel(_cell25Temp).build(),
	// new ElementBuilder().address(0x1719).channel(_cell26Temp).build(),
	// new ElementBuilder().address(0x171a).channel(_cell27Temp).build(),
	// new ElementBuilder().address(0x171b).channel(_cell28Temp).build(),
	// new ElementBuilder().address(0x171c).channel(_cell29Temp).build(),
	// new ElementBuilder().address(0x171d).channel(_cell30Temp).build(),
	// new ElementBuilder().address(0x171e).channel(_cell31Temp).build(),
	// new ElementBuilder().address(0x171f).channel(_cell32Temp).build(),
	// new ElementBuilder().address(0x1720).channel(_cell33Temp).build(),
	// new ElementBuilder().address(0x1721).channel(_cell34Temp).build(),
	// new ElementBuilder().address(0x1722).channel(_cell35Temp).build(),
	// new ElementBuilder().address(0x1723).channel(_cell36Temp).build(),
	// new ElementBuilder().address(0x1724).channel(_cell37Temp).build(),
	// new ElementBuilder().address(0x1725).channel(_cell38Temp).build(),
	// new ElementBuilder().address(0x1726).channel(_cell39Temp).build(),
	// new ElementBuilder().address(0x1727).channel(_cell40Temp).build(),
	// new ElementBuilder().address(0x1728).channel(_cell41Temp).build(),
	// new ElementBuilder().address(0x1729).channel(_cell42Temp).build(),
	// new ElementBuilder().address(0x172a).channel(_cell43Temp).build(),
	// new ElementBuilder().address(0x172b).channel(_cell44Temp).build(),
	// new ElementBuilder().address(0x172c).channel(_cell45Temp).build(),
	// new ElementBuilder().address(0x172d).channel(_cell46Temp).build(),
	// new ElementBuilder().address(0x172e).channel(_cell47Temp).build(),
	// new ElementBuilder().address(0x172f).channel(_cell48Temp).build(),
	// new ElementBuilder().address(0x1730).channel(_cell49Temp).build(),
	// new ElementBuilder().address(0x1731).channel(_cell50Temp).build(),
	// new ElementBuilder().address(0x1732).channel(_cell51Temp).build(),
	// new ElementBuilder().address(0x1733).channel(_cell52Temp).build(),
	// new ElementBuilder().address(0x1734).channel(_cell53Temp).build(),
	// new ElementBuilder().address(0x1735).channel(_cell54Temp).build(),
	// new ElementBuilder().address(0x1736).channel(_cell55Temp).build(),
	// new ElementBuilder().address(0x1737).channel(_cell56Temp).build(),
	// new ElementBuilder().address(0x1738).channel(_cell57Temp).build(),
	// new ElementBuilder().address(0x1739).channel(_cell58Temp).build(),
	// new ElementBuilder().address(0x173a).channel(_cell59Temp).build(),
	// new ElementBuilder().address(0x173b).channel(_cell60Temp).build(),
	// new ElementBuilder().address(0x173c).channel(_cell61Temp).build(),
	// new ElementBuilder().address(0x173d).channel(_cell62Temp).build(),
	// new ElementBuilder().address(0x173e).channel(_cell63Temp).build(),
	// new ElementBuilder().address(0x173f).channel(_cell64Temp).build()));
	// }
}
