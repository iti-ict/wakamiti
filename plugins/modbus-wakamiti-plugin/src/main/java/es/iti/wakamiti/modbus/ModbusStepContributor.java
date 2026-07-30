/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.modbus;


import static org.apache.commons.lang3.StringUtils.isBlank;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.slf4j.Logger;

import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterTCP;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.SetUp;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.annotations.TearDown;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.util.WakamitiLogger;


/**
 * Step contributor for Modbus TCP read/write operations and assertions.
 * <p>
 * Connection settings are stored as scenario state and used by setup/teardown
 * lifecycle hooks.
 * </p>
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "modbus",
        version = "2.13"
)
@I18nResource("es_iti_wakamiti_modbus")
public class ModbusStepContributor implements StepContributor {

    private static final Logger LOGGER = WakamitiLogger.forClass(ModbusStepContributor.class);

    protected String host;
    protected int port;
    protected int slaveId;

    protected ModbusMaster master;
    int[] registersRead;

    protected void setHost(
            String host
    ) {
        this.host = host;
    }

    protected void setPort(
            Integer port
    ) {
        this.port = port;
    }

    /**
     * Creates and connects the Modbus TCP client before scenario execution.
     * <p>
     * Requires host, port and slave identifier to be already configured.
     * </p>
     *
     * @throws WakamitiException if the configured host cannot be resolved or the
     *         TCP connection cannot be established
     */
    @SetUp
    public void createClient() {
        try {
            LOGGER.debug("Creating Modbus client {}:{}", host, port);
            TcpParameters tcpParameters = new TcpParameters();
            tcpParameters.setHost(InetAddress.getByName(host));
            tcpParameters.setPort(port);
            tcpParameters.setKeepAlive(true);

            master = new ModbusMasterTCP(tcpParameters);
            master.connect();
        } catch (ModbusIOException e) {
            throw new WakamitiException("Cannot connect to modbus server", e);
        } catch (UnknownHostException e) {
            throw new WakamitiException("Cannot locate host {}", host, e);
        }
    }

    /**
     * Disconnects the Modbus TCP client after scenario execution.
     * <p>
     * If no client was created, this method can fail with a
     * {@link NullPointerException}.
     * </p>
     *
     * @throws WakamitiException if the underlying client cannot be disconnected
     */
    @TearDown
    public void destroyClient() {
        try {
            master.disconnect();
        } catch (ModbusIOException e) {
            throw new WakamitiException("Cannot disconnect from modbus server");
        }
    }

    /**
     * Sets the base URL for the connection.
     *
     * @param host the base host to be set.
     * @param port the base port to be set.
     */
    @Step(value = "modbus.define.baseURL", args = {"host:word", "port:int"})
    public void setBaseURL(
            String host,
            Integer port
    ) {
        this.host = host;
        this.port = port;
    }

    /**
     * Sets the slave id for the connection.
     *
     * @param slaveId the slave id to be set.
     */
    @Step(value = "modbus.define.slaveId", args = "slaveId:text")
    public void setSlaveId(
            String slaveId
    ) {
        this.slaveId = Integer.parseInt(slaveId);
    }

    /**
     * Reads a contiguous block of holding registers from the configured slave.
     * <p>
     * The returned register values are retained as scenario state for subsequent
     * assertions made with {@link #assertReadValue(Integer)}.
     *
     * @param quantity number of consecutive registers to read
     * @param address zero-based address of the first holding register
     * @throws WakamitiException if no Modbus response can be obtained
     */
    @Step(value = "modbus.execute.read", args = {"quantity:int", "address:int"})
    public void executeRead(
            Integer quantity,
            Integer address
    ) {
        try {
            registersRead = master.readHoldingRegisters(slaveId, address, quantity);
        } catch (Exception e) {
            throw new WakamitiException("Cannot read from modbus server.");
        }
    }

    /**
     * Writes a single holding-register value to the configured slave.
     *
     * @param value 16-bit register value to write
     * @param address address of the target holding register
     * @throws WakamitiException if the write operation cannot be completed
     */
    @Step(value = "modbus.execute.write", args = {"value:int", "address:int"})
    public void executeWrite(
            Integer value,
            Integer address
    ) {
        try {
            master.writeSingleRegister(slaveId, address, value);
        } catch (Exception e) {
            throw new WakamitiException("Cannot write on modbus server.");
        }
    }

    /**
     * Verifies that the last holding-register read contains an expected value.
     * <p>
     * This assertion checks every register returned by the previous read; it does
     * not require the value to occur at a particular offset.
     * The inspected values come from the in-memory {@code registersRead} buffer
     * populated by the latest {@link #executeRead(Integer, Integer)} call.
     *
     * @param value value expected in at least one of the registers
     * @throws WakamitiException if no read has been performed or the value is absent
     */
    @Step(value = "modbus.assert.read.value", args = {"value:int"})
    public void assertReadValue(
            Integer value
    ) {
        assertRegistersRead();
        Arrays.stream(registersRead).filter(v -> v == value).findAny()
                .orElseThrow(() -> new WakamitiException("Register {} not found", value));
    }

    protected void checkURL(
            URL url
    ) {
        if (!isBlank(url.getQuery())) {
            throw new WakamitiException("Query parameters are not allowed here. Please, use steps for that purpose.");
        }
    }

    protected void assertRegistersRead() {
        if (registersRead == null) {
            throw new WakamitiException("No registers read.");
        }
    }

}
