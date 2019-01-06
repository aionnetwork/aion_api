contract SolTypes {
    function getValues() constant returns (uint128, bool, address, bytes32, string, int128) {
        return (1234, true, 0x1234567890123456789012345678901234567890123456789012345678901234, 0x1234567890123456789012345678901234567890123456789012345678901234, "Aion!", -1234);
    }
    
    function boolVal() constant returns (bool) {
        return true;
    }

    function boolVal2(bool data) public returns (bool) {
        return data;
    }
    
    function addressVal() constant returns (address) {
        return 0x1234567890123456789012345678901234567890;
    }
    
    function stringVal() constant returns (string) {
        return "1234";
    }
    
    function setValues() {
        
    }    
    
    function intVal() constant returns (int128) {
        return -1234;
    }    
    
    function uintVal() constant returns (uint128) {
        return 1234;
    }

    function uintVal2(uint128 data) public returns (uint128) {
        return data;
    }
    
    function bytes32Val() constant returns (bytes32) {
        return 0x1234567890123456789012345678901234567890123456789012345678901234;
    }

    function bytes32Val2(bytes32[3] data) public returns (bytes32) {
        return data[0];
    }
}