contract ByteArrayMap {
    mapping(uint128 => bytes) public data;
    function f() {
        bytes memory d = new bytes(1024);
        data[32] = d;
    }
    function g() constant returns (bytes) {
        return data[32];
   }
}