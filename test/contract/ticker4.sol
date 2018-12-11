pragma solidity ^0.4.0;

contract Ticker {

    event Data(uint data);
    event Ti();
	uint private data = 1;

	function Ticker() {
        Ti();
	}

	function tick() {
		data++;
		Data(data);
	}


	function getData() constant returns(uint) {
		return data;
	}
}
