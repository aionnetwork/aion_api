/*******************************************************************************
 * Copyright (c) 2017-2018 Aion foundation.
 *
 *     This file is part of the aion network project.
 *
 *     The aion network project is free software: you can redistribute it
 *     and/or modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation, either version 3 of
 *     the License, or any later version.
 *
 *     The aion network project is distributed in the hope that it will
 *     be useful, but WITHOUT ANY WARRANTY; without even the implied
 *     warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *     See the GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the aion network project source files.
 *     If not, see <https://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Aion foundation.
 *
 ******************************************************************************/

package org.aion.api.test;

import org.aion.api.IAionAPI;
import org.aion.api.IContract;
import org.aion.api.IUtils;
import org.aion.api.impl.AionAPIImpl;
import org.aion.api.sol.*;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.CompileResponse;
import org.aion.api.type.ContractAbiEntry;
import org.aion.api.type.ContractResponse;
import org.aion.base.type.Address;
import org.aion.base.util.ByteArrayWrapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ContractTests {

    // Make sure the password of the testing account been set properly
    private String pw = "";

    private IAionAPI api;

    @Before
    public void Setup() {
        api = IAionAPI.init();
    }

    @Test(timeout = 180000)
    public void TestCreateContractFromSource() throws Throwable {

        String source = "contract testContract {\n"
                + "    bytes32 public a;\n"
                + "    bytes public b;\n"
                + "    bytes8 public c;\n"
                + "    bytes16 public d;\n"
                + "    \n"
                + "    function input32(bytes32 _a) {\n"
                + "        a = _a;\n"
                + "    }\n"
                + "    \n"
                + "    function input(bytes _b) {\n"
                + "        b = _b;\n"
                + "    }\n"
                + "    \n"
                + "    function input8(bytes8 _c) {\n"
                + "        c = _c;\n"
                + "    }\n"
                + "    \n"
                + "    function input16(bytes16 _d) {\n"
                + "        d = _d;\n"
                + "    }\n"
                + "}";

        // hardcoded msgHash to block 1
        api.connect(AionAPIImpl.LOCALHOST_URL);
        api.getContractController().clear();

        Address cb = ((List<Address>)api.getWallet().getAccounts().getObject()).get(0);
        assertNotNull(cb);
        assertTrue(api.getWallet().unlockAccount(cb, pw).getObject());

        ApiMsg msg = api.getContractController().createFromSource(source, cb, 1_000_000L, 1);
        if (msg.isError()) {
            System.out.println("deploy contract failed! " + msg.getErrString());
        }

        IContract contract = api.getContractController().getContract();
        assertEquals(contract.getFrom(), cb);
        assertNotNull(contract.getContractAddress());
        assertTrue(contract.getAbiDefinition().size() == 8);
        api.destroyApi();
    }


        @Test(timeout = 180000)
        public void TestCreateContract2() throws Throwable {

            String source = "contract ByteArrayMap {\n" + "    mapping(uint128 => bytes) public data;\n"
                    + "    function f() {\n" + "        bytes memory d = new bytes(1024);\n" + "        data[32] = d;\n"
                    + "    }\n" + "    function g() constant returns (bytes) {\n" + "        return data[32];\n"
                    + "    }\n" + "}";

            // hardcoded msgHash to block 1
            String url = IAionAPI.LOCALHOST_URL;
            api.connect(url);
            api.getContractController().clear();

            Address cb = ((List<Address>)api.getWallet().getAccounts().getObject()).get(0);
            assertNotNull(cb);
            assertTrue(api.getWallet().unlockAccount(cb, pw).getObject());

            ApiMsg msg = api.getContractController().createFromSource(source, cb, 1_000_000L, 1);
            if (msg.isError()) {
                System.out.println("deploy contract failed! " + msg.getErrString());
            }

            IContract contract = api.getContractController().getContract();
            assertEquals(contract.getFrom(), cb);
            assertNotNull(contract.getContractAddress());
            assertTrue(contract.getAbiDefinition().size() == 3);

            api.destroyApi();
        }

    @Deprecated
    // TestCallGetPrescription included this testcase purpose
    public void TestCreateContractFromAddress() throws Throwable {

        api.connect(AionAPIImpl.LOCALHOST_URL);
        api.getContractController().clear();

        Address cb = ((List<Address>)api.getWallet().getAccounts().getObject()).get(0);
        assertNotNull(cb);
        assertTrue(api.getWallet().unlockAccount(cb, pw).getObject());

        String abiDefinition = "[\n"
                + "    {\n"
                + "        \"constant\": true,\n"
                + "        \"inputs\": [],\n"
                + "        \"name\": \"val\",\n"
                + "        \"outputs\": [\n"
                + "            {\n"
                + "                \"name\": \"\",\n"
                + "                \"type\": \"uint128\"\n"
                + "            }\n"
                + "        ],\n"
                + "        \"payable\": false,\n"
                + "        \"type\": \"function\"\n"
                + "    },\n"
                + "    {\n"
                + "        \"constant\": true,\n"
                + "        \"inputs\": [],\n"
                + "        \"name\": \"bo\",\n"
                + "        \"outputs\": [\n"
                + "            {\n"
                + "                \"name\": \"\",\n"
                + "                \"type\": \"bool\"\n"
                + "            }\n"
                + "        ],\n"
                + "        \"payable\": false,\n"
                + "        \"type\": \"function\"\n"
                + "    },\n"
                + "    {\n"
                + "        \"constant\": true,\n"
                + "        \"inputs\": [],\n"
                + "        \"name\": \"addr\",\n"
                + "        \"outputs\": [\n"
                + "            {\n"
                + "                \"name\": \"\",\n"
                + "                \"type\": \"address\"\n"
                + "            }\n"
                + "        ],\n"
                + "        \"payable\": false,\n"
                + "        \"type\": \"function\"\n"
                + "    },\n"
                + "    {\n"
                + "        \"constant\": true,\n"
                + "        \"inputs\": [],\n"
                + "        \"name\": \"s\",\n"
                + "        \"outputs\": [\n"
                + "            {\n"
                + "                \"name\": \"\",\n"
                + "                \"type\": \"string\"\n"
                + "            }\n"
                + "        ],\n"
                + "        \"payable\": false,\n"
                + "        \"type\": \"function\"\n"
                + "    },\n"
                + "    {\n"
                + "        \"constant\": false,\n"
                + "        \"inputs\": [\n"
                + "            {\n"
                + "                \"name\": \"_add\",\n"
                + "                \"type\": \"uint128\"\n"
                + "            },\n"
                + "            {\n"
                + "                \"name\": \"_s\",\n"
                + "                \"type\": \"string\"\n"
                + "            },\n"
                + "            {\n"
                + "                \"name\": \"_addr\",\n"
                + "                \"type\": \"address\"\n"
                + "            },\n"
                + "            {\n"
                + "                \"name\": \"_bool\",\n"
                + "                \"type\": \"bool\"\n"
                + "            }\n"
                + "        ],\n"
                + "        \"name\": \"tick\",\n"
                + "        \"outputs\": [],\n"
                + "        \"payable\": false,\n"
                + "        \"type\": \"function\"\n"
                + "    }\n"
                + "]";

        //byte[] contractAddress = IUtils.hex2Bytes("11c32489bf9209fec54d8ca5649ec53cda381c92");
        Address contractAddress = Address.ZERO_ADDRESS();
        IContract ct = api.getContractController().getContractAt(cb, contractAddress, abiDefinition);
        if (ct.error()) {
            System.out.println("deploy contract failed! " + ct.getErrString());
        }

        IContract contract = api.getContractController().getContract();
        assertEquals(contract.getContractAddress(), contractAddress);
        assertTrue(contract.getInputParams().size() == 0);
        assertTrue(contract.getOutputParams().size() == 0);

        api.destroyApi();
    }

    @Test
    public void TestCallWithParameterDecode() throws Throwable {

        String sc = "contract SolTypes{ \n" +
                "    function getValues() constant returns (uint128, bool, address, bytes32, string, int128) {\n" +
                "        return (1234, true,0x1234567890123456789012345678901234567890123456789012345678901234,0x1234567890123456789012345678901234567890123456789012345678901234,\"Nuco!\",-1234);\n" +
                "    }\n" +
                "    \n" +
                "    function boolVal() constant returns (bool) {\n" +
                "        return true;\n" +
                "    }\n" +
                "    \n" +
                "    function addressVal() constant returns (address) {\n" +
                "        return 0x1234567890123456789012345678901234567890;\n" +
                "    }\n" +
                "    \n" +
                "    function stringVal() constant returns (string) {\n" +
                "        return \"1234\";\n" +
                "    }\n" +
                "    \n" +
                "    function setValues() {\n" +
                "        \n" +
                "    }    \n" +
                "    \n" +
                "    function intVal() constant returns (int128) {\n" +
                "        return -1234;\n" +
                "    }    \n" +
                "    \n" +
                "    function uintVal() constant returns (uint128) {\n" +
                "        return 1234;\n" +
                "    }  \n" +
                "    \n" +
                "    function bytes32Val() constant returns (bytes32) {\n" +
                "        return 0x1234567890123456789012345678901234567890123456789012345678901234;\n" +
                "    }      \n" +
                "}";

        api.connect(AionAPIImpl.LOCALHOST_URL);
        api.getContractController().clear();


        Address cb = ((List<Address>)api.getWallet().getAccounts().getObject()).get(0);
        assertNotNull(cb);
        assertTrue(api.getWallet().unlockAccount(cb, pw, 3600).getObject());

        ApiMsg msg = api.getContractController().createFromSource(sc, cb, 1_000_000L, 1);
        if (msg.isError()) {
            System.out.println("deploy contract failed! " + msg.getErrString());
        }

        IContract ct = api.getContractController().getContract();
        assertEquals(ct.getFrom(), cb);
        assertNotNull(ct.getContractAddress());

        ContractResponse cr = ct.newFunction("getValues")
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        assertTrue((Long)cr.getData().get(0) == 1234L);
        assertTrue((boolean)cr.getData().get(1));
        assertEquals(ByteArrayWrapper.wrap((byte[])cr.getData().get(2))
                , ByteArrayWrapper.wrap(Address.wrap("1234567890123456789012345678901234567890123456789012345678901234").toBytes()));
        assertEquals(ByteArrayWrapper.wrap((byte[])cr.getData().get(3))
                , ByteArrayWrapper.wrap(IUtils.hex2Bytes("1234567890123456789012345678901234567890123456789012345678901234")));
        assertTrue(((String)cr.getData().get(4)).contentEquals("Nuco!"));
        assertTrue(((Long)cr.getData().get(5)) == -1234L);

        api.destroyApi();
    }

    @Test
    public void TestCallGetStaticArray() throws Throwable {

        String sc = "contract SolArrayTypes{ \n" +
                " \n" +
                "    bool[4] bo = [true,false,true,false];\n" +
                "    address[4] ad = [0x1111111111111111111111111111111111111111111111111111111111111111,\n" +
                "        0x2222222222222222222222222222222222222222222222222222222222222222,\n" +
                "        0x3333333333333333333333333333333333333333333333333333333333333333,\n" +
                "        0x4444444444444444444444444444444444444444444444444444444444444444];\n" +
                "    uint128[4] ui = [1111,2222,3333,4444];    \n" +
                "    \n" +
                "    function getStaticArray() constant returns (bool[4], address[4], uint128[4]) {\n" +
                "        return (bo,ad,ui);\n" +
                "    }\n" +
                "}";

        api.connect(AionAPIImpl.LOCALHOST_URL);
        api.getContractController().clear();

        Address cb = ((List<Address>)api.getWallet().getAccounts().getObject()).get(0);
        assertNotNull(cb);
        assertTrue(api.getWallet().unlockAccount(cb, pw, 3600).getObject());

        ApiMsg msg = api.getContractController().createFromSource(sc, cb, 1_000_000L, 1);
        if (msg.isError()) {
            System.out.println("deploy contract failed! " + msg.getErrString());
        }

        IContract ct = api.getContractController().getContract();
        assertEquals(ct.getFrom(), cb);
        assertNotNull(ct.getContractAddress());

        ContractResponse cr = ct.newFunction("getStaticArray")
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        List<Boolean> bool = Arrays.asList(true, false, true, false);
        List<Address> addr = Arrays.asList(  Address.wrap("1111111111111111111111111111111111111111111111111111111111111111"),
                                            Address.wrap("2222222222222222222222222222222222222222222222222222222222222222"),
                                            Address.wrap("3333333333333333333333333333333333333333333333333333333333333333"),
                                            Address.wrap("4444444444444444444444444444444444444444444444444444444444444444"));

        List<Long> uint = Arrays.asList(1111L, 2222L, 3333L, 4444L);
        assertEquals(cr.getData().get(0), bool);

        List<byte[]> addrAry = (List<byte[]>) cr.getData().get(1);
        List<Address> addrTran = addrAry.stream().map(Address::wrap).collect(Collectors.toList());

        IntStream.range(0,4).forEach(i -> {
            assertTrue(addrTran.get(i).equals(addr.get(i)));
        });
        assertEquals(cr.getData().get(2), uint);

        api.destroyApi();
    }

    @Test
    @Ignore
    public void TestCallGetTuple() throws Throwable {
        api.connect(AionAPIImpl.LOCALHOST_URL);
        api.getContractController().clear();

        String abiDefinition = "[{\"constant\":true,\"inputs\":[],\"name\":\"getValues\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"},{\"name\":\"\",\"type\":\"bool\"},{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"bytes32\"},{\"name\":\"\",\"type\":\"string\"},{\"name\":\"\",\"type\":\"int128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"boolVal\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"intValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"int128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"addressVal\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"uintValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"bytes32ValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"stringVal\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"setValues\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"intVal\",\"outputs\":[{\"name\":\"\",\"type\":\"int128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"uintBoolValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"uintVal\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"bytes32Val\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getTuple\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"},{\"name\":\"\",\"type\":\"uint128[5]\"},{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"uintAddressValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getArrays\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128[5]\"},{\"name\":\"\",\"type\":\"bool[5]\"}],\"payable\":false,\"type\":\"function\"}]\n";

        Address cb = ((List<Address>)api.getWallet().getAccounts().getObject()).get(0);
        assertNotNull(cb);
        assertTrue(api.getWallet().unlockAccount(cb, pw, 3600).getObject());

        Address contractAddress = Address.wrap("cf7eda19f9ef89a12a7abb708f8cc84cccf3c21d123412341234123412341234");

        IContract ct = api.getContractController().getContractAt(cb, contractAddress, abiDefinition);
        assertNotNull(ct);

        if (ct.error()) {
            System.out.println("deploy contract failed! " + ct.getErrString());
        }

        ContractResponse cr = ct.newFunction("getTuple")
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        assertNotNull(cr);

        //TODO:
        //adding data check

        api.destroyApi();
    }

    // Todo: rewrite test later
    @Test
    @Ignore
    public void TestCallGetTupleWithString() throws Throwable {
        api.connect(AionAPIImpl.LOCALHOST_URL);
        api.getContractController().clear();

        String abiDefinition = "[{\"constant\":true,\"inputs\":[],\"name\":\"getValues\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"},{\"name\":\"\",\"type\":\"bool\"},{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"bytes32\"},{\"name\":\"\",\"type\":\"string\"},{\"name\":\"\",\"type\":\"int128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"boolVal\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"intValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"int128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"addressVal\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"uintValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"bytes32ValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"stringVal\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"setValues\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"intVal\",\"outputs\":[{\"name\":\"\",\"type\":\"int128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getTupleWithString\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"},{\"name\":\"\",\"type\":\"uint128[5]\"},{\"name\":\"\",\"type\":\"string\"},{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"uintBoolValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"uintVal\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"bytes32Val\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getTuple\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"},{\"name\":\"\",\"type\":\"uint128[5]\"},{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"uintAddressValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getArrays\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128[5]\"},{\"name\":\"\",\"type\":\"bool[5]\"}],\"payable\":false,\"type\":\"function\"}]\n";

        Address cb = ((List<Address>)api.getWallet().getAccounts().getObject()).get(0);
        assertNotNull(cb);
        assertTrue(api.getWallet().unlockAccount(cb, pw, 3600).getObject());

        Address contractAddress = Address.wrap("825bdcc890ea6ed616f00424bb893cef93703b00");

        IContract ct = api.getContractController().getContractAt(cb, contractAddress, abiDefinition);
        assertNotNull(ct);

        if (ct.error()) {
            System.out.println("deploy contract failed! " + ct.getErrString());
        }

        ContractResponse cr = ct.newFunction("getTupleWithString")
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        assertNotNull(cr);

        //TODO:
        //adding data check


        api.destroyApi();
    }

    @Test
    public void TestCallGetPrescription() throws Throwable {
        String sc = "contract Prescription {\n" +
                "\n" +
                "    struct prescinfo {\n" +
                "        address docID;\n" +
                "        address pharmacyID;\n" +
                "        string drugName;\n" +
                "        uint drugQuant;\n" +
                "        bool redeemed;\n" +
                "        uint attempts;\n" +
                "    }\n" +
                "\n" +
                "    struct patientHistory {\n" +
                "        address[] pid;\n" +
                "        uint attempts;\n" +
                "    }\n" +
                "\n" +
                "    struct doctorHistory {\n" +
                "        address[] pid;\n" +
                "        address[] patientID;\n" +
                "        uint attempts;\n" +
                "    }\n" +
                "\n" +
                "    uint public pidCount;\n" +
                "\n" +
                "    address[] public doctors;\n" +
                "    mapping(address => prescinfo) public prescription;\n" +
                "    mapping(address => doctorHistory) doctor_history;\n" +
                "    mapping(address => patientHistory) patient_history;\n" +
                "\n" +
                "    // events\n" +
                "    event NewDoctor(address _doctorId);\n" +
                "    event NewPrescription(address _patient, address doctorID, address _pid);\n" +
                "\n" +
                "    // public methods\n" +
                "    function prescribe (address _patient, string _drugName, uint _drugQuant, address _pid) {\n" +
                "\n" +
                "        if (!checkExists(msg.sender, doctors)) {\n" +
                "            NewDoctor(msg.sender);\n" +
                "            doctors.push(msg.sender);\n" +
                "        }\n" +
                "\n" +
                "        if (prescription[_pid].docID == address(0x0)) {\n" +
                "            // emit event\n" +
                "            NewPrescription(_patient, msg.sender, _pid);\n" +
                "\n" +
                "            // set\n" +
                "            prescription[_pid].docID = msg.sender;\n" +
                "            prescription[_pid].drugName  = _drugName;\n" +
                "            prescription[_pid].drugQuant = _drugQuant;\n" +
                "            prescription[_pid].redeemed = false;\n" +
                "            patient_history[_patient].pid.push(_pid);\n" +
                "            doctor_history[msg.sender].pid.push(_pid);\n" +
                "            doctor_history[msg.sender].patientID.push(_patient);\n" +
                "            pidCount += 1;\n" +
                "        }\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    function redeem (address _pid, address _pharmacyID) {\n" +
                "        prescription[_pid].redeemed = true;\n" +
                "        prescription[_pid].attempts += 1;\n" +
                "        prescription[_pid].pharmacyID = _pharmacyID;\n" +
                "    }\n" +
                "\n" +
                "    function getPatientHistoryCount (address _patient) constant returns (uint) {\n" +
                "        return patient_history[_patient].pid.length;\n" +
                "    }\n" +
                "\n" +
                "    function getPatientHistory (address _patient, uint index) constant returns (address) {\n" +
                "        return patient_history[_patient].pid[index];\n" +
                "    }\n" +
                "\n" +
                "    function getPatientFraudCount (address _patient) constant returns (uint) {\n" +
                "        uint fraudCount = 0;\n" +
                "        for (uint i = 0; i < patient_history[_patient].pid.length; i++) {\n" +
                "            if (prescription[patient_history[_patient].pid[i]].attempts > 1) {\n" +
                "                fraudCount += 1;\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        return fraudCount;\n" +
                "    }\n" +
                "\n" +
                "    function getFraudPatients () constant returns (uint) {\n" +
                "        uint fraudCount = 0;\n" +
                "        for (uint i = 0; i < doctor_history[msg.sender].patientID.length; i++) {\n" +
                "            fraudCount += getPatientFraudCount(doctor_history[msg.sender].patientID[i]);\n" +
                "        }\n" +
                "\n" +
                "        return fraudCount;\n" +
                "    }\n" +
                "\n" +
                "    \n" +
                "\n" +
                "    function getDoctorCount () constant returns (uint) {\n" +
                "        return doctors.length;\n" +
                "    }\n" +
                "\n" +
                "    function getDoctor (uint index) constant returns (address) {\n" +
                "        return doctors[index];\n" +
                "    }\n" +
                "\n" +
                "    function getDoctorPrescriptionsCount (address doctor_address) constant returns (uint) {\n" +
                "        return doctor_history[doctor_address].pid.length;\n" +
                "    }\n" +
                "\n" +
                "    function getDoctorPrescription(address doctor_address, uint index) constant returns (address){\n" +
                "        return doctor_history[doctor_address].pid[index]; \n" +
                "    }    \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "    // private methods\n" +
                "    function checkExists (address _id, address[] addr) private returns (bool) {\n" +
                "        bool exists = false;\n" +
                "        for (uint i = 0; i < addr.length; i++) {\n" +
                "            if (_id == addr[i]) {\n" +
                "                exists = true;\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        return exists;\n" +
                "    }\n" +
                "}";


        api.connect(AionAPIImpl.LOCALHOST_URL);
        api.getContractController().clear();

        Address cb = ((List<Address>)api.getWallet().getAccounts().getObject()).get(0);
        assertNotNull(cb);
        assertTrue(api.getWallet().unlockAccount(cb, pw, 3600).getObject());

        ApiMsg msg = api.getContractController().createFromSource(sc, cb, 5_000_000L, 1);
        if (msg.isError()) {
            System.out.println("deploy contract failed! " + msg.getErrString());
        }

        IContract ct = api.getContractController().getContract();
        assertEquals(ct.getFrom(), cb);
        assertNotNull(ct.getContractAddress());
        assertTrue(ct.getAbiDefinition().size() == 15);
        assertTrue(ct.getContractEventList().size() == 2);

        String abi = "[{\"constant\":true,\"inputs\":[{\"name\":\"_patient\",\"type\":\"address\"}],\"name\":\"getPatientHistoryCount\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_patient\",\"type\":\"address\"},{\"name\":\"_drugName\",\"type\":\"string\"},{\"name\":\"_drugQuant\",\"type\":\"uint128\"},{\"name\":\"_pid\",\"type\":\"address\"}],\"name\":\"prescribe\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"prescription\",\"outputs\":[{\"name\":\"docID\",\"type\":\"address\"},{\"name\":\"pharmacyID\",\"type\":\"address\"},{\"name\":\"drugName\",\"type\":\"string\"},{\"name\":\"drugQuant\",\"type\":\"uint128\"},{\"name\":\"redeemed\",\"type\":\"bool\"},{\"name\":\"attempts\",\"type\":\"uint128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getFraudPatients\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_patient\",\"type\":\"address\"},{\"name\":\"index\",\"type\":\"uint128\"}],\"name\":\"getPatientHistory\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"doctor_address\",\"type\":\"address\"},{\"name\":\"index\",\"type\":\"uint128\"}],\"name\":\"getDoctorPrescription\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"doctor_address\",\"type\":\"address\"}],\"name\":\"getDoctorPrescriptionsCount\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"index\",\"type\":\"uint128\"}],\"name\":\"getDoctor\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"doctors\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getDoctorCount\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_pid\",\"type\":\"address\"},{\"name\":\"_pharmacyID\",\"type\":\"address\"}],\"name\":\"redeem\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_patient\",\"type\":\"address\"}],\"name\":\"getPatientFraudCount\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"pidCount\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"payable\":false,\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"_doctorId\",\"type\":\"address\"}],\"name\":\"NewDoctor\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"_patient\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"doctorID\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_pid\",\"type\":\"address\"}],\"name\":\"NewPrescription\",\"type\":\"event\"}]";

        IContract ct2 = api.getContractController().getContractAt(cb, ct.getContractAddress(), abi);
        assertNotNull(ct2);

        msg.set(ct2.newFunction("prescribe")
                .setParam(IAddress.copyFrom(Address.wrap("0000000000000000000000000000000000000000000000000000000000000001").toBytes()))
                .setParam(ISString.copyFrom("drug"))
                .setParam(IUint.copyFrom(25L))
                .setParam(IAddress.copyFrom(Address.wrap("0000000000000000000000000000000000000000000000000000000000001234").toBytes()))
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute());

         if (msg.isError()) {
             System.out.println("execute contract tx failed! " + msg.getErrString());
             return;
         }

        ContractResponse cr = msg.getObject();
         assertNotNull(cr);

        cr = ct2.newFunction("prescription")
                .setParam(IAddress.copyFrom(Address.wrap("0000000000000000000000000000000000000000000000000000000000001234").toBytes()))
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();


        List<Object> oparams = cr.getData();

        assertThat(oparams.get(1), is(equalTo(IUtils.hex2Bytes("0000000000000000000000000000000000000000000000000000000000000000"))));
        assertThat(oparams.get(2), is(equalTo("drug")));
        assertThat(oparams.get(3), is(equalTo(25L)));
        assertThat(oparams.get(4), is(equalTo(false)));
        assertThat(oparams.get(5), is(equalTo(0L)));

        api.destroyApi();
    }

    @Test
    public void TestTransactionParameterBytes() throws Throwable {
        String source = "contract testContract {\n"
                + "    bytes32 public a;\n"
                + "    bytes public b;\n"
                + "    bytes8 public c;\n"
                + "    bytes16 public d;\n"
                + "    string public e;\n"
                + "    \n"
                + "    function input32(bytes32 _a) {\n"
                + "        a = _a;\n"
                + "    }\n"
                + "    \n"
                + "    function input(bytes _b) {\n"
                + "        b = _b;\n"
                + "    }\n"
                + "    \n"
                + "    function input8(bytes8 _c) {\n"
                + "        c = _c;\n"
                + "    }\n"
                + "    \n"
                + "    function input16(bytes16 _d) {\n"
                + "        d = _d;\n"
                + "    }\n"
                + "    function inputS(string _e) {\n"
                + "        e = _e;\n"
                + "    }\n"
                + "}";

        api.connect(IAionAPI.LOCALHOST_URL);
        api.getContractController().clear();

        Address from = ((List<Address>)api.getWallet().getAccounts().getObject()).get(0);
        // ensure deployment account is unlocked
        assertTrue(api.getWallet().unlockAccount(from, pw, 600).getObject());

        ApiMsg msg = api.getContractController().createFromSource(source, from, 5_000_000L, 1);
        if (msg.isError()) {
            System.out.println("deploy contract failed! " + msg.getErrString());
        }

        IContract ct = api.getContractController().getContract();
        assertEquals(ct.getFrom(), from);
        assertNotNull(ct.getContractAddress());


        ContractResponse cr = ct.newFunction("input32")
                .setParam(IBytes.copyFrom(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1}))
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        System.out.println("Response Hash: " + cr.getTxHash().toString());

        cr = ct.newFunction("a")
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        assertThat(cr.getData().get(0), is(equalTo(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1})));

        cr = ct.newFunction("inputS")
                .setParam(ISString.copyFrom("25"))
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        System.out.println("Response Hash: " + cr.getTxHash().toString());

        cr = ct.newFunction("e")
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        cr = ct.newFunction("input")
                .setParam(IDynamicBytes.copyFrom(new byte[]{2,5}))
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        System.out.println("Response Hash: " + cr.getTxHash().toString());

        cr = ct.newFunction("b")
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        assertTrue(Arrays.equals((byte[]) cr.getData().get(0), new byte[] {2, 5}));

        cr = ct.newFunction("input8")
                .setParam(IBytes.copyFrom(new byte[]{0,0,0,0,0,0,0,1}))
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        System.out.println("Response Hash: " + cr.getTxHash().toString());

        cr = ct.newFunction("c")
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        assertThat(cr.getData().get(0), is(equalTo(new byte[]{0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0})));

        cr = ct.newFunction("input16")
                .setParam(IBytes.copyFrom(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1}))
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        System.out.println("Response Hash: " + cr.getTxHash().toString());


        cr = ct.newFunction("d")
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        assertThat(cr.getData().get(0), is(equalTo(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1})));

        api.destroyApi();
    }

    @Test
    public void TestTransactionWithDynamicParam() throws Throwable {
        // formatted into something remotely readable
        String s = "contract ticker { \n"
                + "    uint public val;\n"
                + "    uint[] public pub;\n"
                + "    \n"
                + "    struct myStruct {\n"
                + "        uint a;\n"
                + "        uint b;\n"
                + "        uint[10] someList;\n"
                + "    }\n"
                + "    \n"
                + "    mapping(address => myStruct) public myHash;\n"
                + "    \n"
                + "    function tick (uint[] inFromUser) {\n"
                + "        val+= inFromUser[0];\n"
                + "        val+= 1;\n"
                + "    }\n"
                + "}";

        api.connect(IAionAPI.LOCALHOST_URL);
        api.getContractController().clear();

        Address from = ((List<Address>)api.getWallet().getAccounts().getObject()).get(0);

        // ensure deployment account is unlocked
        assertTrue(api.getWallet().unlockAccount(from, pw, 600).getObject());

        Map<String, CompileResponse> compileResponse = api.getTx().compile(s).getObject();

        CompileResponse c = compileResponse.get("ticker");
        for (ContractAbiEntry entry : c.getAbiDefinition()) {
            System.out.println("ABI Function: " + entry.name);
        }

        ApiMsg msg = api.getContractController().createFromSource(s, from, 5_000_000L, 1);
        if (msg.isError()) {
            System.out.println("deploy contract failed! " + msg.getErrString());
        }

        IContract contract = api.getContractController().getContract();
        assertNotNull(contract);

        List<Integer> inputUint = new ArrayList<>();
        inputUint.add(1);
        inputUint.add(2);
        inputUint.add(3);
        inputUint.add(4);

        ContractResponse contractResponse = contract.newFunction("tick")
                .setParam(IUint.copyFrom(inputUint))
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        System.out.println("Hash Response: " + contractResponse.getTxHash().toString());
        api.destroyApi();
    }

    @Test
    public void TestTransactionWithMoreDynamicParam() throws Throwable {
        // formatted into something remotely readable
        String s = "contract ticker { \n"
                + "    uint public val;\n"
                + "    uint[] public pub;\n"
                + "    \n"
                + "    struct myStruct {\n"
                + "        uint a;\n"
                + "        uint b;\n"
                + "        uint[10] someList;\n"
                + "    }\n"
                + "    \n"
                + "    struct a {\n"
                + "        string s;\n"
                + "        uint b;\n"
                + "    }\n"
                + "    \n"
                + "    mapping(address => myStruct) public myHash;\n"
                + "    \n"
                + "    function tick (bool b, uint[] inFromUser, address[5] staticInFromUser) {\n"
                + "    }\n"
                + "    \n"
                + "}";

        api.connect(IAionAPI.LOCALHOST_URL);
        api.getContractController().clear();

        Address from = ((List<Address>)api.getWallet().getAccounts().getObject()).get(0);

        // ensure deployment account is unlocked
        assertTrue(api.getWallet().unlockAccount(from, pw, 600).getObject());

        Map<String, CompileResponse> compileResponse = api.getTx().compile(s).getObject();

        CompileResponse c = compileResponse.get("ticker");
        for (ContractAbiEntry entry : c.getAbiDefinition()) {
            System.out.println("ABI Function: " + entry.name);
        }



        ApiMsg msg = api.getContractController().createFromSource(s, from, 5_000_000L, 1);
        if (msg.isError()) {
            System.out.println("deploy contract failed! " + msg.getErrString());
        }

        IContract contract = api.getContractController().getContract();
        assertNotNull(contract);

        ArrayList<Integer> inputUint = new ArrayList<>();
        inputUint.add(1);
        inputUint.add(2);
        inputUint.add(3);
        inputUint.add(4);

        ArrayList<String> inputAddress = new ArrayList<String>();
        inputAddress.add("0000000000000000000000000000000000000000000000000000000000001001");
        inputAddress.add("0000000000000000000000000000000000000000000000000000000000001002");
        inputAddress.add("0000000000000000000000000000000000000000000000000000000000001003");
        inputAddress.add("0000000000000000000000000000000000000000000000000000000000001004");
        inputAddress.add("0000000000000000000000000000000000000000000000000000000000001005");

        ContractResponse contractResponse = contract.newFunction("tick")
                .setParam(IBool.copyFrom(true))
                .setParam(IUint.copyFrom(inputUint))
                .setParam(IAddress.copyFrom(inputAddress))
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        System.out.println("Hash Response: " + contractResponse.getTxHash().toString());
        api.destroyApi();
    }

    @Test
    public void TestContractTicker() throws Throwable {
        String s = "contract ticker { uint public val; function tick () { val+= 1; } }";
        api.connect(AionAPIImpl.LOCALHOST_URL);
        api.getContractController().clear();


        List<Address> accs = api.getWallet().getAccounts().getObject();
        assertTrue(accs.size() > 0);
        assertTrue(api.getWallet().unlockAccount(accs.get(0), pw).getObject());

        ApiMsg msg = api.getContractController().createFromSource(s, accs.get(0), 5_000_000L, 1);
        if (msg.isError()) {
            System.out.println("deploy contract failed! " + msg.getErrString());
        }

        IContract ct = api.getContractController().getContract();


        ContractResponse cr;
        long cnt = 100;
        for (int i=0 ;i<cnt; i++) {
            cr = ct.newFunction("tick")
                    .setFrom(accs.get(0))
                    .setTxNrgLimit(5_000_000L)
                    .setTxNrgPrice(1L)
                    .build()
                    .nonBlock()
                    .execute()
                    .getObject();
        }

        long timeout = 60000;
        long start = System.currentTimeMillis();
        long res = 0;
        while ((System.currentTimeMillis() - start < timeout)) {
            cr = ct.newFunction("val")
                    .setFrom(accs.get(1))
                    .setTxNrgLimit(5_000_000L)
                    .setTxNrgPrice(1L)
                    .build()
                    .execute()
                    .getObject();

            res = (Long)cr.getData().get(0);
            System.out.println("val: " + res);

            if (cnt == res) {
                break;
            }
            Thread.sleep(500);
        }

        assertTrue(res == cnt);

        api.destroyApi();
    }

    @Test
    public void TestContractThrow() throws Throwable {

        String s = "contract Token {\n" + "\n" + "    function Token() {}\n" + "   \n" + "    function throwMe(){\n"
                + "   \t\tthrow;\n" + "    }\n" + "\n" + "}";

        api.connect(AionAPIImpl.LOCALHOST_URL);
        api.getContractController().clear();


        List<Address> accs = api.getWallet().getAccounts().getObject();
        assertThat(accs.size(), is(greaterThan(0)));
        assertTrue(api.getWallet().unlockAccount(accs.get(1), pw).getObject());

        ApiMsg apiMsg = api.getContractController().createFromSource(s, accs.get(1), 5_000_000L, 1L);
        if (apiMsg.isError()) {
            System.out.println("Deploy contract failed! " + apiMsg.getErrString());
            return;
        }

        IContract ct = api.getContractController().getContract();

        apiMsg = ct.newFunction("throwMe")
                .setFrom(accs.get(1))
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute();

        assertFalse(apiMsg.isError());
        System.out.println("throwMe transaction status! " + apiMsg.getErrString());

        api.destroyApi();
    }

    @Test
    public void TestCreateContractFromSourceDe() throws Throwable {
        String source = "pragma solidity ^0.4.0;\n" +
                "\n" +
                "contract Customer_Payee {\n" +
                "    \n" +
                "   modifier onlyIfParticipant(address partic) {\n" +
                "        bool flag = false;\n" +
                "        for (uint i = 0; i < participants.length; i++) {\n" +
                "            if (participants[i] == partic) {\n" +
                "                flag = true;\n" +
                "            }\n" +
                "        }\n" +
                "        if(!flag) {\n" +
                "            throw;\n" +
                "        }\n" +
                "        _;\n" +
                "    }\n" +
                "    \n" +
                "   enum CONTRACT_STATE {\n" +
                "        PREAPPROVED_CREATED,\n" +
                "        WITHDRAWAL_REQ,\n" +
                "        VALIDATE_WITHDRAWAL_REQ,\n" +
                "        MONEY_TRANSFERRED\n" +
                "    }\n" +
                "\n" +
                "   CONTRACT_STATE state;\n" +
                "    address customer;\n" +
                "    address payee;\n" +
                "    uint preapproved_pmt_amt;\n" +
                "    uint creation_date;\n" +
                "    uint due_pmt_amt;\n" +
                "    uint due_date;\n" +
                "    uint pmt_date;\n" +
                "    address [] participants;\n" +
                "    \n" +
                "   event onPreApprovedCreate(address from, address to, uint preapproved_pmt_amt, uint creation_date, CONTRACT_STATE state);\n" +
                "    event onAddParticipant(address participant);\n" +
                "    event onWithdrawalRequest(address from, address to, uint due_pmt_amt, uint due_date, CONTRACT_STATE state);\n" +
                "    event onWithdrawalAmtValidate(CONTRACT_STATE state);\n" +
                "    event onMoneyTransfer(address from, address to, uint due_pmt_amt, uint pmt_date, CONTRACT_STATE state);\n" +
                "    \n" +
                "   \n" +
                "   function Customer_Payee(address _customer, address _payee, uint _preapproved_pmt_amt, uint _creation_date) public {\n" +
                "        customer = _customer;\n" +
                "        payee = _payee;\n" +
                "        preapproved_pmt_amt = _preapproved_pmt_amt;\n" +
                "        creation_date = _creation_date;\n" +
                "        state = CONTRACT_STATE.PREAPPROVED_CREATED;\n" +
                "        onPreApprovedCreate(customer, payee, preapproved_pmt_amt, creation_date, state);\n" +
                "        participants.push(customer);\n" +
                "        participants.push(payee);\n" +
                "        onAddParticipant(customer);\n" +
                "        onAddParticipant(payee);\n" +
                "    }\n" +
                "    \n" +
                "   /*function add_customer(address _customer) {\n" +
                "        participants.push(_customer);\n" +
                "        onAddParticipant(customer);\n" +
                "        onPreApprovedCreate(_customer, payee, preapproved_pmt_amt, creation_date, state);\n" +
                "    }*/\n" +
                "    \n" +
                "   function get_participants() constant returns (address []) {\n" +
                "        return participants;\n" +
                "    }\n" +
                "    \n" +
                "   function bill_posted(address _payee, uint _due_pmt_amt, uint _due_date)\n" +
                "    onlyIfParticipant(_payee) {\n" +
                "        due_pmt_amt = _due_pmt_amt;\n" +
                "        due_date = _due_date;\n" +
                "        state = CONTRACT_STATE.WITHDRAWAL_REQ;\n" +
                "        onWithdrawalRequest(payee, customer, due_pmt_amt, due_date, state);\n" +
                "        \n" +
                "   }\n" +
                "    \n" +
                "   function view_payee_customer_authorization() constant returns (address, address, uint, uint){\n" +
                "        return (payee, customer, preapproved_pmt_amt, creation_date);\n" +
                "    }\n" +
                "    \n" +
                "   function validate_contract () {\n" +
                "        state = CONTRACT_STATE.VALIDATE_WITHDRAWAL_REQ;\n" +
                "        onWithdrawalAmtValidate(state);\n" +
                "    }\n" +
                "    \n" +
                "   function make_payment(address _payee, uint _pmt_date)\n" +
                "    onlyIfParticipant(_payee) {\n" +
                "        pmt_date = _pmt_date;\n" +
                "        state = CONTRACT_STATE.MONEY_TRANSFERRED;\n" +
                "        onMoneyTransfer(payee, customer, preapproved_pmt_amt, _pmt_date, state);\n" +
                "    }\n" +
                "    \n" +
                "   function get_bill_info() constant returns (address, address, uint, uint) {\n" +
                "        return (payee, customer, due_pmt_amt, due_date);\n" +
                "    }\n" +
                "    \n" +
                "   function get_money_transferred_info() constant returns (address, address, uint, uint) {\n" +
                "        return (payee, customer, preapproved_pmt_amt, pmt_date);\n" +
                "    }\n" +
                "    \n" +
                "   function contract_state() constant returns (int) {\n" +
                "        if (state == CONTRACT_STATE.PREAPPROVED_CREATED) {\n" +
                "           return 0;\n" +
                "        }\n" +
                "        else if (state == CONTRACT_STATE.WITHDRAWAL_REQ) {\n" +
                "           return 1;\n" +
                "        }\n" +
                "        else if (state == CONTRACT_STATE.VALIDATE_WITHDRAWAL_REQ) {\n" +
                "           return 2;\n" +
                "        }\n" +
                "        else if (state == CONTRACT_STATE.MONEY_TRANSFERRED) {\n" +
                "           return 3;\n" +
                "        }\n" +
                "        else {\n" +
                "            return -1;\n" +
                "        }\n" +
                "    }\n" +
                "}";


        // hardcoded msgHash to block 1
        api.connect(AionAPIImpl.LOCALHOST_URL);
        api.getContractController().clear();


        Address cb = ((List<Address>)api.getWallet().getAccounts().getObject()).get(0);
        assertThat(cb, not(equalTo(null)));
        assertTrue(api.getWallet().unlockAccount(cb, pw).getObject());

        ApiMsg apiMsg = api.getContractController().createFromSource(source, cb, 5_000_000L, 1L);
        if (apiMsg.isError()) {
            System.out.println("Deploy contract failed! " + apiMsg.getErrString());
            return;
        }

        IContract contract = api.getContractController().getContract();
        assertThat(contract.getFrom(), is(equalTo(cb)));
        assertThat(contract.getContractAddress(), not(equalTo(null)));
        assertThat(contract.getAbiDefinition().size(), is(equalTo(14)));

        api.destroyApi();
    }

    @Test
    public void TestCustomerPayee() {
        String token = "pragma solidity ^0.4.0;\n" +
                "\n" +
                "contract Customer_Payee {\n" +
                "    \n" +
                "   modifier onlyIfParticipant(address partic) {\n" +
                "        bool flag = false;\n" +
                "        for (uint i = 0; i < participants.length; i++) {\n" +
                "            if (participants[i] == partic) {\n" +
                "                flag = true;\n" +
                "            }\n" +
                "        }\n" +
                "        if(!flag) {\n" +
                "            throw;\n" +
                "        }\n" +
                "        _;\n" +
                "    }\n" +
                "    \n" +
                "   enum CONTRACT_STATE {\n" +
                "        PREAPPROVED_CREATED,\n" +
                "        WITHDRAWAL_REQ,\n" +
                "        VALIDATE_WITHDRAWAL_REQ,\n" +
                "        MONEY_TRANSFERRED\n" +
                "    }\n" +
                "\n" +
                "   CONTRACT_STATE state;\n" +
                "    address customer;\n" +
                "    address payee;\n" +
                "    uint preapproved_pmt_amt;\n" +
                "    uint creation_date;\n" +
                "    uint due_pmt_amt;\n" +
                "    uint due_date;\n" +
                "    uint pmt_date;\n" +
                "    address [] participants;\n" +
                "    \n" +
                "   event onPreApprovedCreate(address from, address to, uint preapproved_pmt_amt, uint creation_date, CONTRACT_STATE state);\n" +
                "    event onAddParticipant(address participant);\n" +
                "    event onWithdrawalRequest(address from, address to, uint due_pmt_amt, uint due_date, CONTRACT_STATE state);\n" +
                "    event onWithdrawalAmtValidate(CONTRACT_STATE state);\n" +
                "    event onMoneyTransfer(address from, address to, uint due_pmt_amt, uint pmt_date, CONTRACT_STATE state);\n" +
                "    \n" +
                "   \n" +
                "   function Customer_Payee(address _customer, address _payee, uint _preapproved_pmt_amt, uint _creation_date) public {\n" +
                "        customer = _customer;\n" +
                "        payee = _payee;\n" +
                "        preapproved_pmt_amt = _preapproved_pmt_amt;\n" +
                "        creation_date = _creation_date;\n" +
                "        state = CONTRACT_STATE.PREAPPROVED_CREATED;\n" +
                "        onPreApprovedCreate(customer, payee, preapproved_pmt_amt, creation_date, state);\n" +
                "        participants.push(customer);\n" +
                "        participants.push(payee);\n" +
                "        onAddParticipant(customer);\n" +
                "        onAddParticipant(payee);\n" +
                "    }\n" +
                "    \n" +
                "   /*function add_customer(address _customer) {\n" +
                "        participants.push(_customer);\n" +
                "        onAddParticipant(customer);\n" +
                "        onPreApprovedCreate(_customer, payee, preapproved_pmt_amt, creation_date, state);\n" +
                "    }*/\n" +
                "    \n" +
                "   function get_participants() constant returns (address []) {\n" +
                "        return participants;\n" +
                "    }\n" +
                "    \n" +
                "   function bill_posted(address _payee, uint _due_pmt_amt, uint _due_date)\n" +
                "    onlyIfParticipant(_payee) {\n" +
                "        due_pmt_amt = _due_pmt_amt;\n" +
                "        due_date = _due_date;\n" +
                "        state = CONTRACT_STATE.WITHDRAWAL_REQ;\n" +
                "        onWithdrawalRequest(payee, customer, due_pmt_amt, due_date, state);\n" +
                "        \n" +
                "   }\n" +
                "    \n" +
                "   function view_payee_customer_authorization() constant returns (address, address, uint, uint){\n" +
                "        return (payee, customer, preapproved_pmt_amt, creation_date);\n" +
                "    }\n" +
                "    \n" +
                "   function validate_contract () {\n" +
                "        state = CONTRACT_STATE.VALIDATE_WITHDRAWAL_REQ;\n" +
                "        onWithdrawalAmtValidate(state);\n" +
                "    }\n" +
                "    \n" +
                "   function make_payment(address _payee, uint _pmt_date)\n" +
                "    onlyIfParticipant(_payee) {\n" +
                "        pmt_date = _pmt_date;\n" +
                "        state = CONTRACT_STATE.MONEY_TRANSFERRED;\n" +
                "        onMoneyTransfer(payee, customer, preapproved_pmt_amt, _pmt_date, state);\n" +
                "    }\n" +
                "    \n" +
                "   function get_bill_info() constant returns (address, address, uint, uint) {\n" +
                "        return (payee, customer, due_pmt_amt, due_date);\n" +
                "    }\n" +
                "    \n" +
                "   function get_money_transferred_info() constant returns (address, address, uint, uint) {\n" +
                "        return (payee, customer, preapproved_pmt_amt, pmt_date);\n" +
                "    }\n" +
                "    \n" +
                "   function contract_state() constant returns (int) {\n" +
                "        if (state == CONTRACT_STATE.PREAPPROVED_CREATED) {\n" +
                "           return 0;\n" +
                "        }\n" +
                "        else if (state == CONTRACT_STATE.WITHDRAWAL_REQ) {\n" +
                "           return 1;\n" +
                "        }\n" +
                "        else if (state == CONTRACT_STATE.VALIDATE_WITHDRAWAL_REQ) {\n" +
                "           return 2;\n" +
                "        }\n" +
                "        else if (state == CONTRACT_STATE.MONEY_TRANSFERRED) {\n" +
                "           return 3;\n" +
                "        }\n" +
                "        else {\n" +
                "            return -1;\n" +
                "        }\n" +
                "    }\n" +
                "}";


        ApiMsg apiMsg = api.connect(IAionAPI.LOCALHOST_URL);
        api.getContractController().clear();

        if (apiMsg.isError()) {
            System.out.println("Connect server failed, exit test! " + apiMsg.getErrString());
            return;
        }
        System.out.println("Get server connected!");
        System.out.println();


        apiMsg.set(api.getWallet().getAccounts());
        if (apiMsg.isError()) {
            System.out.println("GetAccounts failed! " + apiMsg.getErrString());
            return;
        }
        List accs = apiMsg.getObject();

        if (accs.size() < 3) {
            System.out.println("The number of accounts in the server is lower than 3, please check the server has a least 3 accounts to support the test!");
            return;
        }

        System.out.println("Get " + accs.size() + " accounts!");

        Address acc = (Address) accs.get(0);
        System.out.println("Get the first account: " + acc.toString());
        Address acc2 = (Address) accs.get(1);
        System.out.println("Get the second account: " + acc2.toString());
        Address acc3 = (Address) accs.get(2);
        System.out.println("Get the third account: " + acc3.toString());
        System.out.println();

        // unlockAccount before deployContract or send a transaction.
        System.out.println("Unlock account before deploy smart contract or send transactions.");

        apiMsg.set(api.getWallet().unlockAccount(acc, pw, 300));
        if (apiMsg.isError() || !(boolean) apiMsg.getObject()) {
            System.out.println("Unlock account failed! Please check your password input! " + apiMsg.getErrString());
            return;
        }


        apiMsg.set(api.getWallet().unlockAccount(acc3, pw, 300));
        if (apiMsg.isError() || !(boolean) apiMsg.getObject()) {
            System.out.println("Unlock account failed! Please check your password input! " + apiMsg.getErrString());
            return;
        }

        System.out.println("Account unlocked!");
        System.out.println();

        ArrayList<ISolidityArg> param = new ArrayList<>();
        param.add(IAddress.copyFrom("1111111111111111111111111111111111111111111111111111111111111111"));
        param.add(IAddress.copyFrom("1111111111111111111111111111111111111111111111111111111111111111"));
        param.add(IUint.copyFrom(10));
        param.add(IUint.copyFrom(10));

        System.out.println("Prepare to deploy the token contract.");

        apiMsg = api.getContractController().createFromSource(token, acc, 5_000_000L, 1L, param);
        if (apiMsg.isError()) {
            System.out.println("Deploy contract failed! " + apiMsg.getErrString());
            return;
        }

        IContract ct = api.getContractController().getContract();
        assertNotNull(ct);

        System.out.println("Contract deployed!");
        System.out.println();

    }
}
