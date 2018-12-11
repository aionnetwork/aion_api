pragma solidity ^0.4.0;
contract Revert {
	uint private data = 3;

	function setData(uint a) {
		data = a;
	}

	function setData2(uint a) {
		data = a;
		throw;
	}
	
	function getData() constant returns(uint) {
		return data;
	}
}
