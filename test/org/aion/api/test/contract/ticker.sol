contract ticker { 
    uint public val;
    uint[] public pub;
    
    struct myStruct {
        uint a;
        uint b;
        uint[10] someList;
    }
    
    mapping(address => myStruct) public myHash;
    
    function tick (uint[] inFromUser) {
        val+= inFromUser[0];
        val+= 1;
    }
}