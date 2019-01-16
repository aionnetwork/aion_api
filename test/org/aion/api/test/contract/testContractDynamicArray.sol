contract testContractDynamicArray {

    bytes16[] public bt16param;
    bytes32[] public bt32param;

    function setByte16(bytes16[] inp16) public {
        bt16param = inp16;
        bt16param[2] = 8;
    }


    function setByte32(bytes32[] inp32) public {
        bt32param = inp32;
        bt32param[2] = 8;
    }

    function setBlank() public {
        bt32param.push(1);
    }
}