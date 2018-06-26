contract ticker2 {
    uint public val;
    uint[] public pub;
    
    struct myStruct {
        uint a;
        uint b;
        uint[10] someList;
    }
    
    struct a {
        string s;
        uint b;
    }
    
    mapping(address => myStruct) public myHash;
    
    function tick (bool b, uint[] inFromUser, address[5] staticInFromUser) {
    }
}