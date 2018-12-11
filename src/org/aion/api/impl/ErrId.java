package org.aion.api.impl;

import java.util.HashMap;
import java.util.Map;

/** Created by Jay Tseng on 22/11/16. */

/** This class show the response message by given the isError code. */
public final class ErrId {

    private static final Map<Long, String> ERROR_CODES;

    static {
        ERROR_CODES = new HashMap<>();
        ERROR_CODES.put(-101L, "Connect to server failed.");
        ERROR_CODES.put(-102L, "Socket receive message isError.");
        ERROR_CODES.put(-103L, "Server does not have response.");
        ERROR_CODES.put(-104L, "Server return unknown msg.");
        ERROR_CODES.put(-105L, "Json syntax exception.");
        ERROR_CODES.put(-106L, "Json parse fail.");
        ERROR_CODES.put(-107L, "Could not find suitable deployment account.");
        ERROR_CODES.put(-108L, "AbiDefinition can not be null!");
        ERROR_CODES.put(-109L, "Wrong Abi type : ");
        ERROR_CODES.put(-110L, "Cannot find function: ");
        ERROR_CODES.put(-111L, "Cannot execute without function definition.");
        ERROR_CODES.put(-112L, "Input Parameter type unmatched.");
        ERROR_CODES.put(-113L, "Function has not been built!");
        ERROR_CODES.put(
                -114L, "Internal type should only consist of byte[] and ArrayList! What happened?");
        ERROR_CODES.put(-115L, "Input too large, keep within 256bits.");
        ERROR_CODES.put(-116L, "Arraylist is empty or null!");
        ERROR_CODES.put(-117L, "Unsupported input type to instantiate Uint!");
        ERROR_CODES.put(-118L, "Unsupported input type to instantiate Int!");
        ERROR_CODES.put(-119L, "Unsupported input type to instantiate Bytes!");
        ERROR_CODES.put(-120L, "Unsupported input type to instantiate Real!");
        ERROR_CODES.put(-121L, "Unsupported input type to instantiate Bool!");
        ERROR_CODES.put(-123L, "Unsupported input type to instantiate Address!");
        ERROR_CODES.put(-124L, "Unsupported input type to instantiate Ureal!");
        ERROR_CODES.put(-125L, "Should not reach here! Something is wrong!");
        ERROR_CODES.put(-126L, "Server return empty contract!");
        ERROR_CODES.put(
                -127L,
                "Contract compile failed! Please check your solidity compiler and SmartContract!");
        ERROR_CODES.put(-128L, "Contract deploy timeout!");
        ERROR_CODES.put(-129L, "Null api call msg!");
        ERROR_CODES.put(-130L, "The event filter input is null.");
        ERROR_CODES.put(-131L, "Inconsistency params input match with this contract!");
        ERROR_CODES.put(-132L, "Cannot find event: ");
        ERROR_CODES.put(-133L, "Incorrect string format ");
        ERROR_CODES.put(-134L, "The empty event name input.");
        ERROR_CODES.put(-135L, "No matched events compare with the input strings.");
        ERROR_CODES.put(-136L, "No event been registered.");
        ERROR_CODES.put(-137L, "The contract event put into queue cause exception.");
        ERROR_CODES.put(-138L, "The tx nrglimit equal to 0, check your tx nrg settings");
        ERROR_CODES.put(-139L, "The tx nrgPrice equal to 0, check your tx nrg settings.");

        ERROR_CODES.put(-200L, "UnsupportedEncodingException!");

        ERROR_CODES.put(-300L, "Invalid address length!");
        ERROR_CODES.put(-301L, "Null password!");
        ERROR_CODES.put(-302L, "Negative duration!");
        ERROR_CODES.put(-303L, "Null Transaction argument!");
        ERROR_CODES.put(-304L, "Invalid account length!");
        ERROR_CODES.put(-305L, "Null Smart contract source code!");
        ERROR_CODES.put(-306L, "Null CompileResponse!");
        ERROR_CODES.put(-307L, "Negative Nrg!");
        ERROR_CODES.put(-308L, "Negative Nrg price!");
        ERROR_CODES.put(-309L, "Null smart contract constructor data!");
        ERROR_CODES.put(-310L, "Negative block number!");
        ERROR_CODES.put(-311L, "Negative transaction index!");
        ERROR_CODES.put(-312L, "Null address string!");
        ERROR_CODES.put(-313L, "Null byte[] input!");
        ERROR_CODES.put(-314L, "Null String input!");
        ERROR_CODES.put(-315L, "Null param input!");
        ERROR_CODES.put(-316L, "Invalid txHash length!");
        ERROR_CODES.put(-317L, "Invalid req header length!");
        ERROR_CODES.put(-318L, "Api version do not match!");
        ERROR_CODES.put(-319L, "Invalid return code!");
        ERROR_CODES.put(-320L, "Hashcode unmatached!");
        ERROR_CODES.put(-321L, "Invalid msg header length!");
        ERROR_CODES.put(-322L, "Null msg response!");
        ERROR_CODES.put(-323L, "Zmq msg send failed!");
        ERROR_CODES.put(-325L, "Empty contract name!");
        ERROR_CODES.put(-326L, "Empty contract compile result!");
        ERROR_CODES.put(-327L, "Empty contract deploy response!");
        ERROR_CODES.put(-328L, "Invalid rsp msg length!");
        ERROR_CODES.put(-329L, "No valid NBFT transactions!");

        ERROR_CODES.put(-1001L, "DestroyApi exception!");
        ERROR_CODES.put(-1002L, "Can't recv heartbeat msg!");
        ERROR_CODES.put(-1003L, "API not initialized yet!");
        ERROR_CODES.put(-1004L, "Null url string!");
        ERROR_CODES.put(-1005L, "Negative socket connect timeout!");
        ERROR_CODES.put(-1006L, "Thread exception!");
        ERROR_CODES.put(-1008L, "Contract init exception!");
        ERROR_CODES.put(-1009L, "Socket connection failed!");
        ERROR_CODES.put(-1010L, "Transaction timeout!");
        ERROR_CODES.put(-1011L, "No privilege to access this API.");

        ERROR_CODES.put(-1L, "Invalid header length!");
        ERROR_CODES.put(-2L, "Invalid server code!");
        ERROR_CODES.put(-3L, "Invalid function code!");
        ERROR_CODES.put(-4L, "Function exception!");
        ERROR_CODES.put(-5L, "Api version does not match!");
        ERROR_CODES.put(-6L, "Invalid contract bytecode!");
        ERROR_CODES.put(-7L, "Server does not have response!");
        ERROR_CODES.put(-8L, "Invalid address!");
        ERROR_CODES.put(-9L, "No contract source!");
        ERROR_CODES.put(-10L, "Compile contract failed!");
        ERROR_CODES.put(-11L, "Server does not have transaction response!");
        ERROR_CODES.put(-12L, "Does not have to address!");
        ERROR_CODES.put(-13L, "No transaction receipt!");
        ERROR_CODES.put(-14L, "ZMQ handler exception!");
        ERROR_CODES.put(-15L, "API Pending transactions limit!");
        ERROR_CODES.put(-16L, "API transaction queue exception!");
        ERROR_CODES.put(-17L, "Invalid function arguments!");
        ERROR_CODES.put(-18L, "Account exist!");
        ERROR_CODES.put(-19L, "Can't create more then 1000 account at one time!");
        ERROR_CODES.put(-20L, "insert key and password exception!");
        ERROR_CODES.put(-21L, "invalid key file!");

        ERROR_CODES.put(50L, "Thread sleep exception!");
        ERROR_CODES.put(51L, "Transaction timeout!");
        ERROR_CODES.put(52L, "future exception!");
        ERROR_CODES.put(53L, "Put msg to txQueue failed!");
        ERROR_CODES.put(54L, "Msg status is null");
        ERROR_CODES.put(55L, "MsgExecutor send failed!");
        ERROR_CODES.put(56L, "Null message hash!");
        ERROR_CODES.put(57L, "Null or incorrect message hash length input!");

        ERROR_CODES.put(101L, "Transaction received!");
        ERROR_CODES.put(102L, "Transaction been dropped!");
        ERROR_CODES.put(103L, "Transaction in the pending block of the kernel!");
        ERROR_CODES.put(104L, "Transaction in the new pending block of the kernel!");
        ERROR_CODES.put(105L, "Transaction been included in a new block!");
    }

    public static String getErrString(long code) {
        String rtn = ERROR_CODES.get(code);
        return (rtn == null ? "No matched Error string" : rtn);
    }
}
