pragma solidity ^0.4.8;

contract BloomTest {

    event A(uint64 a, bytes32 b);

    function hitMe(uint64 num, bytes32 b) {
        A(num, b);
    }

}