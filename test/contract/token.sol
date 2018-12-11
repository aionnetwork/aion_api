contract MyToken{
    event Transfer(address indexed from, address indexed to, uint128 value);
    string public name;
    string public symbol;
    uint8 public decimals;
    mapping(address=>uint128)
    public balanceOf;

    function MyToken(uint128 initialSupply, string tokenName, uint8 decimalUnits, string tokenSymbol){
        balanceOf[msg.sender]=initialSupply;
        name = tokenName;
        symbol = tokenSymbol;
        decimals = decimalUnits;
    }

    function transfer(address _to,uint64 _value){
        if (balanceOf[msg.sender] < _value || balanceOf[_to] + _value < balanceOf[_to]) throw;
        balanceOf[msg.sender] -= _value;
        balanceOf[_to] += _value;
        Transfer(msg.sender, _to, _value);
    }
}
