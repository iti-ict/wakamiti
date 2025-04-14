/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.modbus;

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
import org.slf4j.Logger;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.isBlank;


@Extension(provider = "es.iti.wakamiti", name = "modbus", version = "2.7")
@I18nResource("es_iti_wakamiti_modbus")
public class ModbusStepContributor implements StepContributor {

    private final Logger logger = WakamitiLogger.forClass(ModbusStepContributor.class);

    protected URL baseURL;
    protected int slaveId;

    protected ModbusMaster master;
    int[] registersRead = null;


    @SetUp
    public void createClient() {
        try {
            TcpParameters tcpParameters = new TcpParameters();
            tcpParameters.setHost(InetAddress.getByName(baseURL.getHost()));
            tcpParameters.setPort(baseURL.getPort());
            tcpParameters.setKeepAlive(true);

            master = new ModbusMasterTCP(tcpParameters);
            master.connect();
        } catch (ModbusIOException e) {
            throw new WakamitiException("Cannot connect to modbus server");
        } catch (UnknownHostException e) {
            throw new WakamitiException("Cannot locate host {}", baseURL);
        }

    }

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
     * @param url the base URL to be set.
     */
    @Step(value = "modbus.define.baseURL", args = "url")
    public void setBaseURL(URL url) {
        checkURL(url);
        this.baseURL = url;
    }

    /**
     * Sets the slave id for the connection.
     *
     * @param slaveId the slave id to be set.
     */
    @Step(value = "modbus.define.slaveId", args = "slaveId")
    public void setSlaveId(String slaveId) {
        this.slaveId = Integer.parseInt(slaveId);
    }

    @Step(value = "modbus.execute.read", args = {"quantity", "address"})
    public void executeRead(int quantity, int address) {
        try {
            registersRead = master.readHoldingRegisters(slaveId, address, quantity);
        } catch (Exception e) {
            throw new WakamitiException("Cannot read from modbus server.");
        }
    }

    @Step(value = "modbus.execute.write", args = {"value", "address"})
    public void executeWrite(int value, int address) {
        try {
            master.writeSingleRegister(slaveId, address, value);
        } catch (Exception e) {
            throw new WakamitiException("Cannot write on modbus server.");
        }
    }

    @Step(value = "modbus.assert.read.value", args =  {"value", "address"})
    public void assertReadValue(int value, int address) {
        assertRegistersRead();
        Arrays.stream(registersRead).filter(v -> v == value).findAny()
                .orElseThrow(() -> new WakamitiException("Register {} not found", value));
    }

    protected void checkURL(URL url) {
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